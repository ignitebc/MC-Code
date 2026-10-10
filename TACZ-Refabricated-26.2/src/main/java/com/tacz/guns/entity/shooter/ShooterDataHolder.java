package com.tacz.guns.entity.shooter;

import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ShooterDataHolder {
    /**
     * 기준 타임스탬프. 시간을 정밀하게 계산해야 하는 상황에 쓴다. 지금은 shoot만 쓴다.
     */
    public long baseTimestamp = System.currentTimeMillis();
    /**
     * 사격 타임스탬프. 사격에 성공하면 갱신하며 단위는 ms다.
     * 사격 대기 시간을 계산하는 데 쓴다.
     */
    public long shootTimestamp = -1L;
    public long lastShootTimestamp = -1L;
    /**
     * 근접 공격 타임스탬프. 총검 키를 누르면 갱신하며 단위는 ms다
     * 사격 대기 시간을 계산하는 데 쓴다
     */
    public long meleeTimestamp = -1L;
    /**
     * 근접 공격에는 준비 동작이 있으며, 이것은 준비 동작용 카운터다
     * > 0일 때: 준비 동작 카운트를 시작하고 tick마다 1씩 줄인다
     * == 0일 때: 총검 근접 공격을 실행한다
     * < 0일 때: 기본 상태이며 아무것도 하지 않는다
     */
    public int meleePrepTickCount = -1;
    /**
     * 총 바꾸기 타임스탬프. 총 바꾸기를 시작할 때 갱신하며 단위는 ms다.
     * 총 바꾸기 진행도를 계산하는 데 쓴다. 총 바꾸기가 끝나야 각종 조작을 할 수 있다.
     */
    public long drawTimestamp = -1L;
    /**
     * 노리쇠 당기기 타임스탬프. 노리쇠 당기기를 시작할 때 갱신하며 단위는 ms다.
     */
    public long boltTimestamp = -1;
    public boolean isBolting = false;
    /**
     * 조준 진행도. 범위는 0~1
     */
    public float aimingProgress = 0;
    /**
     * 조준 타임스탬프. tick마다 갱신하며 단위는 ms다.
     * tick마다 마지막 aimingProgress 갱신 이후 지난 시간을 계산하고, 이를 바탕으로 aimingProgress 증가량을 계산하는 데 쓴다.
     */
    public long aimingTimestamp = -1L;
    /**
     * true이면 조준하는 중이라 aimingProgress가 tick마다 늘어나고,
     * false이면 조준을 푸는 중이라 aimingProgress가 tick마다 줄어든다.
     */
    public boolean isAiming = false;
    /**
     * 장전 타임스탬프. 장전을 시작하는 순간 갱신하며 단위는 ms다.
     * tick마다 장전 시작부터 현재 시점까지의 시간을 계산하고, 이를 바탕으로 재장전 상태와 대기 시간을 계산하는 데 쓴다.
     */
    public long reloadTimestamp = -1;
    /**
     * 장전 상태 캐시. tick마다 갱신한다.
     */
    @Nonnull
    public ReloadState.StateType reloadStateType = ReloadState.StateType.NOT_RELOADING;
    /**
     * 현재 조작하는 총기 아이템의 Supplier. 총을 바꿀 때(draw 메서드) 갱신한다.
     */
    @Nullable
    public Supplier<ItemStack> currentGunItem = null;
    /**
     * 현재 총기의 집어넣기 시간을 캐시해, 다음 총 바꾸기 때 이 시간으로 집어넣기를 계산하게 한다.
     * 이 값은 tacz$CurrentGunItem이 주는 ItemStack이 바뀌어도 바뀌지 않으므로, 알맞은 때에 updatePutAwayTime()을 호출해 갱신해야 한다.
     */
    public float currentPutAwayTimeS = 0;
    /**
     * 질주 관련 매개변수. 조준경을 들여다보면 질주를 막는다
     */
    public float sprintTimeS = 0;
    public long sprintTimestamp = -1;
    /**
     * 총을 든 채 마지막으로 질주한 타임스탬프. "질주 직후 사격"의 탄 퍼짐 불이익을 판정하는 데 쓴다. -1이면 아직 질주한 적이 없다
     */
    public long lastSprintTimestamp = -1;
    /**
     * 탄환 넉백 능력을 기록한다. 음수이면 바닐라 넉백을 쓴다
     */
    public double knockbackStrength = -1;
    /**
     * 사격 수를 기록해 예광탄을 판정한다
     */
    public int shootCount = 0;
    public float chargeProgress = 0f;
    /**
     * 엎드린 상태인지 여부
     */
    public boolean isCrawling = false;
    /**
     * lua 스크립트 데이터를 캐시하는 데 쓴다
     */
    @Nullable
    public LuaValue scriptData = null;

    public long heatTimestamp = -1;
    /**
     * 부착물이 바꾼 각종 속성 캐시
     */
    @Nullable
    public AttachmentCacheProperty cacheProperty = null;

    public void initialData() {
        // 각 상태 초기화
        shootTimestamp = -1;
        meleeTimestamp = -1;
        meleePrepTickCount = -1;
        isAiming = false;
        aimingProgress = 0;
        reloadTimestamp = -1;
        reloadStateType = ReloadState.StateType.NOT_RELOADING;
        sprintTimestamp = -1;
        sprintTimeS = 0;
        lastSprintTimestamp = -1;
        boltTimestamp = -1;
        isBolting = false;
        shootCount = 0;
        chargeProgress = 0f;
        scriptData = null;
        heatTimestamp = -1;
    }
}
