package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.common.pet.PetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

/**
 * 근접 공격으로 겨눌 대상을 고를 때 내 펫은 건너뛰어, 펫 뒤의 몹이나 블록을 겨눈다.
 * <p>
 * 펫은 주인의 공격에 피해를 받지 않으므로, 겨냥이 펫에 걸리면 펫 너머의 몹을 때릴 수 없다.
 * 일반 근접 공격({@code pick})과 창처럼 공격 거리가 정해진 무기({@code raycastHitResult}) 모두 같은 조건을 쓴다.
 */
@Mixin(LocalPlayer.class)
public abstract class PetPickPassThroughMixin
{
    @Redirect(method = "raycastHitResult", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
            target = "Lnet/minecraft/world/entity/EntitySelector;CAN_BE_PICKED:Ljava/util/function/Predicate;"))
    private Predicate<Entity> advancednetherite$skipOwnPetsForRangedAttack()
    {
        return advancednetherite$pickableExceptPetsOf((LocalPlayer) (Object) this);
    }

    @Redirect(method = "pick", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
            target = "Lnet/minecraft/world/entity/EntitySelector;CAN_BE_PICKED:Ljava/util/function/Predicate;"))
    private static Predicate<Entity> advancednetherite$skipOwnPets()
    {
        return advancednetherite$pickableExceptPetsOf(Minecraft.getInstance().player);
    }

    @Unique
    private static Predicate<Entity> advancednetherite$pickableExceptPetsOf(@Nullable LocalPlayer player)
    {
        return entity -> EntitySelector.CAN_BE_PICKED.test(entity) && !PetManager.isPetOf(entity, player);
    }
}
