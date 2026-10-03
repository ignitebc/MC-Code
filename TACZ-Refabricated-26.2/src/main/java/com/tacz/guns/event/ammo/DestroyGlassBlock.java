package com.tacz.guns.event.ammo;

import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class DestroyGlassBlock {
    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        Level level = event.getLevel();
        BlockState state = event.getState();
        BlockPos pos = event.getHitResult().getBlockPos();
        EntityKineticBullet ammo = event.getAmmo();
        if (isBreakableGlass(state)) {
            level.destroyBlock(pos, false, ammo.getOwner());
        }
    }

    /** 탄에 맞으면 깨지는 유리 계열 블록인지. 몬스터 엄폐물 판정도 같은 기준을 쓴다. */
    public static boolean isBreakableGlass(BlockState state) {
        Block stateBlock = state.getBlock();
        NoteBlockInstrument instrument = state.instrument();
        return AmmoConfig.DESTROY_GLASS.get() && (stateBlock instanceof HalfTransparentBlock ||
                stateBlock instanceof StainedGlassPaneBlock ||
                (stateBlock instanceof IronBarsBlock && instrument.equals(NoteBlockInstrument.HAT)));
    }
}
