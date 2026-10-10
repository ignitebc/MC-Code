package com.tacz.guns.api.modifier;

import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 부착물 속성 수정자
 *
 * @param <T> JSON을 읽은 뒤 처리한 데이터 타입
 * @param <K> 부착물 캐시 속성 값
 */
public interface IAttachmentModifier<T, K> {
    /**
     * 부착물 속성 수정자. JSON을 읽을 때 필드 이름으로도 쓴다
     *
     * @return JSON을 읽을 때의 필드 이름
     */
    String getId();

    /**
     * 선택 필드. 예전 버전 JSON 파일과 호환하려고 둔 메서드다
     *
     * @return 예전 버전의 부착물 속성 수정 JSON 필드 이름
     */
    default String getOptionalFields() {
        return StringUtils.EMPTY;
    }

    /**
     * JSON에서 데이터를 읽는다
     *
     * @param json 입력 JSON 문자열
     * @return 읽고 처리한 JSON 객체
     */
    JsonProperty<T> readJson(String json);

    /**
     * 캐시를 초기화한다. 총기의 기본 데이터를 채울 때 쓴다
     *
     * @param gunItem 현재 총기 아이템
     * @param gunData 총기 데이터
     * @return 초기화해 읽은 데이터
     */
    CacheValue<K> initCache(ItemStack gunItem, GunData gunData);

    /**
     * 계산. 각 부착물의 데이터와 총기 데이터를 함께 계산해 최종 값을 낸다
     *
     * @param modifiedValues 각 부착물의 데이터 값
     * @param cache          캐시한 총기 기본값
     */
    void eval(List<T> modifiedValues, CacheValue<K> cache);

    /**
     * 개조 화면의 속성 막대 관련 데이터를 얻는다
     */
    @Environment(EnvType.CLIENT)
    default List<DiagramsData> getPropertyDiagramsData(ItemStack gunItem, GunData gunData, AttachmentCacheProperty cacheProperty) {
        return Collections.emptyList();
    }

    /**
     * 개조 화면의 속성 막대 수를 얻는다. 버튼 위치를 옮기는 데 쓴다
     */
    @Environment(EnvType.CLIENT)
    default int getDiagramsDataSize() {
        return 0;
    }

    /**
     * 속성 막대 데이터
     *
     * @param defaultPercent   기본 총기 값 백분율
     * @param modifierPercent  보정 값 백분율
     * @param modifier         보정 값. 기본값과 비교해 판단하는 데 쓴다
     * @param titleKey         속성 이름 언어 파일 키
     * @param positivelyString 기본값보다 클 때 표시할 글자
     * @param negativeString   기본값보다 작을 때 표시할 글자
     * @param defaultString    기본값과 같을 때 표시할 글자
     * @param positivelyBetter true면 기본값보다 클 때 초록색, 아니면 빨간색으로 표시한다
     */
    @Environment(EnvType.CLIENT)
    record DiagramsData(double defaultPercent, double modifierPercent, Number modifier,
                        String titleKey, String positivelyString,
                        String negativeString, String defaultString,
                        boolean positivelyBetter) {
    }
}