package fuzs.illagerinvasion.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

import java.util.function.Supplier;

public class ServerConfig implements ConfigCore {

    @Config(
            category = "general", description = {
            "Will platinum armor trims provide special effects to the player depending on the piece of armor they are applied to.",
            "Helmet: More experience from killing mobs.",
            "Chestplate: Faster healing when at low health.",
            "Leggings: Better movement speed.",
            "Boots: Increased jump height."
    })
    public boolean platinumArmorTrims = true;

    @Config(
            category = "general", description = {
            "Will a full set of Invoker armor grant a special effect to the player.",
            "This effect will only be applied when the full set is worn."
    })
    public boolean invokerArmorSetBonus = true;

    @Config(
            category = "general", description = {
            "Will a full set of Alchemist armor grant a special effect to the player.",
            "This effect will only be applied when the full set is worn."
    })
    public boolean alchemistArmorSetBonus = true;

    @Config(
            category = "general", description = {
            "Will a full set of Necromancer armor grant a special effect to the player.",
            "This effect will only be applied when the full set is worn."
    })
    public boolean necromancerArmorSetBonus = true;

    @Config(
            category = "general", description = {
            "Will a full set of Sorcerer armor grant a special effect to the player.",
            "This effect will only be applied when the full set is worn."
    })
    public boolean sorcererArmorSetBonus = true;

    @Config(
            category = "raids", description = {
            "Should custom illagers participate in raids."
    })
    public boolean customIllagersInRaids = true;

    @Config(
            category = "raids", description = {
            "Show a boss bar for the Invoker (boss-tier).",
            "If disabled, the Invoker will not add/remove players to its boss bar when seen."
    })
    public boolean invokerBossBar = true;

    @Config(
            category = "raids", description = {
            "Raid wave configuration for custom illagers."
    })
    public final IllagerConfig basher = new IllagerConfig(RaidWavesConfigHelper.BASHER_RAID_WAVES,
            RaidWavesConfigHelper::getBasherRaidWaves);
    public final IllagerConfig provoker = new IllagerConfig(RaidWavesConfigHelper.PROVOKER_RAID_WAVES,
            RaidWavesConfigHelper::getProvokerRaidWaves);
    public final IllagerConfig necromancer = new IllagerConfig(RaidWavesConfigHelper.NECROMANCER_RAID_WAVES,
            RaidWavesConfigHelper::getNecromancerRaidWaves);
    public final IllagerConfig sorcerer = new IllagerConfig(RaidWavesConfigHelper.SORCERER_RAID_WAVES,
            RaidWavesConfigHelper::getSorcererRaidWaves);
    public final IllagerConfig firecaller = new IllagerConfig(RaidWavesConfigHelper.FIRECALLER_RAID_WAVES,
            RaidWavesConfigHelper::getFirecallerRaidWaves);
    public final IllagerConfig illusioner = new IllagerConfig(RaidWavesConfigHelper.ILLUSIONER_RAID_WAVES,
            RaidWavesConfigHelper::getIllusionerRaidWaves);
    public final IllagerConfig archivist = new IllagerConfig(RaidWavesConfigHelper.ARCHIVIST_RAID_WAVES,
            RaidWavesConfigHelper::getArchivistRaidWaves);
    public final IllagerConfig marauder = new IllagerConfig(RaidWavesConfigHelper.MARAUDER_RAID_WAVES,
            RaidWavesConfigHelper::getMarauderRaidWaves);
    public final IllagerConfig inquisitor = new IllagerConfig(RaidWavesConfigHelper.INQUISITOR_RAID_WAVES,
            RaidWavesConfigHelper::getInquisitorRaidWaves);
    public final IllagerConfig alchemist = new IllagerConfig(RaidWavesConfigHelper.ALCHEMIST_RAID_WAVES,
            RaidWavesConfigHelper::getAlchemistRaidWaves);
    public final IllagerConfig invoker = new IllagerConfig(RaidWavesConfigHelper.INVOKER_RAID_WAVES,
            RaidWavesConfigHelper::getInvokerRaidWaves);

    public static class IllagerConfig implements ConfigCore {
        private final int[] raidWaves;
        private final Supplier<int[]> raidWavesSupplier;

        @Config(description = "Does this illager take part in village raids.")
        public boolean participateInRaids = true;

        public IllagerConfig(int[] raidWaves, Supplier<int[]> raidWavesSupplier) {
            this.raidWaves = raidWaves;
            this.raidWavesSupplier = raidWavesSupplier;
        }

        @Override
        public void afterConfigReload() {
            if (this.participateInRaids) {
                System.arraycopy(this.raidWavesSupplier.get(), 0, this.raidWaves, 0, this.raidWaves.length);
            } else {
                System.arraycopy(new int[]{0, 0, 0, 0, 0, 0, 0, 0}, 0, this.raidWaves, 0, this.raidWaves.length);
            }
        }
    }
}
