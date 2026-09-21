"""모델 마무리 공통 처리: 탄약 큐브, 보조 탄창, 부착물 위치 뼈, 개조 화면 시점 뼈."""

from parts import cartridge

# 개조 화면 시점: (부착 위치 기준 카메라 이동량, 카메라 회전). 기본 팩 총기들의 값을 본떴다.
# 카메라는 총의 +x 쪽에 서서 -x 방향을 본다. x 가 작을수록 가까이서 크게 보인다.
REFIT_VIEWS = {
    "muzzle": ((11.0, -0.4, -2.2), (0, 90, 0)),
    "stock": ((11.0, -0.4, 3.0), (0, 90, 0)),
    "grip": ((7.5, -2.6, -0.4), (0, 90, 0)),
    "laser": ((8.0, 0.5, 4.0), (0, 70, 0)),
    "scope": ((8.7, 1.4, 5.0), (0, 50, 0)),
    "extended_mag": ((10.0, 0.0, 0.0), (0, 90, 0)),
}
# 옆면 레일에 붙는 부착물은 아래로 매달린 모양을 +x 쪽으로 눕혀야 한다 (기본 팩의 laser_pos 와 같은 회전)
SIDE_MOUNT_ROTATION = (0, 0, 90)


def finish_model(m, attachment_types=()):
    for bone in ("bullet_in_barrel", "bullet", "bullet_in_mag"):
        if m.has_bone(bone) and bone not in m.cubes:
            if m.meta.get("shotgun"):
                cartridge(m, bone, m.base_bbox(bone), case="red", tip="brass")
            else:
                cartridge(m, bone, m.base_bbox(bone))
    copy_additional_magazine(m)
    place_position_bones(m)
    place_refit_views(m, attachment_types)


def copy_additional_magazine(m):
    """전술 재장전 때 나오는 두 번째 탄창. 베이스에서 기본 탄창과 떨어져 있으면 그만큼 옮긴다."""
    if not m.has_bone("additional_magazine") or "mag_standard" not in m.cubes:
        return
    base_mag, base_extra = m.base_bbox("mag_standard"), m.base_bbox("additional_magazine")
    if base_mag is None or base_extra is None:
        return
    delta = [base_extra[1][axis] - base_mag[1][axis] for axis in range(3)]
    for cube in m.cubes["mag_standard"]:
        pivot = tuple(p + d for p, d in zip(cube.pivot, delta)) if cube.pivot else None
        m.add("additional_magazine", cube.origin[0] + delta[0], cube.origin[1] + delta[1], cube.origin[2] + delta[2],
              *cube.size, cube.material, style=cube.style, rot=cube.rotation, pivot=pivot, detail=cube.detail)


def position_anchor(m):
    """새 위치 뼈를 만들 때 부모를 본뜰 뼈. 부착물 위치 뼈들은 모두 같은 부모 밑에 있다."""
    for name in ("scope_pos", "muzzle_pos", "muzzle_flash"):
        if m.has_bone(name):
            return name
    raise KeyError(f"{m.base_id}: 위치 뼈의 기준으로 삼을 뼈가 없다")


def place_position_bones(m):
    meta = m.meta
    bore_y = meta["bore_y"]
    like = position_anchor(m)
    m.set_pivot("muzzle_pos", 0, bore_y, meta["muzzle_z"], like=like)
    m.set_pivot("muzzle_flash", 0, bore_y, meta["flash_z"], like=like)
    if "scope" in meta:
        m.set_pivot("scope_pos", 0, meta["scope"][0], meta["scope"][1], like=like)
    if "iron_y" in meta:
        iron = m.base_pivot("iron_view")
        m.set_pivot("iron_view", iron[0], meta["iron_y"], iron[2])
    if "grip" in meta:
        m.set_pivot("grip_pos", 0, meta["grip"][0], meta["grip"][1], like=like, rotation=(0, 0, 0))
    if "laser" in meta:
        m.set_pivot("laser_pos", *meta["laser"], like=like, rotation=SIDE_MOUNT_ROTATION)
    if "stock" in meta:
        m.set_pivot("stock_pos", 0, meta["stock"][0], meta["stock"][1], like=like, rotation=(0, 0, 0))
    if "shell" in meta:
        m.set_pivot("shell", *meta["shell"], like=like)


def cube_extent(cubes):
    low = [min(cube.origin[axis] for cube in cubes) for axis in range(3)]
    high = [max(cube.origin[axis] + cube.size[axis] for cube in cubes) for axis in range(3)]
    return low, high


def place_refit_views(m, attachment_types):
    """개조 화면에서 슬롯을 고를 때 카메라가 가는 자리. 새 외형의 부착 위치에 맞춰 다시 잡는다."""
    meta = m.meta
    targets = {
        "muzzle": (meta["bore_y"], meta["muzzle_z"]),
        "scope": meta.get("scope"),
        "grip": meta.get("grip"),
        "stock": meta.get("stock"),
        "laser": meta["laser"][1:] if "laser" in meta else None,
    }
    if "mag_standard" in m.cubes:
        low, high = cube_extent(m.cubes["mag_standard"])
        targets["extended_mag"] = ((low[1] + high[1]) / 2, (low[2] + high[2]) / 2)
    for attachment, target in targets.items():
        if attachment not in attachment_types or target is None:
            continue
        offset, rotation = REFIT_VIEWS[attachment]
        m.set_pivot(f"refit_{attachment}_view", offset[0], target[0] + offset[1], target[1] + offset[2],
                    like="refit_view", rotation=rotation)
    # 총 전체를 보는 기본 시점: 총이 길수록 멀리서 본다
    visible = [cube for bone, cubes in m.cubes.items() if not bone.startswith("mag_extended") for cube in cubes]
    low, high = cube_extent(visible)
    length = high[2] - low[2]
    m.set_pivot("refit_view", 10 + 0.3 * length, meta["bore_y"], (low[2] + high[2]) / 2, rotation=(0, 89.9, 0))
