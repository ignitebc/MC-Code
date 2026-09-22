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
    "S": "m107 m95 minigun ai_awp mg3 fn_evolys",
    "A": "mk14 scar_h m24 rpg7 rpd fn_fal hk_g3 m700 dbs rpk o12 taurus500",
    "B": "aa12 m249 dragunov slr spas_12 mk12 mk47_mutant qbz_191 m1014 groza p90 spr15hb beryl_m762 qbz_95 kar98 "
         "ace32 sks_tactical m16a1 m870",
    "C": "ak47 m320 s12k scar_l mini14 type_81 m416 tommy_gun springfield1873 m16a4 win94 g36k k2 aug famas lonetrail "
         "m4a1 ump45 vector45 deagle",
    "D": "timeless50 micro_uzi db_long js9 vss deagle_golden b93r rhino357 mp5k",
    "E": "p320 r1895 m1911 sawed_off hk_mk23 mp9 cz75",
    "F": "p18c skorpion m9a4",
}

ATTACHMENTS = {
    "S": "muzzle_brake_timeless50 laser_peq6 stock_militech_b5 grip_vertical_ranger scope_mk5hd "
         "muzzle_silencer_vulture muzzle_silencer_wraith ammo_mod_i scope_1873_6x ammo_mod_fmj",
    "A": "grip_rk1_b25u stock_heavy_spas_12 oem_stock_heavy muzzle_brake_cyclone_d2 scope_standard_8x "
         "grip_vertical_military ammo_mod_slug muzzle_silencer_mirage oem_stock_tactical ammo_mod_he "
         "muzzle_silencer_ptilopsis muzzle_silencer_sg laser_lopro grip_osovets_black scope_vudu scope_contender "
         "scope_lpvo_1_6",
    "B": "extended_mag_3 light_extended_mag_3 shotgun_extended_mag_3 sniper_extended_mag_3 muzzle_compensator_trident "
         "muzzle_brake_mastiff_sg muzzle_brake_trex stock_hk_slim_line stock_ak12 muzzle_brake_cthulhu "
         "stock_tactical_ar muzzle_silencer_phantom_s1 ammo_mod_hp stock_tactical_spas_12 laser_nightstick "
         "laser_compact grip_rk0 grip_vertical_talon grip_cqr muzzle_brake_pioneer scope_aug_default grip_td "
         "scope_elcan_4x scope_hamr grip_rk6 scope_98k scope_retro_2x scope_qmk152",
    "C": "sight_acro_rifle sight_uh1 sight_acro_pistol sight_pk06_pistol sight_pk06_rifle muzzle_silencer_knight_qd "
         "stock_m4ss stock_sba3 muzzle_silencer_ursus laser_peq15 oem_stock_light deagle_golden_long_barrel "
         "grip_magpul_afg_2 grip_se_5 muzzle_choke_sg",
    "D": "extended_mag_2 light_extended_mag_2 shotgun_extended_mag_2 sniper_extended_mag_2 sight_t2 sight_t1 "
         "sight_552 sight_deltapoint_pistol sight_deltapoint_rifle sight_fastfire_pistol sight_fastfire_rifle "
         "stock_carbon_bone_c5 stock_moe grip_cobra stock_ripstock",
    "E": "bayonet_m9 sight_exp3 sight_okp7 sight_srs_02 bayonet_6h3 extended_mag_1 light_extended_mag_1 "
         "shotgun_extended_mag_1 sniper_extended_mag_1 sight_coyote sight_sro_dot sight_p90",
    "F": "sight_rmr_dot scope_acog_ta31",
}

# 탄약은 그 탄을 쓰는 총기의 등급을 따른다. 기본 팩에 쓰는 총기가 없는 탄은 등급 없이 흰색.
AMMO = {
    "S": "50bmg 308",
    "A": "rpg_rocket 338 40mm",
    "B": "500mag 12g 762x39 792x57",
    "C": "45_70 45acp 30_06 556x45 58x42",
    "D": "50ae 9mm 57x28",
    "E": "357mag",
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
