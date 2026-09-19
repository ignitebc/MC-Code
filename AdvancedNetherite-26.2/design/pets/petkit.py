"""복셀로 조각한 펫을 마인크래프트 엔티티 모델로 바꾸는 공용 도구.

펫 하나는 Part 트리이고, 각 Part 는 자기 피벗을 원점으로 하는 복셀 격자를 갖는다.
build() 가 하는 일:
  1. 복셀 덩어리를 겹치지 않는 큰 박스들로 분해한다 (박스 UV 큐브 하나 = 박스 하나).
  2. 박스들을 아틀라스에 배치하고, 면의 텍셀마다 그 자리 복셀의 색을 칠한다.
     복셀 경계 음영(베벨)과 틈새 그림자(AO)를 함께 구워 복셀 피규어 느낌을 낸다.
  3. createBodyLayer 에서 부를 Java 메시 클래스를 출력한다.
  4. 게임을 켜지 않고도 모양을 확인할 수 있게 정사영 미리보기를 그린다.

좌표계는 마인크래프트 모델 공간 그대로다 (y 가 아래 방향, 지면은 y=24, 머리는 -z 방향).
"""

from __future__ import annotations

import math
from dataclasses import dataclass, field

import numpy as np
from PIL import Image

# ModelPart.Cube 가 폴리곤에 붙이는 Direction 이름. DOWN 이 y 최솟값 면(화면상 윗면)이다.
FACE_NORMALS = {
    "DOWN": (0, -1, 0),
    "UP": (0, 1, 0),
    "WEST": (-1, 0, 0),
    "NORTH": (0, 0, -1),
    "EAST": (1, 0, 0),
    "SOUTH": (0, 0, 1),
}
# 텍스처 +u, +v 방향으로 한 텍셀 움직일 때 복셀 좌표가 움직이는 방향
FACE_AXES = {
    "DOWN": ((1, 0, 0), (0, 0, -1)),
    "UP": ((1, 0, 0), (0, 0, -1)),
    "WEST": ((0, 0, -1), (0, 1, 0)),
    "NORTH": ((1, 0, 0), (0, 1, 0)),
    "EAST": ((0, 0, 1), (0, 1, 0)),
    "SOUTH": ((-1, 0, 0), (0, 1, 0)),
}


def rgb(hex_color: str) -> tuple[int, int, int]:
    hex_color = hex_color.lstrip("#")
    return tuple(int(hex_color[i:i + 2], 16) for i in (0, 2, 4))


def mix(a, b, t: float):
    t = max(0.0, min(1.0, t))
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def shade(color, factor: float):
    return tuple(max(0, min(255, int(round(c * factor)))) for c in color)


def _hash01(x: int, y: int, z: int, salt: int) -> float:
    n = (x * 73856093) ^ (y * 19349663) ^ (z * 83492791) ^ (salt * 2654435761)
    n = (n ^ (n >> 13)) * 1274126177
    n ^= n >> 16
    return (n & 0xFFFF) / 0xFFFF


class Part:
    """모델 파트 하나. 복셀 좌표는 파트 피벗이 원점인 정수 격자다."""

    def __init__(self, name: str, pivot=(0.0, 0.0, 0.0), rot=(0.0, 0.0, 0.0)):
        self.name = name
        self.pivot = pivot  # 부모 피벗 기준, 모델 유닛
        self.rot = rot  # 라디안 (x, y, z)
        self.vox: dict[tuple[int, int, int], tuple[int, int, int]] = {}
        self.children: list[Part] = []
        self.boxes: list[Box] = []

    def child(self, name: str, pivot=(0.0, 0.0, 0.0), rot=(0.0, 0.0, 0.0)) -> "Part":
        part = Part(name, pivot, rot)
        self.children.append(part)
        return part

    def walk(self):
        yield self
        for child in self.children:
            yield from child.walk()

    # ---- 조각 도구 -------------------------------------------------------

    @staticmethod
    def _color(color, x, y, z):
        return color(x, y, z) if callable(color) else color

    def set(self, x, y, z, color):
        self.vox[(x, y, z)] = self._color(color, x, y, z)

    def box(self, x0, y0, z0, x1, y1, z1, color):
        """[x0,x1) x [y0,y1) x [z0,z1) 을 채운다."""
        for x in range(x0, x1):
            for y in range(y0, y1):
                for z in range(z0, z1):
                    self.vox[(x, y, z)] = self._color(color, x, y, z)

    def ellipsoid(self, center, radius, color, power: float = 2.0):
        """초타원체. power 가 클수록 모서리가 둥근 상자에 가까워진다."""
        cx, cy, cz = center
        rx, ry, rz = radius
        for x in range(math.floor(cx - rx), math.ceil(cx + rx)):
            for y in range(math.floor(cy - ry), math.ceil(cy + ry)):
                for z in range(math.floor(cz - rz), math.ceil(cz + rz)):
                    dx = abs((x + 0.5 - cx) / rx)
                    dy = abs((y + 0.5 - cy) / ry)
                    dz = abs((z + 0.5 - cz) / rz)
                    if dx ** power + dy ** power + dz ** power <= 1.0:
                        self.vox[(x, y, z)] = self._color(color, x, y, z)

    def line(self, start, end, radius: float, color):
        """두 점을 잇는 굵은 선. 뿔, 깃털, 날개뼈처럼 비스듬한 계단 모양에 쓴다."""
        sx, sy, sz = start
        ex, ey, ez = end
        length = math.dist(start, end)
        steps = max(1, int(length * 3))
        reach = math.ceil(radius)
        for i in range(steps + 1):
            t = i / steps
            px, py, pz = sx + (ex - sx) * t, sy + (ey - sy) * t, sz + (ez - sz) * t
            for x in range(math.floor(px) - reach, math.floor(px) + reach + 1):
                for y in range(math.floor(py) - reach, math.floor(py) + reach + 1):
                    for z in range(math.floor(pz) - reach, math.floor(pz) + reach + 1):
                        if math.dist((x + 0.5, y + 0.5, z + 0.5), (px, py, pz)) <= radius:
                            self.vox[(x, y, z)] = self._color(color, x, y, z)

    def remove(self, predicate):
        for key in [k for k in self.vox if predicate(*k)]:
            del self.vox[key]

    def paint(self, predicate, color):
        for key in list(self.vox):
            if predicate(*key):
                self.vox[key] = self._color(color, *key)

    def shell(self, predicate, color, grow_into=None):
        """predicate 를 만족하는 복셀의 바깥 이웃 자리에 한 겹을 덧씌운다 (옷, 목걸이 등)."""
        added = {}
        for (x, y, z) in list(self.vox):
            if not predicate(x, y, z):
                continue
            for dx, dy, dz in FACE_NORMALS.values():
                key = (x + dx, y + dy, z + dz)
                if key in self.vox:
                    continue
                if grow_into is not None and not grow_into(*key):
                    continue
                added[key] = self._color(color, *key)
        self.vox.update(added)

    def surface(self, axis: int, sign: int, a: int, b: int):
        """축 방향으로 봤을 때 (a, b) 칸에서 가장 바깥에 있는 복셀 좌표를 돌려준다."""
        others = [i for i in range(3) if i != axis]
        best = None
        for key in self.vox:
            if key[others[0]] == a and key[others[1]] == b:
                if best is None or (key[axis] - best[axis]) * sign > 0:
                    best = key
        return best

    def paint_front(self, x, y, color, push: int = 0):
        """(x, y) 칸의 가장 앞(-z) 복셀을 칠한다. push 만큼 앞으로 더 돌출시킬 수 있다."""
        key = self.surface(2, -1, x, y)
        if key is None:
            return
        self.vox[key] = self._color(color, *key)
        for step in range(1, push + 1):
            pushed = (key[0], key[1], key[2] - step)
            self.vox[pushed] = self._color(color, *pushed)

    def mirror_x(self):
        """x >= 0 쪽 복셀을 반대편으로 복사해 좌우 대칭을 만든다."""
        for (x, y, z), color in list(self.vox.items()):
            if x >= 0:
                self.vox[(-1 - x, y, z)] = color
        for key in [k for k in self.vox if k[0] < 0 and (-1 - k[0], k[1], k[2]) not in self.vox]:
            del self.vox[key]

    def mirrored_copy(self, name: str, pivot=None, rot=None) -> "Part":
        """x 축으로 뒤집은 사본. 좌우 한 쌍인 다리/날개/귀를 만들 때 쓴다."""
        copy = Part(
            name,
            pivot if pivot is not None else (-self.pivot[0], self.pivot[1], self.pivot[2]),
            rot if rot is not None else (self.rot[0], -self.rot[1], -self.rot[2]))
        copy.vox = {(-1 - x, y, z): color for (x, y, z), color in self.vox.items()}
        for child in self.children:
            copy.children.append(child.mirrored_copy(child.name.replace("left", "right")))
        return copy


@dataclass
class Box:
    x: int
    y: int
    z: int
    w: int
    h: int
    d: int
    faces: list[str] = field(default_factory=list)
    u: int = 0  # 아틀라스 픽셀 좌표
    v: int = 0


def _decompose(occupied: np.ndarray) -> list[tuple[int, int, int, int, int, int]]:
    """복셀 덩어리를 부피가 큰 박스부터 떼어 내며 겹치지 않는 박스들로 나눈다."""
    occupied = occupied.copy()
    size_x, size_y, size_z = occupied.shape
    sizes = sorted(
        ((w * h * d, w, h, d)
         for w in range(1, size_x + 1)
         for h in range(1, size_y + 1)
         for d in range(1, size_z + 1)),
        reverse=True)
    boxes = []
    index = 0
    while occupied.any():
        remaining = int(occupied.sum())
        table = np.zeros((size_x + 1, size_y + 1, size_z + 1), dtype=np.int32)
        table[1:, 1:, 1:] = occupied.cumsum(0).cumsum(1).cumsum(2)
        while True:
            volume, w, h, d = sizes[index]
            if volume > remaining:
                index += 1
                continue
            window = (table[w:, h:, d:] - table[:-w, h:, d:] - table[w:, :-h, d:] - table[w:, h:, :-d]
                      + table[:-w, :-h, d:] + table[:-w, h:, :-d] + table[w:, :-h, :-d]
                      - table[:-w, :-h, :-d])
            hits = np.argwhere(window == volume)
            if len(hits):
                x, y, z = (int(v) for v in hits[0])
                break
            index += 1
        occupied[x:x + w, y:y + h, z:z + d] = False
        boxes.append((x, y, z, w, h, d))
    return boxes


class Model:
    def __init__(self, name: str, root: Part, voxel: float, pixels_per_voxel: int = 4,
                 jitter: float = 0.035, bevel: float = 1.0, occlusion: float = 0.22):
        self.name = name
        self.root = root
        self.voxel = voxel  # 복셀 한 변의 모델 유닛 길이
        self.ppv = pixels_per_voxel
        self.atlas_width = 0
        self.atlas_height = 0
        self.jitter = jitter
        self.bevel = bevel
        self.occlusion = occlusion
        self.texture: Image.Image | None = None

    # ---- 1. 박스 분해 ----------------------------------------------------

    def _build_boxes(self):
        for part in self.root.walk():
            part.boxes = []
            if not part.vox:
                continue
            keys = np.array(list(part.vox))
            low = keys.min(0)
            occupied = np.zeros(keys.max(0) - low + 1, dtype=bool)
            occupied[tuple((keys - low).T)] = True
            for x, y, z, w, h, d in _decompose(occupied):
                box = Box(x + int(low[0]), y + int(low[1]), z + int(low[2]), w, h, d)
                box.faces = [face for face in FACE_NORMALS if self._face_visible(part, box, face)]
                if box.faces:
                    part.boxes.append(box)

    @staticmethod
    def _face_texels(box: Box, face: str):
        """면의 텍셀 (i, j) 마다 그 자리의 복셀 좌표를 돌려준다."""
        x0, y0, z0 = box.x, box.y, box.z
        x1, y1, z1 = x0 + box.w - 1, y0 + box.h - 1, z0 + box.d - 1
        if face in ("DOWN", "UP"):
            y = y0 if face == "DOWN" else y1
            return box.w, box.d, lambda i, j: (x0 + i, y, z1 - j)
        if face == "NORTH":
            return box.w, box.h, lambda i, j: (x0 + i, y0 + j, z0)
        if face == "SOUTH":
            return box.w, box.h, lambda i, j: (x1 - i, y0 + j, z1)
        if face == "WEST":
            return box.d, box.h, lambda i, j: (x0, y0 + j, z1 - i)
        return box.d, box.h, lambda i, j: (x1, y0 + j, z0 + i)

    def _face_origin(self, box: Box, face: str) -> tuple[int, int]:
        """박스 UV 전개도 안에서 면이 시작하는 아틀라스 픽셀 좌표."""
        p = self.ppv
        w, h, d = box.w * p, box.h * p, box.d * p
        offsets = {
            "DOWN": (d, 0), "UP": (d + w, 0),
            "WEST": (0, d), "NORTH": (d, d), "EAST": (d + w, d), "SOUTH": (d + w + d, d),
        }
        return box.u + offsets[face][0], box.v + offsets[face][1]

    def _face_visible(self, part: Part, box: Box, face: str) -> bool:
        normal = FACE_NORMALS[face]
        columns, rows, at = self._face_texels(box, face)
        for i in range(columns):
            for j in range(rows):
                x, y, z = at(i, j)
                if (x + normal[0], y + normal[1], z + normal[2]) not in part.vox:
                    return True
        return False

    # ---- 2. 아틀라스 배치와 채색 -----------------------------------------

    def _pack(self):
        """선반 채우기. 아틀라스가 정사각형에 가깝게 나오는 가장 좁은 폭을 고른다."""
        p = self.ppv
        align = max(1, int(round(p / self.voxel)))  # texOffs 가 정수가 되는 픽셀 간격
        placed = [box for part in self.root.walk() for box in part.boxes]
        placed.sort(key=lambda box: -(box.d + box.h))

        def layout(atlas_width: int) -> int:
            cursor_x = cursor_y = shelf = 0
            for box in placed:
                width = -(-(2 * (box.w + box.d) * p) // align) * align
                if cursor_x + width > atlas_width:
                    cursor_x = 0
                    cursor_y += -(-shelf // align) * align
                    shelf = 0
                box.u, box.v = cursor_x, cursor_y
                cursor_x += width
                shelf = max(shelf, (box.d + box.h) * p)
            return cursor_y + shelf

        atlas_width = align * 32
        widest = max(2 * (box.w + box.d) * p for box in placed)
        while atlas_width < widest or layout(atlas_width) > atlas_width:
            atlas_width *= 2
        needed = layout(atlas_width)
        step = align * 8
        self.atlas_width = atlas_width
        self.atlas_height = -(-needed // step) * step

    def _paint(self):
        p = self.ppv
        pixels = np.zeros((self.atlas_height, self.atlas_width, 4), dtype=np.uint8)
        for salt, part in enumerate(self.root.walk()):
            for box in part.boxes:
                for face in box.faces:  # 그리지 않는 면은 투명하게 비워 둔다
                    origin_u, origin_v = self._face_origin(box, face)
                    columns, rows, at = self._face_texels(box, face)
                    normal = FACE_NORMALS[face]
                    axis_u, axis_v = FACE_AXES[face]
                    for i in range(columns):
                        for j in range(rows):
                            voxel = at(i, j)
                            block = self._voxel_block(part, voxel, normal, axis_u, axis_v, salt)
                            top, left = origin_v + j * p, origin_u + i * p
                            pixels[top:top + p, left:left + p, :3] = block
                            pixels[top:top + p, left:left + p, 3] = 255
        self.texture = Image.fromarray(pixels, "RGBA")

    def _voxel_block(self, part: Part, voxel, normal, axis_u, axis_v, salt) -> np.ndarray:
        """복셀 한 칸이 차지하는 p x p 픽셀 블록. 베벨과 틈새 그림자를 함께 굽는다."""
        p = self.ppv
        x, y, z = voxel
        base = np.array(part.vox[voxel], dtype=np.float32)
        base *= 1.0 + (_hash01(x, y, z, salt) - 0.5) * 2.0 * self.jitter
        block = np.tile(base, (p, p, 1))
        light = np.ones((p, p), dtype=np.float32)

        if p >= 3 and self.bevel > 0:
            light[0, :] += 0.07 * self.bevel
            light[:, 0] += 0.04 * self.bevel
            light[-1, :] -= 0.09 * self.bevel
            light[:, -1] -= 0.06 * self.bevel

        def covered(du: int, dv: int) -> bool:
            key = (x + normal[0] + axis_u[0] * du + axis_v[0] * dv,
                   y + normal[1] + axis_u[1] * du + axis_v[1] * dv,
                   z + normal[2] + axis_u[2] * du + axis_v[2] * dv)
            return key in part.vox

        # 면 바로 바깥층에 이웃 복셀이 서 있으면 그쪽 가장자리가 어두워진다
        falloff = np.linspace(1.0, 0.0, p, dtype=np.float32) ** 1.5
        strength = self.occlusion
        if covered(-1, 0):
            light -= strength * falloff[None, :]
        if covered(1, 0):
            light -= strength * falloff[None, ::-1]
        if covered(0, -1):
            light -= strength * falloff[:, None]
        if covered(0, 1):
            light -= strength * falloff[::-1, None]
        for du, dv in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            if covered(du, dv) and not covered(du, 0) and not covered(0, dv):
                corner_u = falloff if du < 0 else falloff[::-1]
                corner_v = falloff if dv < 0 else falloff[::-1]
                light -= strength * 0.6 * corner_v[:, None] * corner_u[None, :]

        return np.clip(block * np.clip(light, 0.55, 1.2)[:, :, None], 0, 255)

    def build(self):
        self._build_boxes()
        self._pack()
        self._paint()
        return self

    # ---- 3. Java 출력 ----------------------------------------------------

    @property
    def declared_size(self) -> tuple[int, int]:
        scale = self.voxel / self.ppv
        return int(round(self.atlas_width * scale)), int(round(self.atlas_height * scale))

    def box_count(self) -> int:
        return sum(len(part.boxes) for part in self.root.walk())

    def java(self, class_name: str, package: str, source_note: str) -> str:
        def number(value: float) -> str:
            text = f"{value:.4f}".rstrip("0").rstrip(".")
            if text in ("-0", ""):
                text = "0"
            return text + ("F" if "." in text else ".0F")

        face_sets: dict[tuple[str, ...], str] = {}

        def face_constant(faces: list[str]) -> str:
            key = tuple(faces)
            if key not in face_sets:
                face_sets[key] = "FACES_" + "".join(face[0] for face in key)
            return face_sets[key]

        scale = self.voxel / self.ppv
        builders = []
        tree_lines = []

        def method_name(part: Part) -> str:
            pieces = part.name.split("_")
            return pieces[0] + "".join(piece.capitalize() for piece in pieces[1:]) + "Cubes"

        def emit(part: Part, parent_variable: str):
            variable = method_name(part)[:-5]
            pose = "PartPose.ZERO"
            if any(part.rot):
                pose = "PartPose.offsetAndRotation({}, {}, {}, {}, {}, {})".format(
                    *(number(v) for v in (*part.pivot, *part.rot)))
            elif any(part.pivot):
                pose = "PartPose.offset({}, {}, {})".format(*(number(v) for v in part.pivot))
            call = f'{parent_variable}.addOrReplaceChild("{part.name}", {method_name(part)}(), {pose});'
            if part.children:
                call = f"PartDefinition {variable} = " + call
            tree_lines.append("        " + call)

            lines = [f"    private static CubeListBuilder {method_name(part)}()", "    {"]
            if part.boxes:
                lines.append("        return CubeListBuilder.create()")
                for index, box in enumerate(part.boxes):
                    end = ";" if index == len(part.boxes) - 1 else ""
                    lines.append("                .texOffs({}, {}).addBox({}, {}, {}, {}, {}, {}, {}){}".format(
                        int(round(box.u * scale)), int(round(box.v * scale)),
                        *(number(v * self.voxel) for v in (box.x, box.y, box.z, box.w, box.h, box.d)),
                        face_constant(box.faces), end))
            else:
                lines.append("        return CubeListBuilder.create();")
            lines.append("    }")
            builders.append("\n".join(lines))
            for child in part.children:
                emit(child, variable)

        emit(self.root, "root")
        width, height = self.declared_size
        constants = "\n".join(
            "    private static final Set<Direction> {} = EnumSet.of({});".format(
                name, ", ".join("Direction." + face for face in faces))
            for faces, name in sorted(face_sets.items(), key=lambda item: item[1]))

        return f"""package {package};

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;

import java.util.EnumSet;
import java.util.Set;

/**
 * {source_note} 가 생성한 메시. 직접 고치지 말고 스크립트를 고친 뒤 다시 생성한다.
 * 복셀 한 칸은 {number(self.voxel)[:-1]} 유닛이고, 텍스처는 복셀당 {self.ppv}px 로 구워져 있다.
 * 다른 박스에 완전히 가려지는 면은 faces 집합에서 빼서 그리지 않는다.
 */
public final class {class_name}
{{
{constants}

    private {class_name}()
    {{
    }}

    public static LayerDefinition create()
    {{
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

{chr(10).join(tree_lines)}

        return LayerDefinition.create(meshDefinition, {width}, {height});
    }}

{(chr(10) + chr(10)).join(builders)}
}}
"""

    # ---- 4. 미리보기 -----------------------------------------------------

    def render(self, yaw_degrees: float, pitch_degrees: float, size: int = 720,
               background=(198, 176, 148), pose: dict | None = None) -> Image.Image:
        """게임의 엔티티 조명을 흉내 낸 정사영 렌더. pose 로 파트 회전을 덮어쓸 수 있다."""
        texture = np.asarray(self.texture.convert("RGB"), dtype=np.float32)
        quads = []

        def rotation(rx, ry, rz):
            cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
            matrix_x = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
            matrix_y = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
            matrix_z = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
            return matrix_z @ matrix_y @ matrix_x

        def collect(part: Part, matrix: np.ndarray, offset: np.ndarray):
            rot = (pose or {}).get(part.name, part.rot)
            offset = offset + matrix @ np.array(part.pivot, dtype=np.float64)
            matrix = matrix @ rotation(*rot)
            for box in part.boxes:
                low = np.array([box.x, box.y, box.z], dtype=np.float64) * self.voxel
                span = np.array([box.w, box.h, box.d], dtype=np.float64) * self.voxel
                x0, y0, z0 = low
                x1, y1, z1 = low + span
                corners = {
                    "DOWN": ((x0, y0, z1), (x1, y0, z1), (x0, y0, z0)),
                    "UP": ((x0, y1, z1), (x1, y1, z1), (x0, y1, z0)),
                    "NORTH": ((x0, y0, z0), (x1, y0, z0), (x0, y1, z0)),
                    "SOUTH": ((x1, y0, z1), (x0, y0, z1), (x1, y1, z1)),
                    "WEST": ((x0, y0, z1), (x0, y0, z0), (x0, y1, z1)),
                    "EAST": ((x1, y0, z0), (x1, y0, z1), (x1, y1, z0)),
                }
                for face in box.faces:
                    origin, along_u, along_v = (offset + matrix @ np.array(c) for c in corners[face])
                    columns, rows, _ = self._face_texels(box, face)
                    quads.append((origin, along_u - origin, along_v - origin,
                                  matrix @ np.array(FACE_NORMALS[face], dtype=np.float64),
                                  self._face_origin(box, face), columns * self.ppv, rows * self.ppv))
            for child in part.children:
                collect(child, matrix, offset)

        collect(self.root, np.eye(3), np.zeros(3))

        # 게임은 모델을 scale(-1, -1, 1) 로 뒤집어 그린다. 지면(y=24)을 0 으로 맞춘다.
        flip = np.diag([-1.0, -1.0, 1.0])
        yaw, pitch = math.radians(yaw_degrees + 180.0), math.radians(pitch_degrees)  # yaw 0 = 정면
        camera = (np.array([[1, 0, 0], [0, math.cos(pitch), -math.sin(pitch)], [0, math.sin(pitch), math.cos(pitch)]])
                  @ np.array([[math.cos(yaw), 0, math.sin(yaw)], [0, 1, 0], [-math.sin(yaw), 0, math.cos(yaw)]]))
        lights = [np.array(v) / np.linalg.norm(v) for v in ((0.2, 1.0, -0.7), (-0.2, 1.0, 0.7))]

        prepared = []
        points = []
        for origin, vector_u, vector_v, normal, uv, width, height in quads:
            origin = flip @ (origin - np.array([0.0, 24.0, 0.0]))
            vector_u, vector_v, normal = flip @ vector_u, flip @ vector_v, flip @ normal
            brightness = min(1.0, 0.45 + 0.6 * sum(max(0.0, float(normal @ light)) for light in lights))
            origin, vector_u, vector_v = camera @ origin, camera @ vector_u, camera @ vector_v
            prepared.append((origin, vector_u, vector_v, brightness, uv, width, height))
            points.extend([origin, origin + vector_u, origin + vector_v, origin + vector_u + vector_v])

        points = np.array(points)
        low, high = points[:, :2].min(0), points[:, :2].max(0)
        zoom = (size * 0.9) / max(high - low)
        center = (low + high) / 2

        image = np.empty((size, size, 3), dtype=np.float32)
        image[:] = background
        depth = np.full((size, size), -1e9)

        for origin, vector_u, vector_v, brightness, uv, width, height in prepared:
            basis = np.array([[vector_u[0], vector_v[0]], [vector_u[1], vector_v[1]]]) * zoom
            determinant = np.linalg.det(basis)
            if abs(determinant) < 1e-9:
                continue
            screen_origin = (origin[:2] - center) * zoom
            corner_points = np.array([screen_origin, screen_origin + basis[:, 0], screen_origin + basis[:, 1],
                                      screen_origin + basis[:, 0] + basis[:, 1]])
            min_x = max(0, int(math.floor(corner_points[:, 0].min() + size / 2)))
            max_x = min(size - 1, int(math.ceil(corner_points[:, 0].max() + size / 2)))
            min_y = max(0, int(math.floor(size / 2 - corner_points[:, 1].max())))
            max_y = min(size - 1, int(math.ceil(size / 2 - corner_points[:, 1].min())))
            if min_x > max_x or min_y > max_y:
                continue
            grid_x, grid_y = np.meshgrid(np.arange(min_x, max_x + 1), np.arange(min_y, max_y + 1))
            delta_x = (grid_x + 0.5 - size / 2) - screen_origin[0]
            delta_y = (size / 2 - grid_y - 0.5) - screen_origin[1]
            inverse = np.linalg.inv(basis)
            coord_u = inverse[0, 0] * delta_x + inverse[0, 1] * delta_y
            coord_v = inverse[1, 0] * delta_x + inverse[1, 1] * delta_y
            z = origin[2] + coord_u * vector_u[2] + coord_v * vector_v[2]
            inside = (coord_u >= 0) & (coord_u < 1) & (coord_v >= 0) & (coord_v < 1)
            inside &= z > depth[min_y:max_y + 1, min_x:max_x + 1]
            if not inside.any():
                continue
            texel_x = np.clip((uv[0] + coord_u * width).astype(int), 0, texture.shape[1] - 1)
            texel_y = np.clip((uv[1] + coord_v * height).astype(int), 0, texture.shape[0] - 1)
            region = image[min_y:max_y + 1, min_x:max_x + 1]
            region[inside] = texture[texel_y[inside], texel_x[inside]] * brightness
            depth[min_y:max_y + 1, min_x:max_x + 1][inside] = z[inside]

        return Image.fromarray(np.clip(image, 0, 255).astype(np.uint8), "RGB")

    def preview_sheet(self, views=((35, 18), (0, 8), (90, 8), (180, 8), (215, 25), (0, 80)),
                      size: int = 560, pose: dict | None = None) -> Image.Image:
        columns = 3
        rows = -(-len(views) // columns)
        sheet = Image.new("RGB", (columns * size, rows * size), (198, 176, 148))
        for index, (yaw, pitch) in enumerate(views):
            sheet.paste(self.render(yaw, pitch, size, pose=pose), ((index % columns) * size, (index // columns) * size))
        return sheet
