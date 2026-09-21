"""돌격소총 6종의 외형."""

from parts import (ar_stock_adapter, bottom_rail, box_mag, chamfer, curved_mag, front_sight, grip, muzzle_brake, pins,
                   poly_stock, rail, rear_sight, side_rail_stub, side_rails, side_tube, slats, trigger_group, tube,
                   tube_stock)

RIGHT = -1  # 파일 좌표에서 총의 오른쪽(탄피 배출 쪽)은 -x
MAG_LEVELS = ("mag_standard", "mag_extended_1", "mag_extended_2", "mag_extended_3")


def ak_receiver(m, body, bolt, cover_material="metal", rail_on_cover=False):
    """AK 계열 공통 총몸. ak47 베이스 기준 좌표."""
    chamfer(m, body, 1.9, 6.9, 2.6, -2.2, 12.0, "metal", c=0.3, style="panel")
    chamfer(m, body, 1.7, 9.3, 1.3, -1.2, 10.6, cover_material, c=0.4)
    for z in (0.6, 3.0, 5.4):
        m.cbox(body, 1.76, 9.6, 1.02, z, 0.5, cover_material, detail=True)
    m.cbox(body, 1.9, 8.6, 2.0, -4.6, 2.5, "metal")                      # 앞 트러니언·가늠자 받침
    m.cbox(body, 1.5, 9.0, 1.4, 9.4, 0.8, "metal", detail=True)          # 뒤 트러니언
    m.add(body, RIGHT * 0.98 - 0.02, 8.9, -0.6, 0.06, 0.8, 3.4, "void", detail=True)   # 배출구
    m.add(body, RIGHT * 1.0 - 0.06, 7.6, 2.4, 0.08, 0.45, 4.4, "steel", rot=(4, 0, 0), pivot=(0, 7.8, 6.6), detail=True)
    m.add(body, RIGHT * 1.0 - 0.1, 7.3, 6.2, 0.14, 0.9, 0.9, "steel", detail=True)     # 조정간 축
    pins(m, body, 0.95, [(7.6, -1.2), (7.5, 1.6), (8.2, 4.6), (7.4, 8.6), (8.4, 8.9)])
    m.cbox(body, 0.5, 5.6, 1.4, 2.7, 0.35, "steel", detail=True)         # 탄창 멈치
    # 노리쇠 뭉치와 장전 손잡이(사격 때 뒤로 움직인다)
    m.cbox(bolt, 1.0, 9.0, 0.8, -1.0, 4.0, "steel")
    m.add(bolt, RIGHT * 1.0 - 0.9, 9.0, 0.9, 0.9, 0.42, 0.55, "steel")
    m.add(bolt, RIGHT * 1.0 - 1.05, 8.92, 0.8, 0.25, 0.58, 0.75, "steel", detail=True)
    if rail_on_cover:
        m.cbox(body, 1.3, 10.2, 0.5, -3.6, 12.4, "metal_dark")           # 레일 받침 다리
        return rail(m, body, 10.6, -3.4, 8.6)
    return 10.6


def ak_barrel(m, body, z_tip, gas_z=-14.0, sight_z=-18.2, r=0.42):
    tube(m, body, 8.6, z_tip, -4.4, r, "metal_dark")
    side_tube(m, body, 0, 9.95, gas_z, -9.4, 0.46, "metal", detail=False)
    m.cbox(body, 1.1, 8.1, 2.5, gas_z - 1.0, 1.3, "metal")                # 가스 블록
    m.cbox(body, 0.7, 9.6, 0.7, gas_z - 0.8, 0.9, "metal", rot=(35, 0, 0), pivot=(0, 9.9, gas_z - 0.4), detail=True)
    m.cbox(body, 1.0, 8.0, 1.5, sight_z - 0.5, 1.2, "metal")
    m.cbox(body, 0.5, 9.4, 0.8, sight_z - 0.3, 0.8, "metal", detail=True)
    top = front_sight(m, body, sight_z, 10.0, height=1.1)
    m.cbox(body, 0.3, 7.4, 0.7, sight_z - 1.4, 0.3, "steel", detail=True)   # 꽂을대 끝
    side_tube(m, body, 0, 7.75, sight_z - 1.2, -9.6, 0.14, "steel")
    return top


def ak_mags(m, z0, y_top, depth, height, curve, material, w=1.35, style="hribs", grow=(0.0, 0.8, 1.4, 2.0)):
    for level, extra in zip(MAG_LEVELS, grow):
        curved_mag(m, level, z0, y_top, depth, height + extra, curve + extra * 3, w=w, material=material, style=style)


def beryl_m762(m):
    body, bolt = "akm_default", "bolt"
    rail_top = ak_receiver(m, body, bolt, cover_material="metal_dark", rail_on_cover=True)
    m.cbox(body, 1.5, 9.6, 1.0, -4.2, 1.0, "metal_dark", detail=True)
    sight_top = ak_barrel(m, body, -19.3)
    rear_sight(m, body, -4.3, 10.6, height=0.5, length=1.6)
    # 합성수지 핸드가드: 아래쪽은 세로 홈, 위쪽은 가스관 덮개
    chamfer(m, body, 2.1, 6.9, 2.1, -10.0, 7.6, "poly", c=0.35, style="vribs")
    chamfer(m, body, 1.6, 9.2, 1.4, -9.6, 7.0, "poly", c=0.4, style="vents")
    m.cbox(body, 1.9, 8.9, 0.4, -10.2, 0.5, "metal", detail=True)
    m.cbox(body, 1.9, 8.9, 0.4, -2.7, 0.5, "metal", detail=True)
    grip_y = bottom_rail(m, body, 6.9, -9.6, -3.2)
    side_rails(m, body, 1.05, 7.5, -9.4, -5.0)
    trigger_group(m, body, 3.0, 6.9, length=3.6, drop=2.2)
    grip(m, body, 6.5, 7.0, height=5.2, depth=2.3, angle=18, material="poly")
    tube_stock(m, "stock_default", 9.9, 20.6, 8.0)
    m.cbox(body, 1.4, 7.2, 2.2, 9.6, 0.9, "metal", detail=True)          # 개머리판 힌지
    # 베릴 고유의 긴 소염기(총류탄 고리 포함)
    muzzle_brake(m, "muzzle_default", 8.6, -22.4, 3.1, 0.52, slots=4)
    for z in (-21.0, -20.2):
        tube(m, "muzzle_default", 8.6, z, z + 0.3, 0.62, "steel", detail=True)
    ak_mags(m, -0.3, 7.3, 3.1, 7.0, 30, "poly")
    m.meta = {"muzzle_z": -19.3, "flash_z": -22.6, "bore_y": 8.6, "scope": (rail_top, 3.0), "iron_y": sight_top - 0.15,
              "grip": (grip_y, -6.4), "laser": (1.25, 7.95, -7.4), "stock": ar_stock_adapter(m, 8.7, 9.9, 13.4),
              "shell": (-0.6, 9.3, 0.4)}


def ace32(m):
    body, bolt = "akm_default", "bolt"
    rail_top = ak_receiver(m, body, bolt, cover_material="metal", rail_on_cover=True)
    # 핸드가드 위 레일까지 하나로 이어지는 긴 상부 레일
    rail(m, body, 10.6, -11.6, -3.4)
    chamfer(m, body, 2.2, 6.8, 3.8, -11.8, 9.4, "poly", c=0.45, style="vribs")
    for z in (-11.0, -8.6, -6.2):
        m.pair(body, 1.1, 0.08, 7.6, 1.6, z, 1.8, "poly_gray", style="hribs", detail=True)
    grip_y = bottom_rail(m, body, 6.8, -11.2, -4.0)
    laser_x = side_rail_stub(m, body, 1.1, 8.6, -8.0)
    tube(m, body, 8.6, -18.4, -4.4, 0.42, "metal_dark")
    # 가스 블록과 가늠쇠가 한 덩어리이고 가늠쇠 둘레가 고리 모양이다
    m.cbox(body, 1.1, 8.1, 2.4, -14.6, 1.4, "metal")
    m.cbox(body, 0.16, 10.5, 1.1, -14.1, 0.2, "metal_dark", detail=True)
    m.pair(body, 0.34, 0.14, 10.4, 1.4, -14.4, 0.8, "metal_dark", detail=True)
    m.cbox(body, 0.96, 11.7, 0.14, -14.4, 0.8, "metal_dark", detail=True)
    side_tube(m, body, 0, 9.95, -13.4, -11.6, 0.46, "metal", detail=False)
    rear_sight(m, body, 7.0, rail_top, height=0.7, length=1.2)
    trigger_group(m, body, 3.0, 6.9, length=3.6, drop=2.2)
    grip(m, body, 6.5, 7.0, height=5.0, depth=2.5, w=1.7, angle=15, material="poly")
    # 접이식 힌지 + 신축식 개머리판
    stock = "stock_default"
    m.cbox(stock, 1.5, 7.4, 2.2, 9.7, 1.0, "metal")
    tube(m, stock, 8.5, 10.6, 17.6, 0.55, "metal_dark")
    chamfer(m, stock, 1.5, 6.0, 3.9, 14.6, 5.6, "poly", c=0.35, style="panel")
    m.cbox(stock, 1.5, 4.6, 1.6, 17.6, 2.6, "poly", rot=(-28, 0, 0), pivot=(0, 6.0, 20.2))
    m.cbox(stock, 1.7, 4.4, 5.6, 20.2, 0.6, "rubber", style="hribs")
    m.cbox(stock, 1.3, 9.9, 0.5, 14.8, 4.6, "poly", detail=True)
    muzzle_brake(m, "muzzle_default", 8.6, -20.4, 2.0, 0.5, slots=3)
    ak_mags(m, -0.3, 7.3, 3.1, 6.8, 26, "poly")
    m.meta = {"muzzle_z": -18.4, "flash_z": -20.6, "bore_y": 8.6, "scope": (rail_top, 2.0), "iron_y": 11.6,
              "grip": (grip_y, -7.4), "laser": (laser_x, 8.6, -8.0), "stock": ar_stock_adapter(m, 8.7, 9.9, 13.4),
              "shell": (-0.6, 9.3, 0.4)}


def bullpup_ak_receiver(m, body, bolt):
    """불펍 배치: aug 베이스 기준으로 탄창이 z 8~11.6, 손잡이가 z -1 부근."""
    chamfer(m, body, 1.9, 7.0, 2.7, -3.2, 20.8, "metal", c=0.3, style="panel")
    chamfer(m, body, 1.7, 9.5, 1.2, 3.0, 14.2, "metal", c=0.4)
    for z in (5.0, 8.0, 11.0, 14.0):
        m.cbox(body, 1.76, 9.8, 0.95, z, 0.5, "metal", detail=True)
    m.add(body, RIGHT * 0.98 - 0.02, 8.8, 8.2, 0.06, 0.8, 3.6, "void", detail=True)
    pins(m, body, 0.95, [(7.6, -1.6), (7.6, 4.0), (8.2, 7.2), (7.5, 13.0), (8.3, 16.4)])
    m.cbox(bolt, 1.0, 8.9, 0.8, 8.0, 4.2, "steel")
    m.add(bolt, RIGHT * 1.0 - 0.9, 8.95, 10.2, 0.9, 0.42, 0.55, "steel")
    m.add(bolt, RIGHT * 1.0 - 1.05, 8.87, 10.1, 0.25, 0.58, 0.75, "steel", detail=True)


def groza(m):
    body, bolt = "aug_default", "aug_bolt"
    bullpup_ak_receiver(m, body, bolt)
    # 개머리판: 총몸 끝에 바로 붙은 판
    m.cbox(body, 2.1, 5.4, 5.2, 17.6, 0.7, "metal_dark")
    m.cbox(body, 2.2, 5.2, 5.6, 18.3, 0.7, "rubber", style="hribs")
    # 앞쪽 하우징과 일체형 수직 손잡이
    chamfer(m, body, 2.1, 6.6, 3.6, -9.6, 6.6, "poly", c=0.45, style="panel")
    chamfer(m, body, 1.6, 2.6, 4.2, -6.4, 2.3, "poly", c=0.4, style="checker")
    m.cbox(body, 1.7, 2.3, 0.4, -6.5, 2.5, "poly", detail=True)
    trigger_group(m, body, -4.4, 7.0, length=3.4, drop=2.3)
    grip(m, body, -1.0, 7.0, height=5.0, depth=2.3, angle=16, material="poly")
    # 운반 손잡이 겸 조준 레일
    for z in (-3.2, 7.4):
        m.cbox(body, 1.0, 10.0, 1.9, z, 1.0, "metal_dark")
    m.cbox(body, 1.2, 11.7, 0.45, -3.6, 12.4, "metal_dark")
    rail_top = rail(m, body, 12.1, -3.2, 8.4, w=1.3)
    front_sight(m, body, -8.6, 10.2, height=3.0)
    rear_sight(m, body, 8.6, 12.1, height=1.0, length=1.2)
    tube(m, body, 8.9, -14.1, -9.6, 0.44, "metal_dark")
    m.cbox(body, 1.0, 8.2, 1.9, -11.6, 1.2, "metal")
    muzzle_brake(m, "muzzle_default", 8.9, -16.6, 2.5, 0.62, slots=2)
    ak_mags(m, 8.3, 7.2, 3.1, 6.6, 28, "metal_dark")
    m.meta = {"muzzle_z": -14.1, "flash_z": -16.8, "bore_y": 8.9, "scope": (rail_top, 2.4), "iron_y": 13.1,
              "shell": (-0.6, 9.2, 10.0)}


def famas(m):
    body, bolt = "aug_default", "aug_bolt"
    chamfer(m, body, 2.2, 5.8, 3.9, -9.2, 28.2, "poly_gray", c=0.5, style="panel")
    chamfer(m, body, 1.8, 9.4, 1.0, -8.0, 24.0, "poly_gray", c=0.3)
    m.cbox(body, 2.3, 6.4, 3.4, 12.5, 5.8, "poly", detail=True)               # 볼받침 덮개
    m.cbox(body, 2.3, 4.6, 5.6, 19.0, 0.8, "rubber", style="hribs")
    m.cbox(body, 1.9, 4.6, 1.4, 14.0, 5.0, "poly_gray")
    m.add(body, RIGHT * 1.16 - 0.02, 8.4, 8.4, 0.06, 0.8, 3.2, "void", detail=True)
    m.cbox(bolt, 1.0, 8.4, 0.8, 8.6, 2.8, "steel")
    # 손 전체를 감싸는 큰 방아쇠울
    m.cbox(body, 0.5, 1.4, 0.3, -5.0, 6.2, "poly")
    m.cbox(body, 0.5, 1.4, 4.4, -5.0, 0.3, "poly")
    m.cbox(body, 0.32, 3.6, 2.2, -2.6, 0.34, "steel", rot=(-14, 0, 0), pivot=(0, 5.8, -2.6), detail=True)
    grip(m, body, -1.0, 5.9, height=4.6, depth=2.3, angle=14, material="poly")
    # 길게 뻗은 운반 손잡이와 그 위 레일
    for z in (-7.4, 9.2):
        m.cbox(body, 1.1, 10.2, 2.0, z, 1.1, "poly")
    m.cbox(body, 1.3, 12.0, 0.6, -8.0, 18.8, "poly")
    slats(m, body, 0.9, 10.5, 1.5, -5.6, 8.6, "poly", gap=1.6, bar=0.5)
    rail_top = rail(m, body, 12.6, -6.4, 8.4, w=1.3)
    front_sight(m, body, -7.4, 12.6, height=1.2, hood=False)
    rear_sight(m, body, 9.0, 12.6, height=0.8, length=1.0)
    m.cbox("aug_charge", 0.9, 10.3, 0.7, -2.0, 1.6, "steel")                    # 손잡이 아래 장전 손잡이
    tube(m, body, 8.9, -14.1, -9.2, 0.42, "metal_dark")
    for z in (-12.6, -11.4):
        tube(m, body, 8.9, z, z + 0.35, 0.56, "steel", detail=True)
    muzzle_brake(m, "muzzle_default", 8.9, -17.2, 3.1, 0.5, slots=4)
    # 핸드가드 옆에 접어 둔 양각대
    for side in (1, -1):
        m.add(body, side * 1.25 - 0.17, 8.3, -8.4, 0.34, 0.34, 8.6, "metal_dark")
        m.add(body, side * 1.25 - 0.24, 8.2, -9.0, 0.48, 0.5, 0.7, "rubber", detail=True)
    for level, extra in zip(MAG_LEVELS, (0.0, 0.9, 0.9, 1.8)):
        box_mag(m, level, 8.4, 6.1, 3.0, 5.2 + extra, w=1.3, angle=-4, material="metal")
    laser_x = side_rail_stub(m, body, 1.1, 7.0, -5.0)                           # 접힌 양각대 다리 아래쪽
    m.meta = {"muzzle_z": -14.1, "flash_z": -17.4, "bore_y": 8.9, "scope": (rail_top, 1.0), "iron_y": 13.7,
              "laser": (laser_x, 7.0, -5.0), "shell": (-0.7, 8.8, 10.0)}


def ar_receiver(m, body, bolt, z0=0.2, z1=12.4, top=11.0):
    """AR 계열 상·하부 총몸. m16a4 베이스 기준(총열 축 y=9.6)."""
    chamfer(m, body, 1.9, 9.0, top - 9.0, z0, z1 - z0, "metal", c=0.35)
    chamfer(m, body, 1.8, 7.4, 1.8, z0 + 0.4, z1 - z0, "metal", c=0.3, style="panel")
    m.add(body, RIGHT * 0.98 - 0.02, 9.1, 1.6, 0.06, 0.9, 4.2, "void", detail=True)
    m.add(body, RIGHT * 1.0 - 0.3, 9.0, 6.0, 0.3, 0.9, 0.8, "metal", detail=True)       # 탄피 튕김막이
    m.add(body, RIGHT * 1.0 - 0.35, 9.6, 7.4, 0.35, 0.5, 0.9, "metal", detail=True)     # 노리쇠 전진기
    pins(m, body, 0.92, [(8.0, 1.2), (8.0, 8.6), (7.8, 5.2)])
    m.add(body, RIGHT * 0.95 - 0.1, 7.9, 7.2, 0.12, 0.35, 1.3, "steel", rot=(20, 0, 0), pivot=(0, 8.0, 8.4), detail=True)
    m.cbox(bolt, 1.2, 9.2, 0.9, 1.7, 4.0, "steel")


def k2(m):
    body, bolt = "m16_default", "bolt"
    ar_receiver(m, body, bolt)
    m.cbox(body, 1.7, 6.4, 2.7, 1.9, 4.5, "metal", style="panel")             # 탄창 삽입구
    # K2 고유: AK 식으로 노리쇠에 붙은 오른쪽 장전 손잡이
    m.add(bolt, RIGHT * 1.0 - 0.85, 9.5, 3.2, 0.85, 0.4, 0.5, "steel")
    rail_top = rail(m, body, 11.0, 0.6, 9.4)
    chamfer(m, body, 2.2, 8.0, 2.8, -12.6, 12.4, "poly", c=0.5, style="vribs")
    m.cbox(body, 1.4, 10.6, 0.4, -12.2, 11.6, "poly", style="vents")
    m.cbox(body, 2.3, 7.9, 3.0, -13.0, 0.5, "steel", detail=True)
    side_tube(m, body, 0, 10.7, -15.2, -12.6, 0.36, "metal", detail=False)
    tube(m, body, 9.6, -21.8, -12.6, 0.42, "metal_dark")
    m.cbox(body, 1.0, 9.0, 2.2, -16.0, 1.4, "metal")
    m.cbox(body, 0.5, 8.4, 0.7, -17.6, 1.0, "metal", detail=True)            # 착검 돌기
    sight = "sight"
    m.cbox(sight, 0.7, 11.2, 0.9, -15.8, 1.0, "metal_dark")
    m.cbox(sight, 0.16, 12.0, 0.9, -15.4, 0.2, "metal_dark", detail=True)
    m.pair(sight, 0.36, 0.14, 11.9, 1.2, -15.7, 0.8, "metal_dark", detail=True)
    m.cbox(sight, 1.0, 13.1, 0.14, -15.7, 0.8, "metal_dark", detail=True)
    m.cbox(sight, 1.4, 11.0, 0.9, 9.6, 2.4, "metal")
    rear_sight(m, sight, 10.2, 11.9, height=0.8, length=1.4)
    trigger_group(m, body, 5.0, 7.4, length=3.0, drop=2.2)
    grip(m, body, 7.4, 7.6, height=4.8, depth=2.3, angle=17, material="poly")
    stock = "stock_default"
    m.cbox(stock, 1.6, 7.6, 3.0, 12.4, 1.0, "metal")                          # 접철 힌지
    poly_stock(m, stock, 13.2, 23.0, 10.4, 5.2, w=1.6, cutout=False)
    muzzle_brake(m, "muzzle_default", 9.6, -24.0, 2.2, 0.5, slots=3)
    for level, extra in zip(MAG_LEVELS, (0.0, 0.8, 0.8, 0.8)):
        curved_mag(m, level, 2.5, 6.8, 3.2, 5.0 + extra, 12, w=1.3, segments=4, material="metal")
    m.meta = {"muzzle_z": -21.8, "flash_z": -24.2, "bore_y": 9.6, "scope": (rail_top, 5.0), "iron_y": 12.9,
              "stock": ar_stock_adapter(m, 9.6, 12.4, 14.1), "shell": (-0.6, 9.8, 4.2)}


def mk47_mutant(m):
    body, bolt = "m16_default", "bolt"
    ar_receiver(m, body, bolt)
    m.cbox(body, 1.8, 6.0, 3.0, 1.2, 5.6, "metal", style="panel")             # AK 탄창용으로 키운 삽입구
    m.cbox("pull", 1.7, 10.7, 0.3, 10.6, 1.0, "metal_dark")
    m.cbox("pull", 0.6, 10.7, 0.3, 5.0, 5.8, "metal_dark", detail=True)
    rail_top = rail(m, body, 11.0, -17.2, 12.0)
    # 가늘고 긴 프리플로트 핸드가드
    chamfer(m, body, 1.7, 8.3, 2.7, -17.4, 17.6, "metal_dark", c=0.55, style="vents")
    m.cbox(body, 1.9, 8.6, 2.2, -0.4, 0.7, "metal", detail=True)
    grip_y = bottom_rail(m, body, 8.3, -16.8, -4.0, w=1.3)
    laser_x = side_rail_stub(m, body, 0.85, 9.6, -13.0)
    tube(m, body, 9.6, -21.8, -17.4, 0.46, "metal_dark")
    sight = "sight"
    for z in (-16.4, 10.4):
        m.cbox(sight, 1.0, rail_top, 0.4, z, 1.2, "poly")
        m.cbox(sight, 0.7, rail_top + 0.4, 1.0, z + 0.4, 0.3, "poly")
        m.cbox(sight, 0.2, rail_top + 1.0, 0.5, z + 0.38, 0.34, "void", detail=True)
    trigger_group(m, body, 5.0, 7.4, length=3.0, drop=2.2)
    grip(m, body, 7.4, 7.6, height=4.8, depth=2.4, angle=20, material="poly")
    stock = "stock_default"
    tube(m, body, 9.6, 12.4, 19.4, 0.58, "metal_dark")                        # 완충관은 개머리판을 바꿔도 남는다
    chamfer(m, stock, 1.5, 6.4, 4.2, 16.0, 6.2, "poly", c=0.35, style="panel")
    m.cbox(stock, 1.5, 5.0, 1.8, 19.4, 3.0, "poly", rot=(-26, 0, 0), pivot=(0, 6.4, 22.2))
    m.cbox(stock, 1.7, 4.8, 6.0, 22.2, 0.6, "rubber", style="hribs")
    muzzle_brake(m, "muzzle_default", 9.6, -24.4, 2.6, 0.62, slots=2)
    ak_mags(m, 2.2, 6.4, 3.2, 6.4, 30, "poly")
    m.meta = {"muzzle_z": -21.8, "flash_z": -24.6, "bore_y": 9.6, "scope": (rail_top, 5.0), "iron_y": rail_top + 1.25,
              "grip": (grip_y, -8.0), "laser": (laser_x, 9.6, -13.0), "stock": (9.6, 14.1),
              "shell": (-0.6, 9.8, 4.2)}


GUNS = {
    "groza": (groza, "aug", "aug_default"),
    "beryl_m762": (beryl_m762, "ak47", "akm_default"),
    "ace32": (ace32, "ak47", "akm_default"),
    "famas": (famas, "aug", "aug_default"),
    "k2": (k2, "m16a4", "m16_default"),
    "mk47_mutant": (mk47_mutant, "m16a4", "m16_default"),
}
