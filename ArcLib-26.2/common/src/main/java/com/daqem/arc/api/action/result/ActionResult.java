package com.daqem.arc.api.action.result;

public class ActionResult
{

    private boolean cancelAction = false;
    private float destroySpeedModifier = 1.0F;
    private float attackSpeedModifier = 1.0F;
    private float damageModifier = 1.0F;

    public ActionResult merge(ActionResult other)
    {
        this.cancelAction = this.cancelAction || other.cancelAction;
        this.destroySpeedModifier = combineAdditively(this.destroySpeedModifier, other.destroySpeedModifier);
        this.attackSpeedModifier = combineAdditively(this.attackSpeedModifier, other.attackSpeedModifier);
        this.damageModifier = combineAdditively(this.damageModifier, other.damageModifier);
        return this;
    }

    /**
     * 스킬 배율 간 중첩은 합연산으로 통일한다. 배율에서 기준값 1을 뺀 증가분끼리 더하므로
     * 1.2와 1.3을 합치면 1.5가 되고, 한쪽이 기본값 1.0이면 다른 쪽 값이 그대로 유지된다.
     */
    private static float combineAdditively(float firstModifier, float secondModifier)
    {
        return firstModifier + secondModifier - 1.0F;
    }

    public boolean shouldCancelAction()
    {
        return cancelAction;
    }

    public ActionResult withCancelAction(boolean cancelAction)
    {
        this.cancelAction = cancelAction;
        return this;
    }

    public float getDestroySpeedModifier()
    {
        return destroySpeedModifier;
    }

    public float getAttackSpeedModifier()
    {
        return attackSpeedModifier;
    }

    public float getDamageModifier()
    {
        return damageModifier;
    }

    public ActionResult withDestroySpeedModifier(float destroySpeedModifier)
    {
        this.destroySpeedModifier = destroySpeedModifier;
        return this;
    }

    public ActionResult withAttackSpeedModifier(float attackSpeedModifier)
    {
        this.attackSpeedModifier = attackSpeedModifier;
        return this;
    }

    public ActionResult withDamageModifier(float damageModifier)
    {
        this.damageModifier = damageModifier;
        return this;
    }
}
