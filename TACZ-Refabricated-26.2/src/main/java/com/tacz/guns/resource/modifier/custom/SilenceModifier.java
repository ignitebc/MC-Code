package com.tacz.guns.resource.modifier.custom;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.modifier.CacheValue;
import com.tacz.guns.api.modifier.IAttachmentModifier;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.config.common.GunConfig;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.modifier.ModifierText;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class SilenceModifier implements IAttachmentModifier<Pair<Modifier, Boolean>, Pair<Integer, Boolean>> {
    public static final String ID = GunProperties.SILENCE.name();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    @SuppressWarnings("deprecation")
    public SilenceJsonProperty readJson(String json) {
        Data data = CommonAssetsManager.GSON.fromJson(json, Data.class);
        Silence silence = data.getSilence();
        if (silence == null) {
            return new SilenceJsonProperty(Pair.of(new Modifier(), false));
        }
        Modifier distance = silence.getDistance();
        // 兼容旧版本
        if (distance == null) {
            distance = new Modifier();
            distance.setAddend(silence.getDistanceAddend());
        }
        return new SilenceJsonProperty(Pair.of(distance, silence.isUseSilenceSound()));
    }

    @Override
    public CacheValue<Pair<Integer, Boolean>> initCache(ItemStack gunItem, GunData gunData) {
        int defaultDistance = GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get();
        Pair<Modifier, Boolean> builtinSilence = getBuiltinMuzzleSilence(gunData);
        if (builtinSilence == null) {
            return new CacheValue<>(Pair.of(defaultDistance, false));
        }
        double builtinDistance = AttachmentPropertyManager.eval(builtinSilence.left(), defaultDistance);
        return new CacheValue<>(Pair.of((int) Math.round(builtinDistance), builtinSilence.right()));
    }

    /**
     * 일체형 총구(builtin_attachments.muzzle)의 소음 수치를 읽는다.
     * <p>
     * 일체형 부착물은 실제 장착 슬롯에 들어 있지 않아 {@code AttachmentDataUtils.getAllAttachmentData} 가 모으는 효과에서
     * 빠진다. 그래서 VSS 처럼 소음기를 내장한 총이 총성 거리와 소음기 음원 판단에서는 일반 총으로 계산됐다.
     * 다른 능력치까지 일체형 부착물에서 끌어오면 AUG, P90 의 내장 조준경 무게와 조준 시간이 함께 바뀌므로 소음 효과만 읽는다.
     *
     * @return 소음 수치. 일체형 총구가 없거나 소음 수치가 없으면 null
     */
    @Nullable
    private static Pair<Modifier, Boolean> getBuiltinMuzzleSilence(GunData gunData) {
        Identifier muzzleId = gunData.getBuiltInAttachments().get(AttachmentType.MUZZLE);
        if (muzzleId == null) {
            return null;
        }
        AttachmentData attachmentData = gunData.getExclusiveAttachments().get(muzzleId);
        if (attachmentData == null) {
            attachmentData = TimelessAPI.getCommonAttachmentIndex(muzzleId).map(index -> index.getData()).orElse(null);
        }
        if (attachmentData == null) {
            return null;
        }
        JsonProperty<?> silenceProperty = attachmentData.getModifier().get(ID);
        if (silenceProperty == null) {
            return null;
        }
        Object value = silenceProperty.getValue();
        if (value instanceof Pair<?, ?> pair && pair.left() instanceof Modifier distance && pair.right() instanceof Boolean useSilenceSound) {
            return Pair.of(distance, useSilenceSound);
        }
        return null;
    }

    @Override
    public void eval(List<Pair<Modifier, Boolean>> modifiedValues, CacheValue<Pair<Integer, Boolean>> cache) {
        List<Modifier> distanceModifiers = Lists.newArrayList();
        List<Boolean> useSilenceSoundModifiers = Lists.newArrayList();
        modifiedValues.forEach(v -> {
            distanceModifiers.add(v.left());
            useSilenceSoundModifiers.add(v.right());
        });
        Pair<Integer, Boolean> cacheValue = cache.getValue();
        double evalDistance = AttachmentPropertyManager.eval(distanceModifiers, cacheValue.left());
        boolean useSilenceSound = AttachmentPropertyManager.eval(useSilenceSoundModifiers, cacheValue.right());
        cache.setValue(Pair.of((int) Math.round(evalDistance), useSilenceSound));
    }

    public static class SilenceJsonProperty extends JsonProperty<Pair<Modifier, Boolean>> {
        public SilenceJsonProperty(Pair<Modifier, Boolean> value) {
            super(value);
        }

        @Override
        public void initComponents() {
            Pair<Modifier, Boolean> value = this.getValue();
            if (value != null) {
                int defaultDistance = GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get();
                double eval = AttachmentPropertyManager.eval(value.left(), defaultDistance);
                int distance = (int) Math.round(eval);
                if (distance > defaultDistance) {
                    components.add(ModifierText.line("tooltip.tacz.attachment.sound_distance.increase", value.left(), "m", 0xFF5555));
                } else if (distance < defaultDistance) {
                    components.add(ModifierText.line("tooltip.tacz.attachment.sound_distance.decrease", value.left(), "m", 0x55FF55));
                }
                if (value.right()) {
                    components.add(Component.translatable("tooltip.tacz.attachment.silence").withStyle(style -> style.withColor(0x55FF55)));
                }
            }
        }
    }

    private static class Data {
        @Nullable
        @SerializedName("silence")
        private Silence silence = null;

        @Nullable
        public Silence getSilence() {
            return silence;
        }
    }

    private static class Silence {
        @Deprecated
        @SerializedName("distance_addend")
        private int distanceAddend = 0;

        @Nullable
        @SerializedName("distance")
        private Modifier distance = null;

        @SerializedName("use_silence_sound")
        private boolean useSilenceSound = false;

        @Deprecated
        public int getDistanceAddend() {
            return distanceAddend;
        }

        @Nullable
        public Modifier getDistance() {
            return distance;
        }

        public boolean isUseSilenceSound() {
            return useSilenceSound;
        }
    }
}
