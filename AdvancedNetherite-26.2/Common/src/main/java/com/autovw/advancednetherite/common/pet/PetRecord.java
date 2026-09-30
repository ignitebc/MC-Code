package com.autovw.advancednetherite.common.pet;

import java.util.UUID;

/**
 * 플레이어가 소유한 펫 한 마리의 영속 기록.
 * 펫 엔티티는 주인이 접속 중일 때만 존재하며, 이 기록이 소유의 원본이다.
 * 공격력과 최대 체력은 기록하지 않고 등급과 레벨에서 그때그때 계산한다({@link PetStats}).
 * 현재 체력은 최대 체력에서 잃은 양으로 기록한다. 레벨이 오르거나 등급별 체력이 바뀌어도
 * 잃은 양은 그대로라서, 가득 찬 펫은 계속 가득 찬 상태로 남는다.
 *
 * @param id             기록 고유 ID. 살아 있는 펫 엔티티와 1:1로 연결된다.
 * @param petTypeId      펫 엔티티 타입의 레지스트리 ID (예: advancednetherite:dialga_pet)
 * @param enabled        ON/OFF 상태. OFF면 접속해도 소환되지 않는다.
 * @param name           플레이어가 붙인 이름. 비어 있으면 종류 이름과 번호로 부른다.
 * @param level          펫 레벨. 1부터 시작한다.
 * @param exp            현재 레벨에서 쌓은 경험치
 * @param missingHealth  잃은 체력. 0이면 가득 찬 상태다. 꺼 두거나 접속을 끊은 동안에는 회복하지 않는다.
 * @param reviveAtMillis 부활하는 시각(epoch 밀리초). 0이면 살아 있다. 이 시각 전에는 켤 수 없다.
 */
public record PetRecord(UUID id, String petTypeId, boolean enabled, String name, int level, int exp,
                        float missingHealth, long reviveAtMillis)
{
    public PetRecord
    {
        if (name == null) name = "";
        level = PetStats.clampLevel(level);
        exp = Math.max(0, exp);
        if (Float.isNaN(missingHealth) || missingHealth < 0.0F) missingHealth = 0.0F;
        reviveAtMillis = Math.max(0L, reviveAtMillis);
    }

    /** 갓 얻은 펫. 1레벨이고 바로 소환된다. */
    public PetRecord(UUID id, String petTypeId)
    {
        this(id, petTypeId, true, "", 1, 0, 0.0F, 0L);
    }

    public PetRecord withEnabled(boolean newEnabled)
    {
        return new PetRecord(this.id, this.petTypeId, newEnabled, this.name, this.level, this.exp,
                this.missingHealth, this.reviveAtMillis);
    }

    public PetRecord withName(String newName)
    {
        return new PetRecord(this.id, this.petTypeId, this.enabled, newName, this.level, this.exp,
                this.missingHealth, this.reviveAtMillis);
    }

    public PetRecord withProgress(int newLevel, int newExp)
    {
        return new PetRecord(this.id, this.petTypeId, this.enabled, this.name, newLevel, newExp,
                this.missingHealth, this.reviveAtMillis);
    }

    public PetRecord withReviveAt(long newReviveAtMillis)
    {
        return new PetRecord(this.id, this.petTypeId, this.enabled, this.name, this.level, this.exp,
                this.missingHealth, newReviveAtMillis);
    }

    public PetRecord withMissingHealth(float newMissingHealth)
    {
        return new PetRecord(this.id, this.petTypeId, this.enabled, this.name, this.level, this.exp,
                newMissingHealth, this.reviveAtMillis);
    }

    /** 이 레벨의 최대 체력 */
    public float maxHealth()
    {
        return (float) PetStats.maxHealth(rarity(), this.level);
    }

    /**
     * 다음에 소환할 때의 체력. 최대 체력에서 잃은 체력을 뺀 값이다.
     * 등급별 체력이 줄어 잃은 체력이 최대 체력 이상이 되어도 살아 있는 펫이므로 1은 남긴다.
     */
    public float storedHealth()
    {
        return Math.max(1.0F, maxHealth() - this.missingHealth);
    }

    /** 죽어서 부활을 기다리는 중인지 */
    public boolean isReviving(long nowMillis)
    {
        return this.reviveAtMillis > nowMillis;
    }

    /** 등급. 알 수 없는 종류면 일반으로 본다. */
    public PetRarity rarity()
    {
        PetRarity rarity = PetRarity.of(this.petTypeId);
        return rarity == null ? PetRarity.NORMAL : rarity;
    }
}
