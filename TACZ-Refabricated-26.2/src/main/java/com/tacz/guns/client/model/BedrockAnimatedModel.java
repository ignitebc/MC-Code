package com.tacz.guns.client.model;

import com.tacz.guns.api.client.animation.AnimationListener;
import com.tacz.guns.api.client.animation.AnimationListenerSupplier;
import com.tacz.guns.api.client.animation.ObjectAnimationChannel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.bedrock.ModelRendererWrapper;
import com.tacz.guns.client.model.listener.camera.CameraAnimationObject;
import com.tacz.guns.client.model.listener.constraint.ConstraintObject;
import com.tacz.guns.client.model.listener.model.ModelRotateListener;
import com.tacz.guns.client.model.listener.model.ModelScaleListener;
import com.tacz.guns.client.model.listener.model.ModelTranslateListener;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import com.tacz.guns.client.resource.pojo.model.BonesItem;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.tacz.guns.client.model.GunModelConstant.ROOT_NODE;

public class BedrockAnimatedModel extends BedrockModel implements AnimationListenerSupplier {
    public static final String CAMERA_NODE_NAME = "camera";
    public static final String CONSTRAINT_NODE = "constraint";
    private final CameraAnimationObject cameraAnimationObject = new CameraAnimationObject();
    /**
     * 애니메이션 구속 그룹의 경로
     */
    protected @Nullable List<BedrockPart> constraintPath;
    private @Nullable ConstraintObject constraintObject;

    // 루트 그룹
    protected @Nullable BedrockPart root;
    // 1인칭 idle 상태 카메라 위치 그룹의 경로
    protected @Nullable List<BedrockPart> idleSightPath;

    public BedrockAnimatedModel(BedrockModelPOJO pojo, BedrockVersion version) {
        super(pojo, version);
        // 카메라 애니메이션 객체 초기화
        ModelRendererWrapper cameraRendererWrapper = modelMap.get(CAMERA_NODE_NAME);
        if (cameraRendererWrapper != null) {
            cameraAnimationObject.cameraRenderer = cameraRendererWrapper;
        }
        // 애니메이션 구속 객체 초기화
        constraintPath = getPath(modelMap.get(CONSTRAINT_NODE));
        if (constraintPath != null) {
            constraintObject = new ConstraintObject();
            BedrockPart constraintNode = constraintPath.get(constraintPath.size() - 1);
            if (shouldRender.contains(constraintNode)) {
                constraintObject.bonesItem = indexBones.get(CONSTRAINT_NODE);
            } else {
                constraintObject.node = constraintNode;
            }
        }
        root = Optional.ofNullable(modelMap.get(ROOT_NODE)).map(ModelRendererWrapper::getModelRenderer).orElse(null);
        idleSightPath = getPath(modelMap.get("idle_view"));
    }


    @Nullable
    public List<BedrockPart> getConstraintPath() {
        return constraintPath;
    }

    public void cleanAnimationTransform() {
        for (ModelRendererWrapper rendererWrapper : modelMap.values()) {
            rendererWrapper.setOffsetX(0);
            rendererWrapper.setOffsetY(0);
            rendererWrapper.setOffsetZ(0);
            rendererWrapper.getAdditionalQuaternion().set(0, 0, 0, 1);
            rendererWrapper.setScaleX(1);
            rendererWrapper.setScaleY(1);
            rendererWrapper.setScaleZ(1);
        }
        if (constraintObject != null) {
            constraintObject.rotationConstraint.set(0, 0, 0);
            constraintObject.translationConstraint.set(0, 0, 0);
        }
    }

    public void cleanCameraAnimationTransform() {
        cameraAnimationObject.rotationQuaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
    }

    /**
     * @param node     프로그램으로 렌더링 흐름을 처리할 node 이름
     * @param function BedrockPart를 받아 렌더링을 대신할 IModelRenderer를 돌려준다
     */
    public void setFunctionalRenderer(String node, Function<BedrockPart, IFunctionalRenderer> function) {
        ModelRendererWrapper wrapper = modelMap.get(node);
        if (wrapper == null) {
            FunctionalBedrockPart functionalPart = new FunctionalBedrockPart(function, node);
            modelMap.put(node, new ModelRendererWrapper(functionalPart));
        } else if (wrapper.getModelRenderer() instanceof FunctionalBedrockPart functionalPart) {
            functionalPart.functionalRenderer = function;
        }
    }

    @Nonnull
    public CameraAnimationObject getCameraAnimationObject() {
        return cameraAnimationObject;
    }

    @Nullable
    public ConstraintObject getConstraintObject() {
        return constraintObject;
    }

    @Override
    protected void loadNewModel(BedrockModelPOJO pojo) {
        assert pojo.getGeometryModelNew() != null;
        pojo.getGeometryModelNew().deco();
        if (pojo.getGeometryModelNew().getBones() == null) {
            return;
        }
        for (BonesItem bones : pojo.getGeometryModelNew().getBones()) {
            // functionalRender를 지원하도록 FunctionalBedrockPart를 먼저 modelMap에 넣는다
            FunctionalBedrockPart bedrockPart = new FunctionalBedrockPart(null, bones.getName());
            modelMap.putIfAbsent(bones.getName(), new ModelRendererWrapper(bedrockPart));
        }
        super.loadNewModel(pojo);
    }

    @Override
    protected void loadLegacyModel(BedrockModelPOJO pojo) {
        assert pojo.getGeometryModelLegacy() != null;
        pojo.getGeometryModelLegacy().deco();
        if (pojo.getGeometryModelLegacy().getBones() == null) {
            return;
        }
        for (BonesItem bones : pojo.getGeometryModelLegacy().getBones()) {
            // functionalRender를 지원하도록 FunctionalBedrockPart를 먼저 modelMap에 넣는다
            FunctionalBedrockPart bedrockPart = new FunctionalBedrockPart(null, bones.getName());
            modelMap.putIfAbsent(bones.getName(), new ModelRendererWrapper(bedrockPart));
        }
        super.loadLegacyModel(pojo);
    }

    @Override
    public AnimationListener supplyListeners(String nodeName, ObjectAnimationChannel.ChannelType type) {
        ModelRendererWrapper model = modelMap.get(nodeName);
        if (model == null) {
            return null;
        }
        AnimationListener cameraListener = cameraAnimationObject.supplyListeners(nodeName, type);
        if (cameraListener != null) {
            return cameraListener;
        }
        if (constraintObject != null) {
            AnimationListener constraintListener = constraintObject.supplyListeners(nodeName, type);
            if (constraintListener != null) {
                return constraintListener;
            }
        }
        if (type.equals(ObjectAnimationChannel.ChannelType.TRANSLATION)) {
            return new ModelTranslateListener(this, model, nodeName);
        }
        if (type.equals(ObjectAnimationChannel.ChannelType.ROTATION)) {
            return new ModelRotateListener(model);
        }
        if (type.equals(ObjectAnimationChannel.ChannelType.SCALE)) {
            return new ModelScaleListener(model);
        }
        return null;
    }

    public boolean getRenderHand() {
        return true;
    }

    public BedrockPart getRootNode() {
        return root;
    }

    public List<BedrockPart> getIdleSightPath() {
        return idleSightPath;
    }
}