package com.daqem.jobsplus.client.gui.jobs.components;

import java.lang.reflect.Method;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.RecipesScrollWidget;
import com.daqem.uilib.api.component.IComponent;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class RecipesScrollComponent extends EmptyComponent
{
    // 업데이트 내역(표시 텍스트)
    private static final String PATCH_NOTES = """
            ★ 26.01.02 업데이트 ★
            
            ★ 광부 레벨업 조건 변경
            - 섬세한곡괭이는 제외 (경험치,비트코인 X)
            
            - 일반 블록 추가
            조약돌,화강암,안산암,섬록암,흑암,프리즈머린
            응회암,점적석블록,현무암,발광석(경험치 조건 참조)
            직업 경험치 획득 0 ~ 1 -> 1 고정 (버프)

            - hardBlock 추가(심층암, 심층암조약돌, 엔드스톤)
            직업경험치 3 고정, 비트코인 0.05% (추가)
            
            - endBlock 추가(흑요석, 우는흑요석, 보강된심층암)
            직업경험치 3 ~ 5 (추가)
            
            - 광물 캘시 (조건 변경)
            석탄, 철, 구리, 금, 레드스톤, 청금석,
            다이아몬드, 에메랄드, 네더석영, 네더 금
            직업경험치 4 고정, 비트코인 0.07 % (조정)

            - 심층암 광물 캘시 
            직업경험치 300 ~ 500, 비트코인 100% 1개 지급 (추가)   

            ★ 추후 업데이트 ★
            - illager invasion 모드 도입 예정 (추가몬스터)
            - 레이드 추가레벨 도입 예정 (현재 5단계 최대)
            - 광부 스킬 추가 예정 (사거리, 화염저항)
            - 낚시꾼, 갈고리 낚시대 사용시 내구도 감소 예정
            - 사냥꾼 스킬 추가 예정 (힘의물약+1증가, 약탈량증가)
            - 워든 잡을 시 강화조각, 강화원석 드롭 예정
            - 위더 잡을 시 강화조각 드롭 예정
            - 위더, 워든:공격력, 체력, 범위 버프 예정
            - 신규세트 방어구 도입 예정
            - 연금술사 스킬추가예정 (낙뢰물약)
            - 굴착공 스킬추가예정(스페셜 굴착공)
            - 모험가 스킬 개편중
            - 농부 스킬 개편중
            - 대장장이 스킬추가예정 (원거리 데미지감소, 폭발저항)
            - 전 직업 하이퍼스킬 개방 예정 (lv 150 ~ 300)

            ★ 업데이트 완료목록 ★
            - 셜커상자 미리보기 모드 추가

            황금사과, 마법이부여된황금사과, 토템,
            방패의 활용도와 아이템강화 등에 대한
            중요성을 높이기 위해
            보스전, 레이드 겉날개사용금지.
            겉날개는 이동의 수단이지, 도주의 수단이 되는걸 방지.

            ★ 레이드 시작후, 겉날개 사용시 즉사 (80블록,5청크 제한)
            ★ 위더(보스몹) 시작후, 겉날개 사용시 즉사 (80블록,5청크 제한)

            * 잉여자원 처리
            - 익힌 닭고기 10개 -> 에메랄드 1개 (상점추가)
            - 흙 192개(3세트) -> 에메랄드 1개 (상점추가)
            - 네더랙 320개(5세트) -> 에메랄드 1개 (상점추가)

            * 굴착공 (너프)
            - 레벨업조건: 삽으로 캐는 모든블록 -> 특정블록

            * 굴착공 (버프)
            - 삽 채굴시 일정확률 금조각 획득 (스킬추가)
            - 삽 채굴시 일정확률 금괴 획득 (스킬추가)
            - 삽 채굴시 일정확률 금블록 획득 (스킬추가)

            * 사냥꾼 (너프)
            - 몹 처치시 비트코인 0.03% -> 0.01% (몹 자동공장화 가능)

            * 사냥꾼 (버프)
            - 이동속도증가 (걷기,달리기,수영,점프 포함, 스킬추가)
            - 크리퍼 처치시 비트코인 0.04% 확률로 획득 (독립시행)
            - 워든,위더 처치시 비트코인 30% 확률로 3개 획득 (독립시행)
            - 위더스켈레톤 처치시 비트코인 0.4%확률로 획득 (독립시행)

            * 연금술사 (너프)
            - 물약 양조시 비트코인 1% 획득 -> 0.7%

            * 연금술사 (버프)
            - 더블드롭: 연금술관련 몹 처치시 드롭률 2배 (스킬추가)
            - 스페셜인첸터 : 물약 제조시 다이아몬드 획득 (스킬추가)
            - 포션 레벨 +2 증가 (스킬추가)

            * 광부 (버프)
            - 스페셜광물 채굴시 비트코인 0.07% -> 0.1%
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

            ★ 랜덤 상자 대폭 상향 ★
            추후 보상이 업데이트 되더라도
            뽑은거에 대한 갯수 추가 보상을 지급
            단, 확률이 증가한거에 대한 보상은 지급 하지 않음
            ex) 토템2개 -> 4개로 상자보상상향시 추가 2개 지급

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

        // ScrollWidget의 실제 컨텐츠(첫 컴포넌트)
        IComponent scrollContentComponent = recipesScrollWidget.getComponents().getFirst();

        // 레시피가 없을 때: "스크롤 위젯 내부 컨텐츠"를 패치노트로 교체해야 스크롤이 동작함
        if (scrollContentComponent.getComponents().isEmpty())
        {
            int textWidth = Math.max(1, getWidth() - 10);
            // int textWidth = Math.max(1, getWidth() - 16);

            // 여기 값만 바꾸면 크기가 실제로 변해야 정상입니다.
            float textScale = 0.50f;
            // float textScale = 0.70f;

            // scale 적용 시 보이는 폭 = wrapWidth * scale
            // => 줄바꿈 폭을 scale로 역보정
            int wrapWidth = Math.max(1, (int) Math.ceil(textWidth / textScale));

            ScaledMultiLineTextComponent patchNotesText = new ScaledMultiLineTextComponent(0, 0, wrapWidth, Component.literal(PATCH_NOTES), 0xFF000000, textScale);

            EmptyComponent patchContainer = new EmptyComponent(0, 0, textWidth, 0);
            patchContainer.addComponent(patchNotesText);

            // 스케일된 높이로 컨테이너 높이 맞춤(스크롤 핵심)
            patchContainer.setHeight(patchNotesText.getScaledHeight());

            recipesScrollWidget.getComponents().clear();
            recipesScrollWidget.addComponent(patchContainer);

            if (patchContainer.getWidth() <= getWidth())
            {
                this.setWidth(patchContainer.getWidth());
                this.centerHorizontally();
            }
        } else
        {
            if (scrollContentComponent.getHeight() <= getHeight())
            {
                this.setWidth(scrollContentComponent.getWidth());
                this.centerHorizontally();
            }
        }

        this.addWidget(recipesScrollWidget);
    }

    /**
     * UIlib MultiLineTextComponent는 기본적으로 스케일을 지원하지 않는 케이스가 많습니다. 또한
     * graphics.pose()의 스택 타입/메서드명(push/pop 등)이 매핑에 따라 달라집니다.
     *
     * 그래서 pose 스택 조작(푸시/팝/이동/스케일)을 전부 리플렉션으로 처리해서 "컴파일 오류 없이" 동작하도록 만듭니다.
     */
    private static final class ScaledMultiLineTextComponent extends MultiLineTextComponent
    {
        private static final int BASE_LINE_HEIGHT = 9;
        private final float scale;

        public ScaledMultiLineTextComponent(int x, int y, int maxWidth, Component text, int color, float scale)
        {
            super(x, y, maxWidth, text, color);
            this.scale = (scale <= 0.0f) ? 1.0f : scale;
        }

        public int getScaledHeight()
        {
            return (int) Math.ceil(getLines().size() * BASE_LINE_HEIGHT * this.scale);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y)
        {
            Object pose = graphics.pose();

            // push
            if (!invokeNoArg(pose, "pushPose") && !invokeNoArg(pose, "push") && !invokeNoArg(pose, "pushMatrix"))
            {
                // push 메서드를 못 찾으면 스케일 적용이 불가능하므로 그냥 기본 렌더로 폴백
                super.render(graphics, mouseX, mouseY, delta, x, y);
                return;
            }

            // translate
            float tx = (float) getTotalX();
            float ty = (float) getTotalY();
            if (!invoke2f(pose, "translate", tx, ty))
            {
                // 일부 매핑은 translate(x,y,z)
                invoke3f(pose, "translate", tx, ty, 0.0f);
            }

            // scale
            if (!invoke2f(pose, "scale", this.scale, this.scale))
            {
                // 일부 매핑은 scale(x,y,z)
                invoke3f(pose, "scale", this.scale, this.scale, 1.0f);
            }

            for (int i = 0; i < getLines().size(); i++)
            {
                graphics.drawString(getFont(), getLines().get(i), 0, i * BASE_LINE_HEIGHT, getColor(), isDrawShadow());
            }

            // pop
            invokeNoArg(pose, "popPose");
            invokeNoArg(pose, "pop");
            invokeNoArg(pose, "popMatrix");
        }

        private static boolean invokeNoArg(Object target, String methodName)
        {
            try
            {
                Method m = target.getClass().getMethod(methodName);
                m.invoke(target);
                return true;
            } catch (Throwable ignored)
            {
                return false;
            }
        }

        private static boolean invoke2f(Object target, String methodName, float a, float b)
        {
            try
            {
                Method m = target.getClass().getMethod(methodName, float.class, float.class);
                m.invoke(target, a, b);
                return true;
            } catch (Throwable ignored)
            {
                return false;
            }
        }

        private static boolean invoke3f(Object target, String methodName, float a, float b, float c)
        {
            try
            {
                Method m = target.getClass().getMethod(methodName, float.class, float.class, float.class);
                m.invoke(target, a, b, c);
                return true;
            } catch (Throwable ignored)
            {
                return false;
            }
        }
    }
}
