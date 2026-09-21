"""외형 미리보기. 빌드 없이 모양을 확인할 때 쓴다.

    python preview.py <out.png> <모듈:총기ID> ...

옆모습과 슬롯 아이콘 구도를 한 줄씩 붙여 저장한다.
"""

import importlib
import sys
import zlib

from PIL import Image

from build_common import finish_model
from core import GunModel, side_image, slot_icon
from specs import SPECS

BACKGROUND = (58, 60, 68, 255)


def main():
    out = sys.argv[1]
    rows = []
    for spec in sys.argv[2:]:
        module, gun_id = spec.split(":")
        builder, base, body = importlib.import_module(module).GUNS[gun_id]
        model = GunModel(gun_id, base, body, ppu=4.0)
        builder(model)
        finish_model(model, SPECS[gun_id]["atts"])
        texture = model.build_texture(seed=zlib.crc32(gun_id.encode()))
        geo = model.geometry()["minecraft:geometry"][0]
        side = side_image(geo, texture, ppu=11)
        icon = slot_icon(geo, texture, 64).resize((160, 160), Image.NEAREST)
        row = Image.new("RGBA", (side.width + 168, max(side.height, 164)), BACKGROUND)
        row.alpha_composite(side, (0, 0))
        row.alpha_composite(icon, (side.width + 4, 2))
        rows.append(row)
        print(gun_id, "cubes", model.count(), "lod", model.count(detail=False), "tex", model.texture_size)
    sheet = Image.new("RGBA", (max(r.width for r in rows), sum(r.height for r in rows)), BACKGROUND)
    y = 0
    for row in rows:
        sheet.paste(row, (0, y))
        y += row.height
    sheet.save(out)


if __name__ == "__main__":
    main()
