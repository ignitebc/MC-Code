package com.daqem.arc.data.reward.entity;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

public class MultipleArrowsReward extends AbstractReward {

    private final int amount;

    public MultipleArrowsReward(double chance, int priority, int amount) {
        super(chance, priority);
        this.amount = amount;
    }

    @Override
    public Component getDescription() {
        return getDescription(amount);
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.MULTIPLE_ARROWS;
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        Entity entity = actionData.getData(ActionDataType.ENTITY);
        if (entity instanceof AbstractArrow shotArrow) {
            Player player = actionData.getPlayer().arc$getPlayer();
            ItemStack bow;
            if (player.getMainHandItem().getItem() instanceof BowItem) {
                bow = player.getMainHandItem();
            } else if (player.getOffhandItem().getItem() instanceof BowItem) {
                bow = player.getOffhandItem();
            } else {
                return new ActionResult();
            }
            if (bow.getItem() instanceof BowItem bowItem) {
                float power = BowItem.getPowerForTime(bowItem.getUseDuration(bow, player) - player.getUseItemRemainingTicks());
                float[] afloat = getShotPitches(new Random());
                int[] arrowPositions = scatterArrows(amount);
                for (int i = 0; i < amount; i++) {
                    shootProjectile(shotArrow, player.level(), player, bow, Items.ARROW.getDefaultInstance(), afloat[1], power * 3, arrowPositions[i]);
                }
            }
        }
        return new ActionResult();
    }

    public static int[] scatterArrows(int numArrows) {
        if (numArrows < 1 || numArrows > 20) {
            throw new IllegalArgumentException("Number of arrows should be between 1 and 20.");
        }

        int[] arrowPositions = new int[numArrows];
        double interval = 20.0 / (numArrows - 1);

        for (int i = 0; i < numArrows; i++) {
            arrowPositions[i] = (int) (-10 + i * interval);
        }

        return arrowPositions;
    }

    private void shootProjectile(AbstractArrow shotArrow, Level level, LivingEntity livingEntity, ItemStack bow, ItemStack arrow,
                                 float shotPitch, float power, float pitch) {
        if (livingEntity instanceof Player player) {
            AbstractArrow projectile = new AbstractArrow(EntityType.ARROW, livingEntity, level, arrow, bow) {

                @Override
                protected @NotNull ItemStack getDefaultPickupItem() {
                    return arrow;
                }

                @Override
                protected void onHitBlock(@NotNull BlockHitResult blockHitResult) {
                    super.onHitBlock(blockHitResult);
                    this.discard();
                }
            };
            projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            Vec3 vec3 = player.getUpVector(1.0f);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis(pitch * ((float)Math.PI / 180), vec3.x, vec3.y, vec3.z);
            Vec3 vec32 = player.getViewVector(1.0f);
            Vector3f vector3f = vec32.toVector3f().rotate(quaternionf);
            projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), power, 1.0F);

            bow.hurtAndBreak(1, player, player.getUsedItemHand());
            level.addFreshEntity(projectile);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, shotPitch);
        }
    }

    private float[] getShotPitches(Random random) {
        boolean flag = random.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(flag, random), getRandomShotPitch(!flag, random)};
    }

    private float getRandomShotPitch(boolean p_150798_, Random random) {
        float f = p_150798_ ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }

    public int getAmount() {
        return amount;
    }

    public static class Serializer implements IRewardSerializer<MultipleArrowsReward> {

        @Override
        public MultipleArrowsReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new MultipleArrowsReward(
                    chance,
                    priority,
                    Math.min(20, Math.max(1, GsonHelper.getAsInt(jsonObject, "amount"))));
        }

        @Override
        public MultipleArrowsReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new MultipleArrowsReward(
                    chance,
                    priority,
                    friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, MultipleArrowsReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.amount);
        }
    }
}
