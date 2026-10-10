package com.tacz.guns.util;

import com.tacz.guns.resource.CommonAssetsManager;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AllowAttachmentTagMatcher {
    private static final String TAG_PREFIX = "#";
    private static final Cache CACHE = new Cache();

    public record Cache(
            Map<Pair<Identifier, Identifier>, Boolean> allowAttachmentCache,
            Map<Pair<Identifier, Identifier>, Boolean> tagMatchCache
    ) {
        public Cache() {
            this(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        }
    }

    public static boolean match(Identifier gunId, Identifier attachmentId) {
        var key = Pair.of(gunId, attachmentId);
        return CACHE.allowAttachmentCache().computeIfAbsent(key, AllowAttachmentTagMatcher::match0);
    }

    public static boolean match0(Pair<Identifier, Identifier> record) {
        Identifier gunId = record.getLeft();
        Identifier attachmentId = record.getRight();
        Set<String> allowAttachmentTags = CommonAssetsManager.get().getAllowAttachmentTags(gunId);
        // 총기의 allowAttachmentTags가 비었으면 지금은 달 수 있는 부착물이 하나도 없다는 뜻이다
        if (allowAttachmentTags == null || allowAttachmentTags.isEmpty()) {
            return false;
        }
        // allowAttachmentTags를 훑으며 부착물 id를 찾기 시작한다
        AtomicBoolean searchSignal = new AtomicBoolean(false);
        treeSearch(allowAttachmentTags, attachmentId, searchSignal);
        return searchSignal.get();
    }

    /**
     * 부착물에 지정한 태그가 있는지 맞춰 본다.
     * 지금은 내부에서 슬러그탄 특수 태그 판단에 쓰며,
     * 외부(애드온, 모드팩 등)가 자체 특수 태그를 만들기에도 편하다.
     *
     * @param tag          tacz 부착물 태그
     * @param attachmentId 부착물 id
     * @return 부착물 id에 이 부착물 태그가 있는지 여부
     * @since 1.1.7
     */
    public static boolean matchTag(Identifier tag, Identifier attachmentId) {
        var key = Pair.of(tag, attachmentId);
        return CACHE.tagMatchCache().computeIfAbsent(key, AllowAttachmentTagMatcher::matchTag0);
    }

    public static boolean matchTag0(Pair<Identifier, Identifier> record) {
        Identifier tag = record.getLeft();
        Identifier attachmentId = record.getRight();
        Set<String> tagContent = CommonAssetsManager.get().getAttachmentTags(tag);
        // tag에 대응하는 내용 집합이 비었으면 지금은 아무 내용도 없다는 뜻이다
        if (tagContent == null || tagContent.isEmpty()) {
            return false;
        }
        // 내용 집합을 훑으며 부착물 id를 찾기 시작한다
        AtomicBoolean searchSignal = new AtomicBoolean(false);
        treeSearch(tagContent, attachmentId, searchSignal);
        return searchSignal.get();
    }

    private static void treeSearch(Set<String> tags, Identifier attachmentId, AtomicBoolean searchSignal) {
        // tags를 훑으며 부착물 id를 찾기 시작한다
        for (String tag : tags) {
            // tag이면 attachment tag에서 우리 것을 찾는다
            if (tag.startsWith(TAG_PREFIX)) {
                Identifier tagId = Identifier.parse(tag.substring(TAG_PREFIX.length()));
                Set<String> attachmentTags = CommonAssetsManager.get().getAttachmentTags(tagId);
                // 조회한 이 부착물 tag가 비어 있지 않으면 재귀로 찾기 시작한다
                if (attachmentTags != null && !attachmentTags.isEmpty()) {
                    treeSearch(attachmentTags, attachmentId, searchSignal);
                }
            }
            // 부착물 id이면 바로 비교한다
            else {
                Identifier matchAttachmentId = Identifier.parse(tag);
                if (attachmentId.equals(matchAttachmentId)) {
                    searchSignal.set(true);
                    return;
                }
            }
        }
    }

    public static void resetCache() {
        CACHE.allowAttachmentCache().clear();
        CACHE.tagMatchCache().clear();
    }
}
