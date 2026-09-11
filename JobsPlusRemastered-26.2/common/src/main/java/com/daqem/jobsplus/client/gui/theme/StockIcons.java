package com.daqem.jobsplus.client.gui.theme;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.stock.StockCatalog;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Bundled icons only: opening the market never downloads remote images. */
public final class StockIcons {
    private static final Map<String, Identifier> ICONS = StockCatalog.getStocks().stream().collect(
            Collectors.toUnmodifiableMap(StockCatalog.StockDefinition::id,
                    stock -> JobsPlus.getId("stocks/" + stock.id().toLowerCase(Locale.ROOT))));

    // The uploaded company marks are rectangular; preserve their proportions in square UI slots.
    private static final Map<String, Float> ICON_HEIGHT_RATIOS = Map.of(
            "009150", 347.0F / 576.0F,
            "006400", 407.0F / 513.0F);

    private StockIcons() {
    }

    public static void draw(GuiGraphicsExtractor graphics, String stockId, int x, int y, int size) {
        if (stockId == null || size <= 0) {
            return;
        }
        Identifier icon = ICONS.get(stockId);
        if (icon != null) {
            float heightRatio = ICON_HEIGHT_RATIOS.getOrDefault(stockId, 1.0F);
            int iconHeight = Math.max(1, Math.round(size * heightRatio));
            int iconY = y + (size - iconHeight) / 2;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, x, iconY, size, iconHeight);
        }
    }
}
