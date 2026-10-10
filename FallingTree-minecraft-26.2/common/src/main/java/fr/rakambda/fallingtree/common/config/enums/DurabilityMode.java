package fr.rakambda.fallingtree.common.config.enums;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DurabilityMode{
	// 도구를 절대 부수지 않는다
	ABORT(true, (breakCount, breakableCount) -> breakCount <= breakableCount ? -1 : breakCount, (durability) -> durability <= 1),
	// 도구 내구도를 1 남긴다
	SAVE(true, (breakCount, breakableCount) -> breakCount <= breakableCount ? breakCount - 1 : breakCount, (durability) -> durability <= 1),
	// 가능한 만큼 블록을 부순다
	NORMAL(true, (breakCount, breakableCount) -> breakCount, (durability) -> false),
	// 내구도가 더 필요해도 모든 블록을 부순다
	BYPASS(false, (breakCount, breakableCount) -> breakableCount, (durability) -> false);
	
	private final boolean allowAbort;
	private final BiFunction<Integer, Integer, Integer> postProcessor;
	private final Predicate<Integer> shouldCancel;
	
	public int postProcess(int breakCount, int breakableCount){
		return postProcessor.apply(breakCount, breakableCount);
	}
	
	public boolean shouldPreserve(int durability){
		return shouldCancel.test(durability);
	}
}
