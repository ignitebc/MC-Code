"""기본 총기팩의 총기·부착물·탄약 이름에 등급 색을 입힌다.

    python tools/apply_tier_colors.py

등급은 아래 표에서 정한다. 등급을 옮기려면 ID를 다른 줄로 옮긴 뒤 다시 실행하면 된다.
이름 맨 앞의 색 코드만 바꾸며, 표에 없는 항목은 색을 떼어 흰색으로 둔다.
새 총기나 부착물을 추가하고 표에 넣지 않으면 실행이 멈춘다.
"""

import glob
import json
import os
import re
from pathlib import Path

PACK = Path(__file__).resolve().parents[1] / "src/main/resources/assets/tacz/custom/tacz_default_gun"

# F부터 S까지: 연두, 하늘, 파랑, 보라, 노랑, 빨강, 주황
TIER_COLORS = {"F": "§a", "E": "§b", "D": "§9", "C": "§5", "B": "§e", "A": "§c", "S": "§6"}

GUNS = {
    "S": "m95 m107 minigun rpg7 spas_12",
    "A": "ai_awp aa12 fn_evolys scar_h m1014 vector45 taurus500 m320",
    "B": "spr15hb m16a1 rpk m416 ump45 qbz_191 mk14 kar98 m870 m249 sks_tactical",
    "C": "ak47 type_81 g36k m4a1 aug qbz_95 scar_l fn_fal slr hk_g3 m700 p90 mp5k",
    "D": "b93r deagle timeless50 db_long sawed_off springfield1873 m16a4 micro_uzi",
    "E": "lonetrail p320 cz75 m1911 deagle_golden rhino357",
    "F": "m9a4 p18c hk_mk23",
}

ATTACHMENTS = {
    "S": "scope_mk5hd scope_standard_8x muzzle_silencer_wraith muzzle_brake_timeless50 muzzle_silencer_vulture"
         " stock_militech_b5 grip_vertical_ranger laser_peq6 ammo_mod_hp ammo_mod_fmj",
    "A": "scope_vudu scope_lpvo_1_6 scope_hamr scope_elcan_4x muzzle_brake_cyclone_d2 muzzle_silencer_ursus"
         " muzzle_silencer_knight_qd muzzle_silencer_ptilopsis stock_hk_slim_line oem_stock_heavy stock_heavy_spas_12"
         " grip_vertical_military grip_rk1_b25u grip_cqr laser_lopro ammo_mod_i ammo_mod_slug",
    "B": "scope_contender scope_acog_ta31 scope_qmk152 scope_retro_2x scope_98k scope_1873_6x"
         " muzzle_compensator_trident muzzle_brake_cthulhu muzzle_brake_trex muzzle_brake_pioneer"
         " muzzle_silencer_phantom_s1 muzzle_brake_mastiff_sg stock_tactical_ar oem_stock_tactical stock_ak12"
         " stock_tactical_spas_12 grip_se_5 grip_osovets_black grip_rk0 grip_vertical_talon grip_magpul_afg_2"
         " laser_compact laser_peq15 ammo_mod_he"
         " extended_mag_3 light_extended_mag_3 shotgun_extended_mag_3 sniper_extended_mag_3",
    "C": "sight_exp3 sight_552 sight_uh1 sight_srs_02 sight_acro_rifle muzzle_silencer_mirage muzzle_silencer_sg"
         " muzzle_choke_sg deagle_golden_long_barrel stock_sba3 stock_ripstock stock_moe grip_rk6 grip_td"
         " laser_nightstick",
    "D": "sight_acro_pistol sight_deltapoint_pistol sight_deltapoint_rifle sight_fastfire_pistol"
         " sight_fastfire_rifle sight_pk06_pistol sight_pk06_rifle stock_carbon_bone_c5 oem_stock_light stock_m4ss"
         " grip_cobra extended_mag_2 light_extended_mag_2 shotgun_extended_mag_2 sniper_extended_mag_2",
    "E": "sight_t1 sight_t2 sight_okp7 sight_coyote sight_rmr_dot sight_sro_dot bayonet_6h3 bayonet_m9"
         " extended_mag_1 light_extended_mag_1 shotgun_extended_mag_1 sniper_extended_mag_1",
    "F": "scope_aug_default sight_p90",
}

# 탄약은 그 탄을 쓰는 총기의 등급을 따른다. 기본 팩에 쓰는 총기가 없는 탄은 등급 없이 흰색.
AMMO = {
    "S": "50bmg rpg_rocket",
    "A": "338 40mm 500mag",
    "B": "308 30_06 792x57 12g",
    "C": "762x39 58x42 556x45 57x28 45_70",
    "D": "45acp 50ae 357mag",
    "E": "9mm",
}
UNGRADED_AMMO = []  # 사용 총기가 없던 탄약 5종을 제거해 현재는 비어 있다


def strip_comments(text):
    return re.sub(r"^\s*//.*$", "", text, flags=re.M)


def name_keys(kind):
    """인덱스 파일에서 ID → 이름 번역 키를 읽는다 (키 이름이 .name 으로 끝나지 않는 항목도 있다)."""
    keys = {}
    for path in glob.glob(str(PACK / f"data/tacz/index/{kind}/*.json")):
        index = json.loads(strip_comments(open(path, encoding="utf-8").read()))
        keys[os.path.basename(path)[:-5]] = index["name"]
    return keys


def tier_of(table, all_ids, ungraded=()):
    tiers = {item: tier for tier, ids in table.items() for item in ids.split()}
    missing = set(all_ids) - set(tiers) - set(ungraded)
    unknown = set(tiers) - set(all_ids)
    assert not missing and not unknown, f"등급표와 실제 목록이 다르다: 빠짐 {sorted(missing)}, 없는 ID {sorted(unknown)}"
    return tiers


def main():
    colors = {}  # 번역 키 → 색 코드 ("" 이면 색 없음)
    for kind, table, ungraded in (("guns", GUNS, ()), ("attachments", ATTACHMENTS, ()), ("ammo", AMMO, UNGRADED_AMMO)):
        keys = name_keys(kind)
        tiers = tier_of(table, keys, ungraded)
        for item, key in keys.items():
            colors[key] = TIER_COLORS[tiers[item]] if item in tiers else ""

    for lang in ("ko_kr", "en_us"):
        path = PACK / f"assets/tacz/lang/{lang}.json"
        raw = open(path, encoding="utf-8", newline="").read()
        data = json.loads(raw)
        changed = 0
        for key, color in colors.items():
            assert key in data, f"{lang}: 번역 키가 없다 {key}"
            new_value = color + re.sub(r"^(§.)+", "", data[key])
            if new_value == data[key]:
                continue
            pattern = re.escape(json.dumps(key)) + r"\s*:\s*" + re.escape(json.dumps(data[key], ensure_ascii=False))
            replacement = f"{json.dumps(key)}: {json.dumps(new_value, ensure_ascii=False)}"
            raw, hits = re.subn(pattern, lambda _: replacement, raw)
            assert hits == 1, key
            changed += 1
        json.loads(raw)
        open(path, "w", encoding="utf-8", newline="").write(raw)
        print(f"{lang}: {changed} names recolored")


if __name__ == "__main__":
    main()
