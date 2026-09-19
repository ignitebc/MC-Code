"""TACZ 총기 geo 모델을 기본 총기팩 슬롯 아이콘과 같은 각도로 렌더한다.

    python render_gun_slot.py <geo.json> <texture.png> <out.png> [--hide 뼈이름,...] [--size 64]

기본 팩 아이콘과 같은 구도: 총의 오른쪽 면이 보이고 총구가 오른쪽 아래로 내려가는
직교 뷰(yaw 225, pitch 30). 크게 그린 뒤 줄여서 가장자리를 부드럽게 만든다.
"""

import argparse
import json
import math

import numpy as np
from PIL import Image

# 부착물을 달았을 때만 보이는 뼈, 손, 시점용 뼈 등 아이콘에 나오면 안 되는 것들
DEFAULT_HIDDEN = (
    "righthand", "lefthand", "mount", "handguard_tactical", "additional_magazine",
    "mag_extended_1", "mag_extended_2", "mag_extended_3", "bullet", "bullet_in_barrel",
    "bullet_in_mag", "view", "positioning", "positioning2", "camera", "constraint",
)


def rotation(rx, ry, rz):
    rx, ry, rz = (math.radians(v) for v in (rx, ry, rz))
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    mx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    my = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    mz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return mz @ my @ mx


def collect_quads(geo, hidden):
    """모든 큐브 면을 (원점, u축, v축, 법선, uv 사각형) 으로 펼친다. 좌표는 블록벤치 공간(x 반전)."""
    bones = {bone["name"]: bone for bone in geo["bones"]}

    def is_hidden(name):
        while name is not None:
            if name in hidden:
                return True
            name = bones[name].get("parent")
        return False

    def transform_of(name):
        """뼈의 누적 변환 (행렬, 이동)."""
        bone = bones[name]
        parent = bone.get("parent")
        matrix, offset = transform_of(parent) if parent else (np.eye(3), np.zeros(3))
        rot = bone.get("rotation")
        if rot:
            pivot = np.array([-bone["pivot"][0], bone["pivot"][1], bone["pivot"][2]], dtype=float)
            local = rotation(-rot[0], -rot[1], rot[2])
            offset = offset + matrix @ (pivot - local @ pivot)
            matrix = matrix @ local
        return matrix, offset

    quads = []
    for name, bone in bones.items():
        if is_hidden(name) or not bone.get("cubes"):
            continue
        bone_matrix, bone_offset = transform_of(name)
        for cube in bone["cubes"]:
            inflate = cube.get("inflate", 0.0)
            ox, oy, oz = cube["origin"]
            sx, sy, sz = cube["size"]
            x0, x1 = -(ox + sx) - inflate, -ox + inflate
            y0, y1 = oy - inflate, oy + sy + inflate
            z0, z1 = oz - inflate, oz + sz + inflate
            matrix, offset = bone_matrix, bone_offset
            rot = cube.get("rotation")
            if rot:
                pivot = cube.get("pivot", [0, 0, 0])
                pivot = np.array([-pivot[0], pivot[1], pivot[2]], dtype=float)
                local = rotation(-rot[0], -rot[1], rot[2])
                offset = offset + matrix @ (pivot - local @ pivot)
                matrix = matrix @ local
            faces = {
                "north": ((x1, y1, z0), (x0, y1, z0), (x1, y0, z0), (0, 0, -1)),
                "south": ((x0, y1, z1), (x1, y1, z1), (x0, y0, z1), (0, 0, 1)),
                "east": ((x1, y1, z1), (x1, y1, z0), (x1, y0, z1), (1, 0, 0)),
                "west": ((x0, y1, z0), (x0, y1, z1), (x0, y0, z0), (-1, 0, 0)),
                "up": ((x0, y1, z0), (x1, y1, z0), (x0, y1, z1), (0, 1, 0)),
                "down": ((x0, y0, z1), (x1, y0, z1), (x0, y0, z0), (0, -1, 0)),
            }
            for face, (origin, along_u, along_v, normal) in faces.items():
                uv = cube.get("uv", {}).get(face)
                if not uv:
                    continue
                u, v = uv["uv"]
                width, height = uv["uv_size"]
                if face in ("up", "down"):
                    u, v, width, height = u + width, v + height, -width, -height
                origin, along_u, along_v = (offset + matrix @ np.array(p, dtype=float)
                                            for p in (origin, along_u, along_v))
                quads.append((origin, along_u - origin, along_v - origin,
                              matrix @ np.array(normal, dtype=float), (u, v, width, height)))
    return quads


def render(quads, texture, size, yaw, pitch, margin):
    yaw, pitch = math.radians(yaw), math.radians(pitch)
    camera = (np.array([[1, 0, 0], [0, math.cos(pitch), -math.sin(pitch)], [0, math.sin(pitch), math.cos(pitch)]])
              @ np.array([[math.cos(yaw), 0, math.sin(yaw)], [0, 1, 0], [-math.sin(yaw), 0, math.cos(yaw)]]))
    lights = [np.array(v) / np.linalg.norm(v) for v in ((0.3, 1.0, 0.5), (-0.4, 0.6, 0.7))]

    prepared, points = [], []
    for origin, vector_u, vector_v, normal, uv in quads:
        brightness = min(1.0, 0.72 + 0.30 * max(0.0, float(normal @ lights[0]))
                         + 0.25 * max(0.0, float((camera @ normal) @ np.array([0.0, 0.0, 1.0]))))
        origin, vector_u, vector_v = camera @ origin, camera @ vector_u, camera @ vector_v
        prepared.append((origin, vector_u, vector_v, brightness, uv))
        points.extend([origin, origin + vector_u, origin + vector_v, origin + vector_u + vector_v])
    points = np.array(points)
    low, high = points[:, :2].min(0), points[:, :2].max(0)
    zoom = size * (1.0 - 2 * margin) / max(high - low)
    center = (low + high) / 2

    image = np.zeros((size, size, 4), dtype=np.float32)
    depth = np.full((size, size), -1e9)
    for origin, vector_u, vector_v, brightness, (u, v, width, height) in prepared:
        basis = np.array([[vector_u[0], vector_v[0]], [vector_u[1], vector_v[1]]]) * zoom
        if abs(np.linalg.det(basis)) < 1e-9:
            continue
        start = (origin[:2] - center) * zoom
        corners = np.array([start, start + basis[:, 0], start + basis[:, 1], start + basis[:, 0] + basis[:, 1]])
        min_x = max(0, int(math.floor(corners[:, 0].min() + size / 2)))
        max_x = min(size - 1, int(math.ceil(corners[:, 0].max() + size / 2)))
        min_y = max(0, int(math.floor(size / 2 - corners[:, 1].max())))
        max_y = min(size - 1, int(math.ceil(size / 2 - corners[:, 1].min())))
        if min_x > max_x or min_y > max_y:
            continue
        grid_x, grid_y = np.meshgrid(np.arange(min_x, max_x + 1), np.arange(min_y, max_y + 1))
        delta_x = (grid_x + 0.5 - size / 2) - start[0]
        delta_y = (size / 2 - grid_y - 0.5) - start[1]
        inverse = np.linalg.inv(basis)
        coord_u = inverse[0, 0] * delta_x + inverse[0, 1] * delta_y
        coord_v = inverse[1, 0] * delta_x + inverse[1, 1] * delta_y
        z = origin[2] + coord_u * vector_u[2] + coord_v * vector_v[2]
        inside = (coord_u >= 0) & (coord_u < 1) & (coord_v >= 0) & (coord_v < 1)
        inside &= z > depth[min_y:max_y + 1, min_x:max_x + 1]
        if not inside.any():
            continue
        texel_x = np.clip(np.floor(u + coord_u * width).astype(int), 0, texture.shape[1] - 1)
        texel_y = np.clip(np.floor(v + coord_v * height).astype(int), 0, texture.shape[0] - 1)
        sample = texture[texel_y, texel_x]
        inside &= sample[:, :, 3] > 8
        region = image[min_y:max_y + 1, min_x:max_x + 1]
        shaded = sample.copy()
        shaded[:, :, :3] *= brightness
        shaded[:, :, 3] = 255
        region[inside] = shaded[inside]
        depth[min_y:max_y + 1, min_x:max_x + 1][inside] = z[inside]
    return Image.fromarray(np.clip(image, 0, 255).astype(np.uint8), "RGBA")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("geo")
    parser.add_argument("texture")
    parser.add_argument("out")
    parser.add_argument("--hide", default="")
    parser.add_argument("--size", type=int, default=64)
    parser.add_argument("--yaw", type=float, default=225.0)
    parser.add_argument("--pitch", type=float, default=30.0)
    parser.add_argument("--margin", type=float, default=0.0)
    args = parser.parse_args()

    geo = json.load(open(args.geo, encoding="utf-8"))["minecraft:geometry"][0]
    hidden = set(DEFAULT_HIDDEN) | {name for name in args.hide.split(",") if name}
    texture = np.asarray(Image.open(args.texture).convert("RGBA"), dtype=np.float32)
    # 모델이 선언한 텍스처 크기와 실제 PNG 크기가 다르면(고해상도 텍스처) UV 를 그만큼 늘린다
    scale_u = texture.shape[1] / geo["description"]["texture_width"]
    scale_v = texture.shape[0] / geo["description"]["texture_height"]
    quads = [(origin, along_u, along_v, normal, (u * scale_u, v * scale_v, width * scale_u, height * scale_v))
             for origin, along_u, along_v, normal, (u, v, width, height) in collect_quads(geo, hidden)]
    big = render(quads, texture, args.size * 8, args.yaw, args.pitch, args.margin)
    # 알파를 미리 곱해 줄여야 가장자리에 검은 테가 끼지 않는다
    pixels = np.asarray(big, dtype=np.float32)
    pixels[:, :, :3] *= pixels[:, :, 3:4] / 255.0
    small = np.asarray(Image.fromarray(pixels.astype(np.uint8), "RGBA").resize(
        (args.size, args.size), Image.BOX), dtype=np.float32)
    alpha = np.maximum(small[:, :, 3:4], 1.0)
    small[:, :, :3] = np.clip(small[:, :, :3] * 255.0 / alpha, 0, 255)
    Image.fromarray(small.astype(np.uint8), "RGBA").save(args.out)


if __name__ == "__main__":
    main()
