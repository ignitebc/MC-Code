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
    private static LocalPlayer trackedPlayer;
    private static int smithLevel;
    private static int leapLevel;
    private static int shieldTicks;
    private static int shieldCooldown;
    private static int leapCooldown;
    private static boolean wasJumpDown;
    private static boolean charging;
    private static int chargeTicks;
    private static int sequence;
    private static int waitingTicks;

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
        wasJumpDown = false;
        charging = false;
        chargeTicks = 0;
        waitingTicks = 0;
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
            waitingTicks = 0;
            HyperLeapMovement.start(player, new Vec3(packet.directionX(), 0, packet.directionZ()), packet.distance());
        });
    }

    /** 원래 입력을 읽은 뒤 점프만 보류한다. 짧게 누르면 키를 놓는 틱에 일반 점프한다. */
    public static void updateInput(ClientInput input)
    {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        track(player);
        if (player == null || player.input != input) return;
        if (shieldTicks > 0) shieldTicks--;
        if (shieldCooldown > 0) shieldCooldown--;
        if (leapCooldown > 0) leapCooldown--;
        if (waitingTicks > 0) waitingTicks--;
        boolean down = input.keyPresses.jump();
        var state = ((HyperPlayerAccess) player).jobsplus$getHyperState();
        boolean eligible = leapLevel > 0 && player.isAlive() && !player.isSpectator() && !player.isCreative()
                && minecraft.gui.screen() == null && player.onGround() && !player.isInWater()
                && !player.isInLava() && !player.isPassenger() && !player.isFallFlying();
        if (charging && (!eligible || state.leapProtected))
        {
            send(ServerboundHyperLeapPacket.Action.CANCEL);
            charging = false;
            chargeTicks = 0;
        }
        if (eligible && leapCooldown == 0 && waitingTicks == 0 && !state.leapProtected && down && !wasJumpDown)
        {
            sequence++;
            charging = true;
            chargeTicks = 0;
            send(ServerboundHyperLeapPacket.Action.START);
        }
        boolean jump = down;
        if (charging)
        {
            if (down)
            {
                chargeTicks = Math.min(chargeTicks + 1, HyperSkillRules.LEAP_CHARGE_TICKS);
                jump = false;
            }
            else
            {
                boolean normalJump = chargeTicks < HyperSkillRules.LEAP_MIN_CHARGE_TICKS;
                send(normalJump ? ServerboundHyperLeapPacket.Action.CANCEL : ServerboundHyperLeapPacket.Action.RELEASE);
                charging = false;
                jump = normalJump;
                if (!normalJump) waitingTicks = 20;
            }
        }
        if (state.leapProtected || waitingTicks > 0) jump = false;
        Input original = input.keyPresses;
        input.keyPresses = new Input(original.forward(), original.backward(), original.left(), original.right(),
                jump, original.shift(), original.sprint());
        wasJumpDown = down;
    }

    private static void send(ServerboundHyperLeapPacket.Action action)
    {
        NetworkManager.sendToServer(new ServerboundHyperLeapPacket(action, sequence));
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
            panel(graphics, 10, height - 69, Math.min(160, width / 2 - 16), label, progress, color);
        }
        if (charging || waitingTicks > 0)
        {
            double charge = chargeTicks / (double) HyperSkillRules.LEAP_CHARGE_TICKS;
            String chunks = String.format(Locale.ROOT, "%.1f", HyperSkillRules.getLeapDistance(leapLevel) * charge / 16.0D);
            Component label = waitingTicks > 0 ? JobsPlus.translatable("hyper.hud.leap_waiting")
                    : JobsPlus.translatable("hyper.hud.leap_charge", chunks);
            panel(graphics, width / 2 - 90, height - 111, 180, label, charge, 0xFF71DFFF);
        }
        else if (leapLevel > 0 && leapCooldown > 0)
        {
            Component label = JobsPlus.translatable("hyper.hud.leap_cooldown", seconds(leapCooldown));
            graphics.text(minecraft.font, label, width / 2 - minecraft.font.width(label) / 2, height - 91, 0xFFC3D7E0, true);
        }
    }

    private static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, Component label,
                              double progress, int color)
    {
        if (width < 20) return;
        graphics.fill(x, y, x + width, y + 27, 0xB0101B25);
        graphics.fill(x, y, x + 2, y + 27, color);
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
