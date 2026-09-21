"""PUBG 계열 신규 총기 22종의 전투 수치와 설명.

수치의 기준은 같은 탄종을 쓰는 기존 총기 사이에 끼워 넣는 것이다.
각 총기는 'base' 총기의 데이터·표시 설정·애니메이션을 본뜬 뒤 아래 값으로 덮어쓴다.

    ranges  : (최대 피해 거리, 중간 거리) — 중간 구간은 피해의 82%, 그 뒤는 62%
    recoil  : 베이스 반동 곡선에 곱하는 (수직, 수평) 배율
    spread  : 베이스 탄 퍼짐에 곱하는 배율 (aim_spread 가 있으면 조준 시 퍼짐은 그 값으로 고정)
    like    : 조합식 재료를 본뜰 기존 총기(같은 등급)
"""

RANGES = {"ar": (30, 60), "dmr": (60, 120), "smg": (20, 45), "sr": (70, 140), "sg": (11, 23), "mg": (42, 86),
          "pistol": (16, 34)}

SPECS = {
    "groza": dict(
        ko="그로자", en="Groza", type="rifle", sort=20, tier="A", base="aug", like="scar_h",
        ammo="tacz:762x39", mag=30, ext=(34, 37, 40), rpm=750, modes=("auto", "semi"),
        damage=2.85, ranges=RANGES["ar"], armor=0.3, head=1.5, speed=255, weight=3.2, aim=0.18,
        recoil=(1.7, 1.4), spread=1.0, atts=("scope", "muzzle", "extended_mag"),
        desc_ko="7.62mm 불펍 돌격소총. 짧은 전장에 7.62mm 돌격소총 중 가장 높은 연사 화력을 담았고, 부착물은 제한됩니다.",
        desc_en="7.62mm bullpup assault rifle. Highest sustained firepower among 7.62mm rifles in a short body, with limited attachments."),
    "beryl_m762": dict(
        ko="베릴 M762", en="Beryl M762", type="rifle", sort=21, tier="B", base="ak47", like="m416",
        ammo="tacz:762x39", mag=30, ext=(34, 37, 40), rpm=700, modes=("auto", "burst", "semi"), burst=(3, 750, 0.35),
        damage=2.8, ranges=RANGES["ar"], armor=0.25, head=1.5, speed=250, weight=3.6, aim=0.2,
        recoil=(1.25, 1.2), spread=1.05, atts=("scope", "grip", "muzzle", "stock", "extended_mag", "laser"),
        desc_ko="반동이 크지만 화력이 높은 7.62mm 돌격소총. 손잡이 부착물로 반동을 다스려야 제 성능이 나옵니다.",
        desc_en="Hard-kicking, hard-hitting 7.62mm assault rifle. Needs a foregrip to tame its recoil."),
    "ace32": dict(
        ko="ACE32", en="ACE32", type="rifle", sort=22, tier="B", base="ak47", like="m416",
        ammo="tacz:762x39", mag=30, ext=(34, 37, 40), rpm=680, modes=("auto", "semi"),
        damage=2.65, ranges=RANGES["ar"], armor=0.25, head=1.5, speed=250, weight=3.7, aim=0.19,
        recoil=(0.85, 0.8), spread=0.95, atts=("scope", "grip", "muzzle", "stock", "extended_mag", "laser"),
        desc_ko="베릴보다 다루기 쉬운 저반동 7.62mm 돌격소총. 상부 레일이 핸드가드까지 이어집니다.",
        desc_en="A controllable 7.62mm assault rifle with a full-length top rail."),
    "famas": dict(
        ko="FAMAS", en="FAMAS", type="rifle", sort=23, tier="B", base="aug", like="m416",
        ammo="tacz:556x45", mag=25, ext=(30, 30, 35), rpm=900, modes=("auto", "burst", "semi"), burst=(3, 1000, 0.3),
        damage=1.95, ranges=RANGES["ar"], armor=0.2, head=1.5, speed=300, weight=3.6, aim=0.18,
        recoil=(1.1, 1.15), spread=1.0, atts=("scope", "muzzle", "extended_mag", "laser"),
        desc_ko="5.56mm 돌격소총 중 가장 빠른 900 RPM 불펍 소총. 탄창이 25발로 작아 탄 관리가 중요합니다.",
        desc_en="The fastest-firing 5.56mm rifle at 900 RPM. Its 25-round magazine runs dry quickly."),
    "k2": dict(
        ko="K2", en="K2", type="rifle", sort=24, tier="C", base="m16a4", like="ak47",
        ammo="tacz:556x45", mag=30, ext=(40, 40, 40), rpm=700, modes=("auto", "burst", "semi"), burst=(3, 800, 0.34),
        damage=2.2, ranges=RANGES["ar"], armor=0.22, head=1.5, speed=295, weight=3.3, aim=0.19,
        recoil=(1.0, 1.0), spread=1.0, atts=("scope", "muzzle", "stock", "extended_mag"),
        desc_ko="단발·3점사·연사를 모두 쓰는 무난한 5.56mm 제식소총. 접이식 개머리판과 오른쪽 장전 손잡이가 특징입니다.",
        desc_en="A dependable 5.56mm service rifle with semi, burst and auto fire."),
    "mk47_mutant": dict(
        ko="Mk47 뮤턴트", en="Mk47 Mutant", type="rifle", sort=25, tier="B", base="m16a4", like="m416",
        ammo="tacz:762x39", mag=20, ext=(25, 28, 30), rpm=650, modes=("burst", "semi"), burst=(2, 650, 0.28),
        damage=3.1, ranges=(40, 80), armor=0.3, head=1.6, speed=300, weight=3.6, aim=0.19,
        recoil=(1.9, 1.4), spread=0.9, atts=("scope", "grip", "muzzle", "stock", "extended_mag", "laser"),
        desc_ko="자동사격 없이 2점사와 단발로만 쏘는 7.62mm 소총. 탄속이 빨라 중장거리에 강합니다.",
        desc_en="A 7.62mm rifle limited to two-round burst and semi-auto, strong at mid to long range."),
    "mini14": dict(
        ko="미니14", en="Mini14", type="rifle", sort=26, tier="C", base="sks_tactical", like="sks_tactical",
        ammo="tacz:556x45", mag=20, ext=(25, 28, 30), rpm=550, modes=("semi",),
        damage=2.7, ranges=RANGES["dmr"], armor=0.25, head=1.8, speed=480, weight=3.0, aim=0.18,
        recoil=(0.6, 0.6), spread=0.9, atts=("scope", "muzzle", "extended_mag"),
        desc_ko="반동이 작고 탄속이 가장 빠른 경량 5.56mm 지정사수소총. 한 발은 약하지만 빠르게 이어 쏠 수 있습니다.",
        desc_en="A light 5.56mm DMR with the fastest bullet and very low recoil."),
    "mk12": dict(
        ko="Mk12", en="Mk12", type="rifle", sort=27, tier="B", base="spr15hb", like="spr15hb",
        ammo="tacz:556x45", mag=20, ext=(25, 28, 30), rpm=500, modes=("semi",),
        damage=3.0, ranges=RANGES["dmr"], armor=0.3, head=1.85, speed=460, weight=4.0, aim=0.2,
        recoil=(0.9, 0.9), spread=0.85, crawl=0.25, atts=("scope", "grip", "muzzle", "extended_mag"),
        desc_ko="양각대를 갖춘 5.56mm 정밀 지정사수소총. 엎드리면 반동이 크게 줄어듭니다.",
        desc_en="A precise 5.56mm DMR with a bipod. Recoil drops sharply when prone."),
    "vss": dict(
        ko="VSS", en="VSS", type="rifle", sort=28, tier="C", base="ak47", like="sks_tactical",
        ammo="tacz:9mm", mag=10, ext=(15, 20, 20), rpm=700, modes=("auto", "semi"),
        damage=2.4, ranges=(26, 52), armor=0.2, head=1.8, speed=110, gravity=0.3, weight=2.6, aim=0.2,
        recoil=(0.5, 0.5), spread=0.8, atts=("scope", "muzzle", "extended_mag"),
        builtin={"scope": "tacz:scope_acog_ta31", "muzzle": "tacz:muzzle_silencer_mirage"}, silenced=True,
        desc_ko="일체형 소음기와 조준경을 단 자동 지정사수소총. 저속 9mm 탄이라 멀수록 탄이 많이 떨어집니다.",
        desc_en="An automatic DMR with an integral suppressor and scope. Its slow 9mm bullet drops heavily at range."),
    "dragunov": dict(
        ko="드라구노프", en="Dragunov", type="rifle", sort=29, tier="B", base="ak47", like="mk14",
        ammo="tacz:308", mag=10, ext=(15, 18, 20), rpm=220, modes=("semi",),
        damage=4.6, ranges=RANGES["dmr"], armor=0.45, head=2.0, speed=350, pierce=2, weight=4.3, aim=0.22,
        recoil=(1.9, 1.3), spread=0.85, atts=("scope", "muzzle", "extended_mag"),
        desc_ko="한 발이 강하지만 느리고 반동이 큰 7.62mm 반자동 지정사수소총.",
        desc_en="A slow, hard-hitting 7.62mm semi-auto DMR with heavy recoil."),
    "tommy_gun": dict(
        ko="토미 건", en="Tommy Gun", type="smg", sort=6, tier="C", base="ump45", like="ump45",
        ammo="tacz:45acp", mag=30, ext=(40, 50, 50), rpm=750, modes=("auto", "semi"),
        damage=2.4, ranges=RANGES["smg"], armor=0.15, head=1.3, speed=185, weight=4.7, aim=0.2,
        recoil=(1.1, 0.9), spread=1.0, atts=("scope", "grip", "muzzle", "extended_mag"),
        desc_ko="무거운 .45 ACP 탄과 큰 확장 탄창을 쓰는 기관단총. 무겁고 조준이 느린 대신 오래 쏠 수 있습니다.",
        desc_en="A heavy .45 ACP SMG with a big extended magazine. Slow to aim but sustains fire."),
    "mp9": dict(
        ko="MP9", en="MP9", type="smg", sort=7, tier="C", base="micro_uzi", like="mp5k",
        ammo="tacz:9mm", mag=25, ext=(30, 35, 40), rpm=1100, modes=("auto", "semi"),
        damage=1.65, ranges=(18, 38), armor=0.15, head=1.25, speed=185, weight=1.4, aim=0.1,
        recoil=(0.8, 1.0), spread=1.0, atts=("scope", "muzzle", "extended_mag", "laser"),
        desc_ko="9mm 중 가장 빠른 1100 RPM 초소형 기관단총. 가벼워 기동성이 가장 좋지만 사거리가 짧습니다.",
        desc_en="The fastest 9mm SMG at 1100 RPM. Extremely light and mobile, but short-ranged."),
    "js9": dict(
        ko="JS9", en="JS9", type="smg", sort=8, tier="C", base="aug", like="mp5k",
        ammo="tacz:9mm", mag=30, ext=(35, 40, 45), rpm=880, modes=("auto", "semi"),
        damage=1.8, ranges=RANGES["smg"], armor=0.2, head=1.3, speed=200, weight=2.9, aim=0.13,
        recoil=(0.45, 0.45), spread=0.85, atts=("scope", "muzzle", "extended_mag", "laser"),
        desc_ko="반동이 매우 작은 불펍형 9mm 기관단총. 손잡이와 개머리판 슬롯은 없습니다.",
        desc_en="A very stable 9mm bullpup SMG without grip or stock slots."),
    "win94": dict(
        ko="Win94", en="Win94", type="sniper", sort=7, tier="C", base="kar98", like="m700",
        ammo="tacz:45_70", mag=8, ext=None, rpm=100, modes=("semi",),
        damage=6.3, ranges=(48, 95), armor=0.35, head=2.0, speed=240, weight=3.1, aim=0.18,
        recoil=(0.8, 0.9), spread=1.0, atts=("scope",), builtin={"scope": "tacz:scope_retro_2x"},
        desc_ko="8발 관형 탄창을 쓰는 레버액션 소총. 일체형 저배율 조준경이 달려 있고 부착물은 달 수 없습니다.",
        desc_en="A lever-action rifle with an 8-round tube magazine and a fixed low-power scope."),
    "m24": dict(
        ko="M24", en="M24", type="sniper", sort=8, tier="B", base="m700", like="kar98",
        ammo="tacz:308", mag=5, ext=(6, 7, 8), rpm=175, modes=("semi",),
        damage=9.0, ranges=RANGES["sr"], armor=0.55, head=2.0, speed=430, pierce=3, weight=5.4, aim=0.22,
        recoil=(1.1, 1.0), spread=0.9, atts=("scope", "muzzle", "extended_mag"),
        desc_ko="Kar98k보다 탄속이 빠르고 AWM보다는 약한 정통 7.62mm 볼트액션 저격소총.",
        desc_en="A classic 7.62mm bolt-action sniper rifle: faster bullet than the Kar98k, weaker than the AWM."),
    "s12k": dict(
        ko="S12K", en="S12K", type="shotgun", sort=4, tier="B", base="ak47", like="m870",
        ammo="tacz:12g", mag=5, ext=(8, 8, 10), rpm=240, modes=("semi",),
        damage=7.2, pellets=9, ranges=RANGES["sg"], armor=0.25, head=1.33, speed=150, weight=3.6, aim=0.16,
        recoil_like="m1014", spread_like="m1014", bullet_like="m1014", atts=("scope", "muzzle", "stock", "extended_mag"),
        desc_ko="상자 탄창을 쓰는 반자동 산탄총. 탄창째 갈아 끼워 재장전이 빠르고 조준경을 달 수 있습니다.",
        desc_en="A magazine-fed semi-auto shotgun with fast reloads and a scope rail."),
    "dbs": dict(
        ko="DBS", en="DBS", type="shotgun", sort=5, tier="A", base="m870", like="m1014",
        ammo="tacz:12g", mag=13, ext=None, rpm=300, modes=("semi",),
        damage=8.2, pellets=9, ranges=(13, 26), armor=0.3, head=1.33, speed=160, weight=4.2, aim=0.17,
        recoil=(1.0, 1.0), spread=0.95, atts=("scope",), script="tacz:dbs_gun_logic",
        desc_ko="총열이 두 개인 불펍 펌프 산탄총. 두 발을 연달아 쏜 뒤 펌프질을 하며, 약실 포함 14발을 담습니다.",
        desc_en="A twin-barrel bullpup pump shotgun. Fires two quick shots per pump and holds 14 shells."),
    "o12": dict(
        ko="O12", en="O12", type="shotgun", sort=6, tier="A", base="aa12", like="aa12",
        ammo="tacz:12g", mag=5, ext=(10, 15, 20), rpm=300, modes=("semi",),
        damage=5.6, pellets=1, ranges=(40, 80), armor=0.3, head=1.5, speed=220, pierce=2, weight=4.2, aim=0.17,
        recoil=(4.0, 2.5), spread=0.5, aim_spread=0.25, atts=("scope", "grip", "muzzle", "extended_mag", "laser"),
        desc_ko="산탄 대신 슬러그 단일탄을 쏘는 반자동 산탄총. 산탄총 중 유효 사거리가 가장 깁니다.",
        desc_en="A semi-auto shotgun firing single slugs, with the longest reach of any shotgun."),
    "mg3": dict(
        ko="MG3", en="MG3", type="mg", sort=5, tier="S", base="m249", like="fn_evolys",
        ammo="tacz:308", mag=75, ext=None, rpm=660, modes=("auto", "burst"), burst=(3, 990, 0.18, True),
        damage=3.3, ranges=RANGES["mg"], armor=0.4, head=1.5, speed=290, pierce=2, weight=10.5, aim=0.32,
        recoil=(1.2, 1.1), spread=1.0, crawl=0.3, atts=("scope",),
        desc_ko="660 RPM 저속과 990 RPM 고속을 발사 모드로 고르는 7.62mm 경기관총. 고속은 점사 모드 자리에 있습니다.",
        desc_en="A 7.62mm LMG with 660 and 990 RPM settings. The fast setting sits in the burst fire-mode slot."),
    "rpd": dict(
        ko="RPD", en="RPD", type="mg", sort=6, tier="A", base="m249", like="fn_evolys",
        ammo="tacz:762x39", mag=100, ext=None, rpm=650, modes=("auto",),
        damage=2.9, ranges=RANGES["mg"], armor=0.3, head=1.5, speed=260, pierce=2, weight=7.4, aim=0.28,
        recoil=(0.95, 0.9), spread=1.0, crawl=0.3, atts=("scope",),
        desc_ko="100발 드럼을 쓰는 7.62mm 경기관총. 연사는 느리지만 안정적이고 오래 쏠 수 있습니다.",
        desc_en="A 7.62mm LMG fed from a 100-round drum. Slow but steady and long-lasting."),
    "skorpion": dict(
        ko="스콜피온", en="Skorpion", type="pistol", sort=15, tier="D", base="mp5k", like="b93r",
        ammo="tacz:9mm", mag=20, ext=(25, 30, 35), rpm=850, modes=("auto", "semi"),
        damage=1.5, ranges=RANGES["pistol"], armor=0.1, head=1.3, speed=170, weight=1.3, aim=0.1,
        recoil=(0.9, 1.0), spread=1.15, atts=("scope", "muzzle", "stock", "extended_mag", "laser"),
        desc_ko="권총 슬롯에서 자동사격을 하는 9mm 기관권총. 권총 중 부착물을 가장 많이 달 수 있습니다.",
        desc_en="A 9mm machine pistol with full-auto fire and the most attachment slots of any sidearm."),
    "r1895": dict(
        ko="R1895", en="R1895", type="pistol", sort=16, tier="E", base="rhino357", like="rhino357",
        ammo="tacz:357mag", mag=7, ext=None, rpm=150, modes=("semi",),
        damage=3.9, ranges=(22, 45), armor=0.35, head=1.75, speed=190, weight=0.9, aim=0.14,
        recoil=(1.1, 1.0), spread=0.9, atts=("muzzle",),
        desc_ko="7발 실린더를 쓰는 구식 리볼버. 한 발이 강하고, 리볼버로는 드물게 소음기를 달 수 있습니다.",
        desc_en="An old seven-shot revolver. Hits hard and, unusually for a revolver, accepts a suppressor."),
}
