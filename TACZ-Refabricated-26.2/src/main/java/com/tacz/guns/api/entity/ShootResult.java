package com.tacz.guns.api.entity;

public enum ShootResult {
    /**
     * 성공
     */
    SUCCESS,
    /**
     * 알 수 없는 이유로 실패
     */
    UNKNOWN_FAIL,
    /**
     * 사격 대기 시간이 아직 지나지 않음
     */
    COOL_DOWN,
    /**
     * 탄약 없음(또는 예비 탄약 없음)
     */
    NO_AMMO,
    /**
     * 총기 교체 로직을 실행하지 않음
     */
    NOT_DRAW,
    /**
     * 현재 아이템이 총이 아님
     */
    NOT_GUN,
    /**
     * 총기 ID가 없음
     */
    ID_NOT_EXIST,
    /**
     * 수동으로 장전해야 함
     */
    NEED_BOLT,
    /**
     * 재장전 중
     */
    IS_RELOADING,
    /**
     * 총기 교체 중
     */
    IS_DRAWING,
    /**
     * 노리쇠 당기는 중
     */
    IS_BOLTING,
    /**
     * 근접 공격 중
     */
    IS_MELEE,
    /**
     * 질주 중
     */
    IS_SPRINTING,
    /**
     * 네트워크 불안정으로 사격 실패
     */
    NETWORK_FAIL,
    /**
     * Forge 이벤트가 취소함
     */
    FORGE_EVENT_CANCEL,
    /**
     * 무기 과열
     */
    OVERHEATED
}
