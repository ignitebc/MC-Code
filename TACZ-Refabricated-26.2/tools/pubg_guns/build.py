"""PUBG 계열 신규 총기 22종의 리소스를 기본 총기팩에 써 넣는다.

    python tools/pubg_guns/build.py            # 22종 전부
    python tools/pubg_guns/build.py m24 vss    # 일부만

총기마다 index·data·recipe·부착물 태그·display·모델·LOD·텍스처·슬롯/HUD 아이콘을 만들고
한·영 언어 파일의 이름과 설명을 맞춘다. 끝나면 정합성 검사를 돌린다.
수치는 specs.py, 외형은 guns_*.py 에서 고친 뒤 다시 실행하면 된다.
"""

import copy
import json
import re
import sys
import zlib
from pathlib import Path

import guns_ar
import guns_dmr
import guns_other
from build_common import finish_model
from core import PACK, GunModel, hud_icon, load_json, slot_icon, write_json
from specs import SPECS

BUILDERS = {**guns_ar.GUNS, **guns_dmr.GUNS, **guns_other.GUNS}
SOUND_PROFILES = json.loads((Path(__file__).parent / "sound_profiles.json").read_text(encoding="utf-8"))
SOUND_KEYS = ("shoot", "shoot_3p", "silence", "silence_3p")
VIEW_BONES = ("idle_view", "iron_view", "fixed", "ground", "thirdperson_hand", "refit_view")

SCOPE_TAGS = {
    "sniper": ["#tacz:scope", "#tacz:scope_lowsight"],
    "sight_only": ["#tacz:scope_sight"],
    "default": ["#tacz:scope_sight", "#tacz:scope_scope"],
}
SIGHT_ONLY_GUNS = ("tommy_gun", "skorpion", "o12")


def attachment_tags(gun_id, spec):
    gun_type, tags = spec["type"], []
    light = gun_type in ("smg", "pistol")
    for attachment in spec["atts"]:
        if attachment in spec.get("builtin", {}):
            continue  # 일체형 부착물은 슬롯만 열어 두고 바꿔 달 수 없게 한다
        if attachment == "scope":
            key = "sniper" if gun_type == "sniper" else "sight_only" if gun_id in SIGHT_ONLY_GUNS else "default"
            tags += SCOPE_TAGS[key]
        elif attachment == "muzzle":
            if gun_id == "r1895":
                tags.append("#tacz:pistol_silencer")
            elif gun_type == "shotgun":
                tags.append("#tacz:muzzle_shotgun")
            elif gun_type == "pistol":
                tags.append("#tacz:pistol_muzzle")
            else:
                tags += ["#tacz:muzzle", "#tacz:pistol_muzzle"] if gun_type == "smg" else ["#tacz:muzzle"]
        elif attachment == "extended_mag":
            if light:
                tags.append("#tacz:light_extended_mag")
            elif gun_type == "sniper":
                tags.append("#tacz:sniper_extended_mag")
            elif gun_id == "s12k":
                tags.append("#tacz:shotgun_extended_mag")
            else:
                tags.append("#tacz:extended_mag")
        elif attachment == "grip":
            tags.append("#tacz:grip")
        elif attachment == "stock":
            tags.append("#tacz:stock")  # AR 규격 개머리판. 총기 전용 형상이 필요한 oem_stock 은 뺀다
        elif attachment == "laser":
            tags.append("#tacz:pistol_laser" if light else "#tacz:ar_laser")
    is_pellet_shotgun = gun_type == "shotgun" and spec.get("pellets", 1) > 1
    tags += ["#tacz:ammo_mod", "#tacz:ammo_mod_shotgun_exclusive"] if is_pellet_shotgun else ["#tacz:ammo_mod_no_he"]
    return tags


def scale_keyframes(frames, factor):
    for frame in frames:
        frame["value"] = [round(v * factor, 4) for v in frame["value"]]


def gun_data(gun_id, spec):
    def base_data(name):
        return load_json(PACK / f"data/tacz/data/guns/{name}_data.json")

    data = base_data(spec["base"])
    for key in ("builtin_attachments", "exclusive_attachments", "extended_mag_ammo_amount", "burst_data"):
        data.pop(key, None)
    data["ammo"] = spec["ammo"]
    data["ammo_amount"] = spec["mag"]
    if spec["ext"]:
        data["extended_mag_ammo_amount"] = list(spec["ext"])
    data["rpm"] = spec["rpm"]
    data["fire_mode"] = list(spec["modes"])
    if "burst" in spec:
        count, bpm, interval = spec["burst"][:3]
        continuous = len(spec["burst"]) > 3 and spec["burst"][3]
        data["burst_data"] = {"continuous_shoot": continuous, "count": count, "bpm": bpm, "min_interval": interval}

    bullet = copy.deepcopy(base_data(spec["bullet_like"])["bullet"]) if "bullet_like" in spec else data["bullet"]
    near, middle = spec["ranges"]
    damage = spec["damage"]
    bullet.update({"damage": damage, "bullet_amount": spec.get("pellets", 1), "speed": spec["speed"],
                   "pierce": spec.get("pierce", 1)})
    if "gravity" in spec:
        bullet["gravity"] = spec["gravity"]
    bullet.pop("explosion", None)
    bullet["extra_damage"] = {
        "armor_ignore": spec["armor"], "head_shot_multiplier": spec["head"],
        "damage_adjust": [{"distance": near, "damage": damage}, {"distance": middle, "damage": round(damage * 0.82, 2)},
                          {"distance": "infinite", "damage": round(damage * 0.62, 2)}]}
    data["bullet"] = bullet

    if "recoil_like" in spec:
        data["recoil"] = base_data(spec["recoil_like"])["recoil"]
    else:
        scale_keyframes(data["recoil"]["pitch"], spec["recoil"][0])
        scale_keyframes(data["recoil"]["yaw"], spec["recoil"][1])
    if "spread_like" in spec:
        data["inaccuracy"] = base_data(spec["spread_like"])["inaccuracy"]
    else:
        data["inaccuracy"] = {k: round(v * spec["spread"], 3) for k, v in data["inaccuracy"].items()}
    if "aim_spread" in spec:
        data["inaccuracy"]["aim"] = spec["aim_spread"]

    data["weight"] = spec["weight"]
    data["aim_time"] = spec["aim"]
    data["crawl_recoil_multiplier"] = spec.get("crawl", 0.5)
    data["allow_attachment_types"] = list(spec["atts"])
    if "builtin" in spec:
        data["builtin_attachments"] = dict(spec["builtin"])
    if "script" in spec:
        data["script"] = spec["script"]
    return data


def primary_and_layers(layers):
    """가장 큰 레이어를 기본 발사음으로, 나머지는 기본 대비 상대 볼륨의 덧소리로 나눈다."""
    ordered = sorted(layers, key=lambda layer: -layer[1])
    primary = ordered[0]
    extras = [{"sound": sound, "volume": round(min(0.6, volume / primary[1]), 2), "pitch": pitch}
              for sound, volume, pitch in ordered[1:]]
    return primary[0], extras


def gun_display(gun_id, spec):
    display = load_json(PACK / f"assets/tacz/display/guns/{spec['base']}_display.json")
    display["model"] = f"tacz:gun/{gun_id}_geo"
    display["texture"] = f"tacz:gun/uv/{gun_id}"
    display["lod"] = {"model": f"tacz:gun/lod/{gun_id}", "texture": f"tacz:gun/uv/{gun_id}"}
    display["hud"] = f"tacz:gun/hud/{gun_id}"
    display["slot"] = f"tacz:gun/slot/{gun_id}"
    profile = SOUND_PROFILES[gun_id]
    sound_layers = {}
    for key in SOUND_KEYS:
        source = key.replace("shoot", "silence") if spec.get("silenced") else key
        primary, extras = primary_and_layers(profile[source])
        display["sounds"][key] = primary
        if extras:
            sound_layers[key] = extras
    display["sound_layers"] = sound_layers
    return display


def gun_recipe(gun_id, spec):
    recipe = load_json(PACK / f"data/tacz/recipe/gun/{spec['like']}.json")
    recipe["result"] = {"type": "gun", "id": f"tacz:{gun_id}"}
    return recipe


def build_gun(gun_id):
    spec = SPECS[gun_id]
    builder, base, body = BUILDERS[gun_id]
    if base != spec["base"]:
        raise ValueError(f"{gun_id}: 외형 베이스({base})와 수치 베이스({spec['base']})가 다르다")
    model = GunModel(gun_id, base, body, ppu=4.0)
    builder(model)
    model.meta["shotgun"] = spec["type"] == "shotgun"
    finish_model(model, spec["atts"])
    texture = model.build_texture(seed=zlib.crc32(gun_id.encode()))
    geometry = model.geometry()
    assets = PACK / "assets/tacz"
    write_json(assets / f"geo_models/gun/{gun_id}_geo.json", geometry)
    write_json(assets / f"geo_models/gun/lod/{gun_id}.json", model.geometry(lod=True))
    for folder in ("uv", "slot", "hud"):
        (assets / f"textures/gun/{folder}").mkdir(parents=True, exist_ok=True)
    texture.save(assets / f"textures/gun/uv/{gun_id}.png")
    geo = geometry["minecraft:geometry"][0]
    slot_icon(geo, texture).save(assets / f"textures/gun/slot/{gun_id}.png")
    hud_icon(geo, texture).save(assets / f"textures/gun/hud/{gun_id}.png")

    data_root = PACK / "data/tacz"
    write_json(data_root / f"index/guns/{gun_id}.json", {
        "name": f"tacz.gun.{gun_id}.name", "display": f"tacz:{gun_id}_display", "data": f"tacz:{gun_id}_data",
        "tooltip": f"tacz.gun.{gun_id}.desc", "type": spec["type"], "item_type": "modern_kinetic", "sort": spec["sort"]})
    write_json(data_root / f"data/guns/{gun_id}_data.json", gun_data(gun_id, spec))
    write_json(data_root / f"recipe/gun/{gun_id}.json", gun_recipe(gun_id, spec))
    write_json(data_root / f"tacz_tags/attachments/allow_attachments/{gun_id}.json", attachment_tags(gun_id, spec))
    write_json(assets / f"display/guns/{gun_id}_display.json", gun_display(gun_id, spec))
    print(f"{gun_id:12s} 큐브 {model.count():3d} (LOD {model.count(detail=False):3d})  텍스처 {model.texture_size}")


def update_lang(gun_ids):
    """언어 파일의 다른 줄은 건드리지 않고 해당 총기의 이름·설명 줄만 바꾸거나 덧붙인다."""
    for file_name, name_key, desc_key in (("ko_kr.json", "ko", "desc_ko"), ("en_us.json", "en", "desc_en")):
        path = PACK / "assets/tacz/lang" / file_name
        text = path.read_text(encoding="utf-8")
        for gun_id in gun_ids:
            spec = SPECS[gun_id]
            for key, value in ((f"tacz.gun.{gun_id}.name", spec[name_key]), (f"tacz.gun.{gun_id}.desc", spec[desc_key])):
                pattern = re.compile(r'"' + re.escape(key) + r'"\s*:\s*"((?:[^"\\]|\\.)*)"')
                found = pattern.search(text)
                # 이름 앞의 등급 색 코드는 apply_tier_colors.py 가 붙인 것이므로 그대로 둔다
                color = found.group(1)[:2] if found and found.group(1).startswith("§") else ""
                line = f'"{key}": {json.dumps(color + value, ensure_ascii=False)}'
                if found:
                    text = pattern.sub(lambda _match, replacement=line: replacement, text, count=1)
                else:
                    end = text.rstrip().rfind("}")
                    head = text[:end].rstrip()
                    text = head + ("," if not head.endswith(("{", ",")) else "") + f"\n  {line}\n}}\n"
        path.write_text(text, encoding="utf-8")


def check(gun_ids):
    """빌드 없이 확인할 수 있는 정합성 검사. 문제를 찾으면 목록을 돌려준다."""
    problems = []
    ammo = {p.stem for p in (PACK / "data/tacz/index/ammo").glob("*.json")}
    attachments = {p.stem for p in (PACK / "data/tacz/index/attachments").glob("*.json")}
    tags = {p.stem for p in (PACK / "data/tacz/tacz_tags/attachments").glob("*.json")}
    scripts = {p.stem for p in (PACK / "data/tacz/scripts").glob("*.lua")}
    sound_root = PACK / "assets/tacz/tacz_sounds"
    lang = {name: load_json(PACK / f"assets/tacz/lang/{name}.json") for name in ("ko_kr", "en_us")}
    for gun_id in gun_ids:
        def report(message, gun=gun_id):
            problems.append(f"{gun}: {message}")

        data = load_json(PACK / f"data/tacz/data/guns/{gun_id}_data.json")
        display = load_json(PACK / f"assets/tacz/display/guns/{gun_id}_display.json")
        geo = load_json(PACK / f"assets/tacz/geo_models/gun/{gun_id}_geo.json")["minecraft:geometry"][0]
        load_json(PACK / f"assets/tacz/geo_models/gun/lod/{gun_id}.json")
        bones = {bone["name"]: bone for bone in geo["bones"]}
        if data["ammo"].split(":")[1] not in ammo:
            report(f"탄약 없음 {data['ammo']}")
        if "script" in data and data["script"].split(":")[1] not in scripts:
            report(f"스크립트 없음 {data['script']}")
        for builtin in data.get("builtin_attachments", {}).values():
            if builtin.split(":")[1] not in attachments:
                report(f"일체형 부착물 없음 {builtin}")
        for tag in load_json(PACK / f"data/tacz/tacz_tags/attachments/allow_attachments/{gun_id}.json"):
            if tag.startswith("#") and tag.split(":")[1] not in tags:
                report(f"태그 없음 {tag}")
        for name, texts in lang.items():
            for suffix in ("name", "desc"):
                if f"tacz.gun.{gun_id}.{suffix}" not in texts:
                    report(f"번역 없음 {name} {suffix}")
        for key in SOUND_KEYS:
            layered = [layer["sound"] for layer in display.get("sound_layers", {}).get(key, [])]
            for sound in [display["sounds"][key]] + layered:
                if not (sound_root / (sound.split(":")[1] + ".ogg")).exists():
                    report(f"음원 없음 {sound}")
        animation = load_json(PACK / f"assets/tacz/animations/{display['animation'].split(':')[1]}.animation.json")
        animated = set()
        for entry in animation["animations"].values():
            animated |= set(entry.get("bones", {}))
        # 베이스 총기 모델에도 없는 뼈(다른 총과 애니메이션 파일을 같이 쓰는 경우)는 원래 무시되므로 뺀다
        base_geo = load_json(PACK / f"assets/tacz/geo_models/gun/{SPECS[gun_id]['base']}_geo.json")
        base_bones = {bone["name"] for bone in base_geo["minecraft:geometry"][0]["bones"]}
        missing = sorted((animated & base_bones) - set(bones))
        if missing:
            report(f"애니메이션 뼈 없음 {missing}")
        for name in VIEW_BONES:
            if name not in bones:
                report(f"시점 뼈 없음 {name}")
        if BUILDERS[gun_id][2] in animated:
            report(f"몸통 뼈 {BUILDERS[gun_id][2]} 가 애니메이션 대상이다")
        for attachment in data["allow_attachment_types"]:
            if attachment != "extended_mag" and f"{attachment}_pos" not in bones:
                report(f"부착 위치 뼈 없음 {attachment}_pos")
            if f"refit_{attachment}_view" not in bones:
                report(f"개조 화면 시점 뼈 없음 refit_{attachment}_view")
        side_mount = bones.get("laser_pos", {}).get("rotation")
        if "laser" in data["allow_attachment_types"] and side_mount != [0, 0, 90]:
            report(f"레이저 위치 뼈 회전이 옆면 장착용이 아니다: {side_mount}")
        levels = ["mag_extended_1", "mag_extended_2", "mag_extended_3"] if "extended_mag_ammo_amount" in data else []
        for level in levels:
            if level not in bones or not bones[level].get("cubes"):
                report(f"확장 탄창 모양 없음 {level}")
        for folder in ("uv", "slot", "hud"):
            if not (PACK / f"assets/tacz/textures/gun/{folder}/{gun_id}.png").exists():
                report(f"이미지 없음 {folder}")
    return problems


def main():
    gun_ids = sys.argv[1:] or list(SPECS)
    for gun_id in gun_ids:
        build_gun(gun_id)
    update_lang(gun_ids)
    problems = check(gun_ids)
    for problem in problems:
        print("문제:", problem)
    print(f"{len(gun_ids)}종 생성, 문제 {len(problems)}건")


if __name__ == "__main__":
    main()
