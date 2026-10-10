package com.tacz.guns.client.model;

public final class GunModelConstant {
    /**
     * 약실 안의 탄. 클로즈드 볼트 대기 총기 렌더링에 쓰며, 약실에 탄이 없으면 이 그룹을 숨긴다
     */
    public static final String BULLET_IN_BARREL = "bullet_in_barrel";
    /**
     * 탄창 안의 탄. 탄창이 비면 이 그룹을 숨긴다
     */
    public static final String BULLET_IN_MAG = "bullet_in_mag";
    /**
     * 탄띠. 주로 기관총에 쓰며, 탄이 다 떨어지면 숨긴다
     */
    public static final String BULLET_CHAIN = "bullet_chain";
    /**
     * 조준경이 없을 때 보임. 보통 M4에 쓴다
     */
    public static final String CARRY = "carry";
    /**
     * 1단계 확장 탄창을 달았을 때 표시
     */
    public static final String MAG_EXTENDED_1 = "mag_extended_1";
    /**
     * 2단계 확장 탄창을 달았을 때 표시
     */
    public static final String MAG_EXTENDED_2 = "mag_extended_2";
    /**
     * 3단계 확장 탄창을 달았을 때 표시
     */
    public static final String MAG_EXTENDED_3 = "mag_extended_3";
    /**
     * 확장 탄창을 달지 않았을 때 표시
     */
    public static final String MAG_STANDARD = "mag_standard";
    /**
     * 조준경이 있을 때 표시. 조준경을 얹는 레일(예: AKM의 레일)
     */
    public static final String MOUNT = "mount";
    /**
     * 조준경이 없을 때 보임. 가늠쇠
     */
    public static final String SIGHT = "sight";
    /**
     * 조준경이 있을 때 표시. 접힌 가늠쇠
     */
    public static final String SIGHT_FOLDED = "sight_folded";
    /**
     * 플레이어가 총기의 가늠쇠로 조준할 때 플레이어 눈의 위치와 방향으로 이해할 수 있다
     */
    public static final String IRON_VIEW_NODE = "iron_view";
    /**
     * 조준하지 않을 때 플레이어 눈의 위치와 방향
     */
    public static final String IDLE_VIEW_NODE = "idle_view";
    /**
     * 기본 개조 화면 위치 그룹
     */
    public static final String REFIT_VIEW_NODE = "refit_view";
    /**
     * 3인칭 총기 위치 그룹
     */
    public static final String THIRD_PERSON_HAND_ORIGIN_NODE = "thirdperson_hand";
    /**
     * 아이템 액자 위치 그룹
     */
    public static final String FIXED_ORIGIN_NODE = "fixed";
    /**
     * 떨어진 아이템 위치 그룹
     */
    public static final String GROUND_ORIGIN_NODE = "ground";
    /**
     * 탄피 배출 시작점 위치 그룹
     */
    public static final String SHELL_ORIGIN_NODE = "shell";
    public static final String SHELL_ORIGIN_NODE_PREFIX = "shell_";
    /**
     * 총구 화염 위치 그룹
     */
    public static final String MUZZLE_FLASH_ORIGIN_NODE = "muzzle_flash";
    /**
     * 1인칭 왼팔 그룹
     */
    public static final String LEFTHAND_POS_NODE = "lefthand_pos";
    /**
     * 1인칭 오른팔 그룹
     */
    public static final String RIGHTHAND_POS_NODE = "righthand_pos";
    /**
     * 탄창 위치 그룹
     */
    public static final String MAG_NORMAL_NODE = "magazine";
    /**
     * 재장전 때의 두 번째 탄창 위치 그룹
     */
    public static final String MAG_ADDITIONAL_NODE = "additional_magazine";
    /**
     * 부착물 어댑터
     */
    public static final String ATTACHMENT_ADAPTER_NODE = "attachment_adapter";
    /**
     * 기본 총열 덮개
     */
    public static final String HANDGUARD_DEFAULT_NODE = "handguard_default";
    /**
     * 전술 총열 덮개
     */
    public static final String HANDGUARD_TACTICAL_NODE = "handguard_tactical";
    /**
     * 부착물 위치 그룹 접미사. 실제 이름은 부착물 이름(소문자)에 이것을 붙인 것이다
     */
    public static final String ATTACHMENT_POS_SUFFIX = "_pos";
    /**
     * 기본 부착물 그룹 접미사. 부착물을 달면 숨겨지며, 실제 이름은 부착물 이름(소문자)에 이것을 붙인 것이다
     */
    public static final String DEFAULT_ATTACHMENT_SUFFIX = "_default";
    /**
     * 개조 화면 시점 위치 그룹 접두사. 실제 이름은 접두사 + 부착물 이름(소문자) + 접미사
     */
    public static final String REFIT_VIEW_PREFIX = "refit_";
    /**
     * 개조 화면 시점 위치 그룹 접미사. 실제 이름은 접두사 + 부착물 이름(소문자) + 접미사
     */
    public static final String REFIT_VIEW_SUFFIX = "_view";
    /**
     * 루트 그룹
     */
    public static final String ROOT_NODE = "root";
}