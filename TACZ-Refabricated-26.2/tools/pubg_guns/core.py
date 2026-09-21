"""PUBG 계열 신규 총기의 모델·텍스처·아이콘을 만드는 공통 기능.

베이스 총기의 뼈대(이름·부모·피벗·회전)를 그대로 복사한 뒤 큐브만 새로 올린다.
뼈대가 같으므로 베이스 총기의 애니메이션·상태 머신·손 위치를 그대로 쓸 수 있다.
텍스처는 큐브 면마다 재질·크기별 사각형을 아틀라스에 배치하고 절차적으로 칠한다.
"""

import json
import math
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path

import numpy as np
from PIL import Image

TOOLS_DIR = Path(__file__).resolve().parents[1]
PACK = TOOLS_DIR.parent / "src/main/resources/assets/tacz/custom/tacz_default_gun"
sys.path.insert(0, str(TOOLS_DIR))
import render_gun_slot  # noqa: E402

# 손 위치 표시용 큐브는 게임에서 그려지지 않으므로 베이스 것을 그대로 둔다
KEPT_CUBE_BONES = ("lefthand_pos", "righthand_pos")
# 아이콘에는 부착물을 달았을 때만 보이는 어댑터 형상도 나오면 안 된다
ICON_HIDDEN = tuple(render_gun_slot.DEFAULT_HIDDEN) + ("attachment_adapter",)

PALETTE = {
    "metal": (76, 80, 87),
    "metal_dark": (50, 53, 59),
    "steel": (134, 138, 144),
    "poly": (43, 44, 49),
    "poly_gray": (92, 95, 100),
    "fde": (150, 126, 92),
    "olive": (82, 88, 62),
    "wood": (124, 74, 38),
    "wood_dark": (92, 54, 28),
    "wood_red": (138, 66, 34),
    "bakelite": (132, 62, 30),
    "rubber": (24, 24, 26),
    "brass": (188, 148, 62),
    "copper": (172, 98, 58),
    "glass": (40, 70, 92),
    "white": (225, 225, 220),
    "red": (170, 40, 36),
    "void": (10, 10, 12),
}

# 면 방향별 기본 음영: 게임 조명이 평평해도 입체감이 남도록 텍스처에 굽는다
FACE_GROUP = {"up": "up", "down": "down", "east": "side", "west": "side", "north": "end", "south": "end"}
GROUP_SHADE = {"up": 1.12, "down": 0.70, "side": 1.0, "end": 0.86}
WOOD_MATERIALS = ("wood", "wood_dark", "wood_red")


def load_json(path):
    """주석과 끝 쉼표가 섞인 총기팩 JSON 을 읽는다."""
    text = Path(path).read_text(encoding="utf-8-sig")
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    text = re.sub(r"(?m)(^|\s)//.*$", "", text)
    text = re.sub(r",(\s*[}\]])", r"\1", text)
    return json.loads(text)


def write_json(path, value):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


@dataclass
class Cube:
    origin: tuple
    size: tuple
    material: str
    style: str = ""
    rotation: tuple = None
    pivot: tuple = None
    detail: bool = False
    uv: dict = field(default_factory=dict)


class GunModel:
    """베이스 뼈대 위에 새 큐브를 올리는 모델."""

    def __init__(self, gun_id, base_id, body_bone, ppu=3.0, palette=None):
        self.gun_id = gun_id
        self.base_id = base_id
        self.body_bone = body_bone
        self.ppu = ppu
        self.palette = dict(PALETTE)
        self.palette.update(palette or {})
        geo_path = PACK / f"assets/tacz/geo_models/gun/{base_id}_geo.json"
        self.base_geo = load_json(geo_path)["minecraft:geometry"][0]
        self.bones = {bone["name"]: bone for bone in self.base_geo["bones"]}
        self.cubes = {}
        self.pivots = {}
        self.rotations = {}
        self.new_bones = []
        self._check_rest_rotation(body_bone)

    # ---- 뼈 ----
    def _chain(self, name):
        while name is not None:
            yield self.bones[name]
            name = self.bones[name].get("parent")

    def _check_rest_rotation(self, name):
        for bone in self._chain(name):
            rotation = bone.get("rotation")
            if rotation and any(abs(v) > 1e-6 for v in rotation):
                raise ValueError(f"{self.base_id}: '{name}' 의 상위 '{bone['name']}' 에 기본 회전이 있어 큐브가 틀어진다")

    def has_bone(self, name):
        return name in self.bones

    def base_bbox(self, name):
        """베이스 모델에서 해당 뼈 하위 큐브 전체의 (최소, 최대) 좌표."""
        low, high = [1e9] * 3, [-1e9] * 3
        for bone in self.base_geo["bones"]:
            if name not in (b["name"] for b in self._chain(bone["name"])):
                continue
            for cube in bone.get("cubes", []):
                for axis in range(3):
                    low[axis] = min(low[axis], cube["origin"][axis])
                    high[axis] = max(high[axis], cube["origin"][axis] + cube["size"][axis])
        if low[0] > high[0]:
            return None
        return low, high

    def base_pivot(self, name):
        return list(self.bones[name]["pivot"])

    def add_bone(self, name, parent, pivot=(0, 0, 0)):
        """베이스에 없는 뼈를 새로 만든다. 이미 있으면 그대로 둔다."""
        if name not in self.bones:
            bone = {"name": name, "parent": parent, "pivot": list(pivot)}
            self.bones[name] = bone
            self.new_bones.append(bone)

    def set_pivot(self, name, x, y, z, like=None, rotation=None):
        """위치 지정용 뼈의 피벗을 옮긴다. 없으면 like 뼈와 같은 부모 밑에 새로 만든다.

        rotation 을 주면 기본 회전도 그 값으로 바꾼다. (0, 0, 0) 이면 회전을 없앤다.
        """
        if name not in self.bones:
            self.add_bone(name, self.bones[like].get("parent"), (x, y, z))
        self.pivots[name] = [x, y, z]
        if rotation is not None:
            self.rotations[name] = list(rotation)

    # ---- 큐브 ----
    def add(self, bone, x, y, z, w, h, d, material, style="", rot=None, pivot=None, detail=False):
        if bone not in self.bones:
            raise KeyError(f"{self.base_id} 에 '{bone}' 뼈가 없다")
        if material not in self.palette:
            raise KeyError(f"알 수 없는 재질: {material}")
        if min(w, h, d) <= 0:
            raise ValueError(f"{self.gun_id}: 크기가 0 이하인 큐브 ({w}, {h}, {d})")
        cube = Cube((x, y, z), (w, h, d), material, style, rot, pivot, detail)
        self.cubes.setdefault(bone, []).append(cube)
        return cube

    def cbox(self, bone, w, y, h, z, d, material, **kw):
        """x 축 가운데에 놓이는 큐브."""
        return self.add(bone, -w / 2, y, z, w, h, d, material, **kw)

    def pair(self, bone, x_inner, w, y, h, z, d, material, **kw):
        """좌우 대칭으로 두 개."""
        self.add(bone, x_inner, y, z, w, h, d, material, **kw)
        self.add(bone, -x_inner - w, y, z, w, h, d, material, **kw)

    def count(self, detail=True):
        return sum(1 for cubes in self.cubes.values() for cube in cubes if detail or not cube.detail)

    # ---- 텍스처 ----
    def _face_pixels(self, cube, face):
        w, h, d = cube.size
        width, height = {"north": (w, h), "south": (w, h), "east": (d, h), "west": (d, h), "up": (w, d), "down": (w, d)}[face]
        return max(1, round(width * self.ppu)), max(1, round(height * self.ppu))

    def build_texture(self, seed):
        """모든 면에 아틀라스 사각형을 배정하고 칠한다. 같은 재질·크기·방향은 사각형을 공유한다."""
        keys = {}
        for cubes in self.cubes.values():
            for cube in cubes:
                for face in FACE_GROUP:
                    pw, ph = self._face_pixels(cube, face)
                    keys.setdefault((cube.material, cube.style, pw, ph, FACE_GROUP[face]), None)
        ordered = sorted(keys, key=lambda k: (-k[3], -k[2]))
        for size in (128, 256, 512, 1024):
            placed = _shelf_pack(ordered, size)
            if placed is not None:
                break
        else:
            raise ValueError(f"{self.gun_id}: 텍스처가 1024 에도 들어가지 않는다")
        rng = np.random.default_rng(seed)
        image = np.zeros((size, size, 4), dtype=np.float32)
        for key, (u, v) in placed.items():
            material, style, pw, ph, group = key
            image[v:v + ph, u:u + pw] = paint_rect(self.palette[material], material, style, pw, ph, group, rng)
        for cubes in self.cubes.values():
            for cube in cubes:
                for face in FACE_GROUP:
                    pw, ph = self._face_pixels(cube, face)
                    u, v = placed[(cube.material, cube.style, pw, ph, FACE_GROUP[face])]
                    cube.uv[face] = {"uv": [u, v], "uv_size": [pw, ph]}
        self.texture_size = size
        return Image.fromarray(np.clip(image, 0, 255).astype(np.uint8), "RGBA")

    # ---- 출력 ----
    def geometry(self, lod=False):
        bones = []
        for source in list(self.base_geo["bones"]) + self.new_bones:
            bone = {k: v for k, v in source.items() if k not in ("cubes", "poly_mesh", "texture_meshes")}
            name = bone["name"]
            if name in self.pivots:
                bone["pivot"] = [round(v, 4) for v in self.pivots[name]]
            if name in self.rotations:
                bone.pop("rotation", None)
                if any(abs(v) > 1e-6 for v in self.rotations[name]):
                    bone["rotation"] = self.rotations[name]
            cubes = []
            if name in KEPT_CUBE_BONES:
                cubes = [c for c in source.get("cubes", [])]
            for cube in self.cubes.get(name, []):
                if lod and cube.detail:
                    continue
                entry = {"origin": [round(v, 4) for v in cube.origin], "size": [round(v, 4) for v in cube.size]}
                if cube.rotation:
                    entry["pivot"] = [round(v, 4) for v in cube.pivot]
                    entry["rotation"] = [round(v, 4) for v in cube.rotation]
                entry["uv"] = cube.uv
                cubes.append(entry)
            if cubes:
                bone["cubes"] = cubes
            bones.append(bone)
        description = dict(self.base_geo["description"])
        description["identifier"] = f"geometry.{self.gun_id}" + ("_lod" if lod else "")
        description["texture_width"] = self.texture_size
        description["texture_height"] = self.texture_size
        return {"format_version": "1.12.0", "minecraft:geometry": [{"description": description, "bones": bones}]}


def _shelf_pack(keys, size):
    placed, x, y, shelf = {}, 0, 0, 0
    for key in keys:
        pw, ph = key[2], key[3]
        if pw > size:
            return None
        if x + pw > size:
            x, y, shelf = 0, y + shelf, 0
        if y + ph > size:
            return None
        placed[key] = (x, y)
        x += pw
        shelf = max(shelf, ph)
    return placed


def paint_rect(color, material, style, pw, ph, group, rng):
    """면 하나를 칠한다. 위쪽이 밝고 모서리에 하이라이트·그림자가 들어간다."""
    base = np.array(color, dtype=np.float32) * GROUP_SHADE[group]
    shade = np.ones((ph, pw), dtype=np.float32)
    shade *= 1.0 + (rng.random((ph, pw)).astype(np.float32) - 0.5) * 0.07
    if ph >= 3:
        shade *= np.linspace(1.06, 0.93, ph, dtype=np.float32)[:, None]

    is_wood = material in WOOD_MATERIALS
    if is_wood:
        along_u = pw >= ph
        length, across = (pw, ph) if along_u else (ph, pw)
        lines = np.ones((across, length), dtype=np.float32)
        for row in range(across):
            wave = 0.06 * np.sin(np.arange(length) * 0.55 + rng.random() * 6.28 + row * 0.9)
            lines[row] = 1.0 + wave + (0.10 if rng.random() < 0.3 else 0.0) * -1
        shade *= lines if along_u else lines.T
    elif material in ("metal", "metal_dark", "steel") and pw >= 4 and ph >= 2:
        for row in range(ph):
            if rng.random() < 0.25:
                shade[row] *= 1.04

    if style == "vribs":
        shade[:, 1::3] *= 0.72
    elif style == "hribs":
        shade[1::3, :] *= 0.72
    elif style == "checker":
        grid = (np.add.outer(np.arange(ph), np.arange(pw)) % 2).astype(np.float32)
        shade *= 0.80 + 0.22 * grid
    elif style == "vents" and pw >= 6 and ph >= 3:
        top, bottom = max(1, ph // 3), max(2, ph - ph // 3)
        for start in range(1, pw - 2, 4):
            shade[top:bottom, start:start + 2] *= 0.25
    elif style == "panel" and pw >= 6 and ph >= 5:
        shade[1, 1:-1] *= 0.80
        shade[-2, 1:-1] *= 1.10
        shade[1:-1, 1] *= 0.84
        shade[1:-1, -2] *= 1.08

    if pw >= 3 and ph >= 3 and material != "void":
        shade[0, :] *= 1.16
        shade[-1, :] *= 0.78
        if style != "flat":
            shade[:, 0] *= 1.06
            shade[:, -1] *= 0.86
        if not is_wood and material not in ("rubber", "glass"):
            # 모서리가 닳아 밝게 드러난 자국
            wear = rng.random(pw) < (0.0 if style == "flat" else 0.12)
            shade[0, wear] *= 1.35

    rect = np.empty((ph, pw, 4), dtype=np.float32)
    rect[:, :, :3] = base[None, None, :] * shade[:, :, None]
    rect[:, :, 3] = 255
    return rect


def side_image(geo, texture, ppu=10, margin=2.0, bounds=None, hidden=None):
    """총구가 왼쪽을 향하는 옆모습 직교 렌더.

    bounds 는 (zmin, zmax, ymin, ymax). 여러 모델을 같은 좌표로 겹쳐 그릴 때 준다.
    hidden 은 그리지 않을 뼈 이름. 생략하면 아이콘용 기본 목록을 쓴다.
    """
    hidden = set(ICON_HIDDEN) if hidden is None else set(hidden)
    quads = render_gun_slot.collect_quads(geo, hidden)
    points = []
    for origin, vu, vv, _normal, _uv in quads:
        points += [origin, origin + vu, origin + vv, origin + vu + vv]
    points = np.array(points)
    if bounds is None:
        bounds = (points[:, 2].min() - margin, points[:, 2].max() + margin,
                  points[:, 1].min() - margin, points[:, 1].max() + margin)
    zmin, zmax, ymin, ymax = bounds
    width, height = int((zmax - zmin) * ppu), int((ymax - ymin) * ppu)
    image = np.zeros((height, width, 4), dtype=np.float32)
    depth = np.full((height, width), -1e9)
    pixels = np.asarray(texture.convert("RGBA"), dtype=np.float32)
    scale = pixels.shape[1] / geo["description"]["texture_width"]
    for origin, vu, vv, normal, (u, v, w, h) in quads:
        start = np.array([(origin[2] - zmin) * ppu, (origin[1] - ymin) * ppu])
        bu, bv = np.array([vu[2], vu[1]]) * ppu, np.array([vv[2], vv[1]]) * ppu
        basis = np.array([[bu[0], bv[0]], [bu[1], bv[1]]])
        if abs(np.linalg.det(basis)) < 1e-9:
            continue
        corners = np.array([start, start + bu, start + bv, start + bu + bv])
        x0, x1 = max(0, int(corners[:, 0].min())), min(width - 1, int(math.ceil(corners[:, 0].max())))
        y0, y1 = max(0, int(height - corners[:, 1].max())), min(height - 1, int(math.ceil(height - corners[:, 1].min())))
        if x0 > x1 or y0 > y1:
            continue
        grid_x, grid_y = np.meshgrid(np.arange(x0, x1 + 1), np.arange(y0, y1 + 1))
        dx, dy = grid_x + 0.5 - start[0], (height - grid_y - 0.5) - start[1]
        inverse = np.linalg.inv(basis)
        cu, cv = inverse[0, 0] * dx + inverse[0, 1] * dy, inverse[1, 0] * dx + inverse[1, 1] * dy
        z = origin[0] + cu * vu[0] + cv * vv[0]
        inside = (cu >= 0) & (cu < 1) & (cv >= 0) & (cv < 1) & (z > depth[y0:y1 + 1, x0:x1 + 1])
        if not inside.any():
            continue
        tx = np.clip(np.floor((u + cu * w) * scale).astype(int), 0, pixels.shape[1] - 1)
        ty = np.clip(np.floor((v + cv * h) * scale).astype(int), 0, pixels.shape[0] - 1)
        sample = pixels[ty, tx].copy()
        sample[:, :, :3] *= min(1.0, 0.80 + 0.20 * max(0.0, float(normal[1])) + 0.10 * max(0.0, float(normal[0])))
        sample[:, :, 3] = 255
        image[y0:y1 + 1, x0:x1 + 1][inside] = sample[inside]
        depth[y0:y1 + 1, x0:x1 + 1][inside] = z[inside]
    return Image.fromarray(np.clip(image, 0, 255).astype(np.uint8), "RGBA")


def hud_icon(geo, texture, size=(384, 128)):
    """기본 팩과 같은 밝은 회색조 옆모습 HUD 아이콘."""
    side = side_image(geo, texture, ppu=12, margin=0.5)
    pixels = np.asarray(side, dtype=np.float32)
    luminance = pixels[:, :, :3].mean(axis=2)
    opaque = pixels[:, :, 3] > 0
    low, high = np.percentile(luminance[opaque], 5), np.percentile(luminance[opaque], 95)
    gray = 150 + np.clip((luminance - low) / max(1.0, high - low), 0, 1) * 70
    out = np.zeros_like(pixels)
    out[:, :, :3] = gray[:, :, None]
    out[:, :, 3] = pixels[:, :, 3]
    icon = Image.fromarray(out.astype(np.uint8), "RGBA")
    icon.thumbnail((size[0] - 24, size[1] - 16), Image.LANCZOS)
    canvas = Image.new("RGBA", size, (0, 0, 0, 0))
    canvas.alpha_composite(icon, ((size[0] - icon.width) // 2, (size[1] - icon.height) // 2))
    return canvas


def slot_icon(geo, texture, size=64):
    """기본 팩 슬롯 아이콘과 같은 대각선 구도."""
    pixels = np.asarray(texture.convert("RGBA"), dtype=np.float32)
    scale = pixels.shape[1] / geo["description"]["texture_width"]
    quads = [(o, u, v, n, (uv[0] * scale, uv[1] * scale, uv[2] * scale, uv[3] * scale))
             for o, u, v, n, uv in render_gun_slot.collect_quads(geo, set(ICON_HIDDEN))]
    big = np.asarray(render_gun_slot.render(quads, pixels, size * 8, 225.0, 30.0, 0.0), dtype=np.float32)
    big[:, :, :3] *= big[:, :, 3:4] / 255.0
    small = np.asarray(Image.fromarray(big.astype(np.uint8), "RGBA").resize((size, size), Image.BOX), dtype=np.float32)
    alpha = np.maximum(small[:, :, 3:4], 1.0)
    small[:, :, :3] = np.clip(small[:, :, :3] * 255.0 / alpha, 0, 255)
    return Image.fromarray(small.astype(np.uint8), "RGBA")
