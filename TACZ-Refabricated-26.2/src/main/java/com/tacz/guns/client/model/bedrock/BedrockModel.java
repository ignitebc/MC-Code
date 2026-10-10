package com.tacz.guns.client.model.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.renderer.snapshot.BedrockRenderSnapshot;
import com.tacz.guns.client.resource.pojo.model.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class BedrockModel {
    public static BedrockModel dummyModel = new BedrockModel();
    /**
     * ModelRender 하위 모델을 담는 HashMap
     */
    protected final HashMap<String, ModelRendererWrapper> modelMap = new HashMap<>();
    /**
     * Bones를 담는 HashMap. 주로 뒤에서 부모 뼈를 찾아 좌표를 변환할 때 쓴다
     */
    protected final HashMap<String, BonesItem> indexBones = new HashMap<>();
    /**
     * 어떤 모델을 그려야 하는지. 부모 뼈에 들어간 자식 뼈는 그릴 필요가 없다
     */
    protected final List<BedrockPart> shouldRender = new LinkedList<>();
    /**
     * 렌더링이 끝날 때 실행하도록 맡긴 렌더러. 팔 같은 특수 부분 렌더링에 쓴다
     */
    protected List<IFunctionalRenderer> delegateRenderers = new ArrayList<>();
    /**
     * 모델의 중심점
     */
    protected @Nullable Vec3 offset = null;
    /**
     * 모델의 크기
     */
    protected @Nullable Vec2 size = null;

    public BedrockModel(BedrockModelPOJO pojo, BedrockVersion version) {
        if (version == BedrockVersion.LEGACY) {
            loadLegacyModel(pojo);
        }
        if (version == BedrockVersion.NEW) {
            loadNewModel(pojo);
        }
        // 발광 적용
        for (ModelRendererWrapper rendererWrapper : modelMap.values()) {
            if (rendererWrapper.getModelRenderer().name != null && rendererWrapper.getModelRenderer().name.endsWith("_illuminated")) {
                rendererWrapper.getModelRenderer().illuminated = true;
            }
        }
    }

    protected BedrockModel() {
    }

    public void delegateRender(IFunctionalRenderer renderer) {
        delegateRenderers.add(renderer);
    }

    private void setRotationAngle(BedrockPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
        modelRenderer.setInitRotationAngle(x, y, z);
    }

    protected void loadNewModel(BedrockModelPOJO pojo) {
        assert pojo.getGeometryModelNew() != null;
        pojo.getGeometryModelNew().deco();
        if (pojo.getGeometryModelNew().getBones() == null) {
            return;
        }
        Description description = pojo.getGeometryModelNew().getDescription();
        // 텍스처의 너비와 높이
        int texWidth = description.getTextureWidth();
        int texHeight = description.getTextureHeight();

        List<Float> offset = description.getVisibleBoundsOffset();
        float offsetX = offset.get(0);
        float offsetY = offset.get(1);
        float offsetZ = offset.get(2);
        this.offset = new Vec3(offsetX, offsetY, offsetZ);
        float width = description.getVisibleBoundsWidth() / 2.0f;
        float height = description.getVisibleBoundsHeight() / 2.0f;
        this.size = new Vec2(width, height);

        // indexBones에 데이터를 넣어 뒤의 좌표 변환 기준으로 삼는다
        for (BonesItem bones : pojo.getGeometryModelNew().getBones()) {
            // 색인을 넣는다. 뒤의 좌표 변환에 쓴다
            indexBones.putIfAbsent(bones.getName(), bones);
            // 새로 만든 빈 BedrockPart 인스턴스를 넣는다
            // 뒤에서 parent를 붙여야 하므로 먼저 빈 객체를 넣고, 두 번째 순회에서 데이터를 저장한다
            modelMap.putIfAbsent(bones.getName(), new ModelRendererWrapper(new BedrockPart(bones.getName())));
        }

        // ModelRenderer 인스턴스에 데이터를 채우기 시작한다
        for (BonesItem bones : pojo.getGeometryModelNew().getBones()) {
            // 뼈 이름
            String name = bones.getName();
            // 회전. 비어 있을 수 있다
            @Nullable List<Float> rotation = bones.getRotation();
            // 부모 뼈 이름. 비어 있을 수 있다
            @Nullable String parent = bones.getParent();
            // HashMap에 넣는 모델 객체
            BedrockPart model = modelMap.get(name).getModelRenderer();

            // 거울 매개변수
            model.mirror = bones.isMirror();

            // 회전 중심
            model.setPos(convertPivot(bones, 0), convertPivot(bones, 1), convertPivot(bones, 2));

            // Nullable 검사 후 회전 각도 설정
            if (rotation != null) {
                setRotationAngle(model, convertRotation(rotation.get(0)), convertRotation(rotation.get(1)), convertRotation(rotation.get(2)));
            }

            // Null 검사 후 부모 뼈에 묶는다
            if (parent != null) {
                BedrockPart parentPart = modelMap.get(parent).getModelRenderer();
                parentPart.addChild(model);
                model.parent = parentPart;
            } else {
                // 부모 뼈가 없는 모델만 그린다
                shouldRender.add(model);
                model.parent = null;
            }

            // 세상에, Cubes가 비어 있을 수도 있다……
            if (bones.getCubes() == null) {
                continue;
            }

            // Cube List를 넣는다
            for (CubesItem cube : bones.getCubes()) {
                List<Float> uv = cube.getUv();
                @Nullable FaceUVsItem faceUv = cube.getFaceUv();
                List<Float> size = cube.getSize();
                @Nullable List<Float> cubeRotation = cube.getRotation();
                boolean mirror = cube.isMirror();
                float inflate = cube.getInflate();

                // 일반 cube로 저장한다
                if (cubeRotation == null) {
                    if (faceUv == null) {
                        model.cubes.add(new BedrockCubeBox(uv.get(0), uv.get(1),
                                convertOrigin(bones, cube, 0), convertOrigin(bones, cube, 1), convertOrigin(bones, cube, 2),
                                size.get(0), size.get(1), size.get(2), inflate, mirror,
                                texWidth, texHeight));
                    } else {
                        model.cubes.add(new BedrockCubePerFace(
                                convertOrigin(bones, cube, 0), convertOrigin(bones, cube, 1), convertOrigin(bones, cube, 2),
                                size.get(0), size.get(1), size.get(2), inflate,
                                texWidth, texHeight, faceUv));
                    }
                }
                // Cube ModelRender 생성
                else {
                    BedrockPart cubeRenderer = new BedrockPart(null);
                    cubeRenderer.setPos(convertPivot(bones, cube, 0), convertPivot(bones, cube, 1), convertPivot(bones, cube, 2));
                    setRotationAngle(cubeRenderer, convertRotation(cubeRotation.get(0)), convertRotation(cubeRotation.get(1)), convertRotation(cubeRotation.get(2)));
                    if (faceUv == null) {
                        cubeRenderer.cubes.add(new BedrockCubeBox(uv.get(0), uv.get(1),
                                convertOrigin(cube, 0), convertOrigin(cube, 1), convertOrigin(cube, 2),
                                size.get(0), size.get(1), size.get(2), inflate, mirror,
                                texWidth, texHeight));
                    } else {
                        cubeRenderer.cubes.add(new BedrockCubePerFace(
                                convertOrigin(cube, 0), convertOrigin(cube, 1), convertOrigin(cube, 2),
                                size.get(0), size.get(1), size.get(2), inflate,
                                texWidth, texHeight, faceUv));
                    }

                    // 부모 뼈에 추가한다
                    model.addChild(cubeRenderer);
                }
            }
        }
    }

    protected void loadLegacyModel(BedrockModelPOJO pojo) {
        assert pojo.getGeometryModelLegacy() != null;
        pojo.getGeometryModelLegacy().deco();
        if (pojo.getGeometryModelLegacy().getBones() == null) {
            return;
        }

        // 텍스처의 너비와 높이
        int texWidth = pojo.getGeometryModelLegacy().getTextureWidth();
        int texHeight = pojo.getGeometryModelLegacy().getTextureHeight();

        List<Float> offset = pojo.getGeometryModelLegacy().getVisibleBoundsOffset();
        float offsetX = offset.get(0);
        float offsetY = offset.get(1);
        float offsetZ = offset.get(2);
        this.offset = new Vec3(offsetX, offsetY, offsetZ);
        float width = pojo.getGeometryModelLegacy().getVisibleBoundsWidth() / 2.0f;
        float height = pojo.getGeometryModelLegacy().getVisibleBoundsHeight() / 2.0f;
        this.size = new Vec2(width, height);

        // indexBones에 데이터를 넣어 뒤의 좌표 변환 기준으로 삼는다
        for (BonesItem bones : pojo.getGeometryModelLegacy().getBones()) {
            // 색인을 넣는다. 뒤의 좌표 변환에 쓴다
            indexBones.putIfAbsent(bones.getName(), bones);
            // 새로 만든 빈 ModelRenderer 인스턴스를 넣는다
            // 뒤에서 parent를 붙여야 하므로 먼저 빈 객체를 넣고, 두 번째 순회에서 데이터를 저장한다
            modelMap.putIfAbsent(bones.getName(), new ModelRendererWrapper(new BedrockPart(bones.getName())));
        }

        // ModelRenderer 인스턴스에 데이터를 채우기 시작한다
        for (BonesItem bones : pojo.getGeometryModelLegacy().getBones()) {
            // 뼈 이름. 뒤의 애니메이션 때문에 머리·손·다리 등의 뼈 이름은 반드시 고정되어야 한다
            String name = bones.getName();
            // 회전 중심. 비어 있을 수 있다
            @Nullable List<Float> rotation = bones.getRotation();
            // 부모 뼈 이름. 비어 있을 수 있다
            @Nullable String parent = bones.getParent();
            // HashMap에 넣는 모델 객체
            BedrockPart model = modelMap.get(name).getModelRenderer();

            // 거울 매개변수
            model.mirror = bones.isMirror();

            // 회전 중심
            model.setPos(convertPivot(bones, 0), convertPivot(bones, 1), convertPivot(bones, 2));

            // Nullable 검사 후 회전 각도 설정
            if (rotation != null) {
                setRotationAngle(model, convertRotation(rotation.get(0)), convertRotation(rotation.get(1)), convertRotation(rotation.get(2)));
            }

            // Null 검사 후 부모 뼈에 묶는다
            if (parent != null) {
                modelMap.get(parent).getModelRenderer().addChild(model);
            } else {
                // 부모 뼈가 없는 모델만 그린다
                shouldRender.add(model);
            }

            // 세상에, Cubes가 비어 있을 수도 있다……
            if (bones.getCubes() == null) {
                continue;
            }

            // Cube List를 넣는다
            for (CubesItem cube : bones.getCubes()) {
                List<Float> uv = cube.getUv();
                List<Float> size = cube.getSize();
                boolean mirror = cube.isMirror();
                float inflate = cube.getInflate();

                model.cubes.add(new BedrockCubeBox(uv.get(0), uv.get(1),
                        convertOrigin(bones, cube, 0), convertOrigin(bones, cube, 1), convertOrigin(bones, cube, 2),
                        size.get(0), size.get(1), size.get(2), inflate, mirror,
                        texWidth, texHeight));
            }
        }
    }

    /**
     * 베드락판의 회전 중심 계산 방식은 자바판과 달라 변환이 필요하다
     * <p>
     * 부모 모델이 있으면
     * <li>x, z 방향: 이 모델 좌표 - 부모 모델 좌표
     * <li>y 방향: 부모 모델 좌표 - 이 모델 좌표
     * <p>
     * 부모 모델이 없으면
     * <li>x, z 방향은 그대로
     * <li>y 방향: 24 - 이 모델 좌표
     *
     * @param index xyz 중 어느 것인지. x는 0, y는 1, z는 2
     */
    protected float convertPivot(BonesItem bones, int index) {
        if (bones.getParent() != null) {
            if (index == 1) {
                return indexBones.get(bones.getParent()).getPivot().get(index) - bones.getPivot().get(index);
            } else {
                return bones.getPivot().get(index) - indexBones.get(bones.getParent()).getPivot().get(index);
            }
        } else {
            if (index == 1) {
                return 24 - bones.getPivot().get(index);
            } else {
                return bones.getPivot().get(index);
            }
        }
    }

    protected float convertPivot(BonesItem parent, CubesItem cube, int index) {
        assert cube.getPivot() != null;
        if (index == 1) {
            return parent.getPivot().get(index) - cube.getPivot().get(index);
        } else {
            return cube.getPivot().get(index) - parent.getPivot().get(index);
        }
    }

    /**
     * 베드락판과 자바판은 블록 시작 좌표도 다르다. 자바는 상대 좌표이고 y 방향도 다르다.
     * 베드락은 절대 좌표이며 y 방향이 위쪽이다.
     * 사실 규칙은 아주 간단한데, 오후 내내 찾고서야 알았다.
     * <li>x, z축이면 블록 시작 좌표에서 회전 중심 좌표를 빼면 된다
     * <li>y축이면 회전 중심 좌표에서 블록 시작 좌표를 빼고, 다시 블록의 y 길이를 뺀다
     *
     * @param index xyz 중 어느 것인지. x는 0, y는 1, z는 2
     */
    protected float convertOrigin(BonesItem bone, CubesItem cube, int index) {
        if (index == 1) {
            return bone.getPivot().get(index) - cube.getOrigin().get(index) - cube.getSize().get(index);
        } else {
            return cube.getOrigin().get(index) - bone.getPivot().get(index);
        }
    }

    protected float convertOrigin(CubesItem cube, int index) {
        assert cube.getPivot() != null;
        if (index == 1) {
            return cube.getPivot().get(index) - cube.getOrigin().get(index) - cube.getSize().get(index);
        } else {
            return cube.getOrigin().get(index) - cube.getPivot().get(index);
        }
    }

    /**
     * 베드락판은 도를, 자바판은 라디안을 쓴다. 변환은 간단하다
     */
    protected float convertRotation(float degree) {
        return (float) (degree * Math.PI / 180);
    }

    public BedrockPart getNode(String nodeName) {
        ModelRendererWrapper rendererWrapper = modelMap.get(nodeName);
        if (rendererWrapper != null) {
            return rendererWrapper.getModelRenderer();
        } else {
            return null;
        }
    }

    public BonesItem getBone(String name) {
        return indexBones.get(name);
    }

    public void render(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay) {
        render(matrixStack, transformType, renderType, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Deprecated
    public void render(PoseStack matrixStack, ItemDisplayContext transformType, RenderType renderType, int light, int overlay, float red, float green, float blue, float alpha) {
        // 26.2: Minecraft.renderBuffers()는 제거되었다. 대신 submit()이나 명시적인 VertexConsumer를 받는 renderInto()를 쓴다.
        // 대체: 폐기된 경로는 아무것도 하지 않는다. 호출하는 쪽은 submit()으로 옮겨야 한다.
    }
    public void renderInto(PoseStack poseStack, ItemDisplayContext transformType, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        for (BedrockPart model : shouldRender) { model.render(poseStack, transformType, consumer, light, overlay, red, green, blue, alpha); }
        poseStack.popPose();
        for (IFunctionalRenderer renderer : delegateRenderers) { renderer.render(poseStack, consumer, transformType, light, overlay); }
        delegateRenderers = new ArrayList<>();
    }
    public void renderInto(PoseStack poseStack, ItemDisplayContext transformType, VertexConsumer consumer, int light, int overlay) { renderInto(poseStack, transformType, consumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F); }
    public void submit(PoseStack poseStack, ItemDisplayContext transformType, SubmitNodeCollector collector, RenderType renderType, int light, int overlay, float red, float green, float blue, float alpha) {
        // 추출은 지금 일어난다. 지연 콜백은 cleanAnimationTransform()이나 다른 엔티티가 바꿀 수 있는
        // 공유 BedrockPart 객체 대신 변하지 않는 행렬을 받는다.
        BedrockRenderSnapshot snapshot = BedrockRenderSnapshot.capture(
                this, poseStack, transformType, light, overlay, red, green, blue, alpha
        );

        if (!snapshot.isEmpty()) {
            // 스냅숏의 행렬에는 들어온 아이템/엔티티 자세 전체가 이미 들어 있다.
            // 그 루트 변환을 두 번 적용하지 않도록 단위 스택에서 제출한다.
            PoseStack identity = new PoseStack();
            collector.submitCustomGeometry(identity, renderType, (entryPose, consumer) -> snapshot.write(consumer));
        }
        snapshot.submitFunctionalTasks(collector);

        // 예전 위임 렌더러는 VertexConsumer 콜백에서 중첩 RenderType을 안전하게 제출할 수 없다.
        // A3에서 collector 대응 불변 작업으로 옮긴다. 그 전까지는 제출을 넘어
        // 붙잡아 두지 않는다.
        delegateRenderers = new ArrayList<>();
    }
    public void submit(PoseStack matrixStack, ItemDisplayContext transformType, SubmitNodeCollector collector, RenderType renderType, int light, int overlay) { submit(matrixStack, transformType, collector, renderType, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F); }

    protected List<BedrockPart> getPath(@Nullable ModelRendererWrapper rendererWrapper) {
        if (rendererWrapper == null) {
            return null;
        }
        BedrockPart part = rendererWrapper.getModelRenderer();
        List<BedrockPart> path = new ArrayList<>();
        Stack<BedrockPart> stack = new Stack<>();
        do {
            stack.push(part);
            part = part.getParent();
        } while (part != null);
        while (!stack.isEmpty()) {
            part = stack.pop();
            path.add(part);
        }
        return path;
    }

    @Nullable
    public Vec3 getOffset() {
        return offset;
    }

    @Nullable
    public Vec2 getSize() {
        return size;
    }

    public List<BedrockPart> getShouldRender() {
        return shouldRender;
    }

    public HashMap<String, BonesItem> getIndexBones() {
        return indexBones;
    }
}
