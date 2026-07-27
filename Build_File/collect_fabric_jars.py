import os
from pathlib import Path
import shutil
import subprocess
import sys
from typing import Optional


MODULES = [
    ("AdvancedNetherite-main", "Fabric"),
    ("ArcLib-1.21.9", "fabric"),
    ("illagerinvasion-21.10.0-mc1.21.10-fabric/1.21.10", "Fabric"),
    ("ItemRestrictions-1.21.9", "fabric"),
    ("UILib-1.21.9", "fabric"),
    ("YamlConfig-1.21.9", "fabric"),
    ("JobsPlusRemastered-1.21.10", "fabric"),
]

EXCLUDED_NAME_PARTS = (
    "-sources",
    "-dev",
    "-dev-shadow",
    "-shadow",
    "-plain",
    "-javadoc",
)


def workspace_root() -> Path:
    return Path(__file__).resolve().parent.parent


def is_release_jar(path: Path) -> bool:
    name = path.name.lower()
    return path.suffix.lower() == ".jar" and not any(part in name for part in EXCLUDED_NAME_PARTS)


def find_latest_release_jar(module_root: Path, fabric_dir: str) -> Optional[Path]:
    libs_dir = module_root / fabric_dir / "build" / "libs"
    if not libs_dir.exists():
        return None

    jars = [path for path in libs_dir.glob("*.jar") if is_release_jar(path)]
    if not jars:
        return None

    return max(jars, key=lambda path: path.stat().st_mtime)


def build_fabric_module(module_root: Path, fabric_dir: str) -> bool:
    if sys.platform == "win32":
        gradle_wrapper = module_root / "gradlew.bat"
        command = [
            os.environ.get("COMSPEC", "cmd.exe"),
            "/d",
            "/c",
            str(gradle_wrapper),
            f":{fabric_dir}:build",
            "--rerun-tasks",
        ]
    else:
        gradle_wrapper = module_root / "gradlew"
        command = [
            str(gradle_wrapper),
            f":{fabric_dir}:build",
            "--rerun-tasks",
        ]

    if not gradle_wrapper.is_file():
        print(f"Gradle wrapper not found: {gradle_wrapper}")
        return False

    print(f"\nBuilding {module_root.name} ({fabric_dir})...")
    result = subprocess.run(command, cwd=module_root, check=False)
    if result.returncode != 0:
        print(f"Build failed: {module_root.name} (exit code: {result.returncode})")
        return False

    return True


def copy_module_jars() -> int:
    root = workspace_root()
    target_dir = Path(__file__).resolve().parent / "build_files"
    target_dir.mkdir(parents=True, exist_ok=True)

    for module_name, fabric_dir in MODULES:
        module_root = root / module_name
        if not module_root.is_dir():
            print(f"Module directory not found: {module_root}")
            return 1

        if not build_fabric_module(module_root, fabric_dir):
            print("\nJAR collection stopped because a Fabric build failed.")
            return 1

    copied = []
    missing = []

    for module_name, fabric_dir in MODULES:
        module_root = root / module_name
        jar = find_latest_release_jar(module_root, fabric_dir)
        if jar is None:
            missing.append(module_name)
            continue

        target = target_dir / jar.name
        shutil.copy2(jar, target)
        copied.append((module_name, jar, target))

    print("Copied Fabric release jars:")
    for module_name, source, target in copied:
        print(f"  - {module_name}: {source.name} -> {target}")

    if missing:
        print("\nNo release jar found for:")
        for module_name in missing:
            print(f"  - {module_name}")
        print("\nThe Fabric build completed, but no release JAR was generated.")

    return 0 if not missing else 1


if __name__ == "__main__":
    sys.exit(copy_module_jars())
