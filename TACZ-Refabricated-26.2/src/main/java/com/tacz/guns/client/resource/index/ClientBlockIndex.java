package com.tacz.guns.client.resource.index;

import com.google.common.base.Preconditions;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.pojo.display.block.BlockDisplay;
import com.tacz.guns.client.resource.pojo.display.block.BlockTransformParser;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.resource.pojo.BlockIndexPOJO;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.StringUtils;

public class ClientBlockIndex {
    private BedrockModel model;
    private Identifier texture;
    private String name;
    private ItemTransforms transforms = ItemTransforms.NO_TRANSFORMS;
    private String tooltipKey;

    public static ClientBlockIndex getInstance(BlockIndexPOJO pojo) {
        ClientBlockIndex index = new ClientBlockIndex();
        checkIndex(pojo, index);
        BlockDisplay display = checkDisplay(pojo, index);
        checkModel(display, index);
        checkName(pojo, index);
        checkTransforms(display, index);
        return index;
    }

    private static void checkIndex(BlockIndexPOJO blockIndexPOJO, ClientBlockIndex index) {
        Preconditions.checkArgument(blockIndexPOJO != null, "index object file is empty");
        index.tooltipKey = blockIndexPOJO.getTooltip();
    }

    private static void checkName(BlockIndexPOJO blockIndexPOJO, ClientBlockIndex index) {
        index.name = blockIndexPOJO.getName();
        if (StringUtils.isBlank(index.name)) {
            index.name = "custom.tacz.error.no_name";
        }
    }

    private static BlockDisplay checkDisplay(BlockIndexPOJO pojo, ClientBlockIndex index) {
        Identifier display = pojo.getDisplay();
        Preconditions.checkArgument(display != null, "index object missing display field");
        BlockDisplay blockDisplay = ClientAssetsManager.INSTANCE.getBlockDisplay(pojo.getDisplay());
        Preconditions.checkArgument(blockDisplay != null, "there is no corresponding display file");
        return blockDisplay;
    }

    private static void checkModel(BlockDisplay display, ClientBlockIndex index) {
        Identifier modelLocation = display.getModelLocation();
        Preconditions.checkArgument(modelLocation != null, "display object missing model field");
        BedrockModelPOJO modelPOJO = ClientAssetsManager.INSTANCE.getBedrockModelPOJO(modelLocation);
        Preconditions.checkArgument(modelPOJO != null, "there is no corresponding model file");

        // 먼저 1.10.0 버전 베드락 모델 파일인지 판단한다
        if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
            index.model = new BedrockModel(modelPOJO, BedrockVersion.LEGACY);
        }
        // 1.12.0 버전 베드락 모델 파일인지 판단한다
        if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
            index.model = new BedrockModel(modelPOJO, BedrockVersion.NEW);
        }
        Preconditions.checkArgument(index.model != null, "there is no model data in the model file");

        Identifier textureLocation = display.getModelTexture();
        Preconditions.checkArgument(textureLocation != null, "missing default texture");
        index.texture = display.getModelTexture();
    }

    /**
     * 26.2 수정: 이식할 때 이 부분이 통째로 지워져 작업대/조립대의 손에 든 모델이 축소되지 않았다(기본 팩은 scale 0.25를 선언하지만
     * 실제로는 1.0으로 그려짐 => 4배 큼). 여기서 원본 동작을 되살리되 해석만 26.2에서 쓸 수 있는 구현으로 바꿨다.
     * 자세한 내용은 {@link BlockTransformParser} 참고.
     *
     * <p>원본과 일부러 다르게 한 점 하나: 원본은 {@code Preconditions.checkArgument(transforms != null)}로
     * 총기 팩에 transforms를 강제로 요구해, 없으면 예외를 던져 index 로드 전체가 실패했다. 여기서는
     * {@code NO_TRANSFORMS}로 대체해, 서드파티 총기 팩이 이 필드가 없다는 이유로 팩 전체를 못 불러오는 일을 막는다.</p>
     */
    private static void checkTransforms(BlockDisplay display, ClientBlockIndex index) {
        index.transforms = BlockTransformParser.parse(display.getTransforms());
    }

    public ItemTransforms getTransforms() {
        return transforms;
    }

    public BedrockModel getModel() {
        return model;
    }

    public Identifier getTexture() {
        return texture;
    }

    public String getName() {
        return name;
    }

    public String getTooltipKey() {
        return tooltipKey;
    }
}
