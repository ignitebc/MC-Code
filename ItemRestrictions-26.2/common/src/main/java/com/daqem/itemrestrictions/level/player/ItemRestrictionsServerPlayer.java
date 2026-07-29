package com.daqem.itemrestrictions.level.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.itemrestrictions.data.RestrictionResult;

public interface ItemRestrictionsServerPlayer {

    RestrictionResult itemrestrictions$isRestricted(ActionData actionData);
}
