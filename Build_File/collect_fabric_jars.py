import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import time
import zipfile
from typing import Optional


# (모듈 폴더, fabric 하위 폴더, 기대하는 mod ID)
# fabric_dir가 None이면 루트 buildJar 태스크가 만든 병합 JAR(build/libs)을 수집한다.
MODULES = [
    ("UILib-26.2", "fabric", "uilib"),
    ("YamlConfig-26.2", "fabric", "yamlconfig"),
    ("ArcLib-26.2", "fabric", "arc"),
    ("ItemRestrictions-26.2", "fabric", "itemrestrictions"),
    ("JobsPlusRemastered-26.2", "fabric", "jobsplus"),
    ("AdvancedNetherite-26.2", "Fabric", "advancednetherite"),
    ("illagerinvasion-26.2.0-mc26.2-fabric/26.2", "Fabric", "illagerinvasion"),
    ("caramelChat-26.2", "fabric", "caramelchat"),
    ("FallingTree-minecraft-26.2", None, "fallingtree"),
]

EXPECTED_MINECRAFT_VERSION = "26.2"
OUTDATED_NAME_PARTS = ("1.21.9", "1.21.10")

REQUIRED_JAVA_MAJOR_VERSION = 25
BUILD_START_ATTEMPTS = 3
BUILD_START_RETRY_SECONDS = 2

EXCLUDED_NAME_PARTS = (
    "-sources",
    "-dev",
    "-dev-shadow",
    "-shadow",
    "-plain",
    "-javadoc",
)

DEPENDENCY_MANIFEST_NAME = "FABRIC_DEPENDENCIES.md"
DATAGEN_EXCLUDED_MODULES = {
    "AdvancedNetherite-26.2",
}


def workspace_root() -> Path:
    return Path(__file__).resolve().parent.parent


def read_java_major_version(java_home: Path) -> Optional[int]:
    java_executable_name = "java"
    if sys.platform == "win32":
        java_executable_name = "java.exe"

    java_executable = java_home / "bin" / java_executable_name
    release_file = java_home / "release"
    if not java_executable.is_file() or not release_file.is_file():
        return None

    for line in release_file.read_text(encoding="utf-8").splitlines():
        if not line.startswith("JAVA_VERSION="):
            continue

        version = line.split("=", maxsplit=1)[1].strip().strip('"')
        version_parts = version.split(".")
        major_version = version_parts[0]
        if major_version == "1" and len(version_parts) > 1:
            major_version = version_parts[1]

        if not major_version.isdigit():
            return None

        return int(major_version)

    return None


def java_home_candidates() -> list[Path]:
    candidates = []

    configured_java_25_home = os.environ.get("JAVA_25_HOME")
    if configured_java_25_home:
        candidates.append(Path(configured_java_25_home))

    configured_java_home = os.environ.get("JAVA_HOME")
    if configured_java_home:
        candidates.append(Path(configured_java_home))

    search_locations = []
    if sys.platform == "win32":
        program_files = Path(os.environ.get("ProgramFiles", "C:/Program Files"))
        search_locations.extend(
            [
                (program_files / "Eclipse Adoptium", "*"),
                (program_files / "Java", "*"),
                (Path.home() / ".jdks", "*"),
            ]
        )
    elif sys.platform == "darwin":
        search_locations.append(
            (Path("/Library/Java/JavaVirtualMachines"), "*/Contents/Home")
        )
    else:
        search_locations.append((Path("/usr/lib/jvm"), "*"))

    for search_root, search_pattern in search_locations:
        candidates.extend(search_root.glob(search_pattern))

    return candidates


def find_required_java_home() -> Optional[Path]:
    checked_paths = set()

    for candidate in java_home_candidates():
        resolved_candidate = candidate.expanduser().resolve()
        if resolved_candidate in checked_paths:
            continue

        checked_paths.add(resolved_candidate)
        major_version = read_java_major_version(resolved_candidate)
        if major_version == REQUIRED_JAVA_MAJOR_VERSION:
            return resolved_candidate

    return None


def is_release_jar(path: Path) -> bool:
    name = path.name.lower()
    return path.suffix.lower() == ".jar" and not any(part in name for part in EXCLUDED_NAME_PARTS)


def prepare_target_directory(target_dir: Path) -> bool:
    build_file_dir = Path(__file__).resolve().parent
    resolved_target_dir = target_dir.resolve()

    is_expected_parent = resolved_target_dir.parent == build_file_dir
    is_expected_name = resolved_target_dir.name == "build_files"
    if not is_expected_parent or not is_expected_name:
        print(f"Refusing to reset an unexpected target directory: {resolved_target_dir}")
        return False

    if resolved_target_dir.exists():
        shutil.rmtree(resolved_target_dir)

    resolved_target_dir.mkdir(parents=True, exist_ok=True)
    return True


def find_latest_release_jar(module_root: Path, fabric_dir: Optional[str]) -> Optional[Path]:
    if fabric_dir is None:
        libs_dir = module_root / "build" / "libs"
    else:
        libs_dir = module_root / fabric_dir / "build" / "libs"
    if not libs_dir.exists():
        return None

    jars = [path for path in libs_dir.glob("*.jar") if is_release_jar(path)]
    if not jars:
        return None

    return max(jars, key=lambda path: path.stat().st_mtime)


def read_fabric_mod_metadata(jar_path: Path) -> Optional[dict]:
    try:
        with zipfile.ZipFile(jar_path) as jar:
            with jar.open("fabric.mod.json") as manifest:
                return json.loads(manifest.read().decode("utf-8"))
    except (OSError, KeyError, ValueError, zipfile.BadZipFile):
        return None


def references_expected_minecraft(dependency) -> bool:
    if dependency is None:
        # 의존성 선언이 없으면 버전을 판별할 수 없으므로 통과시키고 이름 검사에 맡긴다.
        return True
    if isinstance(dependency, str):
        return EXPECTED_MINECRAFT_VERSION in dependency
    if isinstance(dependency, list):
        return any(EXPECTED_MINECRAFT_VERSION in str(entry) for entry in dependency)
    return False


def validate_release_jar(module_name: str, expected_mod_id: str, jar_path: Path,
                         seen_mod_ids: dict) -> bool:
    jar_name = jar_path.name.lower()
    for outdated_part in OUTDATED_NAME_PARTS:
        if outdated_part in jar_name:
            print(f"Outdated jar selected for {module_name}: {jar_path.name}")
            return False

    metadata = read_fabric_mod_metadata(jar_path)
    if metadata is None:
        print(f"fabric.mod.json is missing or invalid in {module_name}: {jar_path.name}")
        return False

    mod_id = metadata.get("id")
    if mod_id != expected_mod_id:
        print(f"Unexpected mod id in {module_name}: expected '{expected_mod_id}', found '{mod_id}'")
        return False

    if mod_id in seen_mod_ids:
        print(f"Duplicate mod id '{mod_id}' from {module_name} and {seen_mod_ids[mod_id]}")
        return False
    seen_mod_ids[mod_id] = module_name

    depends = metadata.get("depends", {})
    if not references_expected_minecraft(depends.get("minecraft")):
        print(
            f"Jar for {module_name} does not target Minecraft {EXPECTED_MINECRAFT_VERSION}: "
            f"{jar_path.name} (minecraft: {depends.get('minecraft')})"
        )
        return False

    return True


def get_environment_label(jar_path: Path) -> str:
    metadata = read_fabric_mod_metadata(jar_path)
    if metadata is None:
        return ""

    environment = metadata.get("environment", "*")
    if environment == "client":
        return " [client-only]"
    if environment == "server":
        return " [server-only]"
    return ""


def build_fabric_module(module_root: Path, fabric_dir: Optional[str], java_home: Path) -> bool:
    if fabric_dir is None:
        build_task = "buildJar"
    else:
        build_task = f":{fabric_dir}:build"

    if sys.platform == "win32":
        gradle_wrapper = module_root / "gradlew.bat"
        command = [
            os.environ.get("COMSPEC", "cmd.exe"),
            "/d",
            "/c",
            str(gradle_wrapper),
            build_task,
        ]
    else:
        gradle_wrapper = module_root / "gradlew"
        command = [
            str(gradle_wrapper),
            build_task,
        ]

    if not gradle_wrapper.is_file():
        print(f"Gradle wrapper not found: {gradle_wrapper}")
        return False

    if module_root.name in DATAGEN_EXCLUDED_MODULES:
        command.extend(["-x", f":{fabric_dir}:runDatagen"])

    command.append("--rerun-tasks")

    build_environment = os.environ.copy()
    build_environment["JAVA_HOME"] = str(java_home)
    build_environment["PATH"] = str(java_home / "bin") + os.pathsep + build_environment.get("PATH", "")

    build_label = fabric_dir if fabric_dir else "buildJar"
    print(f"\nBuilding {module_root.name} ({build_label})...")
    result = None
    for attempt in range(1, BUILD_START_ATTEMPTS + 1):
        try:
            result = subprocess.run(
                command,
                cwd=module_root,
                env=build_environment,
                check=False,
            )
            break
        except PermissionError as error:
            if attempt == BUILD_START_ATTEMPTS:
                print(f"Could not start the Gradle build: {error}")
                return False

            print(
                f"Gradle process start failed ({attempt}/{BUILD_START_ATTEMPTS}): "
                f"{error}. Retrying in {BUILD_START_RETRY_SECONDS} seconds..."
            )
            time.sleep(BUILD_START_RETRY_SECONDS)

    if result is None:
        print(f"Gradle did not start: {module_root.name}")
        return False

    if result.returncode != 0:
        print(f"Build failed: {module_root.name} (exit code: {result.returncode})")
        return False

    return True


def copy_module_jars() -> int:
    root = workspace_root()
    target_dir = Path(__file__).resolve().parent / "build_files"

    java_home = find_required_java_home()
    if java_home is None:
        print(
            f"Java {REQUIRED_JAVA_MAJOR_VERSION} was not found. "
            f"Install JDK {REQUIRED_JAVA_MAJOR_VERSION} or set JAVA_25_HOME."
        )
        return 1

    print(f"Using Java {REQUIRED_JAVA_MAJOR_VERSION}: {java_home}")

    for module_name, fabric_dir, expected_mod_id in MODULES:
        module_root = root / module_name
        if not module_root.is_dir():
            print(f"Module directory not found: {module_root}")
            return 1

    if not prepare_target_directory(target_dir):
        return 1

    for module_name, fabric_dir, expected_mod_id in MODULES:
        module_root = root / module_name
        if not build_fabric_module(module_root, fabric_dir, java_home):
            print("\nJAR collection stopped because a Fabric build failed.")
            return 1

    release_jars = []
    missing = []

    for module_name, fabric_dir, expected_mod_id in MODULES:
        module_root = root / module_name
        jar = find_latest_release_jar(module_root, fabric_dir)
        if jar is None:
            missing.append(module_name)
            continue

        release_jars.append((module_name, expected_mod_id, jar))

    if missing:
        print("\nNo release jar found for:")
        for module_name in missing:
            print(f"  - {module_name}")
        print("\nThe Fabric build completed, but no release JAR was generated.")
        return 1

    seen_mod_ids = {}
    invalid_modules = []
    for module_name, expected_mod_id, jar in release_jars:
        if not validate_release_jar(module_name, expected_mod_id, jar, seen_mod_ids):
            invalid_modules.append(module_name)

    if invalid_modules:
        print("\nJAR validation failed for:")
        for module_name in invalid_modules:
            print(f"  - {module_name}")
        print("\nNo files were copied. Fix the build outputs and run again.")
        return 1

    copied = []
    for module_name, expected_mod_id, jar in release_jars:
        target = target_dir / jar.name
        shutil.copy2(jar, target)
        copied.append((module_name, jar, target))

    dependency_manifest = Path(__file__).resolve().parent / DEPENDENCY_MANIFEST_NAME
    if not dependency_manifest.is_file():
        print(f"Dependency manifest not found: {dependency_manifest}")
        return 1

    print("Copied Fabric release jars:")
    for module_name, source, target in copied:
        print(f"  - {module_name}: {source.name} -> {target}{get_environment_label(source)}")

    print(f"Dependency manifest: {dependency_manifest}")
    return 0


if __name__ == "__main__":
    sys.exit(copy_module_jars())
