package com.daqem.arc.api.action.result;

public class ActionResult {

    private boolean cancelAction = false;
    private float destroySpeedModifier = 1.0F;
    private float attackSpeedModifier = 1.0F;
    private float damageModifier = 1.0F;

    public ActionResult merge(ActionResult other) {
        this.cancelAction = this.cancelAction || other.cancelAction;
        this.destroySpeedModifier = this.destroySpeedModifier * other.destroySpeedModifier;
        this.attackSpeedModifier = this.attackSpeedModifier * other.attackSpeedModifier;
        this.damageModifier = this.damageModifier * other.damageModifier;
        return this;
    }

    public boolean shouldCancelAction() {
        return cancelAction;
    }

    public ActionResult withCancelAction(boolean cancelAction) {
        this.cancelAction = cancelAction;
        return this;
    }

    public float getDestroySpeedModifier() {
        return destroySpeedModifier;
    }

    public float getAttackSpeedModifier() {
        return attackSpeedModifier;
    }

    public float getDamageModifier() {
        return damageModifier;
    }

    public ActionResult withDestroySpeedModifier(float destroySpeedModifier) {
        this.destroySpeedModifier = destroySpeedModifier;
        return this;
    }

    public ActionResult withAttackSpeedModifier(float attackSpeedModifier) {
        this.attackSpeedModifier = attackSpeedModifier;
        return this;
    }

    public ActionResult withDamageModifier(float damageModifier) {
        this.damageModifier = damageModifier;
        return this;
    }
}