package fuzs.illagerinvasion.common.world.entity.monster;

import fuzs.illagerinvasion.common.init.ModEntityTypes;
import fuzs.illagerinvasion.common.init.ModSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class InvokerFangs extends EvokerFangs {

    public InvokerFangs(EntityType<? extends InvokerFangs> entityType, Level level) {
        super(entityType, level);
    }

    public InvokerFangs(Level level, double x, double y, double z, float yRot, int warmupDelay, LivingEntity owner) {
        this(ModEntityTypes.INVOKER_FANGS_ENTITY_TYPE.value(), level);
        // 상위 클래스에서 복사했으며 yRot 값만 바꿨다
        this.warmupDelayTicks = warmupDelay;
        this.setOwner(owner);
        this.setYRot(yRot * 57.295776F);
        this.setPos(x, y, z);
    }

    @Override
    public void dealDamageTo(LivingEntity target) {
        LivingEntity owner = this.getOwner();
        if (target.isAlive() && !target.isInvulnerable() && target != owner) {
            // 상위 클래스에서 복사했으며 피해를 늘리고 밀치기 효과를 더했다
            if (owner == null) {
                target.hurt(this.damageSources().magic(), 10.0F);
                target.push(0.0, 1.7, 0.0);
            } else if (!owner.isAlliedTo(target)) {
                DamageSource damageSource = this.damageSources().indirectMagic(this, owner);
                if (this.level() instanceof ServerLevel serverLevel && target.hurtServer(serverLevel,
                        damageSource,
                        6.0F)) {
                    EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
                    target.push(0.0, 0.6, 0.0);
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == EntityEvent.START_ATTACKING) {
            this.clientSideAttackStarted = true;
            if (!this.isSilent()) {
                // 상위 클래스에서 복사했으며 사용자 정의 효과음 이벤트를 쓴다
                this.level()
                        .playLocalSound(this.getX(),
                                this.getY(),
                                this.getZ(),
                                ModSoundEvents.INVOKER_FANGS_SOUND_EVENT.value(),
                                this.getSoundSource(),
                                1.0F,
                                this.random.nextFloat() * 0.2F + 0.85F,
                                false);
            }
        }
    }
}
