"""디아루가, 커비, 유니콘, 가젤의 복셀 원형과 표면 색.

슈퍼꼬미와 같은 petkit으로 메시와 UV를 함께 만든다.
피벗은 모델 유닛, 조각 좌표는 복셀 단위다. 네 종 모두 발바닥은 y=24다.
"""

import math

from petkit import Model, Part, mix, rgb


# ---- 디아루가: 푸른 갑주, 은빛 칼날, 다이아몬드 가슴 ------------------------

BLUE = rgb("#214B85")
BLUE_LIGHT = rgb("#387CB0")
BLUE_DARK = rgb("#142B52")
STEEL = rgb("#BACCD7")
STEEL_LIGHT = rgb("#E6F4F7")
STEEL_DARK = rgb("#748DA9")
CYAN = rgb("#34D7EE")
CYAN_CORE = rgb("#C1FAFF")
EYE_RED = rgb("#EE664E")
INK = rgb("#172237")


def dialga_hide(x, y, z):
    tone = (math.sin(y * 0.5) + math.cos(z * 0.3)) * 0.07 + 0.22
    return mix(BLUE, BLUE_LIGHT, tone)


def steel(x, y, z):
    if y % 5 == 0:
        return STEEL_LIGHT
    return mix(STEEL_DARK, STEEL, 0.7)


def build_dialga_leg(body, name, x, z, hind):
    leg = body.child(name, pivot=(x, 3.0, z))
    radius = 3.5
    if hind:
        radius = 4.5
    leg.ellipsoid((0, 3, 0), (radius, 7, radius), dialga_hide, power=2.6)
    leg.box(-3, 7, -3, 3, 16, 3, dialga_hide)
    leg.ellipsoid((0, 18, -2), (4.5, 3, 6), dialga_hide, power=3.0)
    # 무릎 앞의 은빛 방패와 발등의 청록 선.
    leg.box(-3, 5, -4, 3, 8, -3, steel)
    leg.box(-1, 8, -4, 1, 16, -3, CYAN)
    for toe in (-3, 0, 3):
        leg.box(toe - 1, 18, -9, toe + 1, 21, -5, STEEL_LIGHT)
    return leg


def build_dialga():
    body = Part("body", pivot=(0.0, 0.0, 0.0))
    body.ellipsoid((0, 0, 1), (10, 10, 18), dialga_hide, power=2.5)
    body.ellipsoid((0, -2, -11), (11, 11, 10), dialga_hide, power=2.5)
    body.paint(lambda x, y, z: y > 5 and abs(x + 0.5) < 6, BLUE_DARK)
    for side in (-1, 1):
        # 어깨에서 옆구리까지 이어지는 금속 띠와 청록색 홈.
        body.line((side * 9, -5, -10), (side * 10, 3, -2), 1.8, steel)
        body.line((side * 10, 3, -2), (side * 8, 4, 10), 1.2, CYAN)

    chest = body.child("chest_gem", pivot=(0.1, -1.1, -19.1))
    for y in range(-7, 8):
        half = max(1, 7 - abs(y))
        chest.box(-half, y, -1, half, y + 1, 2, STEEL_DARK)
        if abs(y) < 6:
            inner = max(1, 5 - abs(y))
            chest.box(-inner, y, -2, inner, y + 1, -1, CYAN)
            chest.paint(lambda x, py, z: py == y and x < 0 and z == -2, CYAN_CORE)

    # 등 위에 부채처럼 펼쳐진 여섯 장의 은빛 칼날.
    back = body.child("back_plate", pivot=(0.1, -8.1, 2.1))
    for side in (-1, 1):
        for index in range(3):
            z = -5 + index * 6
            height = 15 - index * 3
            for step in range(height):
                x = side * (3 + step // 2)
                back.box(min(0, x), -step - 1, z, max(0, x) + 1, -step, z + 2, steel)
            back.line((side * 3, 0, z - 0.5), (side * (3 + height // 2), -height + 2, z - 0.5), 0.8, CYAN)

    neck = body.child("neck", pivot=(0.05, -7.0, -13.1))
    neck.line((0, 0, 0), (0, -16, -3), 5.5, dialga_hide)
    for level in range(3):
        y = -4 - level * 5
        neck.box(-4, y, -9, 4, y + 2, -7, steel)
        neck.box(-1, y + 2, -8, 1, y + 5, -7, CYAN)
    for side in (-1, 1):
        neck.line((side * 5, -1, 1), (side * 8, -17, 3), 1.8, steel)

    head = neck.child("head", pivot=(-0.05, -18.0, -3.1))
    head.ellipsoid((0, -3, -2), (7, 6, 8), dialga_hide, power=2.6)
    head.ellipsoid((0, 0, -12), (4.5, 3.5, 9), dialga_hide, power=3.0)
    head.box(-4, 2, -20, 4, 4, -5, steel)
    head.box(-3, 1, -21, 3, 2, -14, INK)
    # 이마 중앙 장갑에서 뒤로 뻗는 왕관 모양의 돌기.
    head.box(-2, -10, -9, 2, -7, 4, steel)
    for step in range(12):
        half = max(1, 3 - step // 5)
        head.box(-half, -10 - step, step // 2, half, -9 - step, 4 + step // 2, steel)
    for side in (-1, 1):
        head.line((side * 5, -5, 1), (side * 10, -12, 9), 1.8, steel)
        eye_x = 6
        if side < 0:
            eye_x = -7
        head.box(eye_x, -5, -8, eye_x + 1, -3, -4, INK)
        head.box(eye_x, -4, -8, eye_x + 1, -3, -5, EYE_RED)
        head.set(eye_x, -4, -8, STEEL_LIGHT)
        head.line((side * 6.5, -6, -8), (side * 7.5, -5, -3), 0.8, STEEL_LIGHT)
    head.box(-1, -5, -19, 1, -4, -9, CYAN)

    build_dialga_leg(body, "front_upper_left", 8.1, -11.1, False)
    build_dialga_leg(body, "front_upper_right", -8.1, -11.1, False)
    build_dialga_leg(body, "hind_upper_left", 8.1, 11.1, True)
    build_dialga_leg(body, "hind_upper_right", -8.1, 11.1, True)

    tail_base = body.child("tail_base", pivot=(0.1, 0.1, 16.1))
    tail_base.ellipsoid((0, 0, 7), (5, 4.5, 10), dialga_hide, power=2.5)
    tail_base.box(-1, -5, 1, 1, -4, 14, CYAN)
    tail_mid = tail_base.child("tail_mid", pivot=(-0.1, 0.1, 14.1))
    tail_mid.ellipsoid((0, 0, 6), (3.5, 3, 8), dialga_hide, power=2.5)
    tail_mid.box(-1, -3, 1, 1, -2, 12, CYAN)
    tail_tip = tail_mid.child("tail_tip", pivot=(0.1, 0.1, 12.1))
    for step in range(15):
        half = max(1, 7 - abs(step - 5))
        tail_tip.box(-half, -1, step, half, 1, step + 1, steel)
        tail_tip.box(-1, -2, step, 1, -1, step + 1, CYAN)
    return Model("dialga_pet", body, 1.0, pixels_per_voxel=3, jitter=0.02)


# ---- 커비: 작은 복셀로 만든 구체와 선명한 얼굴 ----------------------------

PINK_LIGHT = rgb("#FFC1D7")
PINK_DARK = rgb("#DF5C8D")
CHEEK = rgb("#EC547F")
RED = rgb("#CA2457")
RED_LIGHT = rgb("#EF4774")
EYE_BLUE = rgb("#426CC1")
WHITE = rgb("#FFF9F5")


def kirby_skin(x, y, z):
    light = max(0.0, min(1.0, (-y + 18) / 36))
    return mix(PINK_DARK, PINK_LIGHT, 0.25 + light * 0.65)


def kirby_shoe(x, y, z):
    return mix(RED, RED_LIGHT, max(0.0, min(1.0, (2 - y) / 7)))


def build_kirby():
    kirby = Part("kirby", pivot=(0.0, 13.0, 0.0))
    body = kirby.child("body")
    body.ellipsoid((0, 0, 0), (18, 18, 17), kirby_skin)
    # 눈과 볼은 구면의 앞면을 따라 칠해 정면·사선에서 모두 이어지게 한다.
    for eye_center in (-5, 5):
        for x in range(eye_center - 3, eye_center + 3):
            for y in range(-9, 3):
                distance = ((x + 0.5 - eye_center) / 2.5) ** 2 + ((y + 0.5 + 3) / 6) ** 2
                if distance > 1:
                    continue
                color = INK
                if y >= -1:
                    color = EYE_BLUE
                if y <= -5 and eye_center - 1 <= x <= eye_center:
                    color = WHITE
                body.paint_front(x, y, color)
        cheek_x = eye_center * 2
        for x in range(cheek_x - 3, cheek_x + 3):
            for y in range(1, 4):
                body.paint_front(x, y, CHEEK)
    for y in range(4, 9):
        half = 3
        if y == 8:
            half = 2
        for x in range(-half, half):
            color = rgb("#762546")
            if y >= 7:
                color = rgb("#FFB3CA")
            body.paint_front(x, y, color)

    hand = kirby.child("hand_left", pivot=(8.0, 1.0, 0.0), rot=(0.0, 0.0, -0.3))
    hand.ellipsoid((4, 0, 0), (7, 6, 5.5), kirby_skin)
    kirby.children.append(hand.mirrored_copy("hand_right"))
    foot = kirby.child("foot_left", pivot=(4.2, 8.5, -1.0))
    foot.ellipsoid((0, 0, -3), (8, 5, 11), kirby_shoe, power=2.5)
    foot.paint(lambda x, y, z: y >= 3, RED)
    kirby.children.append(foot.mirrored_copy("foot_right"))
    return Model("kirby_pet", kirby, 0.5, pixels_per_voxel=4, jitter=0.012, bevel=0.65)


# ---- 유니콘: 진주빛 털, 층진 갈기, 나선 뿔 -------------------------------

IVORY = rgb("#F4EEE3")
IVORY_SHADE = rgb("#C9CFD8")
MANE = rgb("#C9B586")
MANE_LIGHT = rgb("#FFF6DA")
GOLD = rgb("#C69749")
GOLD_LIGHT = rgb("#F6D88D")
HOOF = rgb("#8A7B91")
VIOLET = rgb("#8863BE")


def unicorn_coat(x, y, z):
    return mix(IVORY_SHADE, IVORY, 0.7 + math.sin(z * 0.23) * 0.08 - y * 0.006)


def mane_color(x, y, z):
    if (x + z) % 4 == 0:
        return MANE_LIGHT
    if (x + z) % 4 == 1:
        return MANE
    return rgb("#E7D8AE")


def build_unicorn_leg(parent, name, lower_name, x, z):
    upper = parent.child(name, pivot=(x, 6.0, z))
    upper.ellipsoid((0, 4, 0), (3.5, 9, 4), unicorn_coat, power=2.5)
    upper.box(-2, 8, -2, 2, 15, 2, unicorn_coat)
    lower = upper.child(lower_name, pivot=(0.05, 14.0, 0.1))
    lower.ellipsoid((0, 1, 0), (2.8, 3, 2.8), unicorn_coat, power=2.5)
    lower.box(-2, 1, -2, 2, 14, 2, unicorn_coat)
    lower.ellipsoid((0, 12, 0), (3.5, 3, 3.5), mane_color, power=2.5)
    lower.box(-3, 14, -4, 3, 17, 3, HOOF)
    lower.box(-3, 13, -4, 3, 14, 3, GOLD)
    lower.box(-2, 14, -5, 2, 16, -4, GOLD_LIGHT)
    return upper


def build_mane_lock(parent, name, pivot, length, sweep):
    hair = parent.child(name, pivot=pivot)
    for strand in range(4):
        x = -2.0 + strand * 1.2
        end_y = length - abs(strand - 1) * 2
        hair.line((x, 0, 0), (x + 1, end_y * 0.55, sweep), 1.8, mane_color)
        hair.line((x + 1, end_y * 0.55, sweep), (x, end_y, sweep - 2), 1.2, mane_color)
    return hair


def build_unicorn():
    unicorn = Part("unicorn")
    body = unicorn.child("body", pivot=(0.0, -12.0, 0.0))
    body.ellipsoid((0, 0, 0), (9, 10, 18), unicorn_coat, power=2.4)
    chest = body.child("chest", pivot=(0.1, -1.0, -11.1))
    chest.ellipsoid((0, -2, -3), (8.5, 11, 9), unicorn_coat, power=2.4)
    hips = body.child("hips", pivot=(-0.1, -1.0, 12.1))
    hips.ellipsoid((0, -1, 0), (9, 9, 8), unicorn_coat, power=2.4)
    neck = chest.child("neck", pivot=(-0.05, -7.0, -6.1))
    neck.line((0, 1, 0), (0, -10, -6), 5.5, unicorn_coat)
    neck_upper = neck.child("neck_upper", pivot=(0.05, -10.0, -6.1))
    neck_upper.ellipsoid((0, -2, 0), (4.5, 7, 5), unicorn_coat)
    head = neck_upper.child("head", pivot=(-0.05, -5.0, -2.1))
    head.ellipsoid((0, -2, -3), (5.5, 6, 8), unicorn_coat, power=2.4)
    head.ellipsoid((0, 1, -11), (3.5, 3.5, 7), unicorn_coat, power=2.8)
    head.paint(lambda x, y, z: z <= -15 and y >= 0, rgb("#D7BCC4"))
    for side in (-1, 1):
        eye_x = 5
        if side < 0:
            eye_x = -6
        head.box(eye_x, -4, -7, eye_x + 1, -1, -4, INK)
        head.set(eye_x, -2, -6, VIOLET)
        head.set(eye_x, -4, -7, WHITE)
        head.set(side * 3, 0, -16, HOOF)
    ear = head.child("right_ear", pivot=(3.7, -5.1, 1.1), rot=(0.0, 0.0, 0.2))
    ear.ellipsoid((0, -4, 0), (2.5, 5.5, 2), unicorn_coat)
    ear.paint(lambda x, y, z: z < 0 and -7 <= y <= -2 and abs(x + 0.5) < 1, rgb("#D7BCC4"))
    head.children.append(ear.mirrored_copy("left_ear"))

    horn = head.child("horn", pivot=(0.1, -7.1, -6.1))
    for step in range(19):
        radius = max(0.65, 2.6 - step * 0.115)
        center_z = -step * 0.24
        horn.ellipsoid((0, -step, center_z), (radius, 1.2, radius), GOLD_LIGHT)
        angle = step * 0.75
        x = math.floor(math.cos(angle) * (radius - 0.4))
        z = math.floor(center_z + math.sin(angle) * (radius - 0.4))
        horn.set(x, -step, z, GOLD)

    build_mane_lock(neck, "hair", (5.1, -8.1, 0.1), 18, 6)
    build_mane_lock(neck_upper, "hair2", (3.6, -5.1, 3.1), 16, 7)
    build_mane_lock(head, "hair3", (0.1, -6.1, 4.1), 12, 5)
    build_mane_lock(head, "hair4", (0.1, -7.1, -4.1), 8, -4)
    build_unicorn_leg(chest, "right_arm", "lower_arm_right", 6.1, -2.1)
    build_unicorn_leg(chest, "left_arm", "lower_arm_left", -6.1, -2.1)
    build_unicorn_leg(hips, "right_leg", "lower_leg_right", 6.1, 1.1)
    build_unicorn_leg(hips, "left_leg", "lower_leg_left", -6.1, 1.1)

    tail = hips.child("tail", pivot=(0.1, -3.1, 5.1))
    tail.line((0, 0, 0), (0, 10, 5), 3, mane_color)
    for name, pivot, length, radius in (
            ("tail_2", (0.1, 9.0, 5.1), 10, 3.5),
            ("tail_3", (-0.1, 9.0, 2.1), 9, 3.5),
            ("tail_4", (0.1, 8.0, 0.1), 7, 3),
            ("tail_5", (-0.1, 6.0, -0.1), 5, 2)):
        tail = tail.child(name, pivot=pivot)
        tail.ellipsoid((0, length / 2, 0), (radius, length / 2 + 1, radius), mane_color, power=2.6)
    return Model("unicorn_pet", unicorn, 1.0, pixels_per_voxel=3, jitter=0.018)


# ---- 가젤: 황갈색 등, 흰 배, 얼굴 줄무늬, 휘어진 고리뿔 --------------------

TAN = rgb("#BD7D3D")
TAN_LIGHT = rgb("#DEA661")
CREAM = rgb("#F7E8CA")
STRIPE = rgb("#513923")
HORN = rgb("#393238")
HORN_RING = rgb("#716153")


def gazelle_coat(x, y, z):
    if y >= 2:
        return CREAM
    if y >= 0 and abs(x + 0.5) > 5:
        return STRIPE
    return mix(TAN, TAN_LIGHT, 0.35 - y * 0.035)


def gazelle_face(x, y, z):
    if abs(x + 0.5) < 1.5 and z < 0:
        return CREAM
    return mix(TAN, TAN_LIGHT, 0.5)


def build_gazelle():
    gazelle = Part("gazelle", pivot=(0.0, 15.0, 0.0))
    gazelle.ellipsoid((0, 0, 1), (7, 7.5, 13), gazelle_coat, power=2.4)
    gazelle.ellipsoid((0, -7, -9), (4.5, 9, 5), gazelle_coat, power=2.4)
    head = gazelle.child("head", pivot=(0.05, -5.0, -5.1))
    head.ellipsoid((0, -3, -1), (6.5, 8, 7), gazelle_face, power=2.4)
    head.ellipsoid((0, 1, -8), (4.5, 3.5, 5), gazelle_face, power=2.8)
    head.box(-2, 0, -13, 2, 2, -12, HORN)
    head.box(-1, 2, -13, 1, 4, -12, STRIPE)
    for side in (-1, 1):
        for y in range(-6, 2):
            x = side * (3 + (y + 6) // 4)
            if side < 0:
                x -= 1
            head.paint_front(x, y, STRIPE)
        eye_x = 5
        if side < 0:
            eye_x = -6
        for y in range(-6, -2):
            head.paint_front(eye_x, y, INK, push=1)
            head.paint_front(eye_x - side, y, INK)
        head.paint_front(eye_x, -6, WHITE)
        head.paint_front(eye_x, -3, rgb("#AA753B"))

    ear = head.child("rightEar", pivot=(2.5, -3.0, 0.1), rot=(0.0, 0.0, -0.25))
    ear.ellipsoid((4, -1, 0), (6, 3, 2), TAN_LIGHT)
    ear.paint(lambda x, y, z: z < 0 and x > 1 and abs(y + 0.5) < 1.5, rgb("#B78770"))
    left_ear = ear.mirrored_copy("leftEar")
    head.children.append(left_ear)
    for name, side in (("rightHorn", 1), ("leftHorn", -1)):
        horn = head.child(name, pivot=(side * 1.8, -4.0, 0.1))
        for step in range(16):
            radius = max(0.65, 1.8 - step * 0.07)
            curve_x = side * math.sin(step / 15 * math.pi) * 2
            curve_z = math.sin(step / 15 * math.pi) * 3
            tone = HORN
            if step % 3 == 0:
                tone = HORN_RING
            horn.ellipsoid((curve_x, -step, curve_z), (radius, 1.3, radius), tone)

    for name, x, z in (
            ("rightFronLeg", 2.4, -4.5), ("leftFronLeg", -2.4, -4.5),
            ("rightBackLeg", 2.4, 4.5), ("leftBackLeg", -2.4, 4.5)):
        leg = gazelle.child(name, pivot=(x, 2.5, z))
        leg.ellipsoid((0, 1, 0), (3, 5, 3.5), TAN_LIGHT, power=2.5)
        leg.box(-1, 3, -1, 1, 11, 1, CREAM)
        leg.box(-2, 5, -2, 2, 7, 2, TAN)
        leg.box(-2, 10, -3, 2, 13, 2, HORN)
        leg.box(0, 11, -4, 1, 13, -3, HORN_RING)
    tail = gazelle.child("tail", pivot=(0.05, -1.1, 6.1))
    tail.line((0, 0, 0), (0, 3, 6), 1.5, CREAM)
    tail.ellipsoid((0, 4, 7), (2.5, 3, 3), STRIPE)
    return Model("gazelle_pet", gazelle, 0.5, pixels_per_voxel=4, jitter=0.025)
