package com.daqem.jobsplus.stock;

/**
 * 시세 스냅샷의 상태.
 * <p>
 * 매분 00초에 새 조회가 시작되면 즉시 {@link #REFRESHING}이 되어 거래가 중지되고,
 * 조회 결과에 따라 {@link #READY} 또는 {@link #FAILED}로 바뀐다.
 * 이전 분의 가격으로 거래되는 일을 막기 위해 {@link #READY}가 아니면 체결하지 않는다.
 */
public enum SnapshotStatus
{
    /** 현재 분의 시세를 조회하는 중. 거래 중지. */
    REFRESHING,

    /** 현재 분의 시세 조회를 마침. 종목별 조회 성공 여부에 따라 거래 가능. */
    READY,

    /** 현재 분의 시세를 한 종목도 받지 못함. 거래 중지. */
    FAILED
}
