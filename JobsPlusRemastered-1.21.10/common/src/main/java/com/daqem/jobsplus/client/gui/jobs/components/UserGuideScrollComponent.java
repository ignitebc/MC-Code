package com.daqem.jobsplus.client.gui.jobs.components;

import java.lang.reflect.Method;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.RecipesScrollWidget;
import com.daqem.uilib.api.component.IComponent;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class UserGuideScrollComponent extends EmptyComponent
{
    private static final String RED_TEXT_START = "<red>";
    private static final String RED_TEXT_END = "</red>";

    /**
     * 스킬 설명이 아닌, 별도 팝업만으로 확인하기 어려운
     * 공통 시스템과 아이템 이용 방법을 안내한다.
     */
    private static final String USER_GUIDE = """
            ★ 게임 이용 안내 ★
            이 화면에서는 플레이 중 놓치기 쉬운 공통 규칙과 주요 아이템 사용 방법을 안내합니다.

            ■ 직업 경험치·비트코인 기준
            <red>- 일반 다이아몬드 장비와 인챈트가 없는 상태에서 직접 작업하면 비트코인은 평균 시간당 약 6개, 약 10분당 1개를 획득하도록 조정되어 있습니다.</red>
            <red>- 경험치 증가 스킬을 우선적으로 해금하길 추천드리며, 하루 1시간씩 플레이하면 모든 스킬 해방까지 약 29~30시간이 걸리도록 조정되어 있습니다.</red>

            ■ 직업 기본 최대 체력
            <red>- 시작체력 5칸: 대장장이·사냥꾼·연금술사 / 시작체력 6칸: 농부·낚시꾼·광부 / 시작체력 7칸: 굴착공·모험가</red>
            <red>- 여러 직업의 체력은 평균을 내어 0.5칸 단위로 반올림합니다. 8개 직업을 모두 보유하면 총 체력 10칸이 되며, 대장장이의 강철 체질(체력증가)은 이후 별도로 더해집니다.</red>

            ■ 직업 선택권
            - 직업 선택권을 손에 들고 우클릭하면 동시에 보유할 수 있는 최대 직업 수가 1칸 증가합니다.
            - 최대 8개 직업에 도달한 경우에는 직업 선택권이 사용되지 않으며 아이템도 소모되지 않습니다.
            - 우클릭을 계속 누르고 있어도 연속으로 사용되지 않도록 사용 후 약 1초의 대기 시간이 적용됩니다.

            ■ 아이템 이용 제한
            - 일부 아이템은 직업, 레벨 또는 지정된 조건에 따라 제작, 제련, 양조, 수리, 마법 부여 및 사용이 제한될 수 있습니다.
            - 조건을 만족하지 않은 상태에서는 다음 행동이 제한될 수 있습니다.

            제작
            제련
            양조
            마법 부여
            모루 및 숫돌 사용
            아이템 사용
            블록 설치
            블록 파괴
            특정 아이템으로 블록 파괴
            몬스터 공격

            - 행동이 제한되면 화면에 제한 사유가 표시됩니다. 필요한 직업이나 조건을 확인한 후 다시 시도해 주세요.

            <red>■ 레이드 및 위더 전투 주의사항</red>
            <red>- 레이드가 진행 중인 지역에서는 플레이어 주변 80블록 이내의 철 골렘이 즉시 제거됩니다.
            - 살아 있는 위더가 플레이어 주변 80블록 이내에 있는 경우에도 주변 80블록 이내의 철 골렘이 즉시 제거됩니다.
            - 레이드 진행 중 겉날개로 활강하면 즉시 사망합니다.
            - 살아 있는 위더가 주변 80블록 이내에 있는 상태에서 겉날개로 활강해도 즉시 사망합니다.
            - 겉날개를 착용한 것만으로는 사망하지 않습니다. 실제로 겉날개 활강 상태가 되었을 때 적용됩니다.
            - 전투 중 겉날개 사용으로 사망하면 관련 안내 메시지가 서버 전체에 표시됩니다.
            - 철 골렘과 겉날개를 이용한 전투 우회를 막기 위한 규칙이므로 레이드와 위더 전투 전 장비를 미리 점검해 주세요.</red>

            ■ 비트코인 계좌
            - 인벤토리에 보유한 비트코인은 주식 화면의 입출금 메뉴에서 계좌로 입금할 수 있습니다.
            - 입금과 출금은 10개 단위로만 가능합니다.
            - 비트코인을 입금하면 인벤토리에서 해당 수량이 차감되고 주식 계좌 잔액이 증가합니다.
            - 비트코인을 출금하면 주식 계좌에서 금액이 차감되고 인벤토리로 비트코인이 지급됩니다.
            - 출금 시 출금 금액의 0.2%가 세금으로 추가 차감됩니다.

            예시)
            비트코인 100개 출금
            계좌 차감 금액: 100.2개
            실제 지급 금액: 100개

            ■ 주식 거래
            - 주식 가격은 약 1분마다 갱신됩니다.
            - 가격이 갱신되기 전까지는 주식 표에 표시된 동일한 가격이 유지됩니다.
            - 매수와 매도는 서버에 적용된 현재 주식 가격을 기준으로 처리됩니다.

            예시)
            비트코인 가격이 100,000,000원일 때 매수
            1분 후 가격이 90,000,000원으로 갱신
            이후 매수와 매도는 90,000,000원 기준으로 처리

            - 매수 금액은 비트코인 1개 단위로 입력할 수 있습니다.
            - 구매한 주식의 평가금액은 다음과 같이 계산됩니다.

            보유 주식 수량 × 현재 주식 가격

            - 주식 가격이 상승하면 평가금액이 증가하고, 주식 가격이 하락하면 평가금액이 감소합니다.
            - 판매 입력 금액은 현재 평가금액이 아니라 해당 종목에 투자한 원금을 기준으로 합니다.

            예시)
            비트코인 100개를 투자한 종목에서
            판매 금액으로 50개 입력
            보유 주식의 50%를 현재 가격으로 정산

            - 주식 가격이 상승한 상태라면 입력한 투자원금보다 실제 판매 금액이 많을 수 있습니다.
            - 주식 가격이 하락한 상태라면 입력한 투자원금보다 실제 판매 금액이 적을 수 있습니다.
            - 주식 판매 시 판매 금액의 0.015%가 수수료로 차감됩니다.
            - 정상적인 주식 가격을 불러오지 못한 종목은 가격이 갱신될 때까지 거래할 수 없습니다.
            - 거래내역에서는 최근 거래 50건까지 확인할 수 있습니다.

            ■ 신성한 마법부여대
            - 신성한 마법부여대는 장비에 적용된 마법 부여를 기존보다 한 단계 높이는 데 사용합니다.
            - 다음 세 가지 아이템이 필요합니다.

            마법이 하나만 부여된 마법이 부여된 책
            책과 동일한 마법 및 동일한 레벨이 적용된 장비
            신성한 보석 1개

            - 조건을 만족하면 장비의 해당 마법 레벨이 1 증가합니다.

            예시)
            날카로움 III 마법이 부여된 책
            날카로움 III 다이아몬드 검
            신성한 보석 1개

            결과)
            날카로움 IV 다이아몬드 검

            - 마법이 부여된 책과 신성한 보석 1개가 소모됩니다.
            - 대상 장비는 강화된 결과물로 교체됩니다.
            - 지원하지 않는 마법이나 최대 레벨에 도달한 마법은 신성한 마법부여대에서 강화할 수 없습니다.

            ■ 잃어버린 양초
            - 잃어버린 양초를 들고 블록을 우클릭하면 플레이어 주변 약 8블록 범위의 광물을 탐색합니다.
            - 주변에서 광물이 발견되면 발견된 광물의 종류가 화면에 표시되고 전용 소리가 재생됩니다.
            - 잃어버린 양초는 광물의 정확한 좌표나 방향을 표시하지 않습니다. 주변에 어떤 광물이 있는지를 확인하는 용도로 사용합니다.
            - 주변에서 광물을 찾지 못하면 불이 꺼지는 소리가 재생됩니다.
            - 한 번 사용한 후 약 3초 동안 다시 사용할 수 없습니다.

            ■ 시야의 뿔
            - 시야의 뿔을 사용하면 플레이어 주변 48블록 이내의 적대적 몬스터가 빛나게 표시됩니다.
            - 빛나는 효과는 약 3초 동안 유지됩니다.
            - 벽이나 지형 뒤에 숨어 있는 몬스터의 위치를 빠르게 확인할 때 사용할 수 있습니다.
            - 동물이나 일반 주민은 대상에 포함되지 않으며, 적대적 몬스터에게만 적용됩니다.

            ■ 환영의 가루
            - 환영의 가루를 사용하면 다음 효과가 적용됩니다.

            투명화: 60초
            이동 속도 증가: 10초

            - 사용 시 환영의 가루 1개가 소모됩니다.
            - 사용 후 약 5초 동안 다시 사용할 수 없습니다.
            - 투명화 상태에서도 장비를 착용하거나 아이템을 들고 있으면 다른 플레이어에게 일부 모습이 보일 수 있습니다.

            ■ 플래티넘 강화 도끼
            - 플래티넘 강화 도끼는 일반 근접 무기뿐 아니라 원거리 투척 무기로도 사용할 수 있습니다.
            - 사용 버튼을 약 0.5초 이상 누른 후 놓으면 바라보는 방향으로 도끼를 던집니다.
            - 도끼를 던질 때마다 내구도가 1 감소합니다.
            - 던진 도끼는 월드에 떨어지며 다시 주울 수 있습니다.
            - 도끼를 던진 동안에는 인벤토리에서 도끼가 사라지므로 전투가 끝난 후 반드시 회수해 주세요.
            - 도끼의 남은 내구도가 부족하면 투척할 수 없습니다.

            ■ 랜덤상자 이용 방법
            - 랜덤상자는 단계별로 지정된 전용 열쇠가 필요합니다.
            - 랜덤상자를 한 번 열면 다음 아이템이 소모됩니다.

            랜덤상자 1개
            해당 단계 전용 열쇠 1개

            - 랜덤상자를 열면 아래 보상 중 하나만 지급됩니다.
            - 보상은 인벤토리에 직접 들어오지 않고 플레이어 주변 바닥에 아이템으로 떨어집니다.
            - 보상이 용암이나 낭떠러지에 떨어지지 않도록 안전한 장소에서 랜덤상자를 사용해 주세요.
            - 여러 묶음으로 나뉜 보상도 안내 메시지에서는 전체 수량이 합산되어 표시됩니다.
            - 랜덤상자를 열면 획득한 보상이 서버 전체 메시지로 안내됩니다.

            ■ 1단계 랜덤상자 보상
            불사의 토템 2개 (24%)
            네더라이트 주괴 5개 (20%)
            강화 성공 확률 증가 주문서 +3% × 1 (20%)
            강화 성공 확률 증가 주문서 +5% × 1 (15%)
            강화 성공 확률 증가 주문서 +7% × 1 (10%)
            강화 성공 확률 증가 주문서 +10% × 1 (5%)
            비트코인 20개 (3%)
            비트코인 30개 (2%)
            비트코인 50개 (1%)

            ■ 2단계 랜덤상자 보상
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

            ■ 3단계 랜덤상자 보상
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

            ■ 4단계 랜덤상자 보상
            직업 선택권 × 1 (30%)
            네더라이트 주괴 64개 (10%)
            마법이 부여된 황금 사과 100개 (10%)
            강화 성공 확률 증가 주문서 +7% × 10 (10%)
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

    public UserGuideScrollComponent(JobsScreenState state, int width, int height)
    {
        super(0, 43, width, height - 43);

        RecipesScrollWidget recipesScrollWidget =
                new RecipesScrollWidget(getWidth(), getHeight(), state);

        IComponent scrollContentComponent =
                recipesScrollWidget.getComponents().getFirst();

        /*
         * 표시할 레시피가 없는 경우 기존 레시피 영역을
         * 사용자 게임 안내 화면으로 교체한다.
         */
        if (scrollContentComponent.getComponents().isEmpty())
        {
            int textWidth = Math.max(1, getWidth() - 10);
            float textScale = 0.50f;

            /*
             * 화면에 실제로 표시되는 너비는
             * wrapWidth × textScale이므로 스케일만큼 역보정한다.
             */
            int wrapWidth = Math.max(
                    1,
                    (int) Math.ceil(textWidth / textScale)
            );

            ScaledMultiLineTextComponent guideText =
                    new ScaledMultiLineTextComponent(
                            0,
                            0,
                            wrapWidth,
                            createGuideComponent(),
                            0xFF000000,
                            textScale
                    );

            EmptyComponent guideContainer =
                    new EmptyComponent(0, 0, textWidth, 0);

            guideContainer.addComponent(guideText);
            guideContainer.setHeight(guideText.getScaledHeight());

            recipesScrollWidget.getComponents().clear();
            recipesScrollWidget.addComponent(guideContainer);

            if (guideContainer.getWidth() <= getWidth())
            {
                this.setWidth(guideContainer.getWidth());
                this.centerHorizontally();
            }
        }
        else if (scrollContentComponent.getHeight() <= getHeight())
        {
            this.setWidth(scrollContentComponent.getWidth());
            this.centerHorizontally();
        }

        this.addWidget(recipesScrollWidget);
    }

    private static Component createGuideComponent()
    {
        String guide = USER_GUIDE.strip();
        MutableComponent component = Component.empty();
        int currentIndex = 0;

        while (currentIndex < guide.length())
        {
            int redStart = guide.indexOf(RED_TEXT_START, currentIndex);
            if (redStart < 0)
            {
                component.append(Component.literal(guide.substring(currentIndex)));
                break;
            }

            component.append(Component.literal(guide.substring(currentIndex, redStart)));
            int contentStart = redStart + RED_TEXT_START.length();
            int redEnd = guide.indexOf(RED_TEXT_END, contentStart);
            if (redEnd < 0)
            {
                component.append(
                        Component.literal(guide.substring(contentStart))
                                .withStyle(ChatFormatting.RED)
                );
                break;
            }

            component.append(
                    Component.literal(guide.substring(contentStart, redEnd))
                            .withStyle(ChatFormatting.RED)
            );
            currentIndex = redEnd + RED_TEXT_END.length();
        }

        return component;
    }

    /**
     * MultiLineTextComponent에 출력 배율을 적용하기 위한 컴포넌트.
     *
     * 매핑에 따라 PoseStack 메서드 이름이 달라질 수 있으므로
     * push, pop, translate, scale 호출은 리플렉션으로 처리한다.
     */
    private static final class ScaledMultiLineTextComponent
            extends MultiLineTextComponent
    {
        private static final int BASE_LINE_HEIGHT = 9;

        private final float scale;

        public ScaledMultiLineTextComponent(
                int x,
                int y,
                int maxWidth,
                Component text,
                int color,
                float scale
        )
        {
            super(x, y, maxWidth, text, color);
            this.scale = scale <= 0.0F ? 1.0F : scale;
        }

        public int getScaledHeight()
        {
            return (int) Math.ceil(
                    getLines().size() * BASE_LINE_HEIGHT * this.scale
            );
        }

        @Override
        public void render(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                float delta,
                int x,
                int y
        )
        {
            Object pose = graphics.pose();

            boolean pushed =
                    invokeNoArg(pose, "pushPose")
                            || invokeNoArg(pose, "push")
                            || invokeNoArg(pose, "pushMatrix");

            if (!pushed)
            {
                super.render(graphics, mouseX, mouseY, delta, x, y);
                return;
            }

            float totalX = (float) getTotalX();
            float totalY = (float) getTotalY();

            if (!invoke2f(pose, "translate", totalX, totalY))
            {
                invoke3f(pose, "translate", totalX, totalY, 0.0F);
            }

            if (!invoke2f(pose, "scale", this.scale, this.scale))
            {
                invoke3f(
                        pose,
                        "scale",
                        this.scale,
                        this.scale,
                        1.0F
                );
            }

            for (int index = 0; index < getLines().size(); index++)
            {
                graphics.drawString(
                        getFont(),
                        getLines().get(index),
                        0,
                        index * BASE_LINE_HEIGHT,
                        getColor(),
                        isDrawShadow()
                );
            }

            /*
             * 위에서 성공한 push 방식과 같은 pop 방식만 호출해야 하지만,
             * 기존 구현과의 호환성을 유지하기 위해 사용 가능한 메서드를 찾는다.
             */
            if (!invokeNoArg(pose, "popPose")
                    && !invokeNoArg(pose, "pop"))
            {
                invokeNoArg(pose, "popMatrix");
            }
        }

        private static boolean invokeNoArg(
                Object target,
                String methodName
        )
        {
            try
            {
                Method method =
                        target.getClass().getMethod(methodName);

                method.invoke(target);
                return true;
            }
            catch (ReflectiveOperationException ignored)
            {
                return false;
            }
        }

        private static boolean invoke2f(
                Object target,
                String methodName,
                float first,
                float second
        )
        {
            try
            {
                Method method = target.getClass().getMethod(
                        methodName,
                        float.class,
                        float.class
                );

                method.invoke(target, first, second);
                return true;
            }
            catch (ReflectiveOperationException ignored)
            {
                return false;
            }
        }

        private static boolean invoke3f(
                Object target,
                String methodName,
                float first,
                float second,
                float third
        )
        {
            try
            {
                Method method = target.getClass().getMethod(
                        methodName,
                        float.class,
                        float.class,
                        float.class
                );

                method.invoke(target, first, second, third);
                return true;
            }
            catch (ReflectiveOperationException ignored)
            {
                return false;
            }
        }
    }
}
