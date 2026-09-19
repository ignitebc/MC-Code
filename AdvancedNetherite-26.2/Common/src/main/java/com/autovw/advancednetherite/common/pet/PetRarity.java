package com.autovw.advancednetherite.common.pet;

import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.core.ModEntityTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 펫 등급. 펫 상자 종류와 1:1로 대응하며, 어떤 펫이 어느 상자에서 나오는지와 공격력을 정한다.
 * 펫 도감은 이 순서(일반 → 희귀 → 전설)로 펫을 늘어놓는다.
 */
public enum PetRarity
{
    NORMAL("일반", 1.0, List.of(
            () -> ModEntityTypes.DIALGA_PET,
            () -> ModEntityTypes.KIRBY_PET,
            () -> ModEntityTypes.GOMI_PET)),
    RARE("희귀", 3.0, List.of(
            () -> ModEntityTypes.UNICORN_PET,
            () -> ModEntityTypes.GAZELLE_PET)),
    LEGEND("전설", 5.0, List.of(
            () -> ModEntityTypes.FAIRLINS_PET,
            () -> ModEntityTypes.DARK_DRAGON_PET,
            () -> ModEntityTypes.SCULKEN_RAVEN_PET,
            () -> ModEntityTypes.SUPER_GOMI_PET));

    private final String label;
    private final double attackDamage;
    private final List<Supplier<EntityType<DialgaPetEntity>>> petTypes;
    private List<String> petTypeIds;

    PetRarity(String label, double attackDamage, List<Supplier<EntityType<DialgaPetEntity>>> petTypes)
    {
        this.label = label;
        this.attackDamage = attackDamage;
        this.petTypes = petTypes;
    }

    /** 화면에 쓰는 등급 이름 */
    public String label()
    {
        return this.label;
    }

    /** 이 등급의 펫 상자에서 나온 펫의 공격력 */
    public double attackDamage()
    {
        return this.attackDamage;
    }

    /** 이 등급의 펫 상자에서 나올 수 있는 펫 종류. 상자 아이템이 그대로 쓴다. */
    public List<Supplier<EntityType<DialgaPetEntity>>> petTypes()
    {
        return this.petTypes;
    }

    /** 이 등급에 속한 펫 타입의 레지스트리 ID 목록. 도감 순서와 같다. */
    public synchronized List<String> petTypeIds()
    {
        if (this.petTypeIds == null)
        {
            List<String> ids = new ArrayList<>(this.petTypes.size());
            for (Supplier<EntityType<DialgaPetEntity>> petType : this.petTypes)
            {
                ids.add(BuiltInRegistries.ENTITY_TYPE.getKey(petType.get()).toString());
            }
            this.petTypeIds = List.copyOf(ids);
        }
        return this.petTypeIds;
    }

    /** 펫 타입 ID가 속한 등급. 알 수 없는 ID면 null. */
    public static PetRarity of(String petTypeId)
    {
        for (PetRarity rarity : values())
        {
            if (rarity.petTypeIds().contains(petTypeId))
            {
                return rarity;
            }
        }
        return null;
    }
}
