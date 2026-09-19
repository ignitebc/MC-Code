"""복셀 펫 네 종의 텍스처, Java 메시, 미리보기를 한 번에 다시 만든다.

    python design/pets/build_pets.py            # 전부
    python design/pets/build_pets.py gomi_pet   # 이름으로 골라서

Pillow 와 numpy 가 필요하다. 모양을 바꾸려면 dogs.py / legendaries.py 를 고친 뒤 다시 실행한다.
"""

import sys
from pathlib import Path

from dogs import build_gomi, build_super_gomi
from legendaries import build_dark_dragon, build_sculken_raven

PROJECT = Path(__file__).resolve().parents[2]
TEXTURES = PROJECT / "Common/src/main/resources/assets/advancednetherite/textures/entity"
MESH_PACKAGE = "com.autovw.advancednetherite.client.model.mesh"
MESHES = PROJECT / "Fabric/src/main/java" / MESH_PACKAGE.replace(".", "/")
# 미리보기는 확인용이라 저장소에 두지 않고 바탕화면에 만든다
PREVIEWS = Path.home() / "Desktop" / "pet_preview"

PETS = {
    "gomi_pet": (build_gomi, "GomiPetMesh", "design/pets/dogs.py"),
    "super_gomi_pet": (build_super_gomi, "SuperGomiPetMesh", "design/pets/dogs.py"),
    "dark_dragon_pet": (build_dark_dragon, "DarkDragonPetMesh", "design/pets/legendaries.py"),
    "sculken_raven_pet": (build_sculken_raven, "SculkenRavenPetMesh", "design/pets/legendaries.py"),
}


def main():
    names = sys.argv[1:] or list(PETS)
    MESHES.mkdir(parents=True, exist_ok=True)
    PREVIEWS.mkdir(parents=True, exist_ok=True)
    for name in names:
        builder, class_name, source = PETS[name]
        model = builder().build()
        model.texture.save(TEXTURES / f"{name}.png", optimize=True)
        (MESHES / f"{class_name}.java").write_text(
            model.java(class_name, MESH_PACKAGE, source), encoding="utf-8", newline="\n")
        model.preview_sheet().save(PREVIEWS / f"{name}.png", optimize=True)
        print(f"{name}: {model.box_count()} boxes, texture {model.atlas_width}x{model.atlas_height}px, "
              f"declared {model.declared_size[0]}x{model.declared_size[1]}")


if __name__ == "__main__":
    main()
