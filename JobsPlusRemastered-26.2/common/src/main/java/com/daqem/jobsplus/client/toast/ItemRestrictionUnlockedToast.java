package com.daqem.jobsplus.client.toast;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.itemrestrictions.data.ItemRestriction;
import com.daqem.jobsplus.JobsPlus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ItemRestrictionUnlockedToast implements Toast
{
    private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/recipe");
    private static final long DISPLAY_TIME = 5000L;
    private final List<ItemRestriction> itemRestrictions = new ArrayList<>();
    private long lastChanged;
    private boolean changed;
    private Toast.Visibility wantedVisibility = Visibility.HIDE;
    private int displayedItemRestrictionIndex;

    @Override
    public @NotNull Visibility getWantedVisibility()
    {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager toastManager, long l)
    {
        if (this.changed)
        {
            this.lastChanged = l;
            this.changed = false;
        }

        if (this.itemRestrictions.isEmpty())
        {
            this.wantedVisibility = Visibility.HIDE;
        } 
        else
        {
            this.wantedVisibility = (double) (l - this.lastChanged) >= DISPLAY_TIME * toastManager.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
        }

        this.displayedItemRestrictionIndex = (int) (l / Math.max(1.0, DISPLAY_TIME * toastManager.getNotificationDisplayTimeMultiplier() / this.itemRestrictions.size()) % this.itemRestrictions.size());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, Font font, long l)
    {
        ItemRestriction entry = this.itemRestrictions.get(this.displayedItemRestrictionIndex);
        JobsTheme.sprite(guiGraphics, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
        guiGraphics.text(font, entry.getIcon().getHoverName(), 30, 7, JobsTheme.CYAN, false);
        guiGraphics.text(font, JobsPlus.translatable("job.item_unlocked.toast"), 30, 18, JobsTheme.TEXT, false);
        guiGraphics.fakeItem(entry.getIcon(), 8, 8);
    }

    private void addItem(ItemRestriction itemRestriction)
    {
        this.itemRestrictions.add(itemRestriction);
        this.changed = true;
    }

    public static void addOrUpdate(ToastManager toastManager, ItemRestriction itemRestriction)
    {
        ItemRestrictionUnlockedToast itemRestrictionUnlockedToast = toastManager.getToast(ItemRestrictionUnlockedToast.class, NO_TOKEN);
        if (itemRestrictionUnlockedToast == null)
        {
            itemRestrictionUnlockedToast = new ItemRestrictionUnlockedToast();
            toastManager.addToast(itemRestrictionUnlockedToast);
        }

        ContextMap contextMap = SlotDisplayContext.fromLevel(Objects.requireNonNull(toastManager.getMinecraft().level));
        itemRestrictionUnlockedToast.addItem(itemRestriction);
    }
}
