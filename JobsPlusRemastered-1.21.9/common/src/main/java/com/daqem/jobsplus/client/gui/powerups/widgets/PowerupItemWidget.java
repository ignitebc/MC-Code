package com.daqem.jobsplus.client.gui.powerups.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreen;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.networking.c2s.ServerboundOpenPowerupsScreenPacket;
import com.daqem.jobsplus.networking.c2s.ServerboundStartPowerupPacket;
import com.daqem.jobsplus.networking.c2s.ServerboundTogglePowerUpPacket;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.daqem.uilib.api.skilltree.ISkillTreeItem;
import com.daqem.uilib.api.widget.skilltree.ISkillTreeItemWidget;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


// powerup 추가시 그냥 위젯이 알아서 위치에 맞게 선을 그어줌(ui상) , 따로 건들일 필요 없고, json의 parent만 연결을 잘해주면 됨
// powerup 스킬 추가, 삭제시 json parent 재배치 필요
// {
//   "location": "jobsplus:miner/skill2",
//   "job": "jobsplus:miner",
//   "parent": "jobsplus:miner/skill1",  // 부모 스킬 지정 → 자동으로 연결선 생성
//   "required_level": 5,  // 레벨 5 필요 → 자동으로 레벨 체크
//   "price": 10,
//   "icon": {...},
//   "type": "basic"
// }

public class PowerupItemWidget extends CustomButtonWidget implements ISkillTreeItemWidget
{

    private final ISkillTreeItem skillTreeItem;
    private final PowerupsScreenState state;
    private final Powerup powerup;

    public PowerupItemWidget(ISkillTreeItem skillTreeItem, PowerupsScreenState state, Powerup powerup)
    {
        super(0, 0, 26, 26, powerup != null ? powerup.getPowerupInstance().getName() : state.getJob().getJobInstance().getName(), null, btn ->
        {
            if (btn instanceof PowerupItemWidget button && button.isActive())
            {
                Powerup powerUp = button.getPowerup();
                PowerupInstance powerupInstance = powerUp.getPowerupInstance();
                ResourceLocation location = button.getState().getJob().getJobInstance().getLocation();
                if (powerUp.getState() == PowerupState.ACTIVE || powerUp.getState() == PowerupState.INACTIVE)
                {
                    NetworkManager.sendToServer(new ServerboundTogglePowerUpPacket(location, powerupInstance.getLocation()));
                    if (Minecraft.getInstance().screen instanceof PowerupsScreen powerupsScreen)
                    {
                        button.getPowerup().setState(powerUp.getState() == PowerupState.ACTIVE ? PowerupState.INACTIVE : PowerupState.ACTIVE);
                    }
                } else if (powerUp.getState() == PowerupState.NOT_OWNED)
                {
                    Minecraft.getInstance().setScreen(new ConfirmationScreen(Minecraft.getInstance().screen, new ConfirmationScreenState(JobsPlus.translatable("gui.confirmation.purchase_powerup", powerupInstance.getName(), powerupInstance.getPrice()), () ->
                    {
                        NetworkManager.sendToServer(new ServerboundStartPowerupPacket(location, powerupInstance.getLocation()));
                        NetworkManager.sendToServer(new ServerboundOpenPowerupsScreenPacket(location));
                    })));
                }
            }
        });
        this.skillTreeItem = skillTreeItem;
        this.state = state;
        this.powerup = powerup;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.blitSlot(guiGraphics);
    }

    public PowerupsScreenState getState()
    {
        return state;
    }

    public Powerup getPowerup()
    {
        return powerup;
    }

    private ResourceLocation getSprite()
    {
        ResourceLocation defaultSprite = JobsPlus.getId("powerups/slot_active");
        ResourceLocation lockedSprite = JobsPlus.getId("powerups/slot_locked");
        ResourceLocation notOwnedSprite = JobsPlus.getId("powerups/slot_not_owned");
        if (this.powerup == null)
        {
            Job job = state.getJob();
            if (job.getLevel() > 0)
            {
                return defaultSprite;
            }
            if (state.getCoins() >= job.getJobInstance().getPrice())
            {
                return notOwnedSprite;
            }
            return lockedSprite;
        }
        if (!hasPowerup() && (!hasEnoughCoins() || !hasRequiredLevel()))
        {
            return lockedSprite;
        }
        return switch (this.powerup.getState()) {
        case ACTIVE -> defaultSprite;
        case INACTIVE -> JobsPlus.getId("powerups/slot_inactive");
        case NOT_OWNED -> notOwnedSprite;
        case LOCKED -> lockedSprite;
        };
    }

    private void blitSlot(GuiGraphics guiGraphics)
    {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight());

        ItemStack icon = this.powerup != null ? this.powerup.getPowerupInstance().getIcon() : state.getJob().getJobInstance().getIconItem();
        guiGraphics.renderFakeItem(icon, this.getX() + 5, this.getY() + 5);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, icon, this.getX() + 5, this.getY() + 5);
    }

    @Override
    public void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        // 레벨 조건과 상관없이 마우스 오버 시 항상 툴팁 표시
        if (this.isMouseOver(mouseX, mouseY))
        {
            Component title = this.powerup != null ? this.powerup.getPowerupInstance().getName() : state.getJob().getJobInstance().getName();
            Component description = this.powerup != null ? this.powerup.getPowerupInstance().getDescription() : state.getJob().getJobInstance().getDescription();
            int titleWidth = Math.max(50, Minecraft.getInstance().font.width(title));
            MultiLineTextComponent descriptionComponent = new MultiLineTextComponent(0, 0, titleWidth + getWidth() + 10, description, 0xFF1E1410);
            if (getX() + getWidth() + titleWidth + 18 > guiGraphics.guiWidth())
            {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/text_background"), this.getX() - titleWidth - 18, this.getY() + 7, this.getWidth() + titleWidth + 24, 20 + descriptionComponent.getHeight() + 6 + (this.powerup != null && this.powerup.getPowerupInstance().getRequiredLevel() > 0 ? 25 : 13));
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/bar"), this.getX() - titleWidth - 18, this.getY() + 3, this.getWidth() + titleWidth + 24, 20);
                guiGraphics.drawString(Minecraft.getInstance().font, title, this.getX() - titleWidth - 6, this.getY() + 9, 0xFF1E1410, false);
                if (this.powerup != null)
                {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/line"), this.getX() - titleWidth - 12, this.getY() + 29 + descriptionComponent.getHeight() + 1, 30, 1);
                    int requiredLevel = this.powerup.getPowerupInstance().getRequiredLevel();
                    if (requiredLevel > 0)
                    {
                        guiGraphics.drawString(Minecraft.getInstance().font, JobsPlus.translatable("gui.powerups.required_level", requiredLevel), this.getX() - titleWidth - 11, this.getY() + 29 + descriptionComponent.getHeight() + 4, 0xFF1E1410, false);
                    }
                    MutableComponent price = JobsPlus.translatable("gui.powerups.price", this.powerup.getPowerupInstance().getPrice());
                    guiGraphics.drawString(Minecraft.getInstance().font, price, this.getX() - titleWidth - 11, this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4), 0xFF1E1410, false);
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("jobs/coins"), this.getX() - titleWidth - 11 + Minecraft.getInstance().font.width(price) + 2, this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4), 7, 8);
                }
                descriptionComponent.setX(this.getX() - titleWidth - 11);
                descriptionComponent.setY(this.getY() + 29);
                descriptionComponent.renderBase(guiGraphics, mouseX, mouseY, 0, 0, 0);
            } 
            else
            {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/text_background"), this.getX() - 6, this.getY() + 7, this.getWidth() + titleWidth + 24, 20 + descriptionComponent.getHeight() + 6 + (this.powerup != null && this.powerup.getPowerupInstance().getRequiredLevel() > 0 ? 25 : 13));
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/bar"), this.getX() - 6, this.getY() + 3, this.getWidth() + titleWidth + 24, 20);
                guiGraphics.drawString(Minecraft.getInstance().font, title, this.getX() + this.getWidth() + 8, this.getY() + 9, 0xFF1E1410, false);
                if (this.powerup != null)
                {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/line"), this.getX(), this.getY() + 29 + descriptionComponent.getHeight() + 1, 30, 1);
                    int requiredLevel = this.powerup.getPowerupInstance().getRequiredLevel();
                    if (requiredLevel > 0)
                    {
                        guiGraphics.drawString(Minecraft.getInstance().font, JobsPlus.translatable("gui.powerups.required_level", requiredLevel), this.getX() + 1, this.getY() + 29 + descriptionComponent.getHeight() + 4, 0xFF1E1410, false);
                    }
                    MutableComponent price = JobsPlus.translatable("gui.powerups.price", this.powerup.getPowerupInstance().getPrice());
                    guiGraphics.drawString(Minecraft.getInstance().font, price, this.getX() + 1, this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4), 0xFF1E1410, false);
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("jobs/coins"), this.getX() + 1 + Minecraft.getInstance().font.width(price) + 2, this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4), 7, 8);
                }
                descriptionComponent.setX(this.getX() + 1);
                descriptionComponent.setY(this.getY() + 29);
                descriptionComponent.renderBase(guiGraphics, mouseX, mouseY, 0, 0, 0);
            }
            this.blitSlot(guiGraphics);
        }
    }

    @Override
    public ISkillTreeItem getSkillTreeItem()
    {
        return this.skillTreeItem;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo mouseButtonInfo)
    {
        if (hasPowerup())
            return true;

        boolean isCorrectPowerupState = this.powerup != null && this.powerup.getState() != PowerupState.LOCKED;
        return isCorrectPowerupState && hasEnoughCoins() && hasRequiredLevel();
    }

    @Override
    public boolean isActive()
    {
        return isValidClickButton(new MouseButtonInfo(0, 0));
    }

    private boolean hasEnoughCoins()
    {
        return this.powerup != null && this.state.getCoins() >= this.powerup.getPowerupInstance().getPrice();
    }

    private boolean hasRequiredLevel()
    {
        return this.powerup != null && this.state.getJob().getLevel() >= 1 && this.state.getJob().getLevel() >= this.powerup.getPowerupInstance().getRequiredLevel();
    }

    private boolean hasPowerup()
    {
        return this.powerup != null && (this.powerup.getState() == PowerupState.ACTIVE || this.powerup.getState() == PowerupState.INACTIVE);
    }
}


// // 툴팁 표시로 인한 전체 수정

// package com.daqem.jobsplus.client.gui.powerups.widgets;

// import com.daqem.jobsplus.JobsPlus;
// import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
// import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
// import com.daqem.jobsplus.client.gui.powerups.PowerupsScreen;
// import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
// import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
// import com.daqem.jobsplus.networking.c2s.ServerboundOpenPowerupsScreenPacket;
// import com.daqem.jobsplus.networking.c2s.ServerboundStartPowerupPacket;
// import com.daqem.jobsplus.networking.c2s.ServerboundTogglePowerUpPacket;
// import com.daqem.jobsplus.player.job.Job;
// import com.daqem.jobsplus.player.job.powerup.Powerup;
// import com.daqem.jobsplus.player.job.powerup.PowerupState;
// import com.daqem.uilib.api.skilltree.ISkillTreeItem;
// import com.daqem.uilib.api.widget.skilltree.ISkillTreeItemWidget;
// import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
// import com.daqem.uilib.gui.widget.CustomButtonWidget;
// import dev.architectury.networking.NetworkManager;
// import net.minecraft.client.Minecraft;
// import net.minecraft.client.gui.GuiGraphics;
// import net.minecraft.client.input.MouseButtonInfo;
// import net.minecraft.client.renderer.RenderPipelines;
// import net.minecraft.network.chat.Component;
// import net.minecraft.network.chat.MutableComponent;
// import net.minecraft.resources.ResourceLocation;
// import net.minecraft.world.item.ItemStack;

// // powerup 추가시 그냥 위젯이 알아서 위치에 맞게 선을 그어줌(ui상) , 따로 건들일 필요 없고, json의 parent만 연결을 잘해주면 됨
// // powerup 스킬 추가, 삭제시 json parent 재배치 필요
// // {
// //   "location": "jobsplus:miner/skill2",
// //   "job": "jobsplus:miner",
// //   "parent": "jobsplus:miner/skill1",  // 부모 스킬 지정 → 자동으로 연결선 생성
// //   "required_level": 5,  // 레벨 5 필요 → 자동으로 레벨 체크
// //   "price": 10,
// //   "icon": {...},
// //   "type": "basic"
// // }

// public class PowerupItemWidget extends CustomButtonWidget implements ISkillTreeItemWidget
// {

//     private final ISkillTreeItem skillTreeItem;
//     private final PowerupsScreenState state;
//     private final Powerup powerup;

//     public PowerupItemWidget(ISkillTreeItem skillTreeItem, PowerupsScreenState state, Powerup powerup)
//     {
//         super(0, 0, 26, 26, powerup != null ? powerup.getPowerupInstance().getName() : state.getJob().getJobInstance().getName(), null, btn ->
//         {
//             if (btn instanceof PowerupItemWidget button)
//             {

//                 // 클릭 가능 조건(레벨, 코인, 상태) 체크
//                 if (!button.canClickPowerup())
//                 {
//                     // 필요하면 여기서 "레벨 부족" / "코인 부족" 안내도 가능
//                     return;
//                 }

//                 Powerup powerUp = button.getPowerup();
//                 PowerupInstance powerupInstance = powerUp.getPowerupInstance();
//                 ResourceLocation location = button.getState().getJob().getJobInstance().getLocation();

//                 if (powerUp.getState() == PowerupState.ACTIVE || powerUp.getState() == PowerupState.INACTIVE)
//                 {
//                     // 토글
//                     NetworkManager.sendToServer(new ServerboundTogglePowerUpPacket(location, powerupInstance.getLocation()));
//                     if (Minecraft.getInstance().screen instanceof PowerupsScreen powerupsScreen)
//                     {
//                         button.getPowerup().setState(powerUp.getState() == PowerupState.ACTIVE ? PowerupState.INACTIVE : PowerupState.ACTIVE);
//                     }
//                 } else if (powerUp.getState() == PowerupState.NOT_OWNED)
//                 {
//                     // 구매 확인 창
//                     Minecraft.getInstance().setScreen(new ConfirmationScreen(Minecraft.getInstance().screen, new ConfirmationScreenState(JobsPlus.translatable("gui.confirmation.purchase_powerup", powerupInstance.getName(), powerupInstance.getPrice()), () ->
//                     {
//                         NetworkManager.sendToServer(new ServerboundStartPowerupPacket(location, powerupInstance.getLocation()));
//                         NetworkManager.sendToServer(new ServerboundOpenPowerupsScreenPacket(location));
//                     })));
//                 }
//             }
//         });
//         this.skillTreeItem = skillTreeItem;
//         this.state = state;
//         this.powerup = powerup;
//     }

//     @Override
//     protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
//     {
//         this.blitSlot(guiGraphics);
//     }

//     public PowerupsScreenState getState()
//     {
//         return state;
//     }

//     public Powerup getPowerup()
//     {
//         return powerup;
//     }

//     private ResourceLocation getSprite()
//     {
//         ResourceLocation defaultSprite = JobsPlus.getId("powerups/slot_active");
//         ResourceLocation lockedSprite = JobsPlus.getId("powerups/slot_locked");
//         ResourceLocation notOwnedSprite = JobsPlus.getId("powerups/slot_not_owned");
//         if (this.powerup == null)
//         {
//             Job job = state.getJob();
//             if (job.getLevel() > 0)
//             {
//                 return defaultSprite;
//             }
//             if (state.getCoins() >= job.getJobInstance().getPrice())
//             {
//                 return notOwnedSprite;
//             }
//             return lockedSprite;
//         }
//         if (!hasPowerup() && (!hasEnoughCoins() || !hasRequiredLevel()))
//         {
//             return lockedSprite;
//         }
//         return switch (this.powerup.getState()) {
//         case ACTIVE -> defaultSprite;
//         case INACTIVE -> JobsPlus.getId("powerups/slot_inactive");
//         case NOT_OWNED -> notOwnedSprite;
//         case LOCKED -> lockedSprite;
//         };
//     }

//     private void blitSlot(GuiGraphics guiGraphics)
//     {
//         guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight());

//         ItemStack icon = this.powerup != null ? this.powerup.getPowerupInstance().getIcon() : state.getJob().getJobInstance().getIconItem();
//         guiGraphics.renderFakeItem(icon, this.getX() + 5, this.getY() + 5);
//         guiGraphics.renderItemDecorations(Minecraft.getInstance().font, icon, this.getX() + 5, this.getY() + 5);
//     }

//     @Override
//     public void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY)
//     {
//         // 확인용 주석
//         System.out.println("renderTooltips called - isMouseOver: " + this.isMouseOver(mouseX, mouseY) + ", powerup: " + (this.powerup != null ? this.powerup.getPowerupInstance().getName().getString() : "null"));
        
//         // 레벨 조건과 상관없이 마우스 오버 시 항상 툴팁 표시
//         if (this.isMouseOver(mouseX, mouseY))
//         {
//             Component title = this.powerup != null ? this.powerup.getPowerupInstance().getName() : state.getJob().getJobInstance().getName();
//             Component description = this.powerup != null ? this.powerup.getPowerupInstance().getDescription() : state.getJob().getJobInstance().getDescription();
//             int titleWidth = Math.max(50, Minecraft.getInstance().font.width(title));
//             MultiLineTextComponent descriptionComponent = new MultiLineTextComponent(0, 0, titleWidth + getWidth() + 10, description, 0xFF1E1410);

//             if (getX() + getWidth() + titleWidth + 18 > guiGraphics.guiWidth())
//             {
//                 guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/text_background"), this.getX() - titleWidth - 18, this.getY() + 7, this.getWidth() + titleWidth + 24, 20 + descriptionComponent.getHeight() + 6 + (this.powerup != null && this.powerup.getPowerupInstance().getRequiredLevel() > 0 ? 25 : 13));
//                 guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/bar"), this.getX() - titleWidth - 18, this.getY() + 3, this.getWidth() + titleWidth + 24, 20);
//                 guiGraphics.drawString(Minecraft.getInstance().font, title, this.getX() - titleWidth - 6, this.getY() + 9, 0xFF1E1410, false);
//                 if (this.powerup != null)
//                 {
//                     guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/line"), this.getX() - titleWidth - 12, this.getY() + 29 + descriptionComponent.getHeight() + 1, 30, 1);
//                     int requiredLevel = this.powerup.getPowerupInstance().getRequiredLevel();
//                     if (requiredLevel > 0)
//                     {
//                         guiGraphics.drawString(Minecraft.getInstance().font, JobsPlus.translatable("gui.powerups.required_level", requiredLevel), this.getX() - titleWidth - 11, this.getY() + 29 + descriptionComponent.getHeight() + 4, 0xFF1E1410, false);
//                     }
//                     MutableComponent price = JobsPlus.translatable("gui.powerups.price", this.powerup.getPowerupInstance().getPrice());
//                     guiGraphics.drawString(Minecraft.getInstance().font, price, this.getX() - titleWidth - 11, this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4), 0xFF1E1410, false);
//                     guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("jobs/coins"), this.getX() - titleWidth - 11 + Minecraft.getInstance().font.width(price) + 2, this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4), 7, 8);
//                 }
//                 descriptionComponent.setX(this.getX() - titleWidth - 11);
//                 descriptionComponent.setY(this.getY() + 29);
//                 descriptionComponent.renderBase(guiGraphics, mouseX, mouseY, 0, 0, 0);
//             } 
//             else
//             {
//                 guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/text_background"), this.getX() - 6, this.getY() + 7, this.getWidth() + titleWidth + 24, 20 + descriptionComponent.getHeight() + 6 + (this.powerup != null && this.powerup.getPowerupInstance().getRequiredLevel() > 0 ? 25 : 13));
//                 guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/bar"), this.getX() - 6, this.getY() + 3, this.getWidth() + titleWidth + 24, 20);
//                 guiGraphics.drawString(Minecraft.getInstance().font, title, this.getX() + this.getWidth() + 8, this.getY() + 9, 0xFF1E1410, false);
//                 if (this.powerup != null)
//                 {
//                     guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("powerups/line"), this.getX(), this.getY() + 29 + descriptionComponent.getHeight() + 1, 30, 1);
//                     int requiredLevel = this.powerup.getPowerupInstance().getRequiredLevel();
//                     if (requiredLevel > 0)
//                     {
//                         guiGraphics.drawString(Minecraft.getInstance().font, JobsPlus.translatable("gui.powerups.required_level", requiredLevel), this.getX() + 1, this.getY() + 29 + descriptionComponent.getHeight() + 4, 0xFF1E1410, false);
//                     }
//                     MutableComponent price = JobsPlus.translatable("gui.powerups.price", this.powerup.getPowerupInstance().getPrice());
//                     guiGraphics.drawString(Minecraft.getInstance().font, price, this.getX() + 1, this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4), 0xFF1E1410, false);
//                     guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("jobs/coins"), this.getX() + 1 + Minecraft.getInstance().font.width(price) + 2, this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4), 7, 8);
//                 }
//                 descriptionComponent.setX(this.getX() + 1);
//                 descriptionComponent.setY(this.getY() + 29);
//                 descriptionComponent.renderBase(guiGraphics, mouseX, mouseY, 0, 0, 0);
//             }
//             this.blitSlot(guiGraphics);
//         }
//     }

//     @Override
//     public ISkillTreeItem getSkillTreeItem()
//     {
//         return this.skillTreeItem;
//     }

//     /**
//      * 실제 클릭 허용 조건(레벨, 코인, 파워업 상태)
//      */
//     private boolean canClickPowerup()
//     {
//         if (hasPowerup())
//         {
//             // 이미 보유한 파워업(ON/OFF 토글)은 항상 클릭 허용
//             return true;
//         }

//         boolean isCorrectPowerupState = this.powerup != null && this.powerup.getState() != PowerupState.LOCKED;
//         return isCorrectPowerupState && hasEnoughCoins() && hasRequiredLevel();
//     }

//     @Override
//     protected boolean isValidClickButton(MouseButtonInfo mouseButtonInfo)
//     {
//         // 좌클릭이면 항상 유효. 실제 조건은 canClickPowerup()에서 체크
//         return mouseButtonInfo.button() == 0;
//     }

//     @Override
//     public boolean isActive()
//     {
//         // 툴팁을 항상 보이게 하기 위해, 위젯 자체는 항상 active 취급
//         return true;
//     }

//     private boolean hasEnoughCoins()
//     {
//         return this.powerup != null && this.state.getCoins() >= this.powerup.getPowerupInstance().getPrice();
//     }

//     private boolean hasRequiredLevel()
//     {
//         return this.powerup != null && this.state.getJob().getLevel() >= 1 && this.state.getJob().getLevel() >= this.powerup.getPowerupInstance().getRequiredLevel();
//     }

//     private boolean hasPowerup()
//     {
//         return this.powerup != null && (this.powerup.getState() == PowerupState.ACTIVE || this.powerup.getState() == PowerupState.INACTIVE);
//     }
// }
