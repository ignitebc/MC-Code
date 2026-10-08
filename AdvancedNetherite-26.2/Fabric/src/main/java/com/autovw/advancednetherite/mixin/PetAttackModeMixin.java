package com.autovw.advancednetherite.mixin;

import com.autovw.advancednetherite.common.pet.PetAttackMode;
import com.autovw.advancednetherite.common.pet.PetAttackModeAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 펫 공격 방식을 플레이어 저장 데이터에 둔다. 사망·재접속·차원 이동 후에도 고른 방식을 유지한다. */
@Mixin(ServerPlayer.class)
public abstract class PetAttackModeMixin implements PetAttackModeAccess
{
    @Unique
    private static final String advancednetherite$PET_ATTACK_MODE_TAG = "AdvancedNetheritePetAttackMode";

    @Unique
    private PetAttackMode advancednetherite$petAttackMode = PetAttackMode.AUTO;

    @Override
    public PetAttackMode advancednetherite$getPetAttackMode()
    {
        return this.advancednetherite$petAttackMode;
    }

    @Override
    public void advancednetherite$setPetAttackMode(PetAttackMode mode)
    {
        this.advancednetherite$petAttackMode = mode;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void advancednetherite$savePetAttackMode(ValueOutput valueOutput, CallbackInfo ci)
    {
        valueOutput.putString(advancednetherite$PET_ATTACK_MODE_TAG, this.advancednetherite$petAttackMode.name());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void advancednetherite$loadPetAttackMode(ValueInput valueInput, CallbackInfo ci)
    {
        // 이 값이 생기기 전에 저장된 플레이어는 기존처럼 자동공격이다.
        String savedMode = valueInput.getStringOr(advancednetherite$PET_ATTACK_MODE_TAG, PetAttackMode.AUTO.name());
        this.advancednetherite$petAttackMode = PetAttackMode.fromName(savedMode);
    }

    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("TAIL"))
    private void advancednetherite$copyPetAttackMode(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci)
    {
        if (oldPlayer instanceof PetAttackModeAccess oldAccess)
        {
            this.advancednetherite$petAttackMode = oldAccess.advancednetherite$getPetAttackMode();
        }
    }
}
