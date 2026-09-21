"""부착물을 단 모습 미리보기. 부착 위치 뼈가 맞는지 빌드 없이 확인할 때 쓴다.

    python preview_attachments.py <out.png> <총기ID>=<부착물ID>,<부착물ID>,... ...

기본 총기팩에 이미 만들어진 총기 모델을 읽어, 각 부착물 모델을 해당 위치 뼈(scope_pos 등)에 얹어 그린다.
게임과 같게 개머리판·총구 부착물이 있으면 stock_default·muzzle_default 를 숨기고 어댑터를 보인다.
"""

import copy
import sys

from PIL import Image

from core import PACK, load_json, side_image
from render_gun_slot import DEFAULT_HIDDEN

BACKGROUND = (58, 60, 68, 255)
FAR_LIMIT = 16  # 조준경의 조준선·렌즈 평면처럼 멀리 놓인 큐브는 그리지 않는다


def geometry(path):
    return load_json(PACK / f"assets/tacz/geo_models/{path}.json")["minecraft:geometry"][0]


def mounted(attachment_geo, mount_bone):
    """부착물 모델 전체를 총기의 위치 뼈로 옮긴 모델을 만든다. 부착물의 원점이 장착 지점이다."""
    shift = mount_bone["pivot"]
    result = copy.deepcopy(attachment_geo)
    root = {"name": "__mount", "pivot": list(shift)}
    if mount_bone.get("rotation"):
        root["rotation"] = list(mount_bone["rotation"])
    for bone in result["bones"]:
        bone.setdefault("parent", "__mount")
        bone["pivot"] = [p + s for p, s in zip(bone.get("pivot", [0, 0, 0]), shift)]
        kept = []
        for cube in bone.get("cubes", []):
            is_far = max(abs(v) for v in cube["origin"]) > FAR_LIMIT or max(cube["size"]) > FAR_LIMIT
            if is_far:
                continue
            cube["origin"] = [o + s for o, s in zip(cube["origin"], shift)]
            if "pivot" in cube:
                cube["pivot"] = [p + s for p, s in zip(cube["pivot"], shift)]
            kept.append(cube)
        bone["cubes"] = kept
    result["bones"].insert(0, root)
    return result


def render(gun_id, attachment_ids, ppu=11):
    gun = geometry(f"gun/{gun_id}_geo")
    bones = {bone["name"]: bone for bone in gun["bones"]}
    texture = Image.open(PACK / f"assets/tacz/textures/gun/uv/{gun_id}.png")
    hidden = set(DEFAULT_HIDDEN) - {"mount"}
    layers, shown_adapters = [], set()
    for attachment_id in attachment_ids:
        index = load_json(PACK / f"data/tacz/index/attachments/{attachment_id}.json")
        slot = index["type"]
        display = load_json(PACK / f"assets/tacz/display/attachments/{index['display'].split(':')[1]}.json")
        hidden.add(f"{slot}_default")
        shown_adapters.add(display.get("adapter"))
        model = mounted(geometry(display["model"].split(":")[1]), bones[f"{slot}_pos"])
        skin = Image.open(PACK / f"assets/tacz/textures/{display['texture'].split(':')[1]}.png")
        layers.append((model, skin))
    if not any(load_json(PACK / f"data/tacz/index/attachments/{a}.json")["type"] == "scope" for a in attachment_ids):
        hidden.add("mount")
    hidden |= {bone["name"] for bone in gun["bones"]
               if bone.get("parent") == "attachment_adapter" and bone["name"] not in shown_adapters}
    bounds = (-34, 26, -3, 20)
    image = Image.new("RGBA", (int((bounds[1] - bounds[0]) * ppu), int((bounds[3] - bounds[2]) * ppu)), BACKGROUND)
    image.alpha_composite(side_image(gun, texture, ppu=ppu, bounds=bounds, hidden=hidden))
    for model, skin in layers:
        image.alpha_composite(side_image(model, skin, ppu=ppu, bounds=bounds, hidden=()))
    return image


def main():
    out = sys.argv[1]
    rows = []
    for spec in sys.argv[2:]:
        gun_id, _, attachments = spec.partition("=")
        rows.append(render(gun_id, [a for a in attachments.split(",") if a]))
    sheet = Image.new("RGBA", (rows[0].width, sum(row.height for row in rows)), BACKGROUND)
    y = 0
    for row in rows:
        sheet.paste(row, (0, y))
        y += row.height
    sheet.save(out)


if __name__ == "__main__":
    main()
