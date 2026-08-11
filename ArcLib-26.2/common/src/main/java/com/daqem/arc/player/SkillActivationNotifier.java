package com.daqem.arc.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.player.ArcPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SkillActivationNotifier {

    private static final Pattern TIER_SUFFIX_PATTERN = Pattern.compile("\\s+[IVX]+$");

    private SkillActivationNotifier() {
    }

    /**
     * 액션을 발동시킨 홀더(파워업·직업 등)의 표시 이름을 돌려준다.
     * 이름이 없으면 일반 문구("스킬")로 대체해 항상 표시 가능한 이름을 보장한다.
     */
    public static Component resolveSkillName(ActionData actionData) {
        IActionHolder sourceActionHolder = actionData.getSourceActionHolder();
        if (sourceActionHolder != null) {
            Component displayName = sourceActionHolder.getDisplayName();
            if (displayName != null) {
                return stripTierSuffix(displayName);
            }
        }
        return Component.translatable("arc.skill.unknown_skill");
    }

    /**
     * 알림에는 단계 구분 없이 스킬명만 노출하도록 이름 끝의 로마 숫자 단계(I~X)를 제거한다.
     * 번역이 서버에 로드되지 않아 이름을 문자열로 풀 수 없으면 원본 컴포넌트를 그대로 돌려준다.
     */
    public static Component stripTierSuffix(Component skillName) {
        String resolvedName = skillName.getString();
        Matcher tierMatcher = TIER_SUFFIX_PATTERN.matcher(resolvedName);
        if (!tierMatcher.find()) {
            return skillName;
        }

        String baseName = resolvedName.substring(0, tierMatcher.start());
        if (baseName.isBlank()) {
            return skillName;
        }
        return Component.literal(baseName).setStyle(skillName.getStyle());
    }

    public static void notifyExtraDrop(ActionData actionData, ItemStack itemStack) {
        notifyExtraDrop(actionData, List.of(itemStack));
    }

    public static void notifyExtraDrop(ActionData actionData, List<ItemStack> itemStacks) {
        if (actionData.getPlayer().arc$getPlayer() instanceof ServerPlayer serverPlayer) {
            notifyExtraDrop(serverPlayer, resolveSkillName(actionData), itemStacks);
        }
    }

    public static void notifyExtraDrop(ServerPlayer player, @Nullable Component skillName, ItemStack itemStack) {
        notifyExtraDrop(player, skillName, List.of(itemStack));
    }

    public static void notifyExtraDrop(ServerPlayer player, @Nullable Component skillName, List<ItemStack> itemStacks) {
        List<ItemStack> mergedStacks = mergeStacks(itemStacks);
        if (mergedStacks.isEmpty()) {
            return;
        }

        MutableComponent items = Component.empty();
        for (int index = 0; index < mergedStacks.size(); index++) {
            ItemStack stack = mergedStacks.get(index);
            if (index > 0) {
                items.append(Component.literal(", "));
            }
            items.append(Component.literal("'"))
                    .append(stack.getHoverName())
                    .append(Component.literal("' x" + stack.getCount()));
        }

        Component resolvedSkillName = skillName != null ? skillName : Component.translatable("arc.skill.unknown_skill");
        player.sendSystemMessage(Component.translatable("arc.skill.extra_drop", resolvedSkillName, items)
                .withStyle(ChatFormatting.GOLD));
    }

    /**
     * 추가로 얻은 아이템을 정확히 셀 수 없는 스킬이 발동 사실만 알릴 때 사용한다.
     * 알림은 시전한 플레이어에게만 전송된다.
     */
    public static void notifySkillActivated(ArcPlayer player, Component message) {
        if (player.arc$getPlayer() instanceof ServerPlayer serverPlayer) {
            notifySkillActivated(serverPlayer, message);
        }
    }

    public static void notifySkillActivated(ServerPlayer player, Component message) {
        player.sendSystemMessage(message.copy().withStyle(ChatFormatting.GOLD));
    }

    private static List<ItemStack> mergeStacks(List<ItemStack> itemStacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.isEmpty()) {
                continue;
            }

            ItemStack existing = merged.stream()
                    .filter(stack -> ItemStack.isSameItemSameComponents(stack, itemStack))
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                merged.add(itemStack.copy());
            } else {
                existing.grow(itemStack.getCount());
            }
        }
        return merged;
    }
}
