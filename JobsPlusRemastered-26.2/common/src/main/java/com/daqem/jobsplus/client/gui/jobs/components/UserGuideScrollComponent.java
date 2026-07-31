package com.daqem.jobsplus.client.gui.jobs.components;

import java.lang.reflect.Method;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.RecipesScrollWidget;
import com.daqem.uilib.api.component.IComponent;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

            ■ 직업 기본 최대 체력
            <red>- 시작체력 5칸: 대장장이·사냥꾼·연금술사 
            - 시작체력 6칸: 농부·낚시꾼·광부
            - 시작체력 7칸: 굴착공·모험가</red>

            <red>- 여러 직업의 체력은 평균을 내어 0.5칸 단위로 반올림합니다. 
            - 8개 직업을 모두 보유하면 총 체력 10칸이 되며, 대장장이의 강철 체질(체력증가)은 이후 별도로 더해집니다.</red>

            ■ 직업 경험치·비트코인 기준
            <red>- 비트코인은 평균 시간당 약 6개, 약 10분당 1개를 획득하도록 조정되어 있습니다.</red>
            <red>- 직업 경험치는 정상 행동마다 100% 누적되며, 1 미만의 소수 경험치는 합계가 1 이상이 되면 실제 경험치에 반영됩니다.</red>
            <red>- 경험치 증가 스킬을 우선적으로 해금하길 추천드립니다. 
            - 하루 1시간씩 플레이하면 모든 스킬 해방까지 약 29~30시간이 걸리도록 조정되어 있습니다.</red>

            ■ 직업 선택권
            - 직업 선택권을 손에 들고 우클릭하면 동시에 보유할 수 있는 최대 직업 수가 1칸 증가합니다.
            - 최대 8개 직업에 도달한 경우에는 직업 선택권이 사용되지 않으며 아이템도 소모되지 않습니다.
            - 우클릭을 계속 누르고 있어도 연속으로 사용되지 않도록 사용 후 약 1초의 대기 시간이 적용됩니다.

            ■ 배고픔
            <red>- 이 서버는 이동·점프·공격·채굴 등 행동으로 소모되는 배고픔이 일반 서버보다 1.5배 빨리 소모됩니다. 
            - 식량을 넉넉히 준비하세요.</red>

            ■ 비트코인 계좌
            - 인벤토리에 보유한 비트코인은 주식 화면의 입출금 메뉴에서 계좌로 입금할 수 있습니다.
            - 입금과 출금은 10개 단위로만 가능하며, 한 번에 최대 1,000개까지 처리할 수 있습니다.
            - 비트코인을 입금하면 인벤토리에서 해당 수량이 차감되고 주식 계좌 잔액이 증가합니다.
            - 비트코인을 출금하면 주식 계좌에서 금액이 차감되고 인벤토리로 비트코인이 지급됩니다.
            - 출금 시 출금 금액의 0.2%가 세금으로 추가 차감됩니다.
            - 출금한 비트코인이 인벤토리에 모두 들어가지 않으면 남은 수량은 플레이어 주변 바닥에 떨어집니다.

            예시)
            비트코인 100개 출금
            계좌 차감 금액: 100.2개
            실제 지급 금액: 100개

            ■ 주식 시세와 구매 예약
            - 주식 가격은 약 1분마다 갱신됩니다.
            - 가격이 갱신되기 전까지는 주식 표에 표시된 동일한 가격이 유지됩니다.
            - 구매 버튼을 누르면 즉시 체결되지 않고 다음 분 시작가로 구매가 예약됩니다.
            - 다음 분에 거래 기록이 없는 종목은 예약할 때 확인한 가격을 진입가로 사용합니다.
            - 예약과 동시에 투자금이 계좌에서 먼저 차감되며, 다음 분이 끝난 뒤 진입 가격과 해당 분의 가격 변동을 확인하여 결과가 안내됩니다.
            - <red>구매 예약은 한 번 접수되면 직접 취소할 수 없습니다. 예약 전에 종목과 투자금을 신중하게 확인해 주세요.</red>            
            - 시세 확인이 지연되면 투자금과 예약은 안전하게 유지되며, 확인이 끝날 때까지 같은 종목을 추가 구매하거나 판매할 수 없습니다.
            - 예약이 취소되면 차감했던 투자금은 계좌로 자동 반환됩니다.
            - 주식 화면을 닫거나 로그아웃해도 구매 예약 처리와 보유 포지션의 강제청산 감시는 계속됩니다.

            예시)
            14:30에 비트코인 100개로 구매 예약
            14:31 시작가로 포지션 진입 (거래 기록이 없으면 예약 확인가 사용)
            14:31의 가격 변동과 강제청산 여부를 확인한 뒤 예약 결과 안내

            - 구매 예약과 판매는 비트코인 1개 단위로 입력할 수 있으며, 한 번에 최대 1,000개까지 가능합니다.
            - 한 종목에는 하나의 포지션만 보유할 수 있습니다.
            - 같은 종목을 추가 구매하려면 기존 포지션과 롱·숏 방향 및 배율이 모두 같아야 합니다.
            - 방향이나 배율을 바꾸려면 기존 포지션을 먼저 모두 판매해야 합니다.

            ■ 롱·숏과 배율
            - 롱은 종목 가격이 오르면 수익을 얻고, 가격이 내리면 손실을 봅니다.
            - 숏은 종목 가격이 내리면 수익을 얻고, 가격이 오르면 손실을 봅니다.
            - 배율은 기본(X1), X2, X3, X5, X10, X15, X20 중에서 선택할 수 있습니다.
            - 기본(X1)은 배율을 사용하지 않는 일반 포지션입니다.
            - 선택한 배율만큼 수익률과 손실률이 함께 커집니다.

            예시)
            종목 가격이 5% 상승한 경우
            롱 X2 수익률: +10%
            숏 X2 수익률: -10%

            <red>- 포지션 수익률이 -100% 이하가 되면 투자금 전액을 잃고 즉시 강제청산됩니다.
            - 강제청산은 현재가뿐 아니라 확인 대상인 각 분의 저가와 고가까지 검사합니다.
            - X20은 반대 방향으로 약 5%만 움직여도 강제청산될 수 있으므로 신중하게 선택해 주세요.
            </red>

            ■ 평가금액과 판매
            - 포지션 평가금액은 투자원금에 롱·숏 방향과 선택한 배율의 수익률을 적용하여 계산합니다.
            - 판매 입력 금액은 현재 평가금액이 아니라 해당 종목에 남아 있는 투자원금을 기준으로 합니다.

            예시)
            비트코인 100개를 투자한 종목에서
            판매 금액으로 50개 입력
            해당 포지션의 50%를 현재 가격으로 정산

            - 수익 상태라면 입력한 투자원금보다 실제 판매 금액이 많을 수 있고, 손실 상태라면 더 적을 수 있습니다.
            - 기본(X1) 판매 수수료는 판매 금액의 0.015%이며, 배율을 사용하면 수수료에도 같은 배율이 적용됩니다.
            - 출금 세금 0.2%에는 포지션 배율이 적용되지 않습니다.
            - 시세를 불러오거나 과거 가격을 확인하는 동안에는 안전한 정산을 위해 거래가 잠시 제한될 수 있습니다.
            - 거래내역에서는 최근 거래 50건까지 확인할 수 있습니다.

            ■ 모험에 관하여..
            신규 모험이 업데이트되었습니다. 
            미궁, 일리저요새, 환영술사탑, 주술사오두막, 화염술사오두막을 찾아 상자 전리품을 찾으세요.
            전리품 - 1단계 ~ 4단계 열쇠, 강화원석 보상이 추가 되었습니다.

            ■ 랜덤상자 이용 방법
            - 랜덤상자는 단계별로 지정된 전용 열쇠가 필요합니다.
            - 랜덤상자를 한 번 열면 다음 아이템이 소모됩니다.
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

            ■ 네더라이트 장비 승급 방법
            - 네더라이트 장비는 잿빛 → 태양 → 생명 → 서리빛 순서로 한 단계씩 승급할 수 있습니다.
            - 승급 주괴는 조합창에서 만듭니다. 잿빛 주괴 = 네더라이트 주괴 1개 + 철 주괴 4개
            - 태양의 주괴 = 잿빛 주괴 1개 + 금 주괴 4개 / 생명의 주괴 = 태양의 주괴 1개 + 에메랄드 4개
            - 서리빛 주괴 = 생명의 주괴 1개 + 다이아몬드 4개
            - 대장장이 작업대에 [네더라이트 강화 형판 + 이전 단계 장비 + 다음 단계 주괴]를 넣으면 장비가 승급됩니다.
            - 예시: 네더라이트 검 + 잿빛 주괴 → 잿빛 검, 잿빛 검 + 태양의 주괴 → 태양의 검

            ■ 강화 제작대 이용 방법
            - 강화 제작대는 조합창에서 다이아몬드 블록 8개로 테두리를 채우고 중앙에 네더라이트 블록 1개를 넣어 제작합니다.
            - 강화 제작대에서는 서리빛 네더라이트 검, 도구, 방어구를 최대 +10강까지 강화할 수 있습니다.
            - 강화 제작대 이용 아이템은 서리빛 네더라이트 무기, 방어구, 도구들만 가능합니다.
            - 장비 칸에 강화할 장비를 넣고 원석 칸에 강화 원석 1개를 넣으면 강화를 시도할 수 있습니다.
            - 검은 강화 단계마다 공격력 1, 도구는 채굴 효율 0.1, 방어구는 최대 체력 1이 증가합니다.
            - 성공률은 +1강 시도 100%에서 시작해 단계마다 10%씩 감소하며, +10강 시도는 10%입니다.
            - 강화 성공 확률 증가 주문서를 확률권 칸에 넣으면 표시된 수치만큼 성공률이 올라가며, 강화 시도 시 1개가 소모됩니다.
            - 강화에 실패하면 현재 강화 단계가 1단계 내려갑니다.
            <red>- +3강 시도부터 장비 파괴 확률이 생기며, +3강 1%에서 시작해 단계마다 2%씩 증가하여 +10강 시도는 15%입니다.
            - 강화 파괴 방지권을 방지권 칸에 넣으면 파괴 판정이 발생했을 때 방지권 1개를 소모하고 장비 파괴를 한 번 막습니다.</red>

            ■ 엔드시티에 관하여...
            기존 엔드시티와는 차원이 다른 컨텐츠입니다 (강화버전)
            엔드시티에 갈때는 준비를 잘 하고 가세요.

            <red>■ 레이드 및 위더 전투 주의사항</red>
            <red>- 레이드가 진행 중인 지역에서는 플레이어 주변 80블록 이내의 철 골렘이 즉시 제거됩니다.
            - 살아 있는 위더가 플레이어 주변 80블록 이내에 있는 경우에도 주변 80블록 이내의 철 골렘이 즉시 제거됩니다.
            - 레이드 진행 중 겉날개로 활강하면 즉시 사망합니다.
            - 살아 있는 위더가 주변 80블록 이내에 있는 상태에서 겉날개로 활강해도 즉시 사망합니다.
            - 겉날개를 착용한 것만으로는 사망하지 않습니다. 실제로 겉날개 활강 상태가 되었을 때 적용됩니다.
            - 전투 중 겉날개 사용으로 사망하면 관련 안내 메시지가 서버 전체에 표시됩니다.
            - 철 골렘과 겉날개를 이용한 전투 우회를 막기 위한 규칙이므로 레이드와 위더 전투 전 장비를 미리 점검해 주세요.</red>
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

        // 축소 배율(0.5)이 적용되므로 화면상 실제 행간은 절반이 된다.
        private static final int LINE_SPACING = 2;

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
            int rawHeight = getLines().size() * BASE_LINE_HEIGHT
                    + Math.max(0, getLines().size() - 1) * LINE_SPACING;
            return (int) Math.ceil(rawHeight * this.scale);
        }

        @Override
        public void extractRenderState(
                GuiGraphicsExtractor graphics,
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
                super.extractRenderState(graphics, mouseX, mouseY, delta, x, y);
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
                graphics.text(
                        getFont(),
                        getLines().get(index),
                        0,
                        index * (BASE_LINE_HEIGHT + LINE_SPACING),
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
