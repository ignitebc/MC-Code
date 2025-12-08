package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.data.condition.NotCondition;
import com.daqem.arc.data.condition.OrCondition;
import com.daqem.arc.data.condition.block.BlockCondition;
import com.daqem.arc.data.condition.block.BlocksCondition;
import com.daqem.arc.data.condition.block.NotInBlockPosCacheCondition;
import com.daqem.arc.data.condition.block.ore.IsOreCondition;
import com.daqem.arc.data.condition.block.properties.BlockHardnessCondition;
import com.daqem.arc.data.condition.item.ItemCondition;
import com.daqem.arc.data.condition.item.ItemsCondition;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.components.conditions.BlockConditionComponent;
import com.daqem.jobsplus.client.gui.jobs.components.conditions.ItemConditionComponent;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.TextComponent;
import com.daqem.uilib.gui.component.text.TruncatedTextComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.function.Supplier;

public class ConditionsComponent extends EmptyComponent {

    private static List<Block> ORE_BLOCKS = null;

    public ConditionsComponent(List<ICondition> conditions, Supplier<ScreenRectangle> parentBounds) {
        super(0, 0, 99, 9);

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
        {
            return;
        }

        if (conditions.isEmpty()) 
        {
            TruncatedTextComponent noConditionsText = new TruncatedTextComponent(0, 2, getWidth(), JobsPlus.translatable("gui.jobs.no_conditions"), 0xFF1E1410);
            this.addComponent(noConditionsText);
            this.setHeight(noConditionsText.getHeight());

        } 
        
        else 
        {
            TruncatedTextComponent title = new TruncatedTextComponent(0, 2, getWidth(), JobsPlus.translatable("gui.jobs.conditions").withStyle(style -> style.withUnderlined(true)), 0xFF1E1410);
            this.addComponent(title);

            int yOffset = title.getHeight() + 4;
            Set<ICondition> parsedConditions = new HashSet<>();
            List<NotCondition> notConditions = getNotConditions(conditions);

            conditions = conditions.stream().sorted(Comparator.comparing(x -> 
            {
                if (x instanceof NotCondition)
                    return 3;
                if (x instanceof OrCondition)
                    return 2;
                if (x instanceof BlockCondition || x instanceof BlocksCondition || x instanceof ItemCondition || x instanceof ItemsCondition)
                    return 0;
                return 1;
            })).toList();

            for (ICondition condition : conditions) 
            {
                if (parsedConditions.contains(condition))
                    continue;

                if (condition instanceof NotInBlockPosCacheCondition || condition instanceof OrCondition) 
                {
                    parsedConditions.add(condition);
                    continue;
                }

                if (condition instanceof NotCondition)
                    continue;

                // 블록 조건 묶음
                if (condition instanceof BlockCondition || condition instanceof BlocksCondition) 
                {
                    List<ICondition> blockConditions = conditions.stream()
                            .filter(c -> c instanceof BlockCondition
                                    || c instanceof BlocksCondition
                                    || c instanceof BlockHardnessCondition)
                            .toList();
                    List<ICondition> notBlockConditions = notConditions.stream()
                            .map(NotCondition::getConditions)
                            .flatMap(List::stream)
                            .filter(c -> c instanceof BlockCondition
                                    || c instanceof BlocksCondition
                                    || c instanceof BlockHardnessCondition
                                    || c instanceof IsOreCondition)
                            .toList();
                    Set<Block> allowedBlocks = new HashSet<>();
                    Set<Block> deniedBlocks = new HashSet<>();

                    for (ICondition blockCondition : blockConditions) 
                    {
                        if (blockCondition instanceof BlockCondition bc) 
                        {
                            allowedBlocks.add(bc.getBlock());
                            parsedConditions.add(blockCondition);
                        } 

                        else if (blockCondition instanceof BlocksCondition bcs) 
                        {
                            allowedBlocks.addAll(bcs.getAllBlocks(level.registryAccess()));
                            parsedConditions.add(blockCondition);
                        }
                    }

                    for (ICondition blockCondition : blockConditions) 
                    {
                        if (blockCondition instanceof BlockHardnessCondition bhc) 
                        {
                            for (Block allowedBlock : allowedBlocks) 
                            {
                                float hardness = allowedBlock.defaultBlockState().getDestroySpeed(Objects.requireNonNull(level), BlockPos.ZERO);
                                if (bhc.getMin() > hardness) 
                                {
                                    deniedBlocks.add(allowedBlock);
                                }

                                if (bhc.getMax() < hardness) 
                                {
                                    deniedBlocks.add(allowedBlock);
                                }
                            }
                            parsedConditions.add(blockCondition);
                        }
                    }

                    for (ICondition notBlockCondition : notBlockConditions) 
                    {
                        if (notBlockCondition instanceof BlockCondition nbc) 
                        {
                            deniedBlocks.add(nbc.getBlock());
                            parsedConditions.add(notBlockCondition);
                        } 

                        else if (notBlockCondition instanceof BlocksCondition nbcs) 
                        {
                            deniedBlocks.addAll(nbcs.getAllBlocks(level.registryAccess()));
                            parsedConditions.add(notBlockCondition);
                        } 

                        else if (notBlockCondition instanceof BlockHardnessCondition bhc) 
                        {
                            for (Block allowedBlock : allowedBlocks) 
                            {
                                float hardness = allowedBlock.defaultBlockState().getDestroySpeed(Objects.requireNonNull(level), BlockPos.ZERO);
                                if (bhc.getMin() > hardness) 
                                {
                                    deniedBlocks.add(allowedBlock);
                                }
                                if (bhc.getMax() < hardness) 
                                {
                                    deniedBlocks.add(allowedBlock);
                                }
                            }
                            parsedConditions.add(notBlockCondition);
                        } 

                        else if (notBlockCondition instanceof IsOreCondition) 
                        {
                            for (Block allowedBlock : allowedBlocks) 
                            {
                                if (getOreBlocks().contains(allowedBlock)) 
                                {
                                    deniedBlocks.add(allowedBlock);
                                }
                            }
                            parsedConditions.add(notBlockCondition);
                        }
                    }

                    BlockConditionComponent blockConditionComponent = new BlockConditionComponent(allowedBlocks, deniedBlocks, parentBounds);
                    blockConditionComponent.setY(yOffset);
                    this.addComponent(blockConditionComponent);
                    yOffset += blockConditionComponent.getHeight();
                    continue;
                }

                // 블록 경도 단독
                if (condition instanceof BlockHardnessCondition hardnessCondition) 
                {
                    if (parsedConditions.contains(hardnessCondition))
                        continue;
                    if (conditions.stream().noneMatch(b -> b instanceof BlockCondition || b instanceof BlocksCondition)) 
                    {
                        Set<Block> allowedBlocks = level.registryAccess().lookupOrThrow(Registries.BLOCK).stream().filter(block -> 
                                {
                                    float hardness = block.defaultBlockState().getDestroySpeed(Objects.requireNonNull(level), BlockPos.ZERO);
                                    return hardnessCondition.getMin() <= hardness&& hardness <= hardnessCondition.getMax();
                                })
                                .collect(HashSet::new, HashSet::add, HashSet::addAll);
                        BlockConditionComponent hardnessConditionComponent = new BlockConditionComponent(allowedBlocks, new HashSet<>(), parentBounds);
                        hardnessConditionComponent.setY(yOffset);
                        this.addComponent(hardnessConditionComponent);
                        yOffset += hardnessConditionComponent.getHeight();
                        parsedConditions.add(hardnessCondition);
                        continue;
                    }
                }

                // 광물 여부 단독
                if (condition instanceof IsOreCondition isOreCondition) 
                {
                    if (parsedConditions.contains(isOreCondition))
                        continue;
                    if (conditions.stream().noneMatch(b -> b instanceof BlockCondition || b instanceof BlocksCondition)) 
                    {
                        List<ICondition> notBlockConditions = notConditions.stream()
                                .map(NotCondition::getConditions)
                                .flatMap(List::stream)
                                .filter(c -> c instanceof BlockCondition
                                        || c instanceof BlocksCondition
                                        || c instanceof BlockHardnessCondition)
                                .toList();
                        Set<Block> allowedBlocks = new HashSet<>(getOreBlocks());
                        Set<Block> deniedBlocks = new HashSet<>();

                        for (ICondition notBlockCondition : notBlockConditions) 
                        {
                            if (notBlockCondition instanceof BlockCondition nbc) 
                            {
                                deniedBlocks.add(nbc.getBlock());
                            } 
                            else if (notBlockCondition instanceof BlocksCondition nbcs) 
                            {
                                deniedBlocks.addAll(nbcs.getBlocks());
                            } 
                            else if (notBlockCondition instanceof BlockHardnessCondition bhc) 
                            {
                                for (Block allowedBlock : allowedBlocks) 
                                {
                                    float hardness = allowedBlock.defaultBlockState().getDestroySpeed(Objects.requireNonNull(level), BlockPos.ZERO);
                                    if (bhc.getMin() > hardness) 
                                    {
                                        deniedBlocks.add(allowedBlock);
                                    }

                                    if (bhc.getMax() < hardness) 
                                    {
                                        deniedBlocks.add(allowedBlock);
                                    }
                                }
                            }
                            parsedConditions.add(notBlockCondition);
                        }

                        BlockConditionComponent blockConditionComponent = new BlockConditionComponent(allowedBlocks, deniedBlocks, parentBounds);
                        blockConditionComponent.setY(yOffset);
                        this.addComponent(blockConditionComponent);
                        yOffset += blockConditionComponent.getHeight();
                        parsedConditions.add(isOreCondition);
                        continue;
                    }
                }

                // 아이템 조건
                if (condition instanceof ItemCondition || condition instanceof ItemsCondition) 
                {
                    List<ICondition> itemConditions = conditions.stream().filter(c -> c instanceof ItemCondition || c instanceof ItemsCondition).toList();
                    List<ICondition> notItemConditions = notConditions.stream()
                            .map(NotCondition::getConditions)
                            .flatMap(List::stream)
                            .filter(c -> c instanceof ItemCondition || c instanceof ItemsCondition)
                            .toList();
                    Set<ItemStack> allowedItems = new HashSet<>();
                    Set<ItemStack> deniedItems = new HashSet<>();

                    for (ICondition itemCondition : itemConditions) 
                    {
                        if (itemCondition instanceof ItemCondition bc) 
                        {
                            allowedItems.add(bc.getItemStack());
                            parsedConditions.add(itemCondition);
                        } 

                        else if (itemCondition instanceof ItemsCondition bcs) 
                        {
                            allowedItems.addAll(bcs.getItemStacks(level.registryAccess()));
                            parsedConditions.add(itemCondition);
                        }
                    }

                    for (ICondition notItemCondition : notItemConditions) 
                    {
                        if (notItemCondition instanceof ItemCondition nbc) 
                        {
                            deniedItems.add(nbc.getItemStack());
                            parsedConditions.add(notItemCondition);
                        } 

                        else if (notItemCondition instanceof ItemsCondition nbcs) 
                        {
                            deniedItems.addAll(nbcs.getItemStacks(level.registryAccess()));
                            parsedConditions.add(notItemCondition);
                        }
                    }

                    ItemConditionComponent itemConditionComponent = new ItemConditionComponent(allowedItems, deniedItems, parentBounds);
                    itemConditionComponent.setY(yOffset);
                    this.addComponent(itemConditionComponent);
                    yOffset += itemConditionComponent.getHeight();

                    continue;
                }

                // 기본 조건들 (텍스트만 있는 것) – 한글화 + 줄바꿈
                LocalizedDefaultConditionComponent conditionComponent = new LocalizedDefaultConditionComponent(condition);
                conditionComponent.setY(yOffset);
                this.addComponent(conditionComponent);
                yOffset += conditionComponent.getHeight();
            }

            // NOT 조건 처리
            for (NotCondition notCondition : notConditions) 
            {
                if (parsedConditions.contains(notCondition))
                {
                    continue;
                }

                for (ICondition innerCondition : notCondition.getConditions()) 
                {
                    if (parsedConditions.contains(innerCondition))
                    {
                        continue;
                    }

                    LocalizedNotConditionComponent notConditionComponent = new LocalizedNotConditionComponent(notCondition, innerCondition);
                    notConditionComponent.setY(yOffset);
                    this.addComponent(notConditionComponent);
                    yOffset += notConditionComponent.getHeight();

                    parsedConditions.add(innerCondition);
                }

                parsedConditions.add(notCondition);
            }

            this.setHeight(yOffset);
        }
    }

    private List<NotCondition> getNotConditions(List<ICondition> conditions) 
    {
        return conditions.stream()
                .filter(c -> c instanceof NotCondition)
                .map(c -> (NotCondition) c)
                .toList();
    }

    private List<Block> getOreBlocks() 
    {
        if (ORE_BLOCKS == null) 
        {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null)
                {
                    return List.of();
                }
            List<Block> oreBlocks = level.registryAccess()
                    .lookupOrThrow(Registries.BLOCK)
                    .stream()
                    .filter(block -> new IsOreCondition(false)
                            .isMet(new ActionDataBuilder((ArcPlayer) Minecraft.getInstance().player,
                                    ActionType.BREAK_BLOCK)
                                    .withData(ActionDataType.BLOCK_STATE, block.defaultBlockState())
                                    .build()))
                    .toList();

            ORE_BLOCKS = oreBlocks;
            return oreBlocks;
        }
        return ORE_BLOCKS;
    }

    // ============================================================
    //  한글화 매핑
    // ============================================================

    private static Component getKoreanConditionName(String original) 
    {
        switch (original) 
        {
            case "Entity Type":
            case "Entity Types", "Ready For Shearing":
                return Component.literal("");
            case "Crop Fully Grown":
                return Component.literal("작물이 최대 성장 상태일 때");
            case "Experience Level":
                return Component.literal("인챈트 레벨");
            case "Is Smoking Recipe":
                return Component.literal("훈연기 사용시");
            case "Distance":
                return Component.literal("수영하기");
        }

        return Component.literal(original);
    }

    private static Component getKoreanConditionDescription(String original)
    {
        String lower = original.toLowerCase(Locale.ROOT);

        if (lower.contains("sheep is ready for shearing"))
        {
            return Component.literal("양이 털을 깎을 수 있는 상태일 때");
        }

        if (lower.contains("entity is a"))
        {
            return Component.literal("");
        }

        if (lower.contains("entity is one of the following"))
        {
            return Component.literal("팬텀, 복어, 마그마큐브, 가스트, 토끼, 거미, 동굴거미, 블레이즈, 마녀");
        }

        if (lower.contains("crop is fully grown"))
        {
            return Component.literal("작물이 완전히 자랐을 때");
        }

        int idx = lower.indexOf("experience level is");
        if (idx >= 0)
        {
            String after = original.substring(idx + "experience level is".length()).trim();
            if (after.endsWith("."))
            {
                after = after.substring(0, after.length() - 1).trim();
            }
            return Component.literal("인챈트 레벨을" + after + "로 적용 시");
        }

        if (lower.contains("item is a") || lower.contains("item is an"))
        {
            return Component.literal("아이템이 지정된 대상과 일치할 때");
        }

        if (lower.contains("recipe is a smoking recipe"))
        {
            return Component.literal("훈연기 사용하여 아이템 수거 시 (훈연기 가능 아이템만)");
        }

        if (lower.contains("is met if the player has traveled 10 blocks"))
        {
            return Component.literal("플레이어가 수영으로 10블록 이동 시");
        }

        return Component.literal(original);
    }

    // ============================================================
    //  기본 조건용 내부 컴포넌트 (점 + 멀티라인)
    // ============================================================

    private static class LocalizedDefaultConditionComponent extends EmptyComponent
    {

        public LocalizedDefaultConditionComponent(ICondition condition)
        {
            super(0, 0, 99, 0);

            // 점(•) 표시 – 원래 AbstractConditionComponent와 동일한 구조
            TextComponent dot = new TextComponent(0, 0, Component.literal(" • "), 0xFF1E1410);
            this.addComponent(dot);

            String originalName = condition.getName().getString();
            String originalDesc = condition.getDescription().getString();

            Component name = getKoreanConditionName(originalName);
            Component desc = getKoreanConditionDescription(originalDesc);

            // x = 10, width = getWidth() - 10 → 원본 DefaultConditionComponent와 동일
            MultiLineTextComponent nameComponent = new MultiLineTextComponent(10, 0, getWidth() - 10, name, 0xFF1E1410);
            MultiLineTextComponent descComponent = new MultiLineTextComponent(10, nameComponent.getHeight(), getWidth() - 10, desc, 0xFFD8BF96);

            this.addComponent(nameComponent);
            this.addComponent(descComponent);

            this.setHeight(nameComponent.getHeight() + descComponent.getHeight());
        }
    }

    // ============================================================
    //  NOT 조건용 내부 컴포넌트 (점 + 멀티라인)
    // ============================================================

    private static class LocalizedNotConditionComponent extends EmptyComponent 
    {

        public LocalizedNotConditionComponent(NotCondition notCondition, ICondition innerCondition) 
        {
            super(0, 0, 99, 0);

            TextComponent dot = new TextComponent(0, 0, Component.literal(" • "), 0xFF1E1410);
            this.addComponent(dot);

            String innerOriginalName = innerCondition.getName().getString();
            Component koreanInnerName = getKoreanConditionName(innerOriginalName);

            Component text = Component.literal("NOT: ").append(koreanInnerName);
            MultiLineTextComponent notText = new MultiLineTextComponent(10, 0, getWidth() - 10, text, 0xFF1E1410);

            this.addComponent(notText);
            this.setHeight(notText.getHeight());
        }
    }
}
