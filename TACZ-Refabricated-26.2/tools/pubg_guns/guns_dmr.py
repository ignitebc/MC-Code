"""지정사수소총 4종과 저격소총 2종의 외형."""

from guns_ar import MAG_LEVELS, RIGHT, ak_barrel, ak_receiver
from parts import (bipod, bottom_rail, box_mag, chamfer, curved_mag, front_sight, grip, muzzle_brake, pins, poly_stock,
                   rail, rear_sight, side_tube, skeleton_stock, trigger_group, tube, wedge, wood_stock)


def mini14(m):
    """sks_tactical 베이스(총열 축 y=6.5). 목재 일체형 스톡과 개런드식 노출 노리쇠가 특징."""
    body, bolt = "default_gun", "bolt"
    chamfer(m, body, 2.0, 4.2, 2.4, -12.0, 15.6, "wood", c=0.5)
    chamfer(m, body, 2.0, 4.0, 2.5, 3.6, 5.6, "wood", c=0.45)
    wood_stock(m, body, 9.0, 23.2, 6.4, 2.3, 4.8, 1.5, w=1.8, material="wood")
    chamfer(m, body, 1.6, 6.5, 1.2, -11.0, 9.0, "poly", c=0.4, style="vents")      # 총열 위 덮개
    m.cbox(body, 2.1, 4.4, 2.4, -12.4, 0.5, "steel", detail=True)                  # 앞 밴드
    chamfer(m, body, 1.5, 6.2, 1.7, -2.0, 10.6, "metal", c=0.3)
    m.add(body, RIGHT * 0.78 - 0.02, 7.0, 0.4, 0.06, 0.7, 4.0, "void", detail=True)
    m.cbox(bolt, 1.0, 7.0, 0.75, 0.4, 4.2, "steel")
    m.add(bolt, RIGHT * 0.8 - 0.8, 6.9, 0.8, 0.8, 0.4, 0.6, "steel")
    m.add(body, RIGHT * 1.08 - 0.1, 6.2, -9.4, 0.2, 0.4, 10.0, "steel", detail=True)   # 작동 로드
    tube(m, body, 6.5, -23.6, -2.0, 0.38, "metal_dark")
    m.cbox(body, 1.1, 5.7, 1.7, -13.4, 1.2, "metal")                              # 가스 블록
    sight_top = front_sight(m, body, -23.0, 6.8, height=1.9, hood=True)
    m.pair(body, 0.3, 0.22, 7.9, 1.0, 7.6, 1.0, "metal_dark")                      # 가늠자 보호 귀
    m.cbox(body, 0.5, 7.9, 0.7, 7.9, 0.4, "metal_dark", detail=True)
    trigger_group(m, body, 4.6, 4.1, length=3.6, drop=1.9)
    pins(m, body, 1.0, [(5.2, 4.2), (5.0, 8.0)])
    for z in (-10.6, 21.4):
        m.cbox(body, 0.5, 3.4 if z < 0 else 0.6, 0.7, z, 0.6, "steel", detail=True)  # 멜빵 고리
    mount_top = rail(m, "mount", 7.9, -0.4, 7.0, w=1.3)
    for level, extra in zip(MAG_LEVELS, (0.0, 0.7, 1.1, 1.5)):
        box_mag(m, level, -0.9, 4.4, 2.9, 4.4 + extra, w=1.25, angle=-6, material="metal")
    m.meta = {"muzzle_z": -23.6, "flash_z": -24.0, "bore_y": 6.5, "scope": (mount_top, 3.4), "iron_y": sight_top - 0.2,
              "shell": (-0.5, 7.2, 2.0)}


def mk12(m):
    """spr15hb 베이스(총열 축 y=9.5). 둥근 탄소섬유 핸드가드, 긴 상부 레일, 접힌 양각대, 고정 개머리판."""
    body, bolt = "gun_body", "bolt"
    chamfer(m, body, 1.9, 8.9, 1.6, 0.3, 9.6, "metal", c=0.35)
    chamfer(m, body, 1.8, 7.3, 1.8, 0.5, 9.8, "metal", c=0.3, style="panel")
    m.cbox(body, 1.7, 6.2, 2.8, 0.7, 3.9, "metal", style="panel")
    m.add(body, RIGHT * 0.98 - 0.02, 9.0, 1.4, 0.06, 0.9, 3.8, "void", detail=True)
    m.add(body, RIGHT * 1.0 - 0.3, 8.9, 5.4, 0.3, 0.9, 0.8, "metal", detail=True)
    m.add(body, RIGHT * 1.0 - 0.35, 9.5, 6.8, 0.35, 0.5, 0.9, "metal", detail=True)
    pins(m, body, 0.92, [(7.9, 1.2), (7.9, 8.0), (7.7, 4.9)])
    m.cbox(bolt, 1.2, 9.1, 0.9, 1.5, 3.6, "steel")
    rail_top = rail(m, body, 10.45, -17.4, 9.6)
    tube(m, body, 9.5, -17.6, 0.3, 1.12, "poly_gray", style="checker")             # 탄소섬유 직조 무늬
    for z in (-17.8, 0.0):
        tube(m, body, 9.5, z, z + 0.4, 1.2, "metal", detail=True)
    tube(m, body, 9.5, -23.8, -17.6, 0.46, "metal_dark")
    tube(m, body, 9.5, -21.4, -20.6, 0.62, "metal", detail=True)                   # 소염기 고정 고리
    bipod(m, body, -14.0, 8.4, length=7.6, folded=True)
    grip_y = bottom_rail(m, body, 8.45, -9.0, -3.4, w=1.3)
    sight = "sight"
    for z in (-16.6, 8.0):
        m.cbox(sight, 1.0, rail_top, 0.4, z, 1.2, "metal_dark")
        m.cbox(sight, 0.7, rail_top + 0.4, 0.9, z + 0.4, 0.3, "metal_dark")
    trigger_group(m, body, 4.6, 7.3, length=2.8, drop=2.1)
    grip(m, body, 6.8, 7.5, height=4.8, depth=2.3, angle=18, material="poly")
    stock = "stock_default"
    tube(m, stock, 9.5, 10.1, 12.0, 0.6, "metal")
    poly_stock(m, stock, 11.0, 22.2, 10.3, 4.8, w=1.6, cutout=False)
    muzzle_brake(m, "muzzle_default", 9.5, -26.8, 3.0, 0.56, slots=3)
    for level, extra in zip(MAG_LEVELS, (0.0, 0.7, 1.1, 1.5)):
        curved_mag(m, level, 1.1, 6.6, 3.1, 4.4 + extra, 10, w=1.3, segments=4, material="metal")
    m.meta = {"muzzle_z": -23.8, "flash_z": -27.0, "bore_y": 9.5, "scope": (rail_top, 5.2), "iron_y": rail_top + 1.1,
              "grip": (grip_y, -6.0), "shell": (-0.6, 9.7, 3.0)}


def vss(m):
    """ak47 베이스. 굵은 일체형 소음기와 뚫린 목재 개머리판."""
    body, bolt = "akm_default", "bolt"
    ak_receiver(m, body, bolt, cover_material="metal")
    chamfer(m, body, 1.9, 7.2, 2.6, -7.0, 4.6, "poly", c=0.4, style="vribs")
    # 총열 전체를 감싼 소음기
    tube(m, body, 8.6, -21.0, -6.6, 0.98, "metal_dark", style="hribs")
    tube(m, body, 8.6, -21.5, -21.0, 0.8, "metal_dark")
    m.cbox(body, 0.7, 8.25, 0.7, -21.56, 0.1, "void", detail=True)
    for z in (-7.0, -13.8):
        tube(m, body, 8.6, z, z + 0.5, 1.06, "metal", detail=True)
    sight_top = front_sight(m, body, -19.8, 9.5, height=1.5, hood=True)
    m.cbox(body, 1.0, 9.5, 0.5, -9.4, 2.2, "metal_dark")
    rear_sight(m, body, -9.2, 9.9, height=0.7, length=1.6)
    # 조준경을 얹는 왼쪽 측면 레일 받침
    m.add(body, 0.95, 8.2, 0.0, 0.25, 2.6, 7.0, "metal_dark")
    m.cbox(body, 1.3, 10.6, 0.4, -0.4, 8.2, "metal_dark")
    rail_top = rail(m, body, 10.9, -0.2, 7.6, w=1.3)
    trigger_group(m, body, 3.0, 6.9, length=3.6, drop=2.2)
    grip(m, body, 6.5, 7.0, height=4.6, depth=2.2, angle=20, material="wood", style="")
    skeleton_stock(m, "stock_default", 9.9, 21.0, 9.0, 5.2, w=1.5, material="wood")
    for level, extra in zip(MAG_LEVELS, (0.0, 1.0, 2.0, 2.0)):
        curved_mag(m, level, -0.1, 7.3, 2.9, 3.6 + extra, 12 + extra * 3, w=1.3, segments=4, material="poly")
    # 소음 효과용 일체형 소음기 부착물(길이 7.5)이 굵은 소음기 통 안에 숨도록 부착 위치를 통 안쪽에 둔다
    m.meta = {"muzzle_z": -13.0, "flash_z": -21.8, "bore_y": 8.6, "scope": (rail_top, 3.6), "iron_y": sight_top - 0.2,
              "shell": (-0.6, 9.3, 0.4)}


def dragunov(m):
    """ak47 베이스. 긴 총열, 통풍 구멍이 난 목재 핸드가드, 뚫린 개머리판."""
    body, bolt = "akm_default", "bolt"
    ak_receiver(m, body, bolt, cover_material="metal")
    chamfer(m, body, 1.9, 7.2, 3.1, -11.6, 9.2, "wood_red", c=0.5, style="vents")
    for z in (-12.0, -2.8):
        m.cbox(body, 2.0, 7.1, 3.3, z, 0.5, "steel", detail=True)
    sight_top = ak_barrel(m, body, -27.0, gas_z=-13.0, sight_z=-25.6, r=0.4)
    rear_sight(m, body, -4.3, 10.6, height=0.5, length=1.8)
    muzzle_brake(m, "muzzle_default", 8.6, -30.2, 3.2, 0.5, slots=5)
    m.add("mount", 0.95, 8.2, 0.0, 0.25, 2.6, 7.4, "metal_dark")
    m.cbox("mount", 1.3, 10.6, 0.4, -0.4, 8.6, "metal_dark")
    mount_top = rail(m, "mount", 10.9, -0.2, 8.0, w=1.3)
    trigger_group(m, body, 3.0, 6.9, length=3.6, drop=2.2)
    grip(m, body, 6.5, 7.0, height=4.8, depth=2.3, angle=22, material="wood_red", style="")
    skeleton_stock(m, "stock_default", 9.9, 22.4, 9.0, 5.6, w=1.6, material="wood_red")
    for level, extra in zip(MAG_LEVELS, (0.0, 1.0, 1.6, 2.0)):
        curved_mag(m, level, -0.5, 7.3, 3.4, 4.4 + extra, 10, w=1.4, segments=4, material="metal", style="vribs")
    m.meta = {"muzzle_z": -27.0, "flash_z": -30.4, "bore_y": 8.6, "scope": (mount_top, 4.4), "iron_y": sight_top - 0.15,
              "shell": (-0.6, 9.3, 0.4)}


def m24(m):
    """m700 베이스(총열 축 y=7.2). 올리브색 스톡, 굵은 총열, 조절식 개머리판 받침."""
    body, bolt = "main_body", "rotate"
    chamfer(m, body, 2.2, 4.6, 2.2, -13.0, 17.0, "olive", c=0.55)
    chamfer(m, body, 2.1, 4.3, 2.6, 4.0, 5.4, "olive", c=0.5)
    wedge(m, body, 1.9, 9.4, 12.0, 6.7, 7.0, 4.3, 3.0, "olive")
    m.cbox(body, 1.7, 1.6, 3.2, 9.6, 2.2, "olive", rot=(20, 0, 0), pivot=(0, 4.6, 10.7), style="checker")
    wedge(m, body, 1.9, 12.0, 20.6, 7.0, 7.0, 3.0, 2.4, "olive")
    m.cbox(body, 1.7, 7.2, 0.7, 12.4, 6.4, "olive", detail=True)                   # 볼받침
    # 개머리판 길이 조절부: 금속 봉 두 개와 조절 바퀴
    for y in (3.6, 6.0):
        side_tube(m, body, 0, y, 20.6, 22.0, 0.22, "steel")
    m.cbox(body, 2.2, 4.4, 0.9, 20.9, 0.5, "metal_dark", detail=True)
    m.cbox(body, 2.0, 2.3, 5.4, 22.0, 0.5, "metal_dark")
    m.cbox(body, 2.1, 2.2, 5.6, 22.5, 0.7, "rubber", style="hribs")
    tube(m, body, 7.2, 0.0, 10.6, 0.78, "metal")
    m.add(body, RIGHT * 0.78 - 0.02, 7.0, 3.6, 0.06, 0.7, 3.6, "void", detail=True)
    rail_top = rail(m, body, 7.95, 0.4, 9.6, w=1.3)
    tube(m, body, 7.2, -26.0, 0.0, 0.56, "metal_dark")
    m.cbox(body, 0.5, 6.95, 0.5, -26.06, 0.1, "void", detail=True)
    tube(m, bolt, 7.2, 4.8, 10.9, 0.42, "steel")
    m.add(bolt, RIGHT * 0.5 - 1.5, 6.3, 9.6, 1.5, 0.34, 0.4, "steel", rot=(0, 0, 20), pivot=(RIGHT * 0.5, 6.5, 9.8))
    m.add(bolt, RIGHT * 2.2 - 0.35, 5.5, 9.45, 0.7, 0.7, 0.7, "metal_dark")
    m.cbox(body, 0.6, 7.2, 0.5, 10.6, 0.7, "metal_dark", detail=True)
    trigger_group(m, body, 5.4, 4.4, length=3.4, drop=1.9)
    for z in (-11.8, -9.2, 19.0):
        m.cbox(body, 0.4, 4.1 if z < 0 else 2.1, 0.5, z, 0.4, "steel", detail=True)
    m.cbox("mag_standard", 1.2, 4.1, 0.35, 4.0, 3.9, "metal_dark")
    for level, extra in zip(MAG_LEVELS[1:], (1.0, 1.6, 2.2)):
        box_mag(m, level, 4.2, 4.5, 3.5, 0.6 + extra, w=1.3, material="metal_dark")
    m.meta = {"muzzle_z": -26.0, "flash_z": -26.4, "bore_y": 7.2, "scope": (rail_top, 5.4), "iron_y": rail_top + 0.4,
              "shell": (-0.6, 7.4, 5.4)}


def win94(m):
    """kar98 베이스(총열 축 y=7.6). 레버 고리, 총열 밑 관형 탄창, 호두나무 스톡."""
    body, bolt = "Gunbody", "bolt"
    chamfer(m, body, 1.5, 5.6, 2.9, 1.0, 8.4, "metal", c=0.25, style="panel")
    m.add(body, RIGHT * 0.78 - 0.02, 6.4, 3.2, 0.06, 0.9, 2.4, "metal_dark", detail=True)   # 장전구 덮개
    pins(m, body, 0.76, [(6.2, 1.8), (7.8, 2.4), (6.0, 7.6)])
    tube(m, body, 7.6, -24.0, 1.0, 0.36, "metal_dark")
    side_tube(m, body, 0, 6.72, -22.6, 1.0, 0.33, "metal", detail=False)
    for z in (-21.4, -9.6):
        m.cbox(body, 1.0, 6.2, 1.9, z, 0.6, "metal", detail=True)
    chamfer(m, body, 1.6, 5.9, 1.5, -9.0, 10.0, "wood", c=0.45)
    wood_stock(m, body, 9.4, 25.4, 7.7, 2.0, 4.4, 1.9, w=1.6, material="wood", butt="steel")
    # 레버: 방아쇠울과 이어진 긴 고리
    m.cbox(body, 0.45, 3.3, 0.3, 8.2, 5.4, "metal")
    m.cbox(body, 0.45, 3.3, 2.4, 13.4, 0.3, "metal", rot=(-18, 0, 0), pivot=(0, 5.6, 13.4))
    m.cbox(body, 0.45, 3.3, 2.4, 8.2, 0.3, "metal")
    m.cbox(body, 0.45, 4.0, 1.7, 5.6, 0.3, "metal")
    m.cbox(body, 0.45, 4.0, 0.3, 5.6, 2.8, "metal")
    m.cbox(body, 0.3, 4.4, 1.3, 7.2, 0.3, "steel", rot=(-14, 0, 0), pivot=(0, 5.6, 7.2), detail=True)
    m.cbox(bolt, 0.8, 8.0, 0.5, 4.6, 4.6, "steel")                                # 위로 드러난 노리쇠
    m.cbox(body, 0.4, 8.2, 1.2, 9.2, 0.6, "metal_dark", rot=(24, 0, 0), pivot=(0, 8.2, 9.4))   # 공이치기
    sight_top = front_sight(m, body, -23.2, 7.9, height=1.3, hood=False)
    m.cbox(body, 0.9, 7.95, 0.3, -2.4, 1.6, "metal_dark")
    m.pair(body, 0.12, 0.3, 8.2, 0.8, -1.2, 0.3, "metal_dark", detail=True)
    # 일체형 조준경을 얹는 받침
    m.cbox(body, 1.1, 8.4, 0.35, 1.8, 6.6, "metal_dark")
    for z in (2.4, 7.0):
        m.cbox(body, 0.9, 8.7, 0.4, z, 0.9, "metal_dark", detail=True)
    for z in (-7.4, 23.0):
        m.cbox(body, 0.4, 5.5 if z < 0 else 2.0, 0.5, z, 0.4, "steel", detail=True)
    m.meta = {"muzzle_z": -24.0, "flash_z": -24.4, "bore_y": 7.6, "scope": (9.1, 4.8), "iron_y": sight_top - 0.2,
              "shell": (-0.2, 8.4, 5.6)}


GUNS = {
    "mini14": (mini14, "sks_tactical", "default_gun"),
    "mk12": (mk12, "spr15hb", "gun_body"),
    "vss": (vss, "ak47", "akm_default"),
    "dragunov": (dragunov, "ak47", "akm_default"),
    "m24": (m24, "m700", "main_body"),
    "win94": (win94, "kar98", "Gunbody"),
}
