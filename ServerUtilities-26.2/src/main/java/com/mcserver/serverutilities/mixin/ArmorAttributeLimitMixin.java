package com.mcserver.serverutilities.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Attributes.class)
public abstract class ArmorAttributeLimitMixin {
    // 방어도 생성 구간으로 한정해야 같은 생성자를 쓰는 다른 속성의 상한이 바뀌지 않는다.
    @ModifyArg(
            method = "<clinit>",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/RangedAttribute;<init>(Ljava/lang/String;DDD)V"),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=attribute.name.armor"),
                    to = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/ai/attributes/Attributes;ARMOR:Lnet/minecraft/core/Holder;")),
            index = 3,
            require = 1,
            allow = 1)
    private static double serverutilities$expandArmorLimit(double originalMaximum) {
        return 1024.0D;
    }

    // 방어 강도도 같은 방식으로 확장한다. 서리빛 풀세트가 기본 상한 20에 정확히 닿아 여유가 없었다.
    @ModifyArg(
            method = "<clinit>",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/RangedAttribute;<init>(Ljava/lang/String;DDD)V"),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=attribute.name.armor_toughness"),
                    to = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/ai/attributes/Attributes;"
                                    + "ARMOR_TOUGHNESS:Lnet/minecraft/core/Holder;")),
            index = 3,
            require = 1,
            allow = 1)
    private static double serverutilities$expandArmorToughnessLimit(double originalMaximum) {
        return 1024.0D;
    }
}
