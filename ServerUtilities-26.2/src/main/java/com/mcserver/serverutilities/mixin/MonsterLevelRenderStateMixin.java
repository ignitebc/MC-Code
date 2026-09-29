package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.client.MonsterLevelRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
abstract class MonsterLevelRenderStateMixin implements MonsterLevelRenderState {
    @Unique private int serverutilities$labelLevel;
    @Unique private Vec3 serverutilities$labelAttachment;

    @Override
    public int serverutilities$labelLevel() { return serverutilities$labelLevel; }

    @Override
    public Vec3 serverutilities$labelAttachment() { return serverutilities$labelAttachment; }

    @Override
    public void serverutilities$setLabel(int level, Vec3 attachment) {
        serverutilities$labelLevel = level;
        serverutilities$labelAttachment = attachment;
    }
}
