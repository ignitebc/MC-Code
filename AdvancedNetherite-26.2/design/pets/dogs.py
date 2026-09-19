"""꼬미 / 슈퍼꼬미 복셀 조각. 복셀 한 칸은 0.5 유닛이다.

두 펫은 같은 포메라니안 몸을 쓰고 옷만 다르다.
  꼬미     : 흰 줄무늬 남색 조끼 + 등 위로 말린 꼬리
  슈퍼꼬미 : 파란 슈트, 가슴 발바닥 엠블럼, 빨간 망토, 계단식 깃털 날개
"""

import math

from petkit import Model, Part, mix, rgb

VOXEL = 0.5

# 자식 파트의 피벗은 복셀 격자에서 살짝 어긋나게 둔다. 격자에 딱 맞추면 몸통과 겹치는 자리에서
# 두 파트의 면이 같은 평면에 놓여 깜빡인다(z-fighting). 0.05~0.1 유닛은 눈에 보이지 않는다.

TAN = rgb("#E2A765")
DARK_TAN = rgb("#CE8B4A")
LIGHT_TAN = rgb("#ECC58B")
CREAM = rgb("#F5DFB4")
PALE = rgb("#FBEED2")
EAR_INNER = rgb("#C9825A")
BLACK = rgb("#1C1715")
EYE_BROWN = rgb("#4A2E1E")
WHITE = rgb("#FFFFFF")
PINK = rgb("#EC7F8C")
PINK_DARK = rgb("#D5606F")
MOUTH = rgb("#4A2024")

NAVY = rgb("#1E2A45")
NAVY_LIGHT = rgb("#2B3A5C")
STRIPE = rgb("#ECEAE3")

BLUE = rgb("#2C5FCB")
BLUE_DARK = rgb("#2149A3")
RED = rgb("#D0312D")
RED_DARK = rgb("#A82420")
GOLD = rgb("#F6BE2F")
GOLD_LIGHT = rgb("#FFD95A")
WING_WHITE = rgb("#FBF7EC")
WING_SHADE = rgb("#F1E8D2")
WING_DEEP = rgb("#E0D3B5")

# 망토 분절의 기본 각도(부모 기준). 윗단은 등에 얹히고 아래 두 단이 엉덩이 뒤로 흘러내린다.
# SuperGomiPetModel 은 이 기본 포즈에 흔들림만 더한다.
CAPE_TOP_ANGLE = 1.45
CAPE_MIDDLE_ANGLE = -0.60
CAPE_TIP_ANGLE = -0.15


def head_fur(x, y, z):
    """머리 털색. 정수리는 진한 황갈색, 얼굴 아래쪽과 볼은 크림색으로 밝아진다."""
    side = abs(x + 0.5)
    face = (y + 5) * 0.30 + max(0.0, -z - 9) * 0.06
    color = mix(TAN, CREAM, face)
    if side < 3.0 and y < -9:
        color = mix(color, DARK_TAN, 0.55 if side < 2.0 else 0.3)
    return color


def body_fur(x, y, z):
    """몸통 털색. 등은 황갈색, 배와 가슴은 크림색."""
    return mix(TAN, CREAM, (y + 1) * 0.16 + max(0.0, -z - 6) * 0.08)


def tail_fur(x, y, z):
    return mix(LIGHT_TAN, CREAM, (-y + 1) * 0.10)


def build_head(body: Part) -> Part:
    head = body.child("head", pivot=(0.05, -3.1, -4.1))
    head.ellipsoid((0, -6, -5), (10.5, 9, 8), head_fur, power=3.2)
    # 얼굴 아래쪽이 옆으로 부풀어 볼털이 되고, 머리 전체가 몸보다 넓어진다
    head.ellipsoid((0, -2.5, -5.5), (13, 6, 7.5), head_fur, power=2.6)
    head.ellipsoid((0, 1, -5), (8.5, 3.5, 6.5), head_fur, power=2.4)

    # 귀: 위가 좁아지는 계단형, 앞면 안쪽은 진한 색
    head.box(4, -19, -8, 10, -14, -5, TAN)
    head.box(5, -20, -8, 9, -19, -5, TAN)
    head.box(6, -18, -8, 8, -15, -7, EAR_INNER)
    head.box(5, -17, -8, 9, -15, -7, EAR_INNER)

    # 주둥이
    head.ellipsoid((0, -2.0, -12.5), (4.5, 3.5, 4.0), CREAM, power=2.6)
    head.mirror_x()

    # 눈: 검은 눈동자 + 갈색 테 + 흰 하이라이트
    for side in (1, -1):
        columns = range(4, 7) if side == 1 else range(-7, -4)
        for x in columns:
            for y in range(-9, -6):
                head.paint_front(x, y, BLACK)
        outer = 7 if side == 1 else -8
        for y in range(-9, -6):
            head.paint_front(outer, y, EYE_BROWN)
        head.paint_front(4 if side == 1 else -7, -9, WHITE)  # 둘 다 보는 사람 기준 왼쪽 위

    # 코, 벌린 입, 내민 혀
    head.box(-2, -5, -18, 2, -3, -16, BLACK)
    head.box(-1, -3, -17, 1, -2, -16, BLACK)
    for x in range(-3, 3):
        head.paint_front(x, -1, MOUTH)
    for x in range(-2, 2):
        head.paint_front(x, 0, MOUTH)
    head.box(-1, -1, -17, 1, 2, -15, PINK)
    head.box(-1, 1, -17, 1, 2, -16, PINK_DARK)
    return head


def build_leg(body: Part, name: str, x: float, z: float, fur, cuff=None) -> Part:
    leg = body.child(name, pivot=(x, 3.0, z * 1.03))
    leg.box(-3, -2, -3, 3, 5, 3, fur)
    leg.remove(lambda vx, vy, vz: vx in (-3, 2) and vz in (-3, 2))
    if cuff is not None:
        leg.shell(lambda vx, vy, vz: 2 <= vy < 5, cuff, grow_into=lambda vx, vy, vz: 2 <= vy < 5)
    # 발: 넓적한 크림색 발 + 앞으로 한 칸 나온 발가락
    leg.box(-3, 5, -4, 3, 8, 3, PALE)
    leg.box(-2, 6, -5, 2, 8, -4, PALE)
    return leg


def build_dog_body(fur) -> Part:
    body = Part("body", pivot=(0.0, 17.0, 0.0))
    body.ellipsoid((0, 0, -0.5), (8.5, 7.5, 10), fur, power=3.4)
    return body


def build_gomi() -> Model:
    body = build_dog_body(body_fur)

    # 조끼: 몸 앞쪽 절반을 한 겹 덮고, 흰 줄은 같은 겹의 색만 바꾼다
    body.shell(lambda x, y, z: z < 1, NAVY, grow_into=lambda x, y, z: z < 1)

    def is_vest(x, y, z):
        return body.vox[(x, y, z)] == NAVY

    body.paint(lambda x, y, z: is_vest(x, y, z) and -4 <= z < -2, STRIPE)
    body.paint(lambda x, y, z: is_vest(x, y, z) and z < -8 and -1 <= x < 1, STRIPE)
    body.paint(lambda x, y, z: is_vest(x, y, z) and z < -8 and 5 <= abs(x + 0.5) + y * 1.2 < 7.4, STRIPE)
    body.paint(lambda x, y, z: is_vest(x, y, z) and y < -6 and -1 <= x < 1, STRIPE)
    body.paint(lambda x, y, z: is_vest(x, y, z) and z == 0, NAVY_LIGHT)

    build_head(body)
    leg_fur = lambda x, y, z: mix(LIGHT_TAN, CREAM, y * 0.08)
    build_leg(body, "front_left_leg", 2.4, -3.0, leg_fur)
    build_leg(body, "front_right_leg", -2.4, -3.0, leg_fur)
    build_leg(body, "back_left_leg", 2.4, 3.0, leg_fur)
    build_leg(body, "back_right_leg", -2.4, 3.0, leg_fur)

    # 등 위에서 앞으로 말려 올라간 3단 꼬리
    tail_base = body.child("tail_base", pivot=(0.05, -2.45, 4.1))
    tail_base.ellipsoid((0, -1, 2.5), (3.5, 4, 3.5), tail_fur, power=2.4)
    tail_curve = tail_base.child("tail_curve", pivot=(-0.05, -1.55, 1.55))
    tail_curve.ellipsoid((0, -3, 0.5), (5, 5, 4.5), tail_fur, power=2.4)
    tail_tip = tail_curve.child("tail_tip", pivot=(0.05, -1.45, -1.05))
    tail_tip.ellipsoid((0, -1.5, -4), (4.5, 4, 4.5), tail_fur, power=2.4)
    tail_tip.ellipsoid((0, 2, -6.5), (3, 2.5, 2.5), PALE, power=2.2)

    return Model("gomi_pet", body, VOXEL)


EMBLEM = [
    "RGGGGGGGGR",
    "RGRGRRGRGR",
    "RGGGGGGGGR",
    "RGGGRRGGGR",
    "RGGRRRRGGR",
    ".RGGRRGGR.",
    "..RGGGGR..",
    "...RGGR...",
    "....RR....",
]


def stamp_emblem(part: Part, left: int, top: int, axis_sign: int):
    """엠블럼 도트를 앞면(-z) 또는 뒷면(+z) 표면에 찍는다."""
    for row, line in enumerate(EMBLEM):
        for column, symbol in enumerate(line):
            if symbol == ".":
                continue
            key = part.surface(2, axis_sign, left + column, top + row)
            if key is not None:
                part.vox[key] = RED if symbol == "R" else (GOLD_LIGHT if row < 4 else GOLD)


def build_wing(body: Part, name: str) -> Part:
    """어깨에서 위·바깥으로 뻗는 천사 날개.

    긴 칼깃 7장을 길이를 달리해 부채꼴로 겹쳐 톱니 모양 끝단을 만들고,
    앞뒤로 짧은 덮깃을 한 겹씩 얹어 깃털이 층층이 쌓인 두께를 낸다.
    """
    wing = body.child(name, pivot=(3.0, -3.5, 2.0), rot=(0.0, -0.30, 0.0))

    def feather(index, count, base_length, radius, layer, tones):
        t = index / (count - 1)
        angle = math.radians(74 - 54 * t)
        length = base_length * (1.0 - 0.42 * t) - (index % 2) * 1.5
        start = (1 + 4 * t, 3 * t, layer)
        end = (start[0] + math.cos(angle) * length, start[1] - math.sin(angle) * length, layer)
        wing.line(start, end, radius, tones[index % len(tones)])

    for index in range(7):
        feather(index, 7, 25, 2.1, 1.0, (WING_WHITE, WING_SHADE))
    for index in range(6):
        feather(index, 6, 14, 1.7, -0.5, (WING_SHADE, WING_WHITE))
        feather(index, 6, 14, 1.7, 2.5, (WING_DEEP, WING_SHADE))
    wing.ellipsoid((2, 1, 1), (3.5, 3.5, 2.5), WING_WHITE)
    return wing


def build_super_gomi() -> Model:
    suit = lambda x, y, z: mix(BLUE, BLUE_DARK, (y + 2) * 0.10)
    body = build_dog_body(suit)

    # 허리 금색 벨트, 목 뒤 빨간 깃 (망토가 여기서 시작한다)
    body.shell(lambda x, y, z: 3 <= z < 5, GOLD, grow_into=lambda x, y, z: 3 <= z < 5)
    body.shell(lambda x, y, z: -5 <= z < -2 and y < 2, RED, grow_into=lambda x, y, z: -5 <= z < -2 and y < 2)
    stamp_emblem(body, -5, -1, -1)

    build_head(body)
    leg_suit = lambda x, y, z: BLUE if y < 3 else BLUE_DARK
    for name, x, z in (("front_left_leg", 2.4, -3.0), ("front_right_leg", -2.4, -3.0),
                       ("back_left_leg", 2.4, 3.0), ("back_right_leg", -2.4, 3.0)):
        build_leg(body, name, x, z, leg_suit, cuff=RED)

    left_wing = build_wing(body, "left_wing")
    body.children.append(left_wing.mirrored_copy("right_wing"))

    # 망토: 등에 걸쳤다가 엉덩이 뒤로 흘러내리는 3단 분절. 바깥면은 빨강, 안쪽면은 어두운 빨강.
    def cape_color(x, y, z):
        if z == 0:
            return RED_DARK
        return mix(RED, RED_DARK, 0.4) if int(abs(x + 0.5)) % 5 == 4 else RED

    cape_top = body.child("cape_top", pivot=(0.0, -4.0, -2.0), rot=(CAPE_TOP_ANGLE, 0.0, 0.0))
    for row in range(12):
        half = 7 + row // 4
        cape_top.box(-half, row, 0, half, row + 1, 2, cape_color)
    cape_middle = cape_top.child("cape_middle", pivot=(0.0, 6.0, 0.0), rot=(CAPE_MIDDLE_ANGLE, 0.0, 0.0))
    for row in range(10):
        half = 10 + row // 3
        cape_middle.box(-half, row, 0, half, row + 1, 2, cape_color)
    stamp_emblem(cape_middle, -5, 0, 1)
    for name, side in (("cape_left_tip", 1), ("cape_right_tip", -1)):
        tip = cape_middle.child(name, pivot=(0.0, 5.0, 0.0), rot=(CAPE_TIP_ANGLE, 0.0, 0.0))
        for row in range(8):
            # 가운데가 먼저 파여 끝단이 제비꼬리처럼 두 갈래로 갈라진다
            inner = max(0, row - 2) * 2
            outer = 13 - max(0, row - 5)
            if inner < outer:
                low, high = (inner, outer) if side == 1 else (-outer, -inner)
                tip.box(low, row, 0, high, row + 1, 2, cape_color)

    return Model("super_gomi_pet", body, VOXEL)
