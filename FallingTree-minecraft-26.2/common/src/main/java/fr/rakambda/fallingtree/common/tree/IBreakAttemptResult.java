package fr.rakambda.fallingtree.common.tree;

import fr.rakambda.fallingtree.common.wrapper.IBlockPos;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import fr.rakambda.fallingtree.common.wrapper.IPlayer;

/**
 * {@link TreeHandler#breakTree(ILevel, IPlayer, IBlockPos)}의 성공·실패 결과.
 * 실패는 보통 {@link AbortedResult}이고, 성공한 시도는
 * {@link BreakTreeResult}이다.
 */
public sealed interface IBreakAttemptResult permits SuccessResult, AbortedResult{
	boolean shouldCancel();
}
