package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.api.event.EntityRemoveEvent;
import cn.sh1rocu.tacz.api.extension.IEntityPersistentData;
import cn.sh1rocu.tacz.api.extension.IMoveDistTracker;
import com.tacz.guns.entity.sync.core.DataHolderCapabilityProvider;
import com.tacz.guns.entity.sync.core.SyncedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements IEntityPersistentData, IMoveDistTracker {
    @Shadow
    private Level level;

    /**
     * 26.2에서 제거된 {@code walkDistO}를 다시 만든다.
     *
     * <p>원본 1.21.1은 {@code walkDist} / {@code walkDistO}를 보간해 총 들고 걷는 애니메이션을 움직였다.
     * 26.2는 {@code walkDist}를 {@code moveDist}로 바꾸면서 {@code walkDistO}를 <b>남기지 않았다</b>.
     * {@code moveDist}를 그대로 쓰면 구동 값이 게임 틱(20Hz)마다 한 번만 바뀌고,
     * 렌더링은 프레임(60~144Hz)마다 돌아 계단처럼 끊긴다 — 보기에는 "프레임 저하"처럼 느껴진다.</p>
     *
     * <p>그래서 매 틱 HEAD에서 <b>직전 틱이 끝났을 때의</b> moveDist를 기록해,
     * {@code GunAnimationStateContext#getWalkDist()}가 원본과 같은 선형 보간을 하게 한다.</p>
     */
    @Unique
    private float tacz$moveDistO;

    @Unique
    private boolean tacz$moveDistInit;

    @Unique
    @Override
    public float tacz$getMoveDistO() {
        // 초기화 전에는 현재 값을 돌려줘 증가량을 0으로 만들고, 첫 프레임이 튀지 않게 한다.
        return this.tacz$moveDistInit ? this.tacz$moveDistO : ((Entity) (Object) this).moveDist;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tacz$captureMoveDistO(CallbackInfo ci) {
        this.tacz$moveDistO = ((Entity) (Object) this).moveDist;
        this.tacz$moveDistInit = true;
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void remove(Entity.RemovalReason reason, CallbackInfo ci) {
        // 데이터 홀더 수명 주기는 양쪽 논리 측면 모두에 있다. 이 이벤트를 클라이언트로 한정하면
        // 제거된 비플레이어 엔티티마다 서버 쪽 provider가 새어 나갔다.
        EntityRemoveEvent event = new EntityRemoveEvent((Entity) (Object) this);
        EntityRemoveEvent.EVENT.invoker().onEntityRemove(event);
    }

    @Unique
    private CompoundTag tacz$persistentData;

    @Unique
    @Override
    public CompoundTag tacz$getPersistentData() {
        if (this.tacz$persistentData == null) {
            this.tacz$persistentData = new CompoundTag();
        }
        return tacz$persistentData;
    }

    @Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V"))
    private void tacz$savePersistentData(ValueOutput output, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        DataHolderCapabilityProvider.maybeGet(self)
                .ifPresent(provider -> provider.writeToNbt(this.tacz$getPersistentData()));
        if (this.tacz$persistentData != null) {
            output.store("ForgeData", CompoundTag.CODEC, this.tacz$persistentData.copy());
        }
    }

    /**
     * <h2>몬스터 스포너 극심한 프레임 저하 수정(27차)</h2>
     *
     * <p><b>증상</b>: 이 모드를 설치한 뒤 스포너를 보면 프레임이 급락하고 GPU 사용률이 높게 유지되며,
     * 스포너 안의 회전하는 엔티티와 연기·불꽃 입자가 <b>전혀 그려지지 않았다</b>.</p>
     *
     * <p><b>바닐라 쪽 증폭 요인</b>(26.2 바이트코드 확인):
     * {@code BaseSpawner#getOrCreateDisplayEntity}는 {@code displayEntity == null}일 때만
     * 엔티티를 만든다. 그런데 {@code EntityType.loadEntityRecursive}가 실패하면 null을 돌려주므로
     * {@code displayEntity}가 계속 null로 남고 → {@code SpawnerRenderer#extractRenderState}가
     * <b>매 프레임 엔티티 역직렬화를 처음부터 다시 시도</b>한다.
     * 게다가 {@code clientTick}의 입자 발생도 같은 {@code displayEntity != null}
     * 분기 안(오프셋 20-24)에 있어서, null이면 <b>입자를 하나도 내지 않는다</b> — 이것이 "효과가 안 그려지는" 현상과
     * 프레임 저하의 원인이 같은 이유다.</p>
     *
     * <p><b>이 모드 쪽 비용 원인</b>: {@code Entity#load()}마다 무조건
     * {@code DataHolderCapabilityProvider.get()}을 호출했는데, 이것은
     * {@code Collections.synchronizedMap(WeakHashMap)}에 대한 {@code computeIfAbsent}다 —
     * <b>전역 잠금</b>을 잡고, 접근할 때마다 {@code ReferenceQueue}를 훑어 사라진 약한 참조를 정리한다.
     * 위의 "매 프레임 재시도"와 겹쳐 매 프레임 전역 잠금 경합이 생겼다.</p>
     *
     * <p>더 나쁜 점: display entity는 {@code Entity#remove}를 <b>절대 호출하지 않는다</b>
     * (렌더링용 임시 객체라 월드에 들어가지 않는다). 그래서
     * {@code CapabilityRegistry}에 등록한 {@code EntityRemoveEvent} 정리 로직이
     * <b>한 번도 실행되지 않고</b> — 항목은 GC가 약한 참조를 회수할 때까지 남아 맵 부담을 더 키운다.</p>
     *
     * <p><b>수정</b>: <b>지연 생성</b>으로 바꿨다 — 저장 데이터에 DataHolder 데이터가 실제로 있을 때만
     * provider를 만든다. 대부분의 엔티티(매 프레임 다시 만드는 display entity 포함)는 이 NBT 구역이 아예 없으므로,
     * 그 전역 맵을 전혀 건드리지 않아 잠금 경합이 사라진다.</p>
     *
     * <p>{@code hasSyncedDataKey} 판정은 <b>그대로 둔다</b>. 순수 {@code HashMap} 캐시 조회라
     * 비용이 거의 없고 관계없는 엔티티 대부분을 걸러 준다. 정말 비싼 것은 그 뒤의 {@code get()}이다.</p>
     */
    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V"))
    private void tacz$loadPersistentData(ValueInput input, CallbackInfo ci) {
        input.read("ForgeData", CompoundTag.CODEC).ifPresent(tag -> this.tacz$persistentData = tag);
        Entity self = (Entity) (Object) this;
        if (!SyncedEntityData.instance().hasSyncedDataKey(self.getClass())) {
            return;
        }
        // 지연 생성: 저장된 DataHolder가 없으면 provider를 만들지 않는다.
        // tacz$persistentData가 null이면 이 엔티티에는 ForgeData 구역이 아예 없다는 뜻이다.
        CompoundTag persisted = this.tacz$persistentData;
        if (persisted == null || persisted.getListOrEmpty("DataHolder").isEmpty()) {
            return;
        }
        DataHolderCapabilityProvider.get(self).readFromNbt(persisted);
    }
}