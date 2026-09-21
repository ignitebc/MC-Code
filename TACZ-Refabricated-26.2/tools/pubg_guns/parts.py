"""총기 부품 모양을 큐브 묶음으로 만드는 함수들.

좌표는 Bedrock 모델 공간 그대로다. 총구가 -z, 개머리판이 +z, 위가 +y, 좌우가 x 다.
x 축 회전 각도는 양수일 때 아래쪽이 뒤(+z)로 간다. 권총 손잡이는 양수, 앞으로 휜 탄창은 음수다.
"""

import math


def tube(m, bone, y, z0, z1, r, material, style="", detail=False):
    """z 축을 따라 놓인 팔각 단면 원통. y 는 중심 높이."""
    z0, z1 = min(z0, z1), max(z0, z1)
    flat = r * 0.8284
    pivot = (0, y, (z0 + z1) / 2)
    m.add(bone, -r, y - flat / 2, z0, r * 2, flat, z1 - z0, material, style=style, detail=detail)
    m.add(bone, -flat / 2, y - r, z0, flat, r * 2, z1 - z0, material, style=style, detail=detail)
    if r >= 0.3:
        for angle in (45, -45):
            m.add(bone, -r, y - flat / 2, z0, r * 2, flat, z1 - z0, material, style=style,
                  rot=(0, 0, angle), pivot=pivot, detail=True)


def side_tube(m, bone, x, y, z0, z1, r, material, detail=True):
    """중심이 x 축에서 벗어난 가는 원통(가스관, 관형 탄창, 조준경 노브 등)."""
    z0, z1 = min(z0, z1), max(z0, z1)
    flat = r * 0.8284
    m.add(bone, x - r, y - flat / 2, z0, r * 2, flat, z1 - z0, material, detail=detail)
    m.add(bone, x - flat / 2, y - r, z0, flat, r * 2, z1 - z0, material, detail=detail)


def chamfer(m, bone, w, y, h, z, d, material, c=0.25, style="", detail=False, **kw):
    """모서리를 깎은 상자. 단면이 팔각에 가까워 둥근 몸통처럼 보인다."""
    c = min(c, w / 2 - 0.05, h / 2 - 0.05)
    m.cbox(bone, w, y + c, h - 2 * c, z, d, material, style=style, detail=detail, **kw)
    m.cbox(bone, w - 2 * c, y, h, z, d, material, style=style, detail=True, **kw)


def rail(m, bone, y, z0, z1, w=1.5, material="metal_dark"):
    """피카티니 레일. y 는 레일 바닥 높이, 윗면은 y + 0.5."""
    z0, z1 = min(z0, z1), max(z0, z1)
    m.cbox(bone, w * 0.72, y, 0.22, z0, z1 - z0, material)
    m.cbox(bone, w, y + 0.2, 0.12, z0, z1 - z0, material, detail=True)
    pitch, tooth = 0.8, 0.44
    count = int((z1 - z0) / pitch)
    offset = ((z1 - z0) - (count * pitch - (pitch - tooth))) / 2
    for index in range(count):
        m.cbox(bone, w, y + 0.3, 0.2, z0 + offset + index * pitch, tooth, material, detail=True)
    return y + 0.5


def side_rails(m, bone, x, y, z0, z1, material="metal_dark"):
    """핸드가드 좌우 측면 레일."""
    pitch, tooth = 0.8, 0.44
    count = int((z1 - z0) / pitch)
    for index in range(count):
        m.pair(bone, x, 0.18, y, 0.9, z0 + index * pitch, tooth, material, detail=True)
    m.pair(bone, x - 0.05, 0.1, y + 0.1, 0.7, z0, z1 - z0, material, detail=True)


def bottom_rail(m, bone, y_top, z0, z1, w=1.4, material="metal_dark"):
    """핸드가드 밑면 레일. 이가 아래를 향한다. 손잡이 부착물이 닿는 밑면 높이를 돌려준다."""
    z0, z1 = min(z0, z1), max(z0, z1)
    m.cbox(bone, w * 0.72, y_top - 0.22, 0.22, z0, z1 - z0, material)
    m.cbox(bone, w, y_top - 0.32, 0.12, z0, z1 - z0, material, detail=True)
    pitch, tooth = 0.8, 0.44
    count = int((z1 - z0) / pitch)
    offset = ((z1 - z0) - (count * pitch - (pitch - tooth))) / 2
    for index in range(count):
        m.cbox(bone, w, y_top - 0.5, 0.2, z0 + offset + index * pitch, tooth, material, detail=True)
    return y_top - 0.5


def side_rail_stub(m, bone, x_surface, y, z, length=3.2, material="metal_dark"):
    """레이저를 붙이는 +x 쪽 옆면의 짧은 레일. 레이저가 닿는 바깥면 x 를 돌려준다."""
    m.add(bone, x_surface - 0.05, y - 0.36, z - length / 2, 0.2, 0.72, length, material)
    pitch, tooth = 0.8, 0.44
    for index in range(int(length / pitch)):
        m.add(bone, x_surface + 0.15, y - 0.5, z - length / 2 + 0.18 + index * pitch, 0.2, 1.0, tooth, material, detail=True)
    return x_surface + 0.35


def ar_stock_adapter(m, y_axis, z_rear, z_stock, block_height=2.4, material="metal"):
    """AR 규격 개머리판 부착물을 달 때만 보이는 연결부: 총몸 뒤 받침과 완충관.

    개머리판 부착물은 stock_pos(z_stock)에서 뒤로 뻗으며 완충관을 감싼다.
    """
    parent = m.bones[m.body_bone].get("parent")
    m.add_bone("attachment_adapter", parent)
    m.add_bone("ar_stock_adapter", "attachment_adapter")
    bone = "ar_stock_adapter"
    m.cbox(bone, 1.5, y_axis - block_height / 2, block_height, z_rear - 0.1, 1.0, "metal")
    tube(m, bone, y_axis, z_rear + 0.9, z_stock + 6.4, 0.56, material)
    tube(m, bone, y_axis, z_stock - 0.5, z_stock, 0.72, "metal", detail=True)       # 고정 너트
    return y_axis, z_stock


def slats(m, bone, w, y, h, z0, z1, material, gap=0.9, bar=0.7, inner="void"):
    """방열 구멍이 뚫린 덮개. 안쪽 어두운 심 위에 세로 살을 간격을 두고 세운다."""
    m.cbox(bone, w - 0.3, y + 0.15, h - 0.3, z0, z1 - z0, inner)
    m.cbox(bone, w, y, 0.35, z0, z1 - z0, material)
    m.cbox(bone, w, y + h - 0.35, 0.35, z0, z1 - z0, material)
    position = z0
    while position + bar <= z1 + 1e-6:
        m.cbox(bone, w, y + 0.35, h - 0.7, position, bar, material, detail=True)
        position += bar + gap


def grip(m, bone, z_front, y_top, height=5.0, depth=2.3, w=1.6, angle=17, material="poly", style="checker"):
    """권총 손잡이. (z_front, y_top) 은 손잡이 앞쪽 윗모서리."""
    pivot = (0, y_top, z_front + depth / 2)
    m.cbox(bone, w, y_top - height, height + 0.4, z_front, depth, material, style=style, rot=(angle, 0, 0), pivot=pivot)
    m.cbox(bone, w - 0.4, y_top - height + 0.2, height, z_front - 0.2, depth + 0.4, material,
           rot=(angle, 0, 0), pivot=pivot, detail=True)
    m.cbox(bone, w + 0.12, y_top - height - 0.25, 0.4, z_front - 0.1, depth + 0.25, material,
           rot=(angle, 0, 0), pivot=pivot, detail=True)
    # 손잡이 뒤쪽 위의 턱
    m.cbox(bone, w - 0.2, y_top - 0.9, 1.1, z_front + depth - 0.2, 0.9, material, detail=True)


def trigger_group(m, bone, z, y_top, length=4.2, drop=2.4, material="metal_dark"):
    """방아쇠울과 방아쇠. z 는 방아쇠울 앞끝, y_top 은 총몸 밑면."""
    m.cbox(bone, 0.5, y_top - drop, 0.28, z, length, material)
    m.cbox(bone, 0.5, y_top - drop, drop, z, 0.28, material, detail=True)
    m.cbox(bone, 0.5, y_top - drop + 0.2, 0.3, z + 0.2, 0.5, material, rot=(40, 0, 0),
           pivot=(0, y_top - drop + 0.3, z + 0.3), detail=True)
    trigger_z = z + length * 0.55
    m.cbox(bone, 0.32, y_top - drop * 0.72, drop * 0.72, trigger_z, 0.34, "steel", rot=(-14, 0, 0),
           pivot=(0, y_top, trigger_z), detail=True)
    m.cbox(bone, 0.32, y_top - drop * 0.78, 0.3, trigger_z - 0.35, 0.5, "steel", detail=True)


def box_mag(m, bone, z0, y_top, depth, height, w=1.3, angle=0, material="metal_dark", style="hribs", plate=True):
    """곧은 상자형 탄창. angle 이 음수면 아래가 앞으로 기운다."""
    pivot = (0, y_top, z0 + depth / 2)
    rot = (angle, 0, 0) if angle else None
    kw = {"rot": rot, "pivot": pivot} if rot else {}
    m.cbox(bone, w, y_top - height, height, z0, depth, material, style=style, **kw)
    m.cbox(bone, w + 0.12, y_top - height * 0.62, 0.16, z0 - 0.04, depth + 0.08, material, detail=True, **kw)
    m.cbox(bone, w * 0.5, y_top - height + 0.3, height - 0.6, z0 - 0.08, 0.12, material, detail=True, **kw)
    if plate:
        m.cbox(bone, w + 0.2, y_top - height - 0.22, 0.3, z0 - 0.12, depth + 0.24, material, detail=True, **kw)


def curved_mag(m, bone, z0, y_top, depth, height, curve, w=1.3, segments=6, material="metal_dark", style="hribs"):
    """앞으로 휜 바나나형 탄창. curve 는 끝에서의 총 각도(도)."""
    segment = height / segments
    top_y, top_z = y_top, z0 + depth / 2
    for index in range(segments):
        angle = curve * (index + 0.5) / segments
        rad = math.radians(angle)
        pivot = (0, top_y, top_z)
        m.cbox(bone, w, top_y - segment * 1.12, segment * 1.12, top_z - depth / 2, depth, material, style=style,
               rot=(-angle, 0, 0), pivot=pivot)
        if index % 2 == 1:
            m.cbox(bone, w + 0.12, top_y - segment * 0.6, 0.16, top_z - depth / 2 - 0.04, depth + 0.08, material,
                   rot=(-angle, 0, 0), pivot=pivot, detail=True)
        top_y -= segment * math.cos(rad)
        top_z -= segment * math.sin(rad)
    angle = curve
    m.cbox(bone, w + 0.2, top_y - 0.25, 0.32, top_z - depth / 2 - 0.12, depth + 0.24, material,
           rot=(-angle, 0, 0), pivot=(0, top_y, top_z), detail=True)


def drum(m, bone, y, z, r, w, material="metal_dark"):
    """x 축 방향으로 눕힌 드럼 탄창(옆에서 보면 팔각 원판)."""
    flat = r * 0.8284
    pivot = (0, y, z)
    for angle in (0, 45, 90, 135):
        m.add(bone, -w / 2, y - flat / 2, z - r, w, flat, r * 2, material, style="flat",
              rot=(angle, 0, 0), pivot=pivot, detail=angle in (45, 135))
    # 옆면 가운데의 축과 보강 고리
    for ring, thick in ((0.62, 0.1), (0.3, 0.2)):
        small = r * ring * 0.8284
        for angle in (0, 45, 90, 135):
            m.add(bone, -w / 2 - thick, y - small / 2, z - r * ring, w + thick * 2, small, r * ring * 2,
                  "steel" if ring < 0.5 else material, style="flat", rot=(angle, 0, 0), pivot=pivot, detail=True)


def front_sight(m, bone, z, y_base, height=1.6, hood=True, material="metal_dark"):
    """가늠쇠. 기둥과 좌우 보호 귀."""
    m.cbox(bone, 0.9, y_base, 0.4, z - 0.4, 1.0, material)
    m.cbox(bone, 0.16, y_base + 0.4, height - 0.4, z, 0.2, material, detail=True)
    if hood:
        m.pair(bone, 0.32, 0.14, y_base + 0.3, height, z - 0.25, 0.7, material, detail=True)
    return y_base + height


def rear_sight(m, bone, z, y_base, height=1.0, material="metal_dark", length=1.4):
    """가늠자. 좌우 기둥 사이가 트인 노치형."""
    m.cbox(bone, 1.0, y_base, 0.35, z, length, material)
    m.pair(bone, 0.12, 0.3, y_base + 0.3, height, z + length - 0.4, 0.3, material, detail=True)
    m.cbox(bone, 0.9, y_base + 0.3, height * 0.45, z + length - 0.4, 0.3, material, detail=True)
    return y_base + 0.3 + height


def pins(m, bone, x, points, r=0.22, material="steel"):
    """총몸 옆면의 핀·나사 머리. points 는 (y, z) 목록."""
    for y, z in points:
        m.pair(bone, x, 0.06, y - r, r * 2, z - r, r * 2, material, detail=True)


def muzzle_brake(m, bone, y, z_tip, length, r, material="metal_dark", slots=3):
    """소염기·제퇴기. z_tip 은 총구 끝(가장 작은 z)."""
    tube(m, bone, y, z_tip, z_tip + length, r, material)
    m.cbox(bone, r * 0.9, y - r * 0.45, r * 0.9, z_tip - 0.02, 0.1, "void", detail=True)
    step = length / (slots + 1)
    for index in range(slots):
        z = z_tip + step * (index + 0.6)
        m.pair(bone, r - 0.03, 0.08, y - r * 0.3, r * 0.6, z, step * 0.45, "void", detail=True)
        m.cbox(bone, r * 0.6, y + r - 0.03, 0.08, z, step * 0.45, "void", detail=True)


def bipod(m, bone, z, y_top, length=7.0, spread=16, material="metal_dark", folded=False):
    """양각대. folded 면 총열 밑에 접힌 모양."""
    m.cbox(bone, 1.6, y_top - 0.5, 0.5, z - 0.5, 1.0, material)
    for side in (1, -1):
        if folded:
            m.add(bone, side * 0.55 - 0.18, y_top - 0.95, z - length, 0.36, 0.36, length, material, detail=True)
            m.add(bone, side * 0.55 - 0.26, y_top - 1.05, z - length - 0.3, 0.52, 0.52, 0.5, "rubber", detail=True)
        else:
            pivot = (side * 0.6, y_top - 0.3, z)
            m.add(bone, side * 0.6 - 0.18, y_top - 0.3 - length, z - 0.18, 0.36, length, 0.36, material,
                  rot=(-12, 0, side * spread), pivot=pivot)
            m.add(bone, side * 0.6 - 0.3, y_top - 0.5 - length, z - 0.4, 0.6, 0.3, 0.9, "rubber",
                  rot=(-12, 0, side * spread), pivot=pivot, detail=True)


def wedge(m, bone, w, z0, z1, top0, top1, bottom0, bottom1, material, slice_length=0.6, c=0.3):
    """옆에서 본 윤곽이 사다리꼴인 덩어리. 얇은 조각을 이어 붙여 경사를 만든다.

    조각 옆면은 'flat' 으로 칠해 조각 사이 경계가 보이지 않게 한다.
    """
    count = max(2, int(round((z1 - z0) / slice_length)))
    length = (z1 - z0) / count
    for index in range(count):
        t = (index + 0.5) / count
        top = top0 + (top1 - top0) * t
        bottom = bottom0 + (bottom1 - bottom0) * t
        z = z0 + index * length
        m.cbox(bone, w, bottom + c, top - bottom - 2 * c, z, length + 0.02, material, style="flat", detail=index % 2 == 1)
        m.cbox(bone, w - 2 * c, bottom, top - bottom, z, length + 0.02, material, style="flat", detail=True)


def wood_stock(m, bone, z0, z1, y_top, h_front, h_back, drop, w=1.7, material="wood", butt="rubber"):
    """뒤로 갈수록 넓어지며 내려가는 개머리판."""
    wedge(m, bone, w, z0, z1, y_top, y_top - drop, y_top - h_front, y_top - drop - h_back, material)
    m.cbox(bone, w + 0.2, y_top - drop - h_back - 0.1, h_back + 0.2, z1, 0.55, butt, style="hribs")


def skeleton_stock(m, bone, z0, z1, y_top, height, w=1.5, material="poly", butt="rubber"):
    """가운데가 뚫린 개머리판(드라구노프·VSS 형)."""
    length = z1 - z0
    chamfer(m, bone, w, y_top - 1.1, 1.1, z0, length, material, c=0.2)
    m.cbox(bone, w, y_top - height, 1.0, z0 + length * 0.35, length * 0.65, material)
    m.cbox(bone, w, y_top - height * 0.62, height * 0.5, z0 + 0.4, 1.4, material, rot=(38, 0, 0),
           pivot=(0, y_top - 1.0, z0 + 1.0))
    m.cbox(bone, w, y_top - height - 0.6, height * 0.75, z0 + length * 0.05, 1.3, material, rot=(52, 0, 0),
           pivot=(0, y_top - 1.0, z0 + 0.6))
    m.cbox(bone, w + 0.15, y_top - height, height, z1 - 1.3, 1.3, material)
    m.cbox(bone, w + 0.3, y_top - height - 0.1, height + 0.2, z1, 0.5, butt, style="hribs")
    # 볼받침
    m.cbox(bone, w + 0.2, y_top, 0.5, z0 + length * 0.3, length * 0.45, material, detail=True)


def tube_stock(m, bone, z0, z1, y, material="metal_dark", butt="poly", plate_h=4.2):
    """관 두 개로 된 접이식 개머리판(베릴 형)."""
    side_tube(m, bone, 0, y + 0.6, z0, z1, 0.34, material, detail=False)
    m.cbox(bone, 0.5, y - 2.4, 0.5, z0 + 1.0, z1 - z0 - 1.0, material, rot=(-9, 0, 0), pivot=(0, y - 2.2, z1))
    m.cbox(bone, 1.2, y - 0.2, 1.4, z0 - 0.2, 1.2, material)
    m.cbox(bone, 1.5, y + 1.0 - plate_h, plate_h, z1 - 0.2, 0.9, butt, style="hribs")
    m.cbox(bone, 1.3, y + 0.9, 0.6, z0 + (z1 - z0) * 0.35, (z1 - z0) * 0.4, butt, detail=True)


def poly_stock(m, bone, z0, z1, y_top, height, w=1.6, material="poly", butt="rubber", cutout=True):
    """합성수지 고정 개머리판. cutout 이면 가운데를 비운다."""
    length = z1 - z0
    chamfer(m, bone, w, y_top - 1.4, 1.4, z0, length, material, c=0.3)
    if cutout:
        m.cbox(bone, w * 0.9, y_top - height, 1.0, z0 + length * 0.4, length * 0.6, material)
        m.cbox(bone, w * 0.9, y_top - height * 0.8, height * 0.7, z0 + length * 0.08, 1.2, material,
               rot=(48, 0, 0), pivot=(0, y_top - 1.2, z0 + 0.5))
        m.cbox(bone, w * 0.6, y_top - height + 0.9, height - 2.2, z0 + length * 0.45, length * 0.5, "void", detail=True)
    else:
        wedge(m, bone, w, z0, z1 - 1.0, y_top - 1.2, y_top - 1.2, y_top - 2.6, y_top - height, material)
    m.cbox(bone, w + 0.1, y_top - height, height, z1 - 1.2, 1.2, material, style="panel")
    m.cbox(bone, w + 0.25, y_top - height - 0.1, height + 0.2, z1, 0.5, butt, style="hribs")


def scope(m, bone, y, z0, z1, r=0.75, material="metal_dark", mount_y=None):
    """일체형 조준경(VSS·Win94). y 는 경통 중심 높이."""
    length = z1 - z0
    tube(m, bone, y, z0 + length * 0.22, z1 - length * 0.22, r * 0.72, material)
    tube(m, bone, y, z0, z0 + length * 0.26, r, material)
    tube(m, bone, y, z1 - length * 0.24, z1, r * 0.92, material)
    m.cbox(bone, r * 1.3, y - r * 0.65, r * 1.3, z0 - 0.03, 0.1, "glass", detail=True)
    m.cbox(bone, r * 1.2, y - r * 0.6, r * 1.2, z1 - 0.07, 0.1, "glass", detail=True)
    m.cbox(bone, 0.6, y + r * 0.6, 0.6, (z0 + z1) / 2 - 0.3, 0.6, material, detail=True)
    m.add(bone, r * 0.6, y - 0.3, (z0 + z1) / 2 - 0.3, 0.6, 0.6, 0.6, material, detail=True)
    if mount_y is not None:
        for z in (z0 + length * 0.32, z1 - length * 0.38):
            m.cbox(bone, 1.0, mount_y, y - mount_y, z, 0.8, material)
            m.cbox(bone, r * 1.7, y - r * 0.85, r * 1.7, z + 0.1, 0.6, material, detail=True)


def cartridge(m, bone, bbox, case="brass", tip="copper"):
    """베이스 모델의 탄약 큐브 자리에 같은 크기의 탄약을 놓는다."""
    if bbox is None:
        return
    (x0, y0, z0), (x1, y1, z1) = bbox
    length = z1 - z0
    m.add(bone, x0, y0, z0 + length * 0.32, x1 - x0, y1 - y0, length * 0.68, case)
    m.add(bone, x0 + (x1 - x0) * 0.15, y0 + (y1 - y0) * 0.15, z0, (x1 - x0) * 0.7, (y1 - y0) * 0.7, length * 0.34, tip)
