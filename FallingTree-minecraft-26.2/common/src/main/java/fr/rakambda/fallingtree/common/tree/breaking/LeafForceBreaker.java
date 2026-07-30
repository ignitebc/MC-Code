package fr.rakambda.fallingtree.common.tree.breaking;

import fr.rakambda.fallingtree.common.FallingTreeCommon;
import fr.rakambda.fallingtree.common.tree.Tree;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;

@Log4j2
@RequiredArgsConstructor
public class LeafForceBreaker{
	private final FallingTreeCommon<?> mod;
	
	public void forceBreakDecayLeaves(@NonNull IPlayer player, @NonNull Tree tree, @NonNull ILevel level){
		var radius = mod.getConfiguration().getTrees().getLeavesBreakingForceRadius();
		if(radius > 0){
			tree.getTopMostLog().ifPresent(topLog -> {
				var start = topLog.offset(-radius, -radius, -radius);
				var end = topLog.offset(radius, radius, radius);
				topLog.betweenClosedStream(start, end).forEach(checkPos -> {
					var checkState = level.getBlockState(checkPos);
					var checkBlock = checkState.getBlock();
					if(mod.isLeafBlock(checkBlock)){
						// 청크 보호 등 다른 모드가 파괴 전 이벤트에서 취소할 수 있게 원목과 같은 검사를 거친다
						if(!mod.checkCanBreakBlock(level, checkPos, checkState, player)){
							return;
						}
						if(!player.isCreative() || mod.getConfiguration().isLootInCreative()){
							checkState.dropResources(level, mod.getConfiguration().getTrees().isSpawnItemsAtBreakPoint() ? tree.getHitPos() : checkPos);
						}
						level.removeBlock(checkPos, false);
					}
				});
			});
		}
	}
}
