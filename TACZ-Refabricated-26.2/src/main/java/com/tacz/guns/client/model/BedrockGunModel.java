package com.tacz.guns.client.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.model.functional.*;
import com.tacz.guns.client.model.listener.model.ModelAdditionalMagazineListener;
import com.tacz.guns.client.resource.pojo.display.gun.TextShow;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static com.tacz.guns.client.model.GunModelConstant.*;

public class BedrockGunModel extends BedrockAnimatedModel {
    protected final EnumMap<AttachmentType, List<BedrockPart>> refitAttachmentViewPath = Maps.newEnumMap(AttachmentType.class);
    private final EnumMap<AttachmentType, ItemStack> currentAttachmentItem = Maps.newEnumMap(AttachmentType.class);
    private final Set<String> adapterToRender = Sets.newHashSet();
    private final ArrayList<ShellRender> shellRenderList = new ArrayList<>();

    // 1인칭 가늠쇠 카메라 위치 그룹의 경로
    protected @Nullable List<BedrockPart> ironSightPath;
    // 1인칭 idle 상태 카메라 위치 그룹의 경로
    protected @Nullable List<BedrockPart> idleSightPath;
    // 3인칭 손 아이템 렌더링 원점 위치 그룹의 경로
    protected @Nullable List<BedrockPart> thirdPersonHandOriginPath;
    // 아이템 액자 렌더링 원점 위치 그룹의 경로
    protected @Nullable List<BedrockPart> fixedOriginPath;
    // 땅 위 엔티티 렌더링 원점 위치 그룹의 경로
    protected @Nullable List<BedrockPart> groundOriginPath;
    // 조준경 부착물 위치 그룹의 경로. 다른 부착물은 경로를 저장할 필요 없이 렌더링만 바꾸면 되지만, 조준경 위치 그룹은 1인칭 조준 카메라 위치를 돕는 데 써야 한다.
    protected @Nullable List<BedrockPart> scopePosPath;
    // 총구 화염 위치 그룹
    protected @Nullable List<BedrockPart> muzzleFlashPosPath;
    // 루트 그룹
    protected @Nullable BedrockPart root;
    // 탄창 위치 그룹
    protected @Nullable BedrockPart magazineNode;
    // 재장전 때의 두 번째 탄창 위치 그룹
    protected @Nullable BedrockPart additionalMagazineNode;
    protected @Nullable List<BedrockPart> laserBeamPaths;

    private boolean renderHand = true;
    private boolean renderMount;
    private ItemStack currentGunItem;
    private int currentExtendMagLevel = 0;

    public BedrockGunModel(BedrockModelPOJO pojo, BedrockVersion version) {
        super(pojo, version);

        this.magazineNode = Optional.ofNullable(modelMap.get(MAG_NORMAL_NODE)).map(ModelRendererWrapper::getModelRenderer).orElse(null);
        this.additionalMagazineNode = Optional.ofNullable(modelMap.get(MAG_ADDITIONAL_NODE)).map(ModelRendererWrapper::getModelRenderer).orElse(null);

        // 왼팔
        this.setFunctionalRenderer(LEFTHAND_POS_NODE, bedrockPart -> new LeftHandRender(this));
        // 오른팔
        this.setFunctionalRenderer(RIGHTHAND_POS_NODE, bedrockPart -> new RightHandRender(this));
        // 총구 화염
        this.setFunctionalRenderer(MUZZLE_FLASH_ORIGIN_NODE, bedrockPart -> new MuzzleFlashRender(this));
        // 약실 안의 탄. 클로즈드 볼트 대기 총기에 쓴다
        this.setFunctionalRenderer(BULLET_IN_BARREL, bedrockPart -> ammoHiddenRender(bedrockPart, iGun -> iGun.hasBulletInBarrel(currentGunItem)));
        // 탄창 안의 탄
        this.setFunctionalRenderer(BULLET_IN_MAG, bedrockPart -> ammoHiddenRender(bedrockPart, iGun -> iGun.getCurrentAmmoCount(currentGunItem) > 0));
        // 기관총 탄띠
        this.setFunctionalRenderer(BULLET_CHAIN, bedrockPart -> ammoHiddenRender(bedrockPart, iGun -> iGun.getCurrentAmmoCount(currentGunItem) > 0));
        // 일반 조준경이 있을 때 표시. 조준경을 얹는 레일(예: AKM의 레일)
        this.setFunctionalRenderer(MOUNT, bedrockPart -> scopeHiddenRender(bedrockPart, scopeItem -> scopeItem != null && !scopeItem.isEmpty() && renderMount));
        // 조준경이 없을 때 보임. 보통 M4에 쓴다
        this.setFunctionalRenderer(CARRY, bedrockPart -> scopeHiddenRender(bedrockPart, scopeItem -> scopeItem == null || scopeItem.isEmpty()));
        // 조준경이 있을 때 표시. 접힌 가늠쇠
        this.setFunctionalRenderer(SIGHT_FOLDED, bedrockPart -> scopeHiddenRender(bedrockPart, scopeItem -> scopeItem != null && !scopeItem.isEmpty()));
        // 조준경이 없을 때 보임. 가늠쇠
        this.setFunctionalRenderer(SIGHT, bedrockPart -> scopeHiddenRender(bedrockPart, scopeItem -> scopeItem == null || scopeItem.isEmpty()));
        // 1단계 확장 탄창을 달았을 때 표시
        this.setFunctionalRenderer(MAG_EXTENDED_1, bedrockPart -> extendedMagHiddenRender(bedrockPart, 1));
        // 2단계 확장 탄창을 달았을 때 표시
        this.setFunctionalRenderer(MAG_EXTENDED_2, bedrockPart -> extendedMagHiddenRender(bedrockPart, 2));
        // 3단계 확장 탄창을 달았을 때 표시
        this.setFunctionalRenderer(MAG_EXTENDED_3, bedrockPart -> extendedMagHiddenRender(bedrockPart, 3));
        // 확장 탄창을 달지 않았을 때 표시
        this.setFunctionalRenderer(MAG_STANDARD, bedrockPart -> extendedMagHiddenRender(bedrockPart, 0));
        // 일부 총기 재장전 애니메이션에서는 탄창 두 개가 동시에 나오는데, 이것이 다른 탄창을 프로그램으로 그리는 코드다
        this.setFunctionalRenderer(MAG_ADDITIONAL_NODE, this::renderAdditionalMagazine);
        // 기본 총열 덮개 렌더링
        this.setFunctionalRenderer(HANDGUARD_DEFAULT_NODE, this::handguardDefaultRender);
        // 전술 총열 덮개 렌더링
        this.setFunctionalRenderer(HANDGUARD_TACTICAL_NODE, this::handguardTacticalRender);
        // 그 밖의 위치 그룹 캐시
        this.cacheOtherPath();
        // 개조 UI에서 각 부착물의 근접 시점 위치 그룹 캐시
        this.cacheRefitAttachmentViewPath();
        // 탄피 배출구 캐시
        this.cacheShellOriginNodes();
        // 각 부착물 렌더링 준비
        this.allAttachmentRender();
        // 부착물 어댑터 렌더링
        this.setFunctionalRenderer(ATTACHMENT_ADAPTER_NODE, this::attachmentAdapterNodeRender);
    }

    private void cacheOtherPath() {
        ironSightPath = getPath(modelMap.get(IRON_VIEW_NODE));
        idleSightPath = getPath(modelMap.get(IDLE_VIEW_NODE));
        thirdPersonHandOriginPath = getPath(modelMap.get(THIRD_PERSON_HAND_ORIGIN_NODE));
        fixedOriginPath = getPath(modelMap.get(FIXED_ORIGIN_NODE));
        groundOriginPath = getPath(modelMap.get(GROUND_ORIGIN_NODE));
        muzzleFlashPosPath = getPath(modelMap.get(MUZZLE_FLASH_ORIGIN_NODE));
        scopePosPath = getPath(modelMap.get(AttachmentType.SCOPE.name().toLowerCase() + ATTACHMENT_POS_SUFFIX));
        laserBeamPaths = getPath(modelMap.get("laser_beam"));
        root = Optional.ofNullable(modelMap.get(ROOT_NODE)).map(ModelRendererWrapper::getModelRenderer).orElse(null);
    }

    private void cacheRefitAttachmentViewPath() {
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE) {
                refitAttachmentViewPath.put(type, getPath(modelMap.get(REFIT_VIEW_NODE)));
                continue;
            }
            String nodeName = REFIT_VIEW_PREFIX + type.name().toLowerCase() + REFIT_VIEW_SUFFIX;
            refitAttachmentViewPath.put(type, getPath(modelMap.get(nodeName)));
        }
    }

    private void cacheShellOriginNodes() {
        ModelRendererWrapper rendererWrapper = modelMap.get(SHELL_ORIGIN_NODE);
        int i = 1;
        while (rendererWrapper != null) {
            ShellRender shellRender = new ShellRender(this);
            this.setFunctionalRenderer(rendererWrapper.getModelRenderer().name, bedrockPart -> shellRender);
            shellRenderList.add(shellRender);
            rendererWrapper = modelMap.get(SHELL_ORIGIN_NODE_PREFIX + i);
            i++;
        }
    }

    @Nullable
    private IFunctionalRenderer attachmentAdapterNodeRender(BedrockPart bedrockPart) {
        for (BedrockPart child : bedrockPart.children) {
            if (child.name == null) {
                child.visible = false;
                continue;
            }
            child.visible = adapterToRender.contains(child.name);
        }
        return null;
    }

    private void allAttachmentRender() {
        for (AttachmentType type : AttachmentType.values()) {
            // 조준경 렌더링은 먼저 해야 한다
            if (type == AttachmentType.NONE || type == AttachmentType.SCOPE) {
                continue;
            }
            String positionNodeName = type.name().toLowerCase() + ATTACHMENT_POS_SUFFIX;
            String defaultNodeName = type.name().toLowerCase() + DEFAULT_ATTACHMENT_SUFFIX;
            this.setFunctionalRenderer(positionNodeName, bedrockPart -> {
                bedrockPart.visible = false;
                return new AttachmentRender(this, type);
            });
            this.setFunctionalRenderer(defaultNodeName, bedrockPart -> {
                ItemStack attachmentItem = currentAttachmentItem.get(type);
                if (type == AttachmentType.MUZZLE && checkShowMuzzle(bedrockPart, attachmentItem)) {
                    return null;
                }
                bedrockPart.visible = attachmentItem == null || attachmentItem.isEmpty();
                return null;
            });
        }
    }

    private static boolean checkShowMuzzle(BedrockPart bedrockPart, ItemStack attachmentItem) {
        IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentItem);
        if (iAttachment != null) {
            Identifier attachmentId = iAttachment.getAttachmentId(attachmentItem);
            var attachmentIndex = TimelessAPI.getClientAttachmentIndex(attachmentId);
            if (attachmentIndex.isPresent()) {
                bedrockPart.visible = attachmentIndex.get().isShowMuzzle();
                return true;
            }
        }
        return false;
    }

    @Nullable
    private IFunctionalRenderer handguardTacticalRender(BedrockPart bedrockPart) {
        ItemStack laserItem = currentAttachmentItem.get(AttachmentType.LASER);
        ItemStack gripItem = currentAttachmentItem.get(AttachmentType.GRIP);
        bedrockPart.visible = !laserItem.isEmpty() || !gripItem.isEmpty();
        return null;
    }

    @Nullable
    private IFunctionalRenderer handguardDefaultRender(BedrockPart bedrockPart) {
        ItemStack laserItem = currentAttachmentItem.get(AttachmentType.LASER);
        ItemStack gripItem = currentAttachmentItem.get(AttachmentType.GRIP);
        bedrockPart.visible = laserItem.isEmpty() && gripItem.isEmpty();
        return null;
    }

    /**
     * {@code additional_magazine} 노드 — 재장전 애니메이션에서 <b>총몸에 남아 있는</b> 탄창.
     *
     * <p><b>8차 수정: 2차의 잘못된 변경을 되돌렸다.</b></p>
     *
     * <p>모델의 두 탄창 노드는 의미가 다르다: {@code magazine}은 재장전 때 <b>손을 따라 움직이는</b> 탄창이고,
     * {@code additional_magazine}은 <b>총에 남아 있는</b> 탄창이다. 원본 1.21.1은
     * 이 노드의 변환 아래에서 {@code magazine}의 메시를 <b>한 번 더 그렸다</b>(같은 형상을 두 번 렌더링).
     * 기본 총기 팩의 {@code reload_tactical}/{@code reload_empty}/{@code inspect}
     * 등 애니메이션이 두 노드를 함께 움직이며 이 동작에 기댄다.</p>
     *
     * <p>2차에서 "{@code magazine}은 원래 모델 트리에 있어 순회된다"는 이유로 {@code return null}로 바꿨는데 —
     * <b>그 판단은 틀렸다</b>: 트리에 있는 것은 손을 따라가는 쪽이고, 총에 남는 쪽은 여기서 덧그려야만 한다.
     * 증상이 바로 보고된 것이다: 재장전/빈 탄창 재장전 때 <b>총의 탄창이 그려지지 않고 손의 것만 남았다</b>.</p>
     *
     * <p>지금은 {@link IMirrorGeometry}를 돌려주어 {@code BedrockRenderSnapshot}이 직접 처리한다:
     * 이 노드의 변환 아래에서 자신을 먼저 그리고 {@code magazine}을 그리며, 총몸과 같은 RenderType과
     * DrawCommand 묶음을 써서 재질과 렌더링 순서가 맞는다.</p>
     */
    @Nullable
    private IFunctionalRenderer renderAdditionalMagazine(BedrockPart bedrockPart) {
        return (IMirrorGeometry) () -> magazineNode;
    }

    /**
     * 총기 사용자 정의 글자 표시를 추가한다
     */
    public void setTextShowList(Map<String, TextShow> textShowList) {
        textShowList.forEach((name, textShow) -> this.setFunctionalRenderer(name, bedrockPart -> new TextShowRender(this, textShow, currentGunItem)));
    }

    /** 즉시 렌더링이나 스냅숏 추출 전에 스택에 따라 달라지는 표시 상태를 모두 준비한다. */
    private boolean prepareRenderState(ItemStack gunItem) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return false;
        }
        currentGunItem = gunItem;
        currentExtendMagLevel = 0;
        adapterToRender.clear();
        // 렌더링에 쓰도록 부착물 아이템 캐시를 갱신한다
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE) {
                continue;
            }
            ItemStack attachmentItem = iGun.getAttachment(gunItem, type);
            if (attachmentItem.isEmpty()) {
                attachmentItem = iGun.getBuiltinAttachment(gunItem, type);
            }
            currentAttachmentItem.put(type, attachmentItem);
            IAttachment attachment = IAttachment.getIAttachmentOrNull(attachmentItem);
            if (attachment != null) {
                TimelessAPI.getClientAttachmentIndex(attachment.getAttachmentId(attachmentItem)).ifPresent(index -> {
                    if (type == AttachmentType.EXTENDED_MAG) {
                        currentExtendMagLevel = index.getData().getExtendedMagLevel();
                    }
                    if (type == AttachmentType.SCOPE) {
                        renderMount = index.isShowMount();
                    }
                    if (index.getAdapterNodeName() != null) {
                        adapterToRender.add(index.getAdapterNodeName());
                    }
                });
            }
        }
        return true;
    }


    /**
     * 백엔드에 의존하지 않는 26.2 제출 경로. 부모 클래스가 모든 부품 행렬을 변하지 않는 BedrockRenderSnapshot으로
     * 고정하기 전에 스택에 따라 달라지는 상태를 준비한다.
     */
    public void submit(PoseStack poseStack,
                       ItemStack gunItem,
                       ItemDisplayContext transformType,
                       SubmitNodeCollector collector,
                       RenderType renderType,
                       int light,
                       int overlay) {
        if (!prepareRenderState(gunItem)) {
            return;
        }
        if (laserBeamPaths != null) {
            BeamRenderer.renderLaserBeam(gunItem, poseStack, transformType, laserBeamPaths, collector);
        }

        // A6 기준선: 조준경을 raw-GL stencil 잘라내기 없이 일반 형상으로 제출한다.
        // 그래서 두 백엔드 모두에서 조준경 몸체/고리/조준선이 보인다. PIP 구현이 나오면
        // 핵심 총기 모델을 막지 않고 이 순서대로의 대체 경로를 바꿀 수 있다.
        ItemStack scope = currentAttachmentItem.get(AttachmentType.SCOPE);
        if (scopePosPath != null && scope != null && !scope.isEmpty()) {
            PoseStack scopePose = new PoseStack();
            scopePose.last().pose().set(poseStack.last().pose());
            scopePose.last().normal().set(poseStack.last().normal());
            for (BedrockPart part : scopePosPath) {
                part.translateAndRotateAndScale(scopePose);
            }
            AttachmentRender.submitAttachment(scope.copy(), gunItem.copy(), scopePose,
                    transformType, collector, light, overlay);
        }

        super.submit(poseStack, transformType, collector, renderType, light, overlay);
    }


    @Nullable
    private IFunctionalRenderer ammoHiddenRender(BedrockPart bedrockPart, Predicate<IGun> predicate) {
        IGun iGun = IGun.getIGunOrNull(currentGunItem);
        if (iGun != null) {
            bedrockPart.visible = predicate.test(iGun);
        }
        return null;
    }

    @Nullable
    private IFunctionalRenderer scopeHiddenRender(BedrockPart bedrockPart, Predicate<ItemStack> predicate) {
        // 조준경을 달았을 때 보임
        ItemStack scopeItem = currentAttachmentItem.get(AttachmentType.SCOPE);
        bedrockPart.visible = predicate.test(scopeItem);
        return null;
    }

    @Nullable
    private IFunctionalRenderer extendedMagHiddenRender(BedrockPart bedrockPart, int level) {
        bedrockPart.visible = currentExtendMagLevel == level;
        return null;
    }

    @Override
    public AnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
        AnimationListener listener = super.supplyListeners(nodeName, type);
        if (listener == null) {
            return null;
        }
        if (nodeName.equals(MAG_ADDITIONAL_NODE)) {
            // 추가 탄창은 애니메이션에 그 키프레임이 있을 때만 그린다
            return new ModelAdditionalMagazineListener(listener, this);
        }
        return listener;
    }

    @Override
    public void cleanAnimationTransform() {
        super.cleanAnimationTransform();
        if (additionalMagazineNode != null) {
            additionalMagazineNode.visible = false;
        }
    }

    public EnumMap<AttachmentType, ItemStack> getCurrentAttachmentItem() {
        return currentAttachmentItem;
    }

    public ItemStack getCurrentGunItem() {
        return currentGunItem;
    }

    @Nullable
    public BedrockPart getAdditionalMagazineNode() {
        return additionalMagazineNode;
    }

    @Nullable
    public List<BedrockPart> getIronSightPath() {
        return ironSightPath;
    }

    @Nullable
    public List<BedrockPart> getIdleSightPath() {
        return idleSightPath;
    }

    @Nullable
    public List<BedrockPart> getThirdPersonHandOriginPath() {
        return thirdPersonHandOriginPath;
    }

    @Nullable
    public List<BedrockPart> getFixedOriginPath() {
        return fixedOriginPath;
    }

    @Nullable
    public List<BedrockPart> getGroundOriginPath() {
        return groundOriginPath;
    }

    @Nullable
    public List<BedrockPart> getMuzzleFlashPosPath() {
        return muzzleFlashPosPath;
    }

    @Nullable
    public List<BedrockPart> getScopePosPath() {
        return scopePosPath;
    }

    @Nullable
    public List<BedrockPart> getRefitAttachmentViewPath(AttachmentType type) {
        return refitAttachmentViewPath.get(type);
    }

    @Nullable
    public ShellRender getShellRender(int index) {
        if (index < 0 || index >= shellRenderList.size()) {
            return null;
        }
        return shellRenderList.get(index);
    }

    @Nullable
    public BedrockPart getRootNode() {
        return root;
    }

    public boolean getRenderHand() {
        return renderHand;
    }

    public void setRenderHand(boolean renderHand) {
        this.renderHand = renderHand;
    }
}
