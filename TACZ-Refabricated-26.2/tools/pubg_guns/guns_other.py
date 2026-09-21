"""기관단총 3종, 산탄총 3종, 경기관총 2종, 권총 2종의 외형."""

from guns_ar import MAG_LEVELS, RIGHT, ak_barrel, ak_receiver
from parts import (ar_stock_adapter, bipod, bottom_rail, box_mag, chamfer, curved_mag, drum, front_sight, grip,
                   muzzle_brake, pins, poly_stock, rail, rear_sight, side_rail_stub, side_tube, slats, trigger_group,
                   tube, wedge, wood_stock)


def tommy_gun(m):
    """ump45 베이스(총열 축 y=10.5). 각진 총몸, 냉각 핀 총열, 가로 목재 앞손잡이, 목재 개머리판."""
    body, bolt = "ump45_default", "ump45_bolt"
    chamfer(m, body, 1.9, 9.2, 2.6, -4.0, 14.0, "metal", c=0.25, style="panel")
    chamfer(m, body, 1.6, 8.0, 1.4, 1.6, 8.6, "metal", c=0.25)
    m.cbox(body, 1.5, 8.5, 0.9, -2.2, 3.9, "metal")                               # 탄창 삽입구
    m.add(body, RIGHT * 0.98 - 0.02, 10.5, -2.6, 0.06, 0.45, 6.4, "void", detail=True)   # 장전 손잡이 홈
    m.add(bolt, RIGHT * 1.0 - 0.7, 10.4, 0.0, 0.7, 0.6, 0.7, "steel")
    m.cbox(bolt, 1.0, 10.2, 0.9, -1.6, 3.2, "steel")
    pins(m, body, 0.96, [(9.7, -3.2), (9.6, 5.0), (9.8, 9.2), (8.6, 8.8)])
    tube(m, body, 10.5, -15.0, -4.0, 0.42, "metal_dark")
    fin_z = -9.4
    while fin_z < -4.4:
        tube(m, body, 10.5, fin_z, fin_z + 0.3, 0.64, "metal_dark", detail=True)
        fin_z += 0.62
    sight_top = front_sight(m, body, -14.4, 10.9, height=2.0, hood=False)
    m.cbox(body, 1.0, 10.1, 0.8, -14.9, 1.2, "metal")
    chamfer(m, body, 1.8, 8.2, 1.5, -10.6, 6.4, "wood", c=0.5)                    # 가로 앞손잡이
    m.cbox(body, 0.8, 9.6, 0.5, -10.0, 5.6, "metal", detail=True)
    rail_top = rail(m, body, 11.8, 0.2, 7.6, w=1.2)
    m.cbox(body, 1.2, 11.8, 1.3, 8.4, 0.4, "metal_dark")                           # 가늠자
    m.cbox(body, 0.4, 12.5, 0.5, 8.38, 0.44, "void", detail=True)
    trigger_group(m, body, 2.2, 8.0, length=3.2, drop=2.1)
    grip(m, body, 5.6, 8.2, height=4.8, depth=2.4, angle=20, material="wood", style="")
    wood_stock(m, body, 10.0, 22.0, 10.6, 2.6, 4.8, 1.8, w=1.7, material="wood", butt="steel")
    m.cbox(body, 1.8, 8.6, 2.6, 9.6, 0.8, "metal", detail=True)
    box_mag(m, "mag_standard", -1.7, 9.0, 2.7, 7.6, w=1.2, material="metal_dark", style="")
    box_mag(m, "mag_extended_1", -1.7, 9.0, 2.7, 9.4, w=1.2, material="metal_dark", style="")
    for level in ("mag_extended_2", "mag_extended_3"):
        m.cbox(level, 1.2, 7.4, 1.8, -1.7, 2.7, "metal_dark")
        drum(m, level, 5.2, -0.35, 3.0, 2.0)
    m.meta = {"muzzle_z": -15.0, "flash_z": -15.4, "bore_y": 10.5, "scope": (rail_top, 4.0), "iron_y": sight_top - 0.2,
              "grip": (8.2, -7.4), "shell": (-0.6, 10.8, 0.6)}


def mp9(m):
    """micro_uzi 베이스(총열 축 y=8.4). 손잡이 속 탄창, 일체형 앞손잡이, 오른쪽으로 접은 개머리판."""
    body, bolt = "rec", "bolt"
    chamfer(m, body, 1.9, 6.6, 3.2, -6.4, 14.6, "poly", c=0.4, style="panel")
    chamfer(m, body, 1.6, 7.2, 2.2, -8.0, 1.8, "poly", c=0.4)
    tube(m, body, 8.4, -10.2, -8.0, 0.42, "metal_dark")
    m.add(body, RIGHT * 0.98 - 0.02, 8.6, 0.2, 0.06, 0.7, 2.8, "void", detail=True)
    m.cbox(bolt, 1.0, 8.5, 0.8, 0.4, 2.6, "steel")
    rail_top = rail(m, body, 9.8, -6.0, 7.6, w=1.3)
    m.cbox("pull", 1.4, 9.2, 0.6, 8.0, 0.9, "poly_gray")
    for z in (-5.6, 6.4):
        m.cbox(body, 0.9, rail_top, 0.8, z, 0.5, "poly")
    # 접어 세운 앞손잡이와 큰 방아쇠울
    m.cbox(body, 1.4, 3.0, 3.8, -5.4, 1.8, "poly", style="checker", rot=(-8, 0, 0), pivot=(0, 6.6, -4.5))
    m.cbox(body, 0.5, 4.2, 0.28, -3.6, 4.6, "poly")
    m.cbox(body, 0.32, 4.6, 1.9, -0.6, 0.34, "steel", rot=(-14, 0, 0), pivot=(0, 6.6, -0.6), detail=True)
    grip(m, body, 1.0, 6.8, height=5.0, depth=3.0, w=1.7, angle=8, material="poly")
    # 오른쪽 옆면을 따라 접힌 개머리판
    m.add(body, RIGHT * 1.0 - 0.3, 8.0, -5.4, 0.3, 0.9, 12.8, "poly_gray")
    m.add(body, RIGHT * 1.0 - 0.45, 6.4, -6.0, 0.45, 3.2, 0.7, "poly_gray")
    m.cbox(body, 1.4, 7.0, 2.2, 8.0, 0.8, "poly", detail=True)
    pins(m, body, 0.96, [(7.4, -2.0), (7.4, 5.4), (8.8, 6.6)])
    for level, extra in zip(MAG_LEVELS, (0.0, 0.8, 1.6, 2.4)):
        box_mag(m, level, 1.7, 7.4, 1.9, 7.8 + extra, w=1.1, angle=8, material="poly_gray", style="")
    laser_x = side_rail_stub(m, body, 0.95, 8.0, -4.4, length=2.4)
    m.meta = {"muzzle_z": -10.2, "flash_z": -10.6, "bore_y": 8.4, "scope": (rail_top, 3.0), "iron_y": rail_top + 0.9,
              "laser": (laser_x, 8.0, -4.4), "shell": (-0.6, 8.9, 1.6)}


def js9(m):
    """aug 베이스(총열 축 y=8.9). 불펍 기관단총. 긴 상부 레일과 일체형 핸드스톱."""
    body, bolt = "aug_default", "aug_bolt"
    chamfer(m, body, 2.2, 5.6, 4.4, -8.0, 26.4, "poly_gray", c=0.55, style="panel")
    m.cbox(body, 2.3, 5.2, 5.0, 18.4, 0.8, "rubber", style="hribs")
    for z in (3.6, 5.2, 6.8):
        m.pair(body, 1.1, 0.08, 6.6, 2.4, z, 0.9, "poly", detail=True)             # 옆면 미끄럼 방지 홈
    m.add(body, RIGHT * 1.12 - 0.02, 8.3, 8.2, 0.06, 0.8, 3.0, "void", detail=True)
    m.cbox(bolt, 1.0, 8.3, 0.8, 8.4, 2.6, "steel")
    m.cbox("aug_charge", 0.5, 9.4, 0.5, -3.0, 1.4, "steel")
    rail_top = rail(m, body, 10.0, -7.4, 14.0, w=1.4)
    for z in (-6.8, 12.6):
        m.cbox(body, 0.9, rail_top, 0.9, z, 0.5, "poly")
    chamfer(m, body, 1.7, 7.6, 2.4, -10.2, 2.2, "poly_gray", c=0.45)
    tube(m, body, 8.9, -12.6, -10.2, 0.5, "metal_dark")
    m.cbox(body, 0.5, 8.65, 0.5, -12.66, 0.1, "void", detail=True)
    m.cbox(body, 1.6, 3.0, 2.8, -6.3, 1.9, "poly_gray", style="checker", rot=(-10, 0, 0), pivot=(0, 5.6, -5.4))
    m.cbox(body, 0.5, 2.2, 0.3, -4.4, 3.6, "poly_gray")
    m.cbox(body, 0.32, 3.4, 2.2, -2.4, 0.34, "steel", rot=(-14, 0, 0), pivot=(0, 5.6, -2.4), detail=True)
    grip(m, body, -1.0, 5.8, height=4.6, depth=2.3, angle=14, material="poly_gray")
    pins(m, body, 1.12, [(7.0, -5.0), (7.0, 1.6), (7.2, 15.6)])
    for level, extra in zip(MAG_LEVELS, (0.0, 0.8, 1.6, 2.4)):
        box_mag(m, level, 8.5, 5.9, 2.3, 5.0 + extra, w=1.1, angle=-5, material="metal_dark", style="")
    laser_x = side_rail_stub(m, body, 1.1, 8.2, -5.0)
    m.meta = {"muzzle_z": -12.6, "flash_z": -13.0, "bore_y": 8.9, "scope": (rail_top, 3.5), "iron_y": rail_top + 1.0,
              "laser": (laser_x, 8.2, -5.0), "shell": (-0.7, 8.7, 9.6)}


def skorpion(m):
    """mp5k 베이스(총열 축 y=10.9). 작은 총몸, 위로 접힌 철사 개머리판, 앞으로 휜 탄창, 목재 손잡이."""
    body, bolt = "gun", "bolt"
    chamfer(m, body, 1.8, 9.8, 2.2, -4.0, 11.0, "metal", c=0.35, style="panel")
    chamfer(m, body, 1.6, 8.8, 1.2, -2.2, 9.8, "metal", c=0.25)
    tube(m, body, 10.9, -7.6, -4.0, 0.4, "metal_dark")
    m.pair(body, 0.3, 0.2, 11.6, 1.5, -4.4, 0.7, "metal_dark")                     # 가늠쇠 보호 귀
    m.cbox(body, 0.16, 11.8, 1.1, -4.2, 0.2, "metal_dark", detail=True)
    m.add(body, RIGHT * 0.92 - 0.02, 10.9, -0.6, 0.06, 0.6, 2.4, "void", detail=True)
    m.pair(bolt, 0.9, 0.4, 11.0, 0.5, 1.0, 0.6, "steel")                           # 양쪽 장전 손잡이
    m.cbox(bolt, 1.0, 10.6, 0.8, -0.4, 2.2, "steel")
    rail_top = rail(m, body, 12.0, -1.0, 5.0, w=1.0)
    rear_sight(m, body, 5.6, 12.0, height=0.7, length=1.0)
    # 총몸 위로 접어 올린 철사 개머리판
    stock = "stock_default"
    m.pair(stock, 0.78, 0.2, 12.1, 0.2, -5.4, 12.0, "steel")
    m.cbox(stock, 1.96, 11.2, 1.6, -5.8, 0.4, "steel")
    m.pair(stock, 0.78, 0.2, 10.6, 1.6, 6.5, 0.3, "steel", detail=True)
    trigger_group(m, body, 1.4, 8.8, length=2.8, drop=2.0)
    grip(m, body, 4.2, 9.0, height=4.6, depth=2.2, angle=14, material="wood", style="checker")
    pins(m, body, 0.92, [(10.4, -3.0), (9.4, 3.4), (10.4, 6.2)])
    for level, extra in zip(MAG_LEVELS, (0.0, 1.0, 2.0, 3.0)):
        curved_mag(m, level, -1.6, 9.0, 2.4, 5.0 + extra, 14 + extra * 2, w=1.1, segments=4, material="metal_dark", style="")
    laser_x = side_rail_stub(m, body, 0.9, 10.6, -1.8, length=2.4)
    m.meta = {"muzzle_z": -7.6, "flash_z": -8.0, "bore_y": 10.9, "scope": (rail_top, 2.0), "iron_y": 12.9,
              "laser": (laser_x, 10.6, -1.8), "stock": ar_stock_adapter(m, 10.6, 7.0, 8.2, block_height=2.0),
              "shell": (-0.6, 11.3, 0.6)}


def s12k(m):
    """ak47 베이스. 굵은 산탄 총열과 크고 곧은 상자 탄창."""
    body, bolt = "akm_default", "bolt"
    rail_top = ak_receiver(m, body, bolt, cover_material="metal_dark", rail_on_cover=True)
    chamfer(m, body, 2.2, 6.9, 3.4, -11.0, 8.6, "poly", c=0.5, style="vents")
    sight_top = ak_barrel(m, body, -19.3, gas_z=-12.4, sight_z=-17.8, r=0.58)
    tube(m, "muzzle_default", 8.6, -20.4, -19.3, 0.68, "metal")
    m.cbox("muzzle_default", 0.8, 8.2, 0.8, -20.46, 0.1, "void", detail=True)
    rear_sight(m, body, -4.3, 10.6, height=0.5, length=1.4)
    trigger_group(m, body, 3.0, 6.9, length=3.6, drop=2.2)
    grip(m, body, 6.5, 7.0, height=5.0, depth=2.3, angle=18, material="poly")
    poly_stock(m, "stock_default", 9.9, 21.0, 9.2, 4.8, w=1.6, cutout=True)
    for level, extra in zip(MAG_LEVELS, (0.0, 2.0, 2.0, 3.4)):
        box_mag(m, level, -1.3, 7.2, 4.0, 4.6 + extra, w=1.7, angle=-8, material="poly", style="hribs")
    m.meta = {"muzzle_z": -19.3, "flash_z": -20.6, "bore_y": 8.6, "scope": (rail_top, 3.0), "iron_y": sight_top - 0.15,
              "stock": ar_stock_adapter(m, 8.7, 9.9, 13.4), "shell": (-0.6, 9.3, 0.4)}


def dbs(m):
    """m870 베이스(총열 축 y=9.1). 불펍 몸통, 나란한 두 총열과 두 관형 탄창, 펌프 손잡이."""
    body, pump = "body", "slide3"
    chamfer(m, body, 2.5, 6.2, 4.4, -6.0, 27.0, "poly", c=0.6, style="panel")
    m.cbox(body, 2.6, 5.8, 5.2, 21.0, 0.9, "rubber", style="hribs")
    chamfer(m, body, 1.7, 9.8, 0.9, -17.0, 11.2, "metal", c=0.3, style="vents")
    rail_top = rail(m, body, 10.6, -16.4, 14.0, w=1.4)
    for x in (0.52, -0.52):
        side_tube(m, body, x, 9.1, -21.0, -6.0, 0.44, "metal_dark", detail=False)
        side_tube(m, body, x, 7.85, -20.2, -6.0, 0.4, "metal", detail=False)
        m.add(body, x - 0.22, 8.88, -21.06, 0.44, 0.44, 0.1, "void", detail=True)
    m.cbox(body, 2.1, 7.3, 2.5, -21.0, 0.7, "metal")
    m.cbox(body, 2.1, 7.3, 2.5, -17.4, 0.6, "metal", detail=True)
    chamfer(m, pump, 2.4, 6.0, 2.7, -14.6, 7.2, "poly", c=0.5, style="vribs")
    m.cbox(pump, 2.0, 5.0, 1.4, -15.2, 1.0, "poly")                                # 손 멈춤 턱
    trigger_group(m, body, 2.6, 6.2, length=3.6, drop=2.2)
    grip(m, body, 6.2, 6.4, height=4.8, depth=2.4, angle=16, material="poly")
    m.cbox(body, 1.9, 6.1, 0.2, 10.0, 6.0, "void", detail=True)                    # 밑면 장전·배출구
    m.add(body, RIGHT * 1.25 - 0.06, 7.6, 12.0, 0.08, 1.4, 4.0, "poly_gray", style="hribs", detail=True)
    m.add(body, 1.23, 7.6, 12.0, 0.08, 1.4, 4.0, "poly_gray", style="hribs", detail=True)
    pins(m, body, 1.26, [(7.2, -3.0), (7.2, 9.0), (9.4, 17.0)])
    sight = "sight"
    for z in (-15.6, 12.4):
        m.cbox(sight, 1.0, rail_top, 0.4, z, 1.2, "poly")
        m.cbox(sight, 0.7, rail_top + 0.4, 0.9, z + 0.4, 0.3, "poly")
    m.meta = {"muzzle_z": -21.0, "flash_z": -21.4, "bore_y": 9.1, "scope": (rail_top, 2.0), "iron_y": rail_top + 1.1}


def o12(m):
    """aa12 베이스(총열 축 y=10.8). 키 큰 총몸, 굵은 곧은 탄창, 큰 제퇴기. 3단계 확장은 드럼."""
    body, bolt = "body", "bolt"
    chamfer(m, body, 2.2, 10.0, 4.0, -12.0, 21.0, "metal", c=0.4, style="panel")
    chamfer(m, body, 2.0, 8.0, 2.2, -6.0, 15.0, "metal", c=0.35)
    m.cbox(body, 2.0, 7.2, 3.0, -5.5, 5.0, "metal", style="panel")
    m.add(body, RIGHT * 1.12 - 0.02, 10.9, -3.0, 0.06, 1.2, 4.6, "void", detail=True)
    m.cbox(bolt, 1.4, 10.9, 1.1, -2.6, 3.8, "steel")
    m.add("charge_handle", 1.1, 11.9, -8.4, 0.7, 0.5, 0.9, "steel")
    rail_top = rail(m, body, 14.0, -18.6, 8.6, w=1.5)
    chamfer(m, body, 2.0, 9.4, 4.6, -19.0, 7.0, "metal_dark", c=0.6, style="vents")
    grip_y = bottom_rail(m, body, 9.4, -18.4, -13.0, w=1.3)
    laser_x = side_rail_stub(m, body, 1.0, 11.4, -16.0)
    tube(m, body, 10.8, -22.2, -19.0, 0.62, "metal_dark")
    # aa12 베이스에는 muzzle_default 뼈가 없다. 새로 만들어야 총구 부착물을 달 때 기본 제퇴기가 숨는다
    m.add_bone("muzzle_default", body)
    muzzle_brake(m, "muzzle_default", 10.8, -25.2, 3.0, 0.82, slots=3)
    trigger_group(m, body, -0.4, 8.0, length=3.4, drop=2.2)
    grip(m, body, 3.2, 8.0, height=5.0, depth=2.4, angle=17, material="poly")
    tube(m, body, 11.4, 9.0, 12.0, 0.6, "metal_dark")
    poly_stock(m, body, 10.4, 18.4, 12.6, 5.6, w=1.6, cutout=True)
    pins(m, body, 1.12, [(9.0, -4.4), (9.0, 6.8), (11.6, 7.6)])
    for level, extra in zip(MAG_LEVELS[:3], (0.0, 2.2, 4.2)):
        box_mag(m, level, -5.0, 7.4, 4.0, 3.2 + extra, w=1.8, material="poly", style="hribs")
    m.cbox("mag_extended_3", 1.8, 5.0, 2.4, -5.0, 4.0, "poly")
    drum(m, "mag_extended_3", 3.0, -3.0, 3.4, 2.6, material="poly")
    m.meta = {"muzzle_z": -22.2, "flash_z": -25.4, "bore_y": 10.8, "scope": (rail_top, 3.0), "iron_y": rail_top + 0.4,
              "grip": (grip_y, -15.6), "laser": (laser_x, 11.4, -16.0), "shell": (-0.7, 11.4, -1.0)}


def lmg_common(m, body, stock_material):
    grip_material = "wood" if stock_material == "wood" else "poly"
    trigger_group(m, body, 2.6, 6.5, length=3.4, drop=2.2)
    grip(m, body, 6.0, 6.6, height=5.6, depth=2.4, angle=16, material=grip_material,
         style="" if stock_material == "wood" else "checker")


def mg3(m):
    """m249 베이스(총열 축 y=8.4). 구멍 난 긴 총열 덮개, 나팔형 총구, 물고기 꼬리 개머리판, 펼친 양각대."""
    body = "body"
    chamfer(m, body, 2.1, 6.5, 3.2, -3.0, 14.0, "metal", c=0.35, style="panel")
    slats(m, body, 1.9, 7.3, 2.2, -21.0, -3.0, "metal_dark", gap=1.0, bar=0.6)
    tube(m, body, 8.4, -21.4, -21.0, 1.0, "metal")
    tube(m, body, 8.4, -23.2, -21.4, 0.92, "metal_dark")
    tube(m, body, 8.4, -24.2, -23.2, 0.66, "metal_dark")
    m.cbox(body, 0.6, 8.1, 0.6, -24.26, 0.1, "void", detail=True)
    m.add(body, RIGHT * 1.0 - 0.1, 7.5, -8.4, 0.12, 1.8, 5.2, "metal", style="panel", detail=True)   # 총열 교환 덮개
    chamfer(m, "cap", 1.9, 9.6, 0.9, -0.4, 9.4, "metal_dark", c=0.3)
    rail_top = rail(m, "cap", 10.5, 1.0, 8.4, w=1.3)
    m.cbox(body, 1.2, 9.7, 1.6, 9.6, 1.2, "metal")
    m.cbox(body, 0.3, 10.9, 0.5, 10.0, 0.4, "void", detail=True)
    sight_top = front_sight(m, body, -19.4, 9.5, height=2.0, hood=False)
    m.add("pull_handle", RIGHT * 1.05 - 0.8, 7.6, 2.0, 0.8, 0.6, 0.8, "steel")
    lmg_common(m, body, "poly")
    tube(m, body, 8.2, 11.0, 14.0, 0.75, "metal")
    wedge(m, body, 1.7, 14.0, 23.0, 9.0, 10.2, 7.4, 4.2, "poly")
    m.cbox(body, 1.9, 4.0, 6.4, 23.0, 0.6, "rubber", style="hribs")
    bipod(m, body, -18.6, 7.3, length=7.4, spread=18)
    pins(m, body, 1.06, [(7.4, -1.6), (7.2, 5.0), (8.6, 9.4)])
    # 75발 벨트 드럼과 급탄 통로
    drum(m, "magazine", 3.8, 1.6, 2.9, 2.6, material="olive")
    m.add("magazine", 0.9, 6.0, 0.4, 0.7, 2.6, 2.4, "olive")
    for index in range(5):
        m.add("bullet_chain", 1.0 + index * 0.02, 8.9, 1.0 + index * 0.62, 1.3, 0.34, 0.34, "brass", detail=True)
    m.meta = {"muzzle_z": -24.2, "flash_z": -24.6, "bore_y": 8.4, "scope": (rail_top, 5.0), "iron_y": sight_top - 0.2,
              "shell": (-0.9, 6.8, 2.0)}


def rpd(m):
    """m249 베이스. 목재 개머리판·손잡이·핸드가드, 총열 밑 가스관, 총몸 밑 원형 드럼."""
    body = "body"
    chamfer(m, body, 1.9, 6.4, 3.0, -2.0, 13.0, "metal", c=0.3, style="panel")
    chamfer(m, "cap", 1.7, 9.4, 0.8, 0.0, 8.2, "metal", c=0.3)
    rail_top = rail(m, "cap", 10.2, 0.6, 7.8, w=1.2)
    tube(m, body, 8.4, -22.2, -2.0, 0.45, "metal_dark")
    side_tube(m, body, 0, 7.3, -14.0, -2.0, 0.4, "metal", detail=False)
    m.cbox(body, 1.1, 6.7, 2.4, -15.0, 1.3, "metal")
    chamfer(m, body, 2.0, 6.0, 2.0, -10.6, 8.2, "wood", c=0.5)
    for z in (-10.9, -2.6):
        m.cbox(body, 2.1, 5.9, 2.2, z, 0.4, "steel", detail=True)
    sight_top = front_sight(m, body, -21.0, 8.8, height=2.0, hood=True)
    m.cbox(body, 1.0, 9.4, 0.4, 8.6, 2.4, "metal_dark")
    rear_sight(m, body, 9.4, 9.8, height=0.8, length=1.4)
    tube(m, body, 8.4, -22.8, -22.2, 0.56, "metal")
    m.add("pull_handle", RIGHT * 0.95 - 0.8, 7.4, 2.0, 0.8, 0.5, 0.7, "steel")
    lmg_common(m, body, "wood")
    wood_stock(m, body, 11.0, 23.0, 9.2, 2.6, 5.2, 1.4, w=1.7, material="wood", butt="steel")
    bipod(m, body, -19.0, 8.0, length=7.0, folded=True)
    pins(m, body, 0.96, [(7.2, -1.0), (7.0, 5.2), (8.4, 9.6)])
    drum(m, "magazine", 3.2, 1.6, 3.0, 2.2, material="metal_dark")
    m.add("magazine", -0.7, 6.0, 0.4, 1.4, 0.6, 2.4, "metal_dark")
    for index in range(4):
        m.add("bullet_chain", 0.9, 8.7, 1.2 + index * 0.62, 1.2, 0.32, 0.32, "brass", detail=True)
    m.meta = {"muzzle_z": -22.8, "flash_z": -23.2, "bore_y": 8.4, "scope": (rail_top, 4.6), "iron_y": sight_top - 0.2,
              "shell": (-0.9, 6.6, 2.0)}


def r1895(m):
    """rhino357 베이스. 총열이 실린더 위쪽에 있는 나강 리볼버. 7발 실린더와 격자무늬 목재 손잡이."""
    body = "gun_body"
    axis = m.base_pivot("cy_base")
    cy, cz = axis[1], 2.7
    bore_y = cy + 0.55
    tube(m, body, bore_y, -8.0, 0.6, 0.36, "metal_dark")
    m.cbox(body, 0.9, bore_y - 0.5, 1.0, -0.4, 1.0, "metal")
    m.cbox(body, 0.16, bore_y + 0.35, 0.9, -7.6, 0.5, "metal_dark")               # 가늠쇠
    side_tube(m, body, 0, cy - 0.35, -3.4, 0.6, 0.16, "metal_dark")               # 탄피 밀대
    m.cbox(body, 1.3, cy - 1.7, 3.3, 0.5, 1.2, "metal", style="panel")
    m.cbox(body, 1.3, cy - 1.7, 3.3, 3.7, 2.2, "metal", style="panel")
    m.cbox(body, 1.0, cy + 1.2, 0.4, 0.5, 5.4, "metal")                            # 윗대
    m.cbox(body, 1.0, cy - 1.7, 0.4, 0.5, 5.4, "metal")
    m.cbox(body, 0.3, cy + 1.6, 0.35, 5.2, 0.5, "metal_dark", detail=True)        # 가늠자 홈
    m.add(body, RIGHT * 0.66 - 0.2, cy - 0.7, 3.8, 0.22, 1.5, 1.2, "metal_dark", detail=True)   # 장전문
    tube(m, "cy_base", cy, cz - 0.95, cz + 0.95, 1.06, "metal_dark", style="hribs")
    tube(m, "cy_base", cy, cz + 0.95, cz + 1.1, 0.7, "steel", detail=True)
    m.cbox("hammer", 0.4, cy + 0.5, 1.5, 6.0, 0.6, "metal_dark", rot=(22, 0, 0), pivot=(0, cy + 0.5, 6.2))
    m.cbox("trigger", 0.3, cy - 3.0, 1.5, 3.2, 0.34, "steel", rot=(-14, 0, 0), pivot=(0, cy - 1.6, 3.2))
    m.cbox(body, 0.45, cy - 3.6, 0.28, 1.6, 3.4, "metal")
    m.cbox(body, 0.45, cy - 3.6, 2.0, 1.6, 0.28, "metal")
    grip(m, body, 4.4, cy - 1.5, height=4.4, depth=2.3, w=1.5, angle=24, material="wood", style="checker")
    m.cbox(body, 0.5, cy - 6.6, 0.5, 6.6, 0.5, "steel", detail=True)               # 끈 고리
    pins(m, body, 0.66, [(cy - 1.0, 4.4), (cy - 1.2, 1.0)])
    m.meta = {"muzzle_z": -8.0, "flash_z": -8.4, "bore_y": bore_y, "iron_y": bore_y + 1.2}


GUNS = {
    "tommy_gun": (tommy_gun, "ump45", "ump45_default"),
    "mp9": (mp9, "micro_uzi", "rec"),
    "js9": (js9, "aug", "aug_default"),
    "skorpion": (skorpion, "mp5k", "gun"),
    "s12k": (s12k, "ak47", "akm_default"),
    "dbs": (dbs, "m870", "body"),
    "o12": (o12, "aa12", "body"),
    "mg3": (mg3, "m249", "body"),
    "rpd": (rpd, "m249", "body"),
    "r1895": (r1895, "rhino357", "gun_body"),
}
