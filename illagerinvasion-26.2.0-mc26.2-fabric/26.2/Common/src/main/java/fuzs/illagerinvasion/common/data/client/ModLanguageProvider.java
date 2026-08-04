package fuzs.illagerinvasion.common.data.client;

import fuzs.illagerinvasion.common.init.ModEntityTypes;
import fuzs.illagerinvasion.common.init.ModItems;
import fuzs.illagerinvasion.common.init.ModRegistry;
import fuzs.illagerinvasion.common.init.ModSoundEvents;
import fuzs.illagerinvasion.common.world.inventory.ImbuingMenu;
import fuzs.illagerinvasion.common.world.level.block.ImbuingTableBlock;
import fuzs.puzzleslib.common.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.common.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.common.api.init.v3.registry.ResourceKeyHelper;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.addCreativeModeTab(ModRegistry.CREATIVE_MODE_TAB, "일리저 침공");
        builder.add(ImbuingTableBlock.CONTAINER_IMBUE, "장비 강화");
        builder.add("container.imbue.enhance", "강화하기");
        builder.add("container.imbue.confirm.title", "장비 강화");
        builder.add("container.imbue.confirm.message", "정말 강화하시겠습니까?");
        builder.add("container.imbue.confirm.enhance", "강화");
        builder.add("container.imbue.confirm.no", "아니오");
        builder.add("container.imbue.broadcast.success", "%s님의 '%s' +%s 강화되었습니다.");
        builder.add("container.imbue.broadcast.failure", "%s님의 '%s' 강화에 실패하여 +%s강이 되었습니다.");
        builder.add("container.imbue.broadcast.destroyed", "%s님의 '%s' 강화에 실패하여 파괴되었습니다!!!");
        builder.add("container.imbue.broadcast.protected",
                "%s님의 '%s' 강화 실패로 파괴될 뻔했지만 강화 파괴 방지권이 장비를 보호했습니다. (+%s강)");
        builder.add("container.imbue.result.title", "강화 결과");
        builder.add("container.imbue.result.success", "강화를 성공하여 +%s강이 되었습니다.");
        builder.add("container.imbue.result.failure", "강화가 실패하여 +%s강이 되었습니다.");
        builder.add("container.imbue.result.destroyed", "강화가 실패하여 아이템이 파괴되었습니다.");
        builder.add("container.imbue.result.confirm", "확인");
        builder.add("container.imbue.successChance", "성공률 : %s%%");
        builder.add("container.imbue.destroyChance", "파괴확률 : %s%%");
        builder.add("container.imbue.slot.equipment", "장비");
        builder.add("container.imbue.slot.enhancementGem", "원석");
        builder.add("container.imbue.slot.successScroll", "확률권");
        builder.add("container.imbue.slot.protectionScroll", "방지권");
        builder.add(ImbuingMenu.EnhanceState.EQUIPMENT_MISSING.getComponent(), "강화할 장비를 넣어 주세요.");
        builder.add(ImbuingMenu.EnhanceState.EQUIPMENT_NOT_SUPPORTED.getComponent(),
                "서리빛 네더라이트 장비만 강화할 수 있습니다.");
        builder.add(ImbuingMenu.EnhanceState.ENHANCEMENT_GEM_MISSING.getComponent(), "강화 원석이 필요합니다.");
        builder.add(ImbuingMenu.EnhanceState.MAX_LEVEL_REACHED.getComponent(), "이미 최대 강화 단계입니다.");
        builder.add(ModItems.LOST_CANDLE_ITEM.value().getDescriptionId() + ".foundNearby", "근처에서 %s 발견");
        builder.add(ResourceKeyHelper.getTranslationKey(ModRegistry.REVEAL_INSTRUMENT), "탐지");
        builder.add(ResourceKeyHelper.getTranslationKey(ModRegistry.PLATINUM_TRIM_MATERIAL), "백금 재료");
        builder.add(ModItems.UNUSUAL_DUST_ITEM.value(), "이상한 가루");
        builder.add(ModItems.MAGICAL_FIRE_CHARGE_ITEM.value(), "마법 화염구");
        builder.add(ModItems.ILLUSIONARY_DUST_ITEM.value(), "환영의 가루");
        builder.add(ModItems.LOST_CANDLE_ITEM.value(), "잃어버린 촛불");
        builder.add(ModItems.HORN_OF_SIGHT_ITEM.value(), "시야의 뿔피리");
        builder.add(ModItems.HALLOWED_GEM_ITEM.value(), "성스러운 보석");
        builder.add(ModItems.PLATINUM_INFUSED_HATCHET_ITEM.value(), "백금 주입 손도끼");
        builder.add(ModItems.PLATINUM_CHUNK_ITEM.value(), "백금 덩어리");
        builder.add(ModItems.PLATINUM_SHEET_ITEM.value(), "백금 판");
        builder.add(ModItems.PRIMAL_ESSENCE_ITEM.value(), "원초의 정수");
        builder.addPotion(ModRegistry.BERSERKING_POTION, "광폭화");
        builder.add(ModItems.PROVOKER_SPAWN_EGG_ITEM.value(), "도발자 소환 알");
        builder.add(ModItems.SURRENDERED_SPAWN_EGG_ITEM.value(), "항복한 자 소환 알");
        builder.add(ModItems.ILLUSIONER_SPAWN_EGG_ITEM.value(), "환영술사 소환 알");
        builder.add(ModItems.NECROMANCER_SPAWN_EGG_ITEM.value(), "강령술사 소환 알");
        builder.add(ModItems.BASHER_SPAWN_EGG_ITEM.value(), "파쇄병 소환 알");
        builder.add(ModItems.SORCERER_SPAWN_EGG_ITEM.value(), "주술사 소환 알");
        builder.add(ModItems.ARCHIVIST_SPAWN_EGG_ITEM.value(), "기록관 소환 알");
        builder.add(ModItems.INQUISITOR_SPAWN_EGG_ITEM.value(), "심문관 소환 알");
        builder.add(ModItems.MARAUDER_SPAWN_EGG_ITEM.value(), "약탈자 소환 알");
        builder.add(ModItems.INVOKER_SPAWN_EGG_ITEM.value(), "찬란한 기원자 소환 알");
        builder.add(ModItems.ALCHEMIST_SPAWN_EGG_ITEM.value(), "연금술사 소환 알");
        builder.add(ModItems.FIRECALLER_SPAWN_EGG_ITEM.value(), "화염술사 소환 알");
        builder.add(ModRegistry.IMBUING_TABLE_BLOCK.value(), "강화 제작대");
        builder.add(ModRegistry.MAGIC_FIRE_BLOCK.value(), "마법 화염");
        builder.add(ModEntityTypes.PROVOKER_ENTITY_TYPE.value(), "도발자");
        builder.add(ModEntityTypes.INVOKER_ENTITY_TYPE.value(), "찬란한 기원자");
        builder.add(ModEntityTypes.SURRENDERED_ENTITY_TYPE.value(), "항복한 자");
        builder.add(ModEntityTypes.NECROMANCER_ENTITY_TYPE.value(), "강령술사");
        builder.add(ModEntityTypes.BASHER_ENTITY_TYPE.value(), "파쇄병");
        builder.add(ModEntityTypes.SORCERER_ENTITY_TYPE.value(), "주술사");
        builder.add(ModEntityTypes.ARCHIVIST_ENTITY_TYPE.value(), "기록관");
        builder.add(ModEntityTypes.INQUISITOR_ENTITY_TYPE.value(), "심문관");
        builder.add(ModEntityTypes.MARAUDER_ENTITY_TYPE.value(), "약탈자");
        builder.add(ModEntityTypes.ALCHEMIST_ENTITY_TYPE.value(), "연금술사");
        builder.add(ModEntityTypes.FIRECALLER_ENTITY_TYPE.value(), "화염술사");
        builder.add(ModEntityTypes.SKULL_BOLT_ENTITY_TYPE.value(), "해골 탄환");
        builder.add(ModEntityTypes.HATCHET_ENTITY_TYPE.value(), "손도끼");
        builder.add(ModEntityTypes.INVOKER_FANGS_ENTITY_TYPE.value(), "기원자의 송곳니");
        builder.add(ModEntityTypes.FLYING_MAGMA_ENTITY_TYPE.value(), "비행 마그마");
        builder.add(ModSoundEvents.HORN_OF_SIGHT_SOUND_EVENT.value(), "시야의 뿔피리: 울림");
        builder.add(ModSoundEvents.LOST_CANDLE_FIND_ORE_SOUND_EVENT.value(), "광물의 울림");
        builder.add(ModSoundEvents.SURRENDERED_AMBIENT_SOUND_EVENT.value(), "항복한 자: 쇠사슬 소리");
        builder.add(ModSoundEvents.SURRENDERED_HURT_SOUND_EVENT.value(), "항복한 자: 피해를 입음");
        builder.add(ModSoundEvents.SURRENDERED_CHARGE_SOUND_EVENT.value(), "항복한 자: 돌진");
        builder.add(ModSoundEvents.SURRENDERED_DEATH_SOUND_EVENT.value(), "항복한 자: 죽음");
        builder.add(ModSoundEvents.NECROMANCER_SUMMON_SOUND_EVENT.value(), "강령술사: 소환");
        builder.add(ModSoundEvents.ARCHIVIST_AMBIENT_SOUND_EVENT.value(), "기록관: 기록을 읊음");
        builder.add(ModSoundEvents.ARCHIVIST_HURT_SOUND_EVENT.value(), "기록관: 피해를 입음");
        builder.add(ModSoundEvents.ARCHIVIST_DEATH_SOUND_EVENT.value(), "기록관: 죽음");
        builder.add(ModSoundEvents.INVOKER_FANGS_SOUND_EVENT.value(), "기원자의 송곳니");
        builder.add(ModSoundEvents.INVOKER_HURT_SOUND_EVENT.value(), "찬란한 기원자: 피해를 입음");
        builder.add(ModSoundEvents.INVOKER_DEATH_SOUND_EVENT.value(), "찬란한 기원자: 죽음");
        builder.add(ModSoundEvents.INVOKER_AMBIENT_SOUND_EVENT.value(), "찬란한 기원자: 의식을 준비함");
        builder.add(ModSoundEvents.INVOKER_COMPLETE_CAST_SOUND_EVENT.value(), "찬란한 기원자: 의식 완료");
        builder.add(ModSoundEvents.INVOKER_TELEPORT_CAST_SOUND_EVENT.value(), "찬란한 기원자: 순간이동");
        builder.add(ModSoundEvents.INVOKER_FANGS_CAST_SOUND_EVENT.value(), "찬란한 기원자: 송곳니 소환");
        builder.add(ModSoundEvents.INVOKER_BIG_CAST_SOUND_EVENT.value(), "찬란한 기원자: 대의식 시전");
        builder.add(ModSoundEvents.INVOKER_SUMMON_CAST_SOUND_EVENT.value(), "찬란한 기원자: 소환 의식");
        builder.add(ModSoundEvents.INVOKER_SHIELD_BREAK_SOUND_EVENT.value(), "기원자의 보호막 파괴");
        builder.add(ModSoundEvents.INVOKER_SHIELD_CREATE_SOUND_EVENT.value(), "기원자의 보호막");
        builder.add(ModSoundEvents.ILLAGER_BRUTE_AMBIENT_SOUND_EVENT.value(), "일리저 야만용사: 으르렁거림");
        builder.add(ModSoundEvents.ILLAGER_BRUTE_HURT_SOUND_EVENT.value(), "일리저 야만용사: 피해를 입음");
        builder.add(ModSoundEvents.ILLAGER_BRUTE_DEATH_SOUND_EVENT.value(), "일리저 야만용사: 죽음");
        builder.add(ModSoundEvents.PROVOKER_AMBIENT_SOUND_EVENT.value(), "도발자: 중얼거림");
        builder.add(ModSoundEvents.PROVOKER_HURT_SOUND_EVENT.value(), "도발자: 피해를 입음");
        builder.add(ModSoundEvents.PROVOKER_DEATH_SOUND_EVENT.value(), "도발자: 죽음");
        builder.add(ModSoundEvents.PROVOKER_CELEBRATE_SOUND_EVENT.value(), "도발자: 환호");
        builder.add(ModSoundEvents.BASHER_AMBIENT_SOUND_EVENT.value(), "파쇄병: 으르렁거림");
        builder.add(ModSoundEvents.BASHER_HURT_SOUND_EVENT.value(), "파쇄병: 피해를 입음");
        builder.add(ModSoundEvents.BASHER_DEATH_SOUND_EVENT.value(), "파쇄병: 죽음");
        builder.add(ModSoundEvents.BASHER_CELEBRATE_SOUND_EVENT.value(), "파쇄병: 환호");
        builder.add(ModSoundEvents.FIRECALLER_AMBIENT_SOUND_EVENT.value(), "화염술사: 중얼거림");
        builder.add(ModSoundEvents.FIRECALLER_HURT_SOUND_EVENT.value(), "화염술사: 피해를 입음");
        builder.add(ModSoundEvents.FIRECALLER_DEATH_SOUND_EVENT.value(), "화염술사: 죽음");
        builder.add(ModSoundEvents.FIRECALLER_CAST_SOUND_EVENT.value(), "화염술사: 시전");
        builder.add(ModSoundEvents.SORCERER_CAST_SOUND_EVENT.value(), "주술사: 시전");
        builder.add(ModSoundEvents.SORCERER_COMPLETE_CAST_SOUND_EVENT.value(), "주술사: 시전 완료");
        builder.add(ModSoundEvents.SORCERER_HURT_SOUND_EVENT.value(), "주술사: 피해를 입음");
        builder.add(ModSoundEvents.SORCERER_DEATH_SOUND_EVENT.value(), "주술사: 죽음");
        builder.add(ModSoundEvents.SORCERER_AMBIENT_SOUND_EVENT.value(), "주술사: 주문을 읊음");
        builder.add(ModSoundEvents.SORCERER_CELEBRATE_SOUND_EVENT.value(), "주술사: 환호");
    }
}
