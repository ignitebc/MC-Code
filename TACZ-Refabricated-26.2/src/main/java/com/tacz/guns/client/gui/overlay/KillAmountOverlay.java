package com.tacz.guns.client.gui.overlay;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.client.RenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

/**
 * 처치 수 안내. <b>원본 1.21.1과 항목별로 맞췄다</b>.
 *
 * <h2>원본과 달랐던 점(사용자 실측 대조 그림 57 / 01)</h2>
 * 예전 구현은 "기능만 만든" 것이라 모양이 원본과 전혀 달랐다:
 * <table border="1">
 *   <tr><th></th><th>예전 구현</th><th>원본(지금은 맞춤)</th></tr>
 *   <tr><td>글자</td><td>{@code × 1}</td><td>{@code ☠ x 01}(해골 기호 + 한 자리 앞 0 채움)</td></tr>
 *   <tr><td>위치</td><td>화면 정중앙 약간 아래</td><td><b>오른쪽 아래</b>, 조준선 오른쪽</td></tr>
 *   <tr><td>크기</td><td>없음(원래 글자 크기)</td><td>{@code 0.5}배</td></tr>
 *   <tr><td>색</td><td>고정 빨강 {@code 0xFF5555}</td><td>연속 처치 수에 따른 <b>HSV 그라데이션</b>(노랑→빨강)</td></tr>
 *   <tr><td>사라짐</td><td>처음부터 선형</td><td>앞 2/3는 완전 불투명, 뒤 1/3만 사라짐</td></tr>
 *   <tr><td>조건</td><td>수만 확인</td><td><b>주 손에 총을 들고 있어야</b> 함</td></tr>
 * </table>
 *
 * <h2>26.2 이식 요점</h2>
 * <ul>
 *   <li>{@code PoseStack} → {@code Matrix3x2fStack}, {@code pushPose/popPose} →
 *       {@code pushMatrix/popMatrix}({@code GunSmithTableScreen}에서 이미 검증한 방식과 같다).</li>
 *   <li>{@code RenderSystem.enableBlend()} 등은 제거되었다 — 26.2의 GUI 글자는
 *       {@code GuiRenderState}를 거치고 혼합은 파이프라인이 알아서 하므로 직접 켜고 끌 필요가 없다.</li>
 *   <li>색은 <b>반드시 알파를 포함</b>해야 한다: {@code GuiGraphicsExtractor#text}의 첫 명령이
 *       {@code if (ARGB.alpha(color) == 0) return;}이다. 원본의
 *       {@code Mth.hsvToRgb(...) + (alpha << 24)}는 이를 자연히 만족하므로 그대로 둔다.</li>
 * </ul>
 */
public class KillAmountOverlay {
    private static long killTimestamp = -1L;
    private static int killAmount = 0;

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        if (!RenderConfig.KILL_AMOUNT_ENABLE.get()) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int timeout = (int) (RenderConfig.KILL_AMOUNT_DURATION_SECOND.get() * 1000);
        // 연속 처치 수가 이 값에 이르면 가장 빨간색이 된다. 원본은 30이다.
        float colorCount = 30;

        long remainTime = System.currentTimeMillis() - killTimestamp;
        if (remainTime > timeout) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!(player instanceof IClientPlayerGunOperator)) {
            return;
        }
        // 원본 의미: 주 손에 총을 들고 있을 때만 처치 안내를 표시한다.
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IGun)) {
            return;
        }

        String text;
        if (killAmount < 10) {
            text = "\u2620 x 0" + killAmount;
        } else {
            text = "\u2620 x " + killAmount;
        }
        int fontWith = mc.font.width(text);
        // 앞 2/3 시간은 불투명하게 두고 마지막 1/3에서 사라지기 시작한다.
        double fadeOutTime = timeout / 3.0 * 2;
        float hue = (1 - Math.min((killAmount / colorCount), 1)) * 0.15f;
        int alpha = 0xFF;
        if (remainTime > fadeOutTime) {
            alpha = 0xFF - (int) ((remainTime - fadeOutTime) / (timeout - fadeOutTime) * 0xF0);
        }
        int color = Mth.hsvToRgb(hue, 0.75f, 1) + (alpha << 24);

        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        {
            // 먼저 크기를 줄이고 2배 좌표로 위치를 잡는다 — 원본과 글자 그대로 같다:
            // 0.5배로 줄인 뒤에는 화면 픽셀 (x, y)가 그리기 좌표 (2x, 2y)에 대응한다.
            poseStack.scale(0.5f, 0.5f);
            graphics.text(mc.font, text, (int) (width - fontWith / 2.0f), (height - 45) * 2 - 1, color, false);
        }
        poseStack.popMatrix();
    }

    public static void markTimestamp() {
        int timeout = (int) (RenderConfig.KILL_AMOUNT_DURATION_SECOND.get() * 1000);
        if (System.currentTimeMillis() - killTimestamp > timeout) {
            killAmount = 0;
        }
        killTimestamp = System.currentTimeMillis();
        killAmount += 1;
    }
}
