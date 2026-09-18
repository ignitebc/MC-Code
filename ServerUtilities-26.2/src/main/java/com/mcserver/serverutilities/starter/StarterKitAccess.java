package com.mcserver.serverutilities.starter;

public interface StarterKitAccess {
    /** 시작 장비를 이미 받았는지 여부. 최초 접속 한 번만 지급하기 위한 기록이다. */
    boolean serverutilities$starterKitGiven();

    void serverutilities$setStarterKitGiven(boolean value);
}
