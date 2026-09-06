package com.daqem.jobsplus.client.gui.powerups.skilltree;

import com.daqem.uilib.api.skilltree.ISkillTreeItem;
import com.daqem.uilib.skilltree.AbstractSkillTree;

import java.util.List;

public class PowerupsSkillTree extends AbstractSkillTree
{

    public PowerupsSkillTree(List<ISkillTreeItem> items)
    {
        super(items);
        setSkillTreeItemWidth(24);
        setSkillTreeItemHeight(24);
        setHorizontalSpacing(8);
        setVerticalSpacing(8);
    }
}
