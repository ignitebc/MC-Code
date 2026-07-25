package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.arc.api.action.IAction;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpReward;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.TruncatedTextComponent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class ActionItemComponent extends EmptyComponent
{

    public ActionItemComponent(IAction action, JobsScreenState state, Supplier<ScreenRectangle> parentBounds)
    {
        super(0, 0, 99, 0);

        Component nameText = getKoreanActionName(action);
        
        // 원본 수정, act -> 넘어온 이름 변경 ex)on break block -> 블록 부술때
        //TruncatedTextComponent nameComponent = new TruncatedTextComponent(0, 0, getWidth(), action.getName(), state.getSelectedJob().getJobInstance().getColorDecimal() | 0xFF000000);

        TruncatedTextComponent nameComponent = new TruncatedTextComponent(0, 0, getWidth(), nameText, state.getSelectedJob().getJobInstance().getColorDecimal() | 0xFF000000);
        JobExpReward jobExpReward = action.getRewards().stream().filter(reward -> reward instanceof JobExpReward).map(reward -> (JobExpReward) reward).findFirst().orElse(null);
        if (jobExpReward == null)
            return;
        Component experienceText = jobExpReward.getMin() == jobExpReward.getMax() ? JobsPlus.translatable("gui.jobs.experience.reward", jobExpReward.getMin()) : JobsPlus.translatable("gui.jobs.experience.reward.range", jobExpReward.getMin(), jobExpReward.getMax());
        TruncatedTextComponent experienceComponent = new TruncatedTextComponent(0, nameComponent.getHeight(), getWidth(), experienceText, 0xFF1E1410);

        ConditionsComponent conditionsComponent = new ConditionsComponent(action.getConditions(), parentBounds);
        conditionsComponent.setY(nameComponent.getHeight() + experienceComponent.getHeight());

        this.addComponent(nameComponent);
        this.addComponent(experienceComponent);
        this.addComponent(conditionsComponent);

        this.setHeight(nameComponent.getHeight() + experienceComponent.getHeight() + conditionsComponent.getHeight() + 2);
    }

    /**
     * ARC 쪽에서 넘어온 action 이름(Component)을 기반으로
     * 원하는 한글 문구로 치환해주는 메서드 (하드코딩).
     */
    private Component getKoreanActionName(IAction action) {
        String original = action.getName().getString();

        switch (original) {
            case "On Break Block":
                return Component.literal("블록 부술 시");

            case "On Place Block":
                return Component.literal("블록 설치 시");

            case "On Smelt Item":
                return Component.literal("아이템 제련 시");

            case "On Kill Entity":
                return Component.literal("엔티티 처치 시");

            case "On Craft Item":
                return Component.literal("아이템 제작 시");

            case "On Swim":
                return Component.literal("수영할 때");

            case "On Fished Up Item":
                return Component.literal("해당아이템 낚을 시");

            case "On Plant Crop":
                return Component.literal("작물 심을 시");

            case "On Harvest Crop":
                return Component.literal("작물 수확 시");

            case "On Interact Entity":
                return Component.literal("양털을 깎을 시");
            
            case "On Breed Animal":
                return Component.literal("동물 먹이줄 시");

            case "On Tame Animal":
                return Component.literal("동물 번식 시");

            case "On Grind Item":
                return Component.literal("숯돌 사용 시");

            case "On Use Anvil":
                return Component.literal("모루 사용 시");

            case "On Enchant Item":
                return Component.literal("인챈트 할 때");

            case "On Strip Log":
                return Component.literal("나무껍질 벗길 시");

            case "On Brew Potion":
                return Component.literal("포션 제작 시");

            case "On Throw Item":
                return Component.literal("포션 던질 시");

            case "On Drink":
                return Component.literal("포션 사용 시");

            // 변환 대상이 아니면 ARC 원본 그대로 표시
            default:
                return action.getName();
        }
    }
}
