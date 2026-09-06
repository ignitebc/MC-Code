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

    private StockIcons() {
    }

    public static void draw(GuiGraphicsExtractor graphics, String stockId, int x, int y, int size) {
        if (stockId == null || size <= 0) {
            return;
        }
        Identifier icon = ICONS.get(stockId);
        if (icon != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, x, y, size, size);
        }
    }
}
