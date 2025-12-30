package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.RecipesScrollWidget;
import com.daqem.uilib.api.component.IComponent;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;

public class RecipesScrollComponent extends EmptyComponent
{
    // 업데이트 내역(표시 텍스트)
    // Java 21 기준 텍스트 블록 사용 가능
    private static final String PATCH_NOTES = """
            ★★★ 추후 업데이트 ★★★
            * illager invasion 모드 도입 예정
            * 레이드 시작 후, 겉날개 사용 시 즉사 도입 예정
            * 위더(보스몹) 시작 후, 겉날개 사용 시 즉사 도입 예정
            * 레이드 추가레벨 도입 예정 (현재 5단계 최대)
            * 사냥꾼 스킬 추가 예정 (힘의물약+1증가, 약탈량)
            * 광부 스킬 추가 예정 (사거리, 화염저항)
            * 낚시꾼, 갈고리 낚시대 사용시 내구도 감소 예정
            * 워든 잡을 시 강화조각, 강화원석 드롭 예정
            * 위더 잡을 시 강화조각 드롭 예정
            * 위더, 워든:공격력, 체력, 범위 버프 예정
            * 신규세트 방어구 도입 예정
            * 연금술사 스킬추가예정 (낙뢰물약, 피흡)
            * 굴착공 스킬 개편중
            * 모험가 스킬 개편중

            ★★★ 업데이트 완료 ★★★
            * 연금술사 스킬 전면 개편

            * 익힌 닭고기 10개 -> 에메랄드 1개 (상점추가)
            * 흙 192개(3세트) -> 에메랄드 1개 (상점추가)
            * 네더랙 320개(5세트) -> 에메랄드 1개 (상점추가)

            *사냥꾼 (스킬 추가)
            - 이동속도증가 (걷기, 달리기, 수영, 점프 포함)

            *사냥꾼 (너프)
            - 몹 처치시 비트코인 0.04% -> 0.02%

            *사냥꾼 (추가)
            - 크리퍼 처치시 비트코인 0.04% 확률로 획득 (독립시행)
            - 워든,위더 처치시 비트코인 7% 확률로 3개 획득 (독립시행)
            - 위더스켈레톤 처치시 비트코인 0.4%확률로 획득 (독립시행)

            * 광부 (버프)
            - 스페셜 광부1단계 0.05% -> 0.5%
            - 스페셜 광부2단계  0.1% -> 1%
            - 스페셜 광부3단계  0.15% -> 1.5%
            - 스페셜 광부4단계  0.2% -> 2%
            - 스페셜 광부5단계  0.25% -> 2.5%
            - 스페셜 광부6단계  0.3% -> 3%
            - 스페셜 광부7단계  0.35% -> 3.5%
            - 스페셜 광부8단계  0.4% -> 4%
            - 스페셜 광부9단계  0.45% -> 4.5%
            - 스페셜 광부10단계  0.5% -> 5%

            ★★★ 랜덤 상자 대폭 상향 ★★★
            - 1단계 랜덤상자 보상 목록
            불사의 토템 2개 (24%)
            네더라이트 주괴 5개 (20%)
            강화 성공 확률 증가 주문서 +3% × 1 (20%)
            강화 성공 확률 증가 주문서 +5% × 1 (15%)
            강화 성공 확률 증가 주문서 +7% × 1 (10%)
            강화 성공 확률 증가 주문서 +10% × 1 (5%)
            비트코인 20개 (3%)
            비트코인 30개 (2%)
            비트코인 50개 (1%)

            - 2단계 랜덤상자 보상 목록
            불사의 토템 3개 (10%)
            네더라이트 주괴 10개 (10%)
            강화 조각 2개 (9%)
            강화 성공 확률 증가 주문서 +3% × 1 (20%)
            강화 성공 확률 증가 주문서 +5% × 1 (15%)
            강화 성공 확률 증가 주문서 +7% × 1 (12%)
            강화 성공 확률 증가 주문서 +10% × 1 (10%)
            강화 보호 주문서 × 1 (8.5%)
            일반 펫 상자 × 1 (1%)
            비트코인 50개 (2%)
            비트코인 100개 (1.5%)
            비트코인 200개 (1%)

            - 3단계 랜덤상자 보상 목록
            불사의 토템 20개 (10%)
            네더라이트 주괴 30개 (10%)
            사망 시 아이템 보존권 × 1 (5%)
            강화 조각 4개 (10%)
            강화 원석 2개 (10%)
            강화 성공 확률 증가 주문서 +7% × 2 (12%)
            강화 성공 확률 증가 주문서 +10% × 2 (8%)
            강화 보호 주문서 × 2 (7%)
            직업 선택권 × 1 (15%)
            희귀 펫 상자 × 1 (4%)
            비트코인 200개 (1.5%)
            비트코인 300개 (1%)
            비트코인 400개 (0.5%)
            마법이 부여된 황금 사과 50개 (6%)

            - 4단계 랜덤상자 보상 목록
            직업 선택권 × 1 (30%)
            네더라이트 주괴 64개 (10%)
            마법이 부여된 황금 사과 100개 (15%)
            강화 성공 확률 증가 주문서 +7% × 10 (15%)
            강화 성공 확률 증가 주문서 +10% × 5 (10%)
            강화 조각 10개 (6.5%)
            강화 원석 5개 (6.5%)
            강화 보호 주문서 × 5 (6.5%)
            사망 시 아이템 보존권 × 5 (6.5%)
            전설 펫 상자 × 1 (1%)
            비트코인 500개 (1.5%)
            비트코인 700개 (1%)
            비트코인 1000개 (0.5%)
            """;

    public RecipesScrollComponent(JobsScreenState state)
    {
        super(0, 43, 117, 124);

        RecipesScrollWidget recipesScrollWidget = new RecipesScrollWidget(getWidth(), getHeight(), state);

        IComponent scrollContentComponent = recipesScrollWidget.getComponents().getFirst();
        if (scrollContentComponent.getHeight() <= getHeight())
        {
            this.setWidth(scrollContentComponent.getWidth());
            this.centerHorizontally();
        }

        // 레시피가 없을 때 "No recipe" 대신 업데이트 내역 출력
        if (scrollContentComponent.getComponents().isEmpty())
        {
            // 기존 색상 유지 (원 코드: 0xFFD8BF96)
            MultiLineTextComponent patchNotesText = new MultiLineTextComponent(0, 0, getWidth(), Component.literal(PATCH_NOTES), 0xFFD8BF96);

            // 글자 크기: 기본보다 살짝 줄이기 (UIlib에 scale API가 있을 때만 적용)
            tryApplyTextScale(patchNotesText, 0.6f);

            this.addComponent(patchNotesText);
        }

        this.addWidget(recipesScrollWidget);
    }

    /**
     * UIlib 버전에 따라 스케일 함수명이 다를 수 있어 리플렉션으로 안전하게 적용합니다. - setScale(float) -
     * setTextScale(float)
     */
    private static void tryApplyTextScale(Object component, float scale)
    {
        if (component == null)
            return;

        if (invokeIfExists(component, "setScale", scale))
            return;
        invokeIfExists(component, "setTextScale", scale);
    }

    private static boolean invokeIfExists(Object target, String methodName, float arg)
    {
        try
        {
            Method m = target.getClass().getMethod(methodName, float.class);
            m.invoke(target, arg);
            return true;
        } catch (Throwable ignored)
        {
            return false;
        }
    }
}
