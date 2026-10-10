package com.tacz.guns.client.animation.statemachine;

import cn.sh1rocu.tacz.api.extension.IMoveDistTracker;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.util.LuaNbtAccessor;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import com.tacz.guns.util.ShooterMagazineBonus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.luaj.vm2.LuaTable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unused")
public class GunAnimationStateContext extends ItemAnimationStateContext {
    private ItemStack currentGunItem;
    private IGun iGun;
    private GunDisplayInstance display;
    private GunData gunData;
    private float walkDistAnchor = 0f;
    private LuaNbtAccessor nbtUtil;

    private <T> Optional<T> processGunData(BiFunction<IGun, GunDisplayInstance, T> processor) {
        if (iGun != null && display != null) {
            return Optional.ofNullable(processor.apply(iGun, display));
        }
        return Optional.empty();
    }

    private <T> Optional<T> processGunOperator(Function<IClientPlayerGunOperator, T> processor) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            return Optional.ofNullable(processor.apply(IClientPlayerGunOperator.fromLocalPlayer(player)));
        }
        return Optional.empty();
    }

    private <T> Optional<T> processRemoteGunOperator(Function<IGunOperator, T> processor) {
        return processCameraEntity(entity -> {
            if (entity instanceof LivingEntity) {
                IGunOperator gunOperator = IGunOperator.fromLivingEntity((LivingEntity) entity);
                return processor.apply(gunOperator);
            }
            return null;
        });
    }

    private <T> Optional<T> processCameraEntity(Function<Entity, T> processor) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity != null) {
            return Optional.ofNullable(processor.apply(entity));
        }
        return Optional.empty();
    }

    /**
     * 약실에 탄이 있는지 얻는다.
     *
     * @return 약실에 탄이 있는지. 오픈 볼트 방식 총기면 false를 돌려준다.
     */
    public boolean hasBulletInBarrel() {
        return processGunData((iGun, gunIndex) -> {
            Bolt boltType = gunData.getBolt();
            return boltType != Bolt.OPEN_BOLT && iGun.hasBulletInBarrel(currentGunItem);
        }).orElse(false);
    }

    public boolean isOverHeat() {
        return gunData.getHeatData() != null && iGun.isOverheatLocked(currentGunItem);
    }

    public float getHeatProgress() {
        return gunData.getHeatData() != null ?
                Mth.clamp(iGun.getHeatAmount(currentGunItem) / gunData.getHeatData().getHeatMax(), 0f, 1f) : 0f;
    }

    /**
     * 총기의 사격 간격(밀리초)을 얻는다
     *
     * @return 사격 간격
     */
    public long getShootInterval() {
        return processCameraEntity(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                FireMode fireMode = iGun.getFireMode(currentGunItem);
                if (fireMode == FireMode.BURST) {
                    long coolDown = (long) (gunData.getBurstData().getMinInterval() * 1000f);
                    return Math.max(coolDown, 0L);
                }
                long coolDown = gunData.getShootInterval(livingEntity, fireMode, currentGunItem);
                return Math.max(coolDown, 0L);
            }
            return 0L;
        }).orElse(0L);
    }

    /**
     * 마지막으로 사격한 timestamp(시스템 시각, 밀리초)를 돌려준다. 총기를 바꾸면 -1로 초기화된다.
     *
     * @return 마지막 사격 timestamp. 총기를 바꾸면 -1로 초기화된다.
     */
    public long getLastShootTimestamp() {
        return processGunOperator(operator -> operator.getDataHolder().clientLastShootTimestamp).orElse(-1L);
    }

    /**
     * 현재 시스템 시각(밀리초)을 얻는다.
     *
     * @return 현재 시스템 시각
     */
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 사격 간격을 조정한다(클라이언트 표시에만 적용).
     *
     * @param alpha 더하거나 뺄 사격 간격(밀리초). 양수면 간격이 늘고 음수면 준다.
     */
    public void adjustClientShootInterval(long alpha) {
        processGunOperator(operator -> {
            long timestamp = operator.getDataHolder().clientShootTimestamp;
            operator.getDataHolder().clientShootTimestamp = timestamp + alpha;
            return null;
        });
    }

    /**
     * 탄창 안의 탄약 수를 얻는다.
     *
     * @return 탄창 안의 탄약 수. 약실 안의 탄은 세지 않는다.
     */
    public int getAmmoCount() {
        return processGunData((iGun, gunIndex) -> iGun.getCurrentAmmoCount(currentGunItem)).orElse(0);
    }

    /**
     * 총기 탄창의 최대 탄약 수를 얻는다.
     *
     * @return 총기 탄창의 최대 탄약 수. 약실 안의 탄은 세지 않는다.
     */
    public int getMaxAmmoCount() {
        return processGunData(
                (iGun, gunIndex) ->
                        ShooterMagazineBonus.maxAmmoCount(Minecraft.getInstance().player, currentGunItem, gunData)
        ).orElse(0);
    }

    /**
     * 플레이어(또는 가상 예비 탄약)에게 소모할 탄약이 있는지 확인한다. 보통 반복 재장전을 끊을 때 쓴다.
     * 크리에이티브 플레이어는 바로 true를 돌려준다
     *
     * @return 플레이어(또는 가상 예비 탄약)에게 소모할 탄약이 있는지
     */
    public boolean hasAmmoToConsume() {
        if (!processRemoteGunOperator(IGunOperator::needCheckAmmo).orElse(true)) {
            return true;
        }
        if (iGun.useDummyAmmo(currentGunItem)) {
            return iGun.getDummyAmmoAmount(currentGunItem) > 0;
        }
        return processCameraEntity(entity -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        return livingEntity.tacz$getItemHandler(null)
                                .map(cap -> {
                                    // 인벤토리 확인
                                    for (int i = 0; i < cap.getSlots(); i++) {
                                        ItemStack checkAmmoStack = cap.getStackInSlot(i);
                                        if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(currentGunItem, checkAmmoStack)) {
                                            return true;
                                        }
                                        if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(currentGunItem, checkAmmoStack)) {
                                            return true;
                                        }
                                    }
                                    return false;
                                }).orElse(false);
                    }
                    return false;
                }
        ).orElse(false);
    }

    /**
     * 총기 확장 탄창 단계를 얻는다.
     *
     * @return 확장 단계(0~3). 0은 확장 탄창 없음, 1~3은 해당 단계의 확장 탄창 장착
     */
    public int getMagExtentLevel() {
        return processGunData(
                (iGun, gunIndex) ->
                        AttachmentDataUtils.getMagExtendLevel(currentGunItem, gunData)
        ).orElse(0);
    }

    /**
     * 총기의 현재 발사 모드를 얻는다.
     *
     * @return FireMode 열거형의 ordinal 값
     */
    public int getFireMode() {
        return processGunData((iGun, gunIndex) -> iGun.getFireMode(currentGunItem).ordinal()).orElse(0);
    }

    /**
     * 총을 든 플레이어의 조준 진행도를 얻는다.
     *
     * @return 조준 진행도(0~1).
     * 0은 조준 안 함, 1은 조준 완료.
     */
    public float getAimingProgress() {
        return processGunOperator(operator -> operator.getClientAimingProgress(partialTicks)).orElse(0f);
    }

    /**
     * 플레이어가 지금 조준 중인지 얻는다. 조준 중이면 aiming progress가 늘고, 아니면 준다.
     *
     * @return 플레이어가 지금 조준 중인지
     */
    public boolean isAiming() {
        return processGunOperator(IClientPlayerGunOperator::isAim).orElse(false);
    }

    /**
     * 플레이어의 사격 대기 시간을 얻는다.
     *
     * @return 플레이어의 사격 대기 시간(밀리초, ms)
     */
    public long getShootCoolDown() {
        return processGunOperator(IClientPlayerGunOperator::getClientShootCoolDown).orElse(0L);
    }

    /**
     * 플레이어의 재장전 상태를 얻는다
     *
     * @return 플레이어의 재장전 상태
     */
    public int getReloadStateType() {
        return processCameraEntity(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                return IGunOperator.fromLivingEntity(livingEntity).getSynReloadState().getStateType().ordinal();
            }
            return ReloadState.StateType.NOT_RELOADING.ordinal();
        }).orElse(ReloadState.StateType.NOT_RELOADING.ordinal());
    }

    /**
     * 플레이어 키 입력이 위쪽인지 얻는다.
     *
     * @return 플레이어 키 입력이 위쪽인지(이동의 앞으로 키, 예: W)
     */
    public boolean isInputUp() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.forward()).orElse(false);
    }

    /**
     * 플레이어 키 입력이 아래쪽인지 얻는다.
     *
     * @return 플레이어 키 입력이 아래쪽인지(이동의 뒤로 키, 예: S)
     */
    public boolean isInputDown() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.backward()).orElse(false);
    }

    /**
     * 플레이어 키 입력이 왼쪽인지 얻는다.
     *
     * @return 플레이어 키 입력이 왼쪽인지(이동의 왼쪽 키, 예: A)
     */
    public boolean isInputLeft() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.left()).orElse(false);
    }

    /**
     * 플레이어 키 입력이 오른쪽인지 얻는다.
     *
     * @return 플레이어 키 입력이 오른쪽인지(이동의 오른쪽 키, 예: D)
     */
    public boolean isInputRight() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.right()).orElse(false);
    }

    /**
     * 플레이어 키 입력이 점프인지 얻는다.
     *
     * @return 플레이어 키 입력이 점프인지(이동의 점프 키, 예: Space)
     */
    public boolean isInputJumping() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(player -> player.input.keyPresses.jump()).orElse(false);
    }

    /**
     * 플레이어가 지금 엎드려 기는 중인지 얻는다
     *
     * @return 플레이어가 지금 엎드려 기는 중인지
     */
    public boolean isCrawl() {
        return processGunOperator(IClientPlayerGunOperator::isCrawl).orElse(false);
    }

    /**
     * 플레이어가 땅에 닿아 있는지 얻는다
     *
     * @return 플레이어가 땅에 닿아 있는지
     */
    public boolean isOnGround() {
        return processCameraEntity(Entity::onGround).orElse(false);
    }

    /**
     * 플레이어가 웅크렸는지 얻는다
     *
     * @return 플레이어가 웅크렸는지
     */
    public boolean isCrouching() {
        return processCameraEntity(Entity::isCrouching).orElse(false);
    }

    /**
     * 플레이어가 지금 총을 비스듬히 들어야 하는지 얻는다
     * 웅크린 상태이고 총기가 비스듬히 들기를 허용해야 한다
     *
     * @return 플레이어가 지금 총을 비스듬히 들어야 하는지
     */
    public boolean shouldSlide() {
        return processCameraEntity(e -> e.isCrouching() && gunData.canSlide()).orElse(false);
    }

    /**
     * 플레이어의 현재 걷기 거리에 기준점을 찍는다. 이후 getWalkDist()는 이 기준점과의 상대값을 돌려준다
     */
    public void anchorWalkDist() {
        processCameraEntity(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                walkDistAnchor = tacz$walkDistance(livingEntity);
            }
            return null;
        });
    }

    /**
     * <b>원본 1.21.1과 같은 의미</b>의 걷기 거리를 얻는다(partialTick으로 보간함).
     *
     * <p><b>26.2 수정: 총 들고 걷는 애니메이션 "떨림"의 실제 원인 — 애니메이션 속도가 약 6.7배 빨랐다.</b></p>
     *
     * <p>원본 1.21.1은 {@code Entity#walkDist}를 썼다:</p>
     * <pre>
     * entity.walkDist + (entity.walkDist - entity.walkDistO) * partialTicks
     * </pre>
     *
     * <p>이식할 때 잘못해서 {@code livingEntity.walkAnimation.position(...)}으로 바꿨다.
     * <b>두 값은 같은 양이 아니다</b>(디컴파일 확인):</p>
     *
     * <table border="1">
     *   <tr><th></th><th>틱당 증가량</th><th>2.0 주기 한 번 걷기</th></tr>
     *   <tr><td>{@code moveDist}(= 예전 walkDist)</td>
     *       <td>{@code += 수평 이동량 * 0.6}</td><td>약 33틱 ≈ 1.67초</td></tr>
     *   <tr><td>{@code walkAnimation.position}</td>
     *       <td>{@code += min(이동량 * 4.0, 1.0)}(0.4 완화)</td><td>약 5틱 ≈ 0.25초</td></tr>
     * </table>
     *
     * <p>보통 걷기 속도 0.1칸/틱으로 계산하면 뒤쪽이 앞쪽의 약 <b>6.7배</b>다.
     * 그런데 총기 팩 Lua는 {@code setAnimationProgress(track, (getWalkDist() % 2.0) / 2.0)}로 구동하므로,
     * 2.0을 걷기 한 주기로 고정한다 — 그래서 걷기 애니메이션이 약 6.7배 속도로 재생됐다.
     * 둘 다 이동 속도에 <b>비례</b>하므로 "빨리 걸을수록 빨리 떨리고,
     * 느림 물약을 마시면 느려지는" 모습이 되었다. 본질은 떨림이 아니라 <b>빨라진 총 들고 걷기 애니메이션</b>이다.</p>
     *
     * <p>26.2에서 {@code Entity.walkDist}는 {@code Entity.moveDist}로 이름이 바뀌었고
     * (javap 확인: {@code public float moveDist}, 여전히 {@code += 수평 이동량 * 0.6}),
     * 의미가 예전 {@code walkDist}와 같으므로 이것으로 바꿨다.</p>
     *
     * <p><b>보간에 대해(7차 보충)</b>: {@code moveDist}에는 짝이 되는 {@code moveDistO}가 없다.
     * 6차에서는 "증가량이 작아 그대로 써도 계단 현상이 보이지 않을 것"이라 판단했는데 — <b>틀린 판단이었다</b>.
     * 실제로는 애니메이션이 눈에 띄게 뻑뻑하고 프레임이 떨어진 것처럼 보였다. 지금은 {@code EntityMixin}이 매 틱 HEAD에서 직전 틱의
     * {@code moveDist}를 기록해({@link IMoveDistTracker} 참고) {@code walkDistO}를 다시 만들고,
     * 원본과 <b>똑같은</b> 선형 보간을 해 올바른 단위와 부드러움을 함께 얻는다.</p>
     */
    private float tacz$walkDistance(LivingEntity livingEntity) {
        // ------------------------------------------------------------------
        // [우선 경로] 26.2 공식 walkDist/walkDistO 후계: ClientAvatarState
        //
        // 20차 수정: r6/r7은 "26.2가 walkDist를 Entity.moveDist로 이름만 바꿨고
        // 의미가 완전히 같다"고 판단했는데 — 절반만 맞고, 멀티플레이 환경에서는 틀렸다.
        //
        // 바이트코드 사실(Entity.move, 오프셋 601~648):
        //     if (level.isClientSide() && !isLocalInstanceAuthoritative()) -> 구간 전체 건너뜀
        //     ...
        //     applyMovementEmissionAndPlaySound(...)   // moveDist를 쓰는 유일한 곳
        // 이 조건을 단계별로 펼치면:
        //     Entity.isLocalInstanceAuthoritative() -> Player.isLocalClientAuthoritative()
        //         -> Player.isLocalPlayer()      = iconst_0  (기반 클래스는 항상 false)
        //            LocalPlayer.isLocalPlayer() = iconst_1  (자기 플레이어만)
        //
        // 즉 클라이언트에서 moveDist는 LocalPlayer만 쌓이고 RemotePlayer는 항상 0이다.
        // 그 결과 **다른 플레이어의 총 들고 걷는 애니메이션이 완전히 멈춘다**((0 % 2.0)/2.0은 항상 0)
        // 혼자 시험해서는 알 수 없다 — 자기 애니메이션은 정상이다.
        //
        // 26.2는 이를 위한 전용 수단을 제공한다(모두 바이트코드 확인):
        //     ClientAvatarState.walkDist / walkDistO                (private 필드)
        //     ClientAvatarState.addWalkDistance(float)              (walkDist += v)
        //     ClientAvatarState.tick(Vec3,Vec3)                     (walkDistO = walkDist)
        //     ClientAvatarState.getInterpolatedWalkDistance(float)  (Mth.lerp(pt, walkDistO, walkDist))
        // 구동하는 쪽은 LocalPlayer.move 끝부분: Mth.length(dx,dz) * 0.6F -> addWalkedDistance
        //     -> AbstractClientPlayer.addWalkedDistance -> clientAvatarState.addWalkDistance
        //
        // 핵심: 단위가 똑같이 **×0.6**이라 원본 1.21.1의 walkDist와 같다.
        // 또 getInterpolatedWalkDistance가 바로 원본이 손으로 쓴 보간식의 공식 구현이며,
        // 자기 플레이어와 원격 플레이어 **모두에 똑같이 동작**한다(RemotePlayer도 AbstractClientPlayer다).
        // 바닐라도 AvatarRenderer.extractCapeState에서 이렇게 쓴다.
        if (livingEntity instanceof AbstractClientPlayer clientPlayer) {
            return clientPlayer.avatarState().getInterpolatedWalkDistance(this.partialTicks);
        }

        // ------------------------------------------------------------------
        // [대체 경로] 플레이어가 아닌 엔티티(총을 든 좀비 등)는 moveDist를 쓴다.
        // 이런 엔티티는 클라이언트에서 서버가 위치를 동기화하므로 moveDist 조건을 똑같이 통과하지 못하지만,
        // ClientAvatarState도 없고 더 나은 값도 없다.
        // EntityMixin이 다시 만든 moveDistO는 여기서 여전히 의미가 있다(서버 쪽·싱글플레이).
        float moveDist = livingEntity.moveDist;
        if (livingEntity instanceof IMoveDistTracker tracker) {
            float moveDistO = tracker.tacz$getMoveDistO();
            return moveDist + (moveDist - moveDistO) * this.partialTicks;
        }
        return moveDist;
    }

    /**
     * 기준점 대비 걷기 거리를 얻는다. 기준점이 없으면 걷기 거리를 그대로 얻는다.
     *
     * @return 기준점 대비 걷기 거리. 기준점이 없으면 걷기 거리를 그대로 돌려준다.
     */

    public float getWalkDist() {
        return processCameraEntity(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                // 원본과 같은 단위(moveDist)여야 한다. 아니면 애니메이션이 약 6.7배 빨라진다. tacz$walkDistance 참고.
                float currentWalkDist = tacz$walkDistance(livingEntity);
                return currentWalkDist - walkDistAnchor;
            }
            return 0f;
        }).orElse(0f);
    }

    /**
     * 지정한 번호의 탄피 배출구에서 탄피 하나를 내보낸다
     *
     * @param index 탄피 배출구 번호
     */
    public void popShellFrom(int index) {
        if (display.getShellEjection() != null) {
            BedrockGunModel gunModel = display.getGunModel();
            if (gunModel != null) {
                ShellRender shellRender = gunModel.getShellRender(index);
                Vector3f velocity = display.getShellEjection().getRandomVelocity();
                if (shellRender != null) {
                    shellRender.addShell(velocity);
                }

                var lod = display.getLodModel();
                if (lod != null) {
                    ShellRender lodShell = lod.getLeft().getShellRender(index);
                    if (lodShell != null) {
                        lodShell.addShell(velocity);
                    }
                }
            }
        }
    }

    /**
     * 총기 display에 선언한 상태 기계 매개변수를 얻는다
     *
     * @return 상태 기계 매개변수 표
     */
    public LuaTable getStateMachineParams() {
        LuaTable param = display.getStateMachineParam();
        return param == null ? new LuaTable() : param;
    }

    /**
     * 현재 총기 아이템의 NBT 데이터 접근자를 얻는다.<br/>
     * 클라이언트 쪽에서 NBT 데이터를 바꾸면 서버 데이터와 어긋날 수 있으므로 바꾸지 않는다.<br/>
     * 상태 기계 스크립트에서는 읽기만 해야 한다
     *
     * @return NBT 데이터 접근자
     */
    public LuaNbtAccessor getNbtAccessor() {
        return nbtUtil;
    }

    /**
     * 총기의 부착물 ID를 얻는다
     *
     * @return 부착물 ID. 종류가 틀렸거나 해당 부착물이 없으면 빈 부착물 ID 'tacz:empty'
     */
    public String getAttachment(String type) {
        try {
            AttachmentType t = AttachmentType.valueOf(type);
            return iGun.getAttachmentId(currentGunItem, t).toString();
        } catch (IllegalArgumentException e) {
            return DefaultAssets.EMPTY_ATTACHMENT_ID.toString();
        }
    }

    /**
     * 현재 충전 진행도를 얻는다
     * @return 현재 충전 진행도
     */
    public float getChargeProgress() {
        return processGunOperator(IClientPlayerGunOperator::getChargeProgress).orElse(0f);
    }

    /**
     * 현재 총기의 최대 충전 값을 얻는다
     * @return 현재 총기의 최대 충전 값
     */
    public float getMaxCharge() {
        return processGunData((iGun, gunIndex) -> {
            var chargeData = gunData.getChargeData(iGun.getFireMode(currentGunItem));
            return chargeData != null ? chargeData.getMaxCharge() : 0f;
        }).orElse(0f);
    }

    /**
     * 현재 총기의 충전 발사 기준값을 얻는다(hold 모드에서만 유효)
     * @return 현재 총기의 충전 발사 기준값
     */
    public float getChargeThreshold() {
        return processGunData((iGun, gunIndex) -> {
            var chargeData = gunData.getChargeData(iGun.getFireMode(currentGunItem));
            return chargeData != null ? chargeData.getFireThreshold() : 0f;
        }).orElse(0f);
    }

    /**
     * 지금 충전 중인지 얻는다
     * @return 지금 충전 중인지
     */
    public boolean isCharging() {
        return processGunOperator(IClientPlayerGunOperator::isCharging).orElse(false);
    }

    /**
     * 상태 기계 스크립트에서 호출하지 않는다. 상태 기계를 갱신할 때 현재 아이템 객체를 설정하는 데 쓴다.
     */
    public void setCurrentGunItem(ItemStack currentGunItem) {
        this.currentGunItem = currentGunItem;
        this.iGun = IGun.getIGunOrNull(currentGunItem);
        if (iGun != null) {
            display = TimelessAPI.getGunDisplay(currentGunItem).orElse(null);
            gunData = TimelessAPI.getClientGunIndex(iGun.getGunId(currentGunItem))
                    .map(ClientGunIndex::getGunData).orElse(null);
        }
        if (currentGunItem.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != null) {
            nbtUtil = LuaNbtAccessor.from(currentGunItem);
        }
    }
}
