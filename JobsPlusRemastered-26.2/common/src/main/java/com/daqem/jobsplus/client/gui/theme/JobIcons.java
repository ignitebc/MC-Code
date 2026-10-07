package com.daqem.jobsplus.client.gui.theme;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 직업과 스킬의 표시만 교체하며 데이터팩의 아이템 정의는 유지한다. */
public final class JobIcons {
    private static final Map<String, Identifier> JOB_ICONS = textures("jobs", Set.of(
            "hunter", "miner", "digger", "farmer", "fisherman", "smith", "alchemist", "adventurer"));
    private static final Map<String, Identifier> SKILL_ICONS = textures("skills", Set.of(
            "job_exp", "less_fall_damage", "run_speed", "safe_fall", "water_breath", "water_speed",
            "alchemist_special_find", "drop_multiplier", "harmful_potion_immunity", "longer_potions",
            "stronger_potions", "gold_block_digger", "gold_ingot_digger", "gold_nugget_digger",
            "shovel_efficiency", "shovel_range", "auto_replant", "bone_meal_saver", "double_drops",
            "livestock_bounty", "range_harvest", "casting_speed", "fish_school", "full_catch",
            "get_special_item", "grappling_hook", "attack_speed", "exp_boost_mob", "fire_arrows",
            "multiple_arrows", "exp_boost_block", "mining_special_find", "ore_refining",
            "pickaxe_efficiency", "pickaxe_range", "lapis_refund", "less_damage_melee", "null_hit",
            "steel_constitution", "sword_range"));
    private static final List<String> TIERS = List.of("i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x");

    private JobIcons() {
    }

    private static Map<String, Identifier> textures(String folder, Set<String> names) {
        return names.stream().collect(Collectors.toUnmodifiableMap(name -> name,
                name -> JobsPlus.getId("textures/gui/" + folder + "/" + name + ".png")));
    }

    public static void job(GuiGraphicsExtractor graphics, JobInstance job, int x, int y, int size) {
        Identifier location = job.getLocation();
        Identifier texture = null;
        if (location.getNamespace().equals("jobsplus")) {
            texture = JOB_ICONS.get(location.getPath());
        }
        if (texture != null) {
            texture(graphics, texture, x, y, size);
        } else {
            item(graphics, job.getIconItem(), x, y, size, null);
        }
    }

    public static void skill(GuiGraphicsExtractor graphics, PowerupInstance powerup, int x, int y, int size) {
        Identifier location = powerup.getLocation();
        String path = location.getPath();
        int separator = path.lastIndexOf('_');
        int tier = 0;
        Identifier texture = null;
        if (location.getNamespace().equals("jobsplus") && separator > path.lastIndexOf('/')) {
            tier = TIERS.indexOf(path.substring(separator + 1)) + 1;
            if (tier > 0) {
                String family = path.substring(path.lastIndexOf('/') + 1, separator);
                texture = SKILL_ICONS.get(family);
            }
        }
        if (texture == null) {
            String count = null;
            if (powerup.getIconCount() > 1) {
                count = Integer.toString(powerup.getIconCount());
            }
            item(graphics, powerup.getIcon(), x, y, size, count);
            return;
        }
        texture(graphics, texture, x, y, size);
        // 같은 효과의 I~X는 이미지를 공유하고 단계는 선명한 숫자로 구분한다.
        String label = Integer.toString(tier);
        float scale = 0.65f;
        int labelWidth = (int) Math.ceil(Minecraft.getInstance().font.width(label) * scale);
        int labelX = x + size - labelWidth;
        int labelY = y + size - 6;
        graphics.fill(labelX - 1, labelY - 1, x + size + 1, y + size + 1, JobsTheme.BACKGROUND);
        graphics.pose().pushMatrix();
        graphics.pose().translate(labelX, labelY);
        graphics.pose().scale(scale, scale);
        graphics.text(Minecraft.getInstance().font, label, 0, 0, JobsTheme.TEXT, true);
        graphics.pose().popMatrix();
    }

    private static void texture(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int size) {
        // 정규화된 전체 UV를 사용해 생성 이미지의 실제 픽셀 크기에 의존하지 않는다.
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y,
                0.0F, 0.0F, size, size, 1, 1, 1, 1);
    }

    private static void item(GuiGraphicsExtractor graphics, ItemStack item, int x, int y, int size, String count) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(size / 16.0f, size / 16.0f);
        graphics.fakeItem(item, 0, 0);
        graphics.itemDecorations(Minecraft.getInstance().font, item, 0, 0, count);
        graphics.pose().popMatrix();
    }
}
