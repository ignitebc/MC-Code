package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.boss.BossHealthRules;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Attributes.class)
public abstract class HealthAttributeLimitMixin {
    // 체력 2,000이 서버와 클라이언트 양쪽에서 1,024로 잘리지 않도록 해당 속성만 확장한다.
    @ModifyArg(method = "<clinit>",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/RangedAttribute;<init>(Ljava/lang/String;DDD)V"),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=attribute.name.max_health"),
                    to = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/ai/attributes/Attributes;MAX_HEALTH:Lnet/minecraft/core/Holder;")),
            index = 3, require = 1, allow = 1)
    private static double serverutilities$expandHealthLimit(double originalMaximum) {
        return BossHealthRules.MAX_HEALTH_LIMIT;
    }
}
