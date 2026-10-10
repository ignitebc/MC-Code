package fr.rakambda.fallingtree.common.tree.breaking;

import java.util.concurrent.atomic.AtomicInteger;

public class LootHandler{
	private final int maxDropping;
	
	private AtomicInteger currentlyBroken = new AtomicInteger(0);
	
	public LootHandler(int wantToBreakCount, float trunkLootPercentage){
		maxDropping = (int) Math.ceil(wantToBreakCount * trunkLootPercentage);
	}
	
	/**
	 * @return 전리품을 떨어뜨려야 하면 true, 아니면 false
	 */
	public boolean breakNewTrunk(){
		return currentlyBroken.accumulateAndGet(1, Integer::sum) <= maxDropping;
	}
}
