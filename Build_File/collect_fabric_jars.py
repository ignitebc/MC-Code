from pathlib import Path
import shutil
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


def copy_module_jars() -> int:
    root = workspace_root()
    target_dir = Path(__file__).resolve().parent
    target_dir.mkdir(parents=True, exist_ok=True)

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
        print("\nRun the module's Fabric build first, then run this script again.")

    return 0 if not missing else 1


if __name__ == "__main__":
    sys.exit(copy_module_jars())
