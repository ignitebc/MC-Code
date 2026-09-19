"""레전더리 펫(암흑드래곤 / 스컬큰 레이븐) 복셀 조각. 복셀 한 칸은 1 유닛이다.

렌더러에서 0.3 배로 줄여 그리므로 화면상 복셀 크기는 꼬미 계열과 비슷하다.
"""

import math

from petkit import Model, Part, _hash01, mix, rgb

VOXEL = 1.0

# 회전이 없는 자식 파트의 피벗은 복셀 격자에서 0.1 유닛쯤 어긋나게 둔다.
# 격자에 딱 맞추면 부모와 겹치는 자리에서 면이 같은 평면에 놓여 깜빡인다(z-fighting).

# ---- 암흑드래곤 -----------------------------------------------------------

SCALE_DARK = rgb("#17141E")
SCALE_BASE = rgb("#221E2C")
SCALE_LIGHT = rgb("#36304A")
BELLY = rgb("#3B3350")
BELLY_LINE = rgb("#2A2438")
BONE = rgb("#ECE6D6")
BONE_SHADE = rgb("#C9C0A8")
BONE_DEEP = rgb("#9E947B")
MEMBRANE = rgb("#6A3FA8")
MEMBRANE_DEEP = rgb("#3F2470")
MEMBRANE_EDGE = rgb("#8E5BD6")
GLOW = rgb("#C56BFF")
GLOW_CORE = rgb("#F3D6FF")
GOLD = rgb("#E0A92A")
GOLD_DEEP = rgb("#A87414")

# 기본 포즈 각도. DarkDragonPetModel 은 이 포즈에 움직임만 더한다.
NECK_ANGLE = 0.55  # 목은 이만큼 위로 들고, 머리는 같은 만큼 되돌려 수평을 본다
JAW_ANGLE = 0.25
WING_INNER_ANGLE = -0.40  # 안쪽 날개는 위로 들고
WING_OUTER_ANGLE = 0.30  # 바깥 날개는 다시 늘어뜨린다


def dragon_scales(x, y, z):
    """벽돌쌓기처럼 어긋난 비늘 무늬. 비늘마다 위쪽이 밝고 아래 경계가 어둡다."""
    row = (y + z) // 2
    cell = (x + z + (row % 2) * 2) % 4
    if (y + z) % 2 == 0 and cell == 0:
        return SCALE_LIGHT
    if (y + z) % 2 == 1 and cell in (1, 3):
        return SCALE_DARK
    return mix(SCALE_BASE, SCALE_LIGHT, _hash01(x, y, z, 7) * 0.35)


def dragon_hide(belly_from: float, belly=BELLY, line=BELLY_LINE):
    """등은 비늘, 배 쪽은 가로 마디가 있는 배판."""
    def color(x, y, z):
        if y >= belly_from and abs(x + 0.5) < 5.5:
            return line if z % 3 == 0 else belly
        return dragon_scales(x, y, z)
    return color


def bone(x, y, z):
    return mix(BONE, BONE_SHADE, _hash01(x, y, z, 3) * 0.7)


def add_spine_plates(part: Part, z_positions, top_of, height=3):
    """척추를 따라 선 뼈 가시. top_of(z) 는 그 자리의 등 표면 y 를 돌려준다."""
    for z in z_positions:
        top = top_of(z)
        for step in range(height):
            part.box(-1, top - step - 1, z + step // 2, 1, top - step, z + 3 - (step + 1) // 2, bone)


def build_dragon_wing(body: Part) -> Part:
    wing = body.child("wing_left", pivot=(8.0, -7.0, -9.0), rot=(0.0, 0.0, WING_INNER_ANGLE))

    def membrane_color(x, y, z, reach):
        depth = z / max(1.0, reach)
        color = mix(MEMBRANE, MEMBRANE_DEEP, depth * 0.9)
        if z >= reach - 1.5:
            color = MEMBRANE_EDGE
        return mix(color, MEMBRANE_DEEP, _hash01(x, y, z, 11) * 0.25)

    def add_membrane(part: Part, length: int, reach_at, ribs):
        for x in range(length):
            reach = reach_at(x)
            # 너덜너덜한 뒷단: 곳곳이 찢겨 짧아진다
            reach -= (2 if _hash01(x // 2, 0, 0, 5) > 0.7 else 0)
            for z in range(1, int(reach)):
                part.set(x, 0, z, lambda vx, vy, vz: membrane_color(vx, vy, vz, reach))
        for start_x, end_x, end_z in ribs:
            part.line((start_x, 0.5, 0.5), (end_x, 0.5, end_z), 0.9, SCALE_DARK)

    inner_reach = lambda x: 20 + 4 * abs(math.sin(math.pi * x / 10.0)) - 0.12 * x
    add_membrane(wing, 28, inner_reach, ((2, 6, 21), (12, 17, 22), (22, 27, 20)))
    wing.line((0, 0.5, 0.5), (28, 0.5, 0.5), 2.0, dragon_scales)
    for x in (6, 15, 24):
        wing.box(x, -4, 0, x + 2, -1, 2, bone)
        wing.box(x, -5, 0, x + 1, -4, 1, bone)

    outer = wing.child("wing_left_outer", pivot=(28.0, 0.0, 0.0), rot=(0.0, 0.0, WING_OUTER_ANGLE))
    outer_reach = lambda x: (19 - 0.78 * x) + 3 * abs(math.sin(math.pi * x / 8.0))
    add_membrane(outer, 23, outer_reach, ((1, 7, 18), (9, 15, 11)))
    outer.line((0, 0.5, 0.5), (23, 0.5, 0.5), 1.5, dragon_scales)
    for x in (6, 14):
        outer.box(x, -3, 0, x + 2, -1, 2, bone)
        outer.box(x, -4, 0, x + 1, -3, 1, bone)
    # 날개 끝 갈고리 발톱
    outer.line((22, 0.5, 0.5), (26, -2.5, -2.5), 1.0, bone)
    return wing


def build_dragon_leg(body: Part, name: str, x: float, z: float, front: bool) -> Part:
    leg = body.child(name, pivot=(x, 2.0, z + 0.1))
    leg.ellipsoid((0, 3, 0 if front else 1), (4.5, 7, 6), dragon_scales, power=2.4)  # 허벅지
    leg.box(-3, 7, -2, 3, 13, 3, dragon_scales)
    leg.box(-3, 9, 3, 3, 11, 4, bone)  # 뒤꿈치 뼈 돌기
    # 넓적한 발과 뼈 발톱 세 개
    leg.box(-4, 13, -6, 4, 17, 4, dragon_scales)
    for claw_x in (-4, -1, 2):
        leg.box(claw_x, 14, -9, claw_x + 2, 17, -6, bone)
        leg.box(claw_x, 16, -11, claw_x + 2, 17, -9, BONE_SHADE)
    return leg


def build_dark_dragon() -> Model:
    body = Part("body", pivot=(0.0, 5.0, 0.0))
    body.ellipsoid((0, 0, 0), (9.5, 8.5, 19), dragon_hide(4), power=2.6)
    body.ellipsoid((0, -1.5, -9), (11, 10.5, 10), dragon_hide(5), power=2.4)  # 가슴
    body.ellipsoid((0, 0, 11), (9, 8, 9), dragon_hide(4), power=2.4)  # 엉덩이

    def back_top(z):
        key = body.surface(1, -1, 0, z)
        return key[1] if key else -8

    add_spine_plates(body, range(-14, 16, 6), back_top, height=4)

    # 목: 앞쪽 위로 뻗고, 머리는 다시 수평으로 되돌린다
    neck = body.child("neck", pivot=(0.0, -5.0, -15.0), rot=(-NECK_ANGLE, 0.0, 0.0))
    neck.ellipsoid((0, 0, -8), (6.5, 6.5, 11), dragon_hide(3), power=2.6)
    add_spine_plates(neck, range(-14, -1, 5), lambda z: -6, height=3)

    head = neck.child("head", pivot=(0.0, -1.0, -16.0), rot=(NECK_ANGLE, 0.0, 0.0))
    head.ellipsoid((0, -1, -6), (8, 7, 8.5), dragon_scales, power=2.8)
    head.box(-5, -4, -26, 5, 3, -12, dragon_scales)  # 주둥이
    head.box(-4, -5, -25, 4, -4, -13, dragon_scales)
    head.remove(lambda x, y, z: z < -23 and y < -2 and abs(x + 0.5) > 3)
    # 두개골 장갑판: 이마에서 주둥이 위까지 덮는 뼈
    head.shell(lambda x, y, z: y <= -6 and z < 0, bone, grow_into=lambda x, y, z: y <= -7)
    head.box(-4, -6, -24, 4, -5, -12, bone)
    head.box(-2, -7, -18, 2, -6, -12, bone)
    head.box(-1, -8, -27, 1, -4, -24, bone)  # 코뿔
    # 눈두덩 뼈와 발광하는 눈
    for side in (1, -1):
        low, high = (5, 9) if side == 1 else (-9, -5)
        head.box(low, -6, -14, high, -5, -7, bone)
        eye_x = 7 if side == 1 else -8
        for z in range(-13, -9):
            for y in (-4, -3):
                head.set(eye_x, y, z, GLOW)
        head.set(eye_x, -4, -12, GLOW_CORE)
        head.set(eye_x, -4, -11, GLOW_CORE)
        nostril = 4 if side == 1 else -5
        head.set(nostril, -2, -26, GLOW)
    # 윗니
    for z in range(-25, -13, 2):
        head.box(-5, 3, z, -4, 5, z + 1, BONE)
        head.box(4, 3, z, 5, 5, z + 1, BONE)
    # 뒤로 길게 휜 큰 뿔 한 쌍 + 옆으로 뻗은 짧은 뿔 한 쌍
    for side in (1, -1):
        base = 4.5 * side
        head.line((base, -7, -2), (base + side * 1.5, -13, 6), 1.6, bone)
        head.line((base + side * 1.5, -13, 6), (base + side * 2.5, -21, 11), 1.2, bone)
        head.line((7.0 * side, -2, -2), (12.0 * side, -6, 4), 1.2, bone)
        head.line((6.0 * side, 2, -4), (9.0 * side, 4, 1), 1.0, BONE_SHADE)

    jaw = head.child("jaw", pivot=(0.0, 3.0, -11.0), rot=(JAW_ANGLE, 0.0, 0.0))
    jaw.box(-4, 0, -14, 4, 3, 0, dragon_scales)
    jaw.box(-3, 3, -12, 3, 4, -1, BELLY)
    jaw.paint(lambda x, y, z: y == 0 and abs(x + 0.5) < 3 and z < -1, GLOW)
    jaw.paint(lambda x, y, z: y == 0 and abs(x + 0.5) < 2 and -11 < z < -2, GLOW_CORE)
    for z in range(-13, -2, 2):
        jaw.box(-4, -2, z, -3, 0, z + 1, BONE)
        jaw.box(3, -2, z, 4, 0, z + 1, BONE)

    wing = build_dragon_wing(body)
    body.children.append(wing.mirrored_copy("wing_right"))

    build_dragon_leg(body, "front_left_leg", 9.4, -10.0, True)
    body.children.append(body.children[-1].mirrored_copy("front_right_leg"))
    build_dragon_leg(body, "hind_left_leg", 9.4, 12.0, False)
    body.children.append(body.children[-1].mirrored_copy("hind_right_leg"))

    # 꼬리: 3단으로 가늘어지고, 아랫면은 금색 배판, 끝은 뼈 칼날
    def tail_hide(x, y, z):
        if y >= 2 and abs(x + 0.5) < 3.5:
            return GOLD_DEEP if z % 3 == 0 else GOLD
        return dragon_scales(x, y, z)

    tail_1 = body.child("tail_1", pivot=(0.1, 0.1, 17.0))
    tail_1.ellipsoid((0, 0, 8), (6, 5.5, 10), tail_hide, power=2.6)
    add_spine_plates(tail_1, (3, 9), lambda z: -5, height=3)
    tail_2 = tail_1.child("tail_2", pivot=(-0.1, 0.1, 15.0))
    tail_2.ellipsoid((0, 0, 8), (4.5, 4, 10), tail_hide, power=2.6)
    add_spine_plates(tail_2, (3, 9), lambda z: -4, height=2)
    tail_3 = tail_2.child("tail_3", pivot=(0.1, 0.1, 15.0))
    tail_3.ellipsoid((0, 0, 7), (3, 2.8, 9), tail_hide, power=2.4)
    for step in range(8):
        half = [2, 4, 6, 7, 6, 4, 3, 1][step]
        tail_3.box(-half, -1, 14 + step, half, 1, 15 + step, bone)

    return Model("dark_dragon_pet", body, VOXEL, pixels_per_voxel=3)


# ---- 스컬큰 레이븐 --------------------------------------------------------

FEATHER_DARK = rgb("#0D1018")
FEATHER_BASE = rgb("#151A27")
FEATHER_LIGHT = rgb("#232C42")
FEATHER_SHEEN = rgb("#2F3D63")
SCULK_DEEP = rgb("#0A2F3C")
SCULK = rgb("#0E6F78")
SCULK_GLOW = rgb("#2FE0D0")
SCULK_CORE = rgb("#C8FFF6")
BEAK = rgb("#2B2F3A")
BEAK_LIGHT = rgb("#4A5060")
BEAK_TIP = rgb("#14161C")
CLAW = rgb("#0B0C10")
LEG = rgb("#3A3D47")


def raven_feathers(x, y, z):
    """겹겹이 포개진 깃털 무늬. 깃 끝마다 밝은 테가 돌고, 드문드문 스컬크가 번져 있다."""
    row = (z + y) // 3
    column = (x + (row % 2) * 2) % 4
    patch = _hash01(x // 3, y // 3, z // 4, 21)
    if patch > 0.955:
        spark = _hash01(x, y, z, 22)
        return SCULK_GLOW if spark > 0.72 else (SCULK if spark > 0.35 else SCULK_DEEP)
    if (z + y) % 3 == 0:
        return FEATHER_DARK if column == 0 else FEATHER_LIGHT
    return mix(FEATHER_BASE, FEATHER_SHEEN, _hash01(x, y, z, 23) * 0.3 + (0.25 if y < -5 else 0.0))


def flight_feathers(part: Part, feathers):
    """(x0, x1, z0, z1, y, 끝색) 목록으로 납작한 깃을 층층이 깐다."""
    for x0, x1, z0, z1, y, tip in feathers:
        for z in range(z0, z1):
            for x in range(x0, x1):
                at_tip = z >= z1 - 2
                at_shaft = x == (x0 + x1) // 2
                color = tip if at_tip else (FEATHER_DARK if at_shaft else raven_feathers(x, y, z))
                part.set(x, y, z, color)


def build_raven_wing(body: Part) -> Part:
    """몸 옆에 접어 붙인 날개. 덮깃 덩어리 뒤로 긴 칼깃이 계단처럼 빠져나온다."""
    wing = body.child("left_wing", pivot=(7.5, -5.1, -7.1))
    wing.ellipsoid((1.5, 5, 9), (2.5, 8, 12), raven_feathers, power=2.6)
    wing.ellipsoid((1.5, 1, 1), (3, 5, 5), raven_feathers, power=2.4)
    for index in range(6):
        length = 30 - index * 3
        top = 3 + index * 2
        tip = SCULK_GLOW if index % 2 == 0 else SCULK
        for z in range(12, 12 + length):
            for y in range(top, top + 3):
                at_tip = z >= 12 + length - 2
                color = tip if at_tip and y == top + 1 else (SCULK_DEEP if at_tip else raven_feathers(2, y, z))
                wing.set(1, y, z, color)
                wing.set(2, y, z, FEATHER_DARK if y == top + 2 else color)
    return wing


def build_raven_leg(body: Part, name: str, x: float) -> Part:
    leg = body.child(name, pivot=(x, 8.0, 4.1))
    leg.ellipsoid((0, 1, 0), (4, 4.5, 4), raven_feathers, power=2.4)  # 허벅지 깃털
    leg.box(-1, 4, -1, 2, 10, 2, LEG)
    leg.box(-1, 6, -1, 2, 7, 2, BEAK_LIGHT)
    # 앞발가락 셋, 뒷발가락 하나, 끝은 검은 발톱
    for toe_x, spread in ((-1, -3), (0, 0), (1, 3)):
        leg.line((toe_x + 0.5, 9.0, -0.5), (toe_x + spread + 0.5, 9.0, -6.5), 1.0, LEG)
        leg.box(toe_x + spread, 8, -9, toe_x + spread + 1, 10, -7, CLAW)
    leg.line((0.5, 9.0, 1.5), (0.5, 9.0, 5.5), 1.0, LEG)
    leg.box(0, 8, 6, 1, 10, 8, CLAW)
    return leg


def build_sculken_raven() -> Model:
    body = Part("body", pivot=(0.0, 6.0, 0.0))
    body.ellipsoid((0, 0, 3), (9.5, 9.5, 16), raven_feathers, power=2.5)
    body.ellipsoid((0, -2, -7), (9, 10, 9), raven_feathers, power=2.3)  # 부푼 가슴
    # 등을 타고 흐르는 스컬크 줄기
    body.paint(lambda x, y, z: y < -6 and abs(x + 0.5) < 2 and _hash01(0, 0, z, 31) > 0.3, SCULK_DEEP)
    body.paint(lambda x, y, z: y < -6 and abs(x + 0.5) < 1 and _hash01(0, 0, z, 31) > 0.3, SCULK)
    body.paint(lambda x, y, z: y < -6 and abs(x + 0.5) < 1 and z % 5 == 0, SCULK_GLOW)

    neck = body.child("neck", pivot=(0.0, -7.0, -10.0), rot=(0.35, 0.0, 0.0))
    neck.ellipsoid((0, -4, -1), (6, 7, 6), raven_feathers, power=2.3)
    # 목 아래로 삐죽삐죽 늘어진 멱깃
    for index, (x, length) in enumerate(((-4, 4), (-2, 6), (0, 7), (2, 6), (4, 4))):
        neck.box(x - 1, 0, -6, x + 1, length, -3, FEATHER_LIGHT if index % 2 else FEATHER_BASE)

    head = neck.child("head", pivot=(0.0, -9.0, -1.0), rot=(-0.35, 0.0, 0.0))
    head.ellipsoid((0, -3, -2), (7, 6.5, 8), raven_feathers, power=2.4)
    # 정수리에 돋은 스컬크 촉수 뿔
    for side in (1, -1):
        head.line((2.5 * side, -8, 0), (4.0 * side, -13, 4), 1.0, SCULK)
        head.set(3 if side == 1 else -4, -14, 4, SCULK_GLOW)
        head.set(3 if side == 1 else -4, -15, 5, SCULK_CORE)
    # 눈: 청록 발광
    for side in (1, -1):
        eye_x = 6 if side == 1 else -7
        for y in (-6, -5, -4):
            for z in (-8, -7, -6):
                head.set(eye_x, y, z, SCULK_GLOW)
                head.set(eye_x - side, y, z, SCULK_DEEP)
        head.set(eye_x, -6, -8, SCULK_CORE)
        head.set(eye_x, -5, -7, FEATHER_DARK)  # 눈동자
        for z in range(-9, -5):
            head.set(eye_x, -7, z, FEATHER_DARK)  # 눈썹깃
    # 윗부리: 굵고 끝이 아래로 굽은 까마귀 부리
    for step in range(12):
        half = max(1, 3 - step // 4)
        top = -4 + step // 5
        bottom = -1 if step < 10 else 1
        tone = BEAK_LIGHT if step < 3 else BEAK
        head.box(-half, top, -10 - step, half, bottom, -9 - step, BEAK_TIP if step >= 10 else tone)
    head.box(-1, -5, -14, 1, -4, -9, BEAK_LIGHT)  # 부리 등선
    head.box(-2, -5, -11, 2, -3, -8, raven_feathers)  # 콧깃

    beak = head.child("beak", pivot=(0.0, -1.0, -9.0))
    for step in range(9):
        half = max(1, 3 - step // 3)
        beak.box(-half, 0, -1 - step, half, 2 if step < 6 else 1, -step, BEAK)
    beak.paint(lambda x, y, z: y == 0 and abs(x + 0.5) < 1 and z < -1, SCULK_DEEP)

    wing = build_raven_wing(body)
    body.children.append(wing.mirrored_copy("right_wing"))

    # 꼬리: 부채꼴로 펼친 긴 깃, 끝마다 스컬크 발광
    tail = body.child("tail", pivot=(0.0, 1.0, 17.0), rot=(0.18, 0.0, 0.0))
    tail.ellipsoid((0, 0, 1), (6, 4, 5), raven_feathers, power=2.3)
    flight_feathers(tail, (
        (-2, 2, 2, 30, 1, SCULK_GLOW),
        (2, 6, 2, 27, 0, SCULK), (-6, -2, 2, 27, 0, SCULK),
        (6, 10, 2, 23, 1, SCULK_GLOW), (-10, -6, 2, 23, 1, SCULK_GLOW),
        (10, 13, 2, 18, 0, SCULK), (-13, -10, 2, 18, 0, SCULK),
    ))

    build_raven_leg(body, "left_leg", 4.1)
    body.children.append(body.children[-1].mirrored_copy("right_leg"))

    return Model("sculken_raven_pet", body, VOXEL, pixels_per_voxel=3)
