package com.mcserver.serverutilities.client;

import com.mcserver.serverutilities.tier.EquipmentTierSummary;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * 장비 등급 툴팁의 수치 칸을 같은 세로줄에 맞춘다.
 *
 * <p>마인크래프트 글꼴은 글자마다 폭이 달라 공백 개수만으로는 줄을 맞출 수 없다. 대신 글꼴로 각 항목
 * 칸의 실제 폭을 재고, 일반 공백(기본 글꼴 4px)과 굵은 공백(5px)을 섞어 모자란 폭을 정확히 채운다.
 * 두 폭을 섞으면 12px 이상의 모든 폭을 만들 수 있으므로 칸 사이 최소 간격을 12px로 둔다.
 * 폭은 실행 중에 재므로 리소스팩이나 유니코드 글꼴로 바뀌어도 그 글꼴에 맞춰진다.
 */
public final class TooltipColumnLayout {
    private static final int MIN_COLUMN_GAP = 12;
    private static final String SPACE = " ";

    private TooltipColumnLayout() { }

    public static List<Component> align(List<EquipmentTierSummary.Row> rows) {
        Minecraft minecraft = Minecraft.getInstance();
        // 글꼴의 글자 폭 캐시는 렌더 스레드에서만 써야 한다. 크리에이티브 검색 목록처럼 다른 스레드에서
        // 툴팁을 만들 때는 줄을 맞추지 않는다. 그런 툴팁은 화면에 그리지 않고 검색에만 쓴다.
        if (!minecraft.isSameThread()) return EquipmentTierSummary.joinWithSpace(rows);

        Font font = minecraft.font;
        int spaceWidth = font.width(SPACE);
        int boldSpaceWidth = font.width(Component.literal(SPACE).withStyle(ChatFormatting.BOLD));
        if (spaceWidth <= 0 || boldSpaceWidth <= 0) return EquipmentTierSummary.joinWithSpace(rows);

        int labelColumnWidth = 0;
        for (EquipmentTierSummary.Row row : rows) {
            labelColumnWidth = Math.max(labelColumnWidth, font.width(row.label()));
        }
        int detailX = labelColumnWidth + MIN_COLUMN_GAP;

        List<Component> lines = new ArrayList<>(rows.size());
        for (EquipmentTierSummary.Row row : rows) {
            int paddingWidth = detailX - font.width(row.label());
            lines.add(Component.empty()
                    .append(row.label())
                    .append(padding(paddingWidth, spaceWidth, boldSpaceWidth))
                    .append(row.detail()));
        }
        return lines;
    }

    /**
     * 폭이 정확히 width인 공백. 굵은 공백을 적게 쓰는 조합부터 찾는다.
     *
     * <p>두 공백 폭으로 정확히 만들 수 없는 글꼴이면 가장 가까운 일반 공백 개수로 채운다.
     */
    private static Component padding(int width, int spaceWidth, int boldSpaceWidth) {
        int boldCount = 0;
        int plainCount = -1;
        for (int bold = 0; bold * boldSpaceWidth <= width; bold++) {
            int rest = width - bold * boldSpaceWidth;
            if (rest % spaceWidth == 0) {
                boldCount = bold;
                plainCount = rest / spaceWidth;
                break;
            }
        }
        if (plainCount < 0) {
            boldCount = 0;
            plainCount = Math.max(1, Math.round(width / (float) spaceWidth));
        }

        MutableComponent padding = Component.literal(SPACE.repeat(plainCount));
        if (boldCount > 0) {
            padding.append(Component.literal(SPACE.repeat(boldCount)).withStyle(ChatFormatting.BOLD));
        }
        return padding;
    }
}
