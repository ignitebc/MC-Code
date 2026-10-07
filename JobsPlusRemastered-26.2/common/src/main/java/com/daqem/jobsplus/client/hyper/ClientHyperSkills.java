package com.daqem.jobsplus.client.hyper;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.accessor.HyperPlayerAccess;
import com.daqem.jobsplus.networking.c2s.ServerboundHyperLeapPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundHyperLeapPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundHyperStatusPacket;
import com.daqem.jobsplus.player.job.hyper.HyperLeapMovement;
import com.daqem.jobsplus.player.job.hyper.HyperSkillRules;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class ClientHyperSkills
{
    private static final float SHIELD_HUD_SCALE = 0.3F;
    private static final int SHIELD_PANEL_WIDTH = 160;
    private static final int PANEL_HEIGHT = 27;
    private static final int HUD_MARGIN = 4;
    private static LocalPlayer trackedPlayer;
    private static int smithLevel;
    private static int leapLevel;
    private static int shieldTicks;
    private static int shieldCooldown;
    private static int leapCooldown;
    private static boolean wasLeapDown;
    private static boolean charging;
    private static int chargeTicks;
    private static int sequence;

    private ClientHyperSkills() {}

    private static void track(LocalPlayer player)
    {
        if (trackedPlayer == player) return;
        trackedPlayer = player;
        smithLevel = 0;
        leapLevel = 0;
        shieldTicks = 0;
        shieldCooldown = 0;
        leapCooldown = 0;
        wasLeapDown = false;
        charging = false;
        chargeTicks = 0;
    }

    public static void receiveStatus(ClientboundHyperStatusPacket packet, NetworkManager.PacketContext context)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            LocalPlayer player = minecraft.player;
            track(player);
            if (player == null) return;
            smithLevel = packet.smithLevel();
            leapLevel = packet.leapLevel();
            shieldTicks = packet.shieldTicks();
            shieldCooldown = packet.shieldCooldown();
            leapCooldown = packet.leapCooldown();
            if (!packet.leaping())
            {
                var state = ((HyperPlayerAccess) player).jobsplus$getHyperState();
                state.leapProtected = false;
                state.leapMotionTicks = 0;
            }
        });
    }

    public static void receiveLeap(ClientboundHyperLeapPacket packet, NetworkManager.PacketContext context)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            LocalPlayer player = minecraft.player;
            track(player);
            if (player == null || !player.isAlive()) return;
            charging = false;
            HyperLeapMovement.start(player, new Vec3(packet.directionX(), 0, packet.directionZ()), packet.distance());
        });
    }

    /** 웅크리기와 점프를 함께 누를 때만 충전하며, 일반 점프 입력은 그대로 둔다. */
    public static void updateInput(ClientInput input)
    {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        track(player);
        if (player == null || player.input != input) return;
        if (shieldTicks > 0) shieldTicks--;
        if (shieldCooldown > 0) shieldCooldown--;
        if (leapCooldown > 0) leapCooldown--;
        boolean down = input.keyPresses.jump();
        boolean leapDown = down && input.keyPresses.shift();
        var state = ((HyperPlayerAccess) player).jobsplus$getHyperState();
        boolean eligible = leapLevel > 0 && player.isAlive() && !player.isSpectator() && !player.isCreative()
                && minecraft.gui.screen() == null && player.onGround() && !player.isInWater()
                && !player.isInLava() && !player.isPassenger() && !player.isFallFlying() && !player.isSleeping();
        if (charging && (!leapDown || !eligible || leapCooldown > 0 || state.leapProtected))
        {
            send(ServerboundHyperLeapPacket.Action.CANCEL);
            charging = false;
            chargeTicks = 0;
        }
        if (eligible && leapCooldown == 0 && !state.leapProtected && leapDown && !wasLeapDown)
        {
            sequence++;
            charging = true;
            chargeTicks = 0;
            send(ServerboundHyperLeapPacket.Action.START);
        }
        else if (charging)
        {
            chargeTicks = Math.min(chargeTicks + 1, HyperSkillRules.LEAP_CHARGE_TICKS);
        }
        boolean jump = down && !charging && !state.leapProtected;
        Input original = input.keyPresses;
        input.keyPresses = new Input(original.forward(), original.backward(), original.left(), original.right(),
                jump, original.shift(), original.sprint());
        wasLeapDown = leapDown;
    }

    private static void send(ServerboundHyperLeapPacket.Action action)
    {
        NetworkManager.sendToServer(new ServerboundHyperLeapPacket(action, sequence));
    }

    public static boolean shouldSuppressJump(LocalPlayer player)
    {
        return player == trackedPlayer && (charging
                || ((HyperPlayerAccess) player).jobsplus$getHyperState().leapProtected);
    }

    /** 자동 점프 등이 키보드 처리 뒤에 다시 켠 점프 입력도 실제 이동 전에 제거한다. */
    public static void suppressJumpInput(LocalPlayer player)
    {
        if (!shouldSuppressJump(player)) return;
        Input original = player.input.keyPresses;
        if (original.jump())
        {
            player.input.keyPresses = new Input(original.forward(), original.backward(), original.left(), original.right(),
                    false, original.shift(), original.sprint());
        }
        player.setJumping(false);
    }

    public static void render(GuiGraphicsExtractor graphics)
    {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || player != trackedPlayer || !player.isAlive()
                || player.isSpectator()) return;
        int height = graphics.guiHeight();
        int width = graphics.guiWidth();
        if (smithLevel > 0 || shieldTicks > 0)
        {
            Component label;
            double progress;
            int color;
            if (shieldTicks > 0)
            {
                label = JobsPlus.translatable("hyper.hud.shield_active", seconds(shieldTicks));
                progress = shieldTicks / (double) HyperSkillRules.SHIELD_DURATION_TICKS;
                color = 0xFFA7F3FF;
                if (minecraft.options.getCameraType().isFirstPerson())
                {
                    // 중앙 시야를 가리지 않고 가장자리의 얇은 막으로 무적 상태를 알린다.
                    graphics.fillGradient(0, 0, width, 12, 0x3646D8EA, 0x0046D8EA);
                    graphics.fillGradient(0, height - 12, width, height, 0x0046D8EA, 0x3646D8EA);
                    graphics.fill(0, 0, 2, height, 0x4646D8EA);
                    graphics.fill(width - 2, 0, width, height, 0x4646D8EA);
                }
            }
            else if (shieldCooldown == 0)
            {
                label = JobsPlus.translatable("hyper.hud.shield_ready");
                progress = 1;
                color = 0xFF65DCD4;
            }
            else
            {
                label = JobsPlus.translatable("hyper.hud.shield_cooldown", seconds(shieldCooldown));
                progress = 1 - shieldCooldown / (double) HyperSkillRules.getShieldCooldownTicks(smithLevel);
                color = 0xFFB5BFCB;
            }
            graphics.pose().pushMatrix();
            graphics.pose().translate(HUD_MARGIN, height - HUD_MARGIN - PANEL_HEIGHT * SHIELD_HUD_SCALE);
            graphics.pose().scale(SHIELD_HUD_SCALE, SHIELD_HUD_SCALE);
            panel(graphics, 0, 0, SHIELD_PANEL_WIDTH, label, progress, color);
            graphics.pose().popMatrix();
        }
        if (charging)
        {
            double charge = chargeTicks / (double) HyperSkillRules.LEAP_CHARGE_TICKS;
            String chunks = String.format(Locale.ROOT, "%.1f", HyperSkillRules.getLeapDistance(leapLevel) / 16.0D);
            Component label = chargeTicks >= HyperSkillRules.LEAP_CHARGE_TICKS ? JobsPlus.translatable("hyper.hud.leap_waiting")
                    : JobsPlus.translatable("hyper.hud.leap_charge", chunks);
            panel(graphics, width / 2 - 90, height - 111, 180, label, charge, 0xFF71DFFF);
        }
    }

    private static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, Component label,
                              double progress, int color)
    {
        if (width < 20) return;
        graphics.fill(x, y, x + width, y + PANEL_HEIGHT, 0xB0101B25);
        graphics.fill(x, y, x + 2, y + PANEL_HEIGHT, color);
        graphics.text(Minecraft.getInstance().font, label, x + 7, y + 5, color, true);
        graphics.fill(x + 7, y + 19, x + width - 7, y + 22, 0xFF2C3C49);
        int filled = (int) Math.round((width - 14) * Math.clamp(progress, 0, 1));
        graphics.fill(x + 7, y + 19, x + 7 + filled, y + 22, color);
    }

    private static String seconds(int ticks)
    {
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0D);
    }
}
