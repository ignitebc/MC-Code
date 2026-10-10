package com.tacz.guns.client.renderer.snapshot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.model.FunctionalBedrockPart;
import com.tacz.guns.client.model.IFunctionalRenderer;
import com.tacz.guns.client.model.IFunctionalSubmitter;
import com.tacz.guns.client.model.IMirrorGeometry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.tacz.guns.client.model.bedrock.BedrockCube;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 변할 수 있는 BedrockModel의 제출마다 만드는 변하지 않는 스냅숏.
 *
 * <p>예전 렌더러는 공유 모델을 바꾸고 곧바로 정점을 올릴 수 있었다. 26.2에서는
 * 형상을 추출한 뒤에 소비하므로, BedrockPart 참조를 붙잡아 두면 나중의
 * cleanAnimationTransform() 호출이나 다른 엔티티 제출이 이번 프레임의 자세를 덮어쓸 수 있다.
 * 이 클래스는 추출하는 동안 모든 부품 행렬을 계산하고, 지연된 사용자 정의 형상 콜백을 위해
 * 변하지 않는 행렬과 고정된 cube 형상만 남긴다.</p>
 */
public final class BedrockRenderSnapshot {
    private final List<DrawCommand> drawCommands;
    private final List<IFunctionalSubmitter.SubmitTask> functionalTasks;
    private final int skippedFunctionalNodeCount;

    private BedrockRenderSnapshot(List<DrawCommand> drawCommands,
                                  List<IFunctionalSubmitter.SubmitTask> functionalTasks,
                                  int skippedFunctionalNodeCount) {
        this.drawCommands = List.copyOf(drawCommands);
        this.functionalTasks = List.copyOf(functionalTasks);
        this.skippedFunctionalNodeCount = skippedFunctionalNodeCount;
    }

    public static BedrockRenderSnapshot capture(BedrockModel model,
                                                 PoseStack rootPose,
                                                 ItemDisplayContext displayContext,
                                                 int light,
                                                 int overlay,
                                                 float red,
                                                 float green,
                                                 float blue,
                                                 float alpha) {
        PoseStack working = copyOf(rootPose);
        Builder builder = new Builder(displayContext, overlay, red, green, blue, alpha);
        for (BedrockPart part : model.getShouldRender()) {
            builder.capturePart(part, working, light);
        }
        return new BedrockRenderSnapshot(builder.commands, builder.functionalTasks, builder.skippedFunctionalNodes);
    }

    /**
     * 노드 하나를 루트로 형상 스냅숏을 한 번 만든다(모델 전체의 shouldRender 목록을 훑지 않는다).
     *
     * <p>조준경의 ocular / division / scope_body처럼 <b>주 렌더링 목록에 없고</b>
     * 순서대로 따로 그려야 하는 부품에 쓴다({@code BedrockAttachmentModel#submitTempPart} 참고).</p>
     *
     * <p>주의: {@code rootPose}에는 이 노드 자신과 부모 사슬의 변환이 <b>이미</b> 적용되어 있어야 하므로,
     * 여기서는 루트 노드에 다시 적용하지 않고 자식 노드로 재귀할 때만 적용한다.</p>
     */
    public static BedrockRenderSnapshot captureSubtree(BedrockPart root,
                                                       PoseStack rootPose,
                                                       ItemDisplayContext displayContext,
                                                       int light,
                                                       int overlay,
                                                       float red,
                                                       float green,
                                                       float blue,
                                                       float alpha) {
        PoseStack working = copyOf(rootPose);
        Builder builder = new Builder(displayContext, overlay, red, green, blue, alpha);
        builder.captureGeometry(root, working, light);
        return new BedrockRenderSnapshot(builder.commands, builder.functionalTasks, builder.skippedFunctionalNodes);
    }

    public boolean isEmpty() {
        return this.drawCommands.isEmpty();
    }

    /** A3 collector 이전으로 일부러 미룬 예전 기능 노드 수. */
    public int skippedFunctionalNodeCount() {
        return this.skippedFunctionalNodeCount;
    }

    public void submitFunctionalTasks(SubmitNodeCollector collector) {
        for (IFunctionalSubmitter.SubmitTask task : this.functionalTasks) {
            task.submit(collector);
        }
    }

    public void write(VertexConsumer consumer) {
        PoseStack poseStack = new PoseStack();
        for (DrawCommand command : this.drawCommands) {
            PoseStack.Pose pose = poseStack.last();
            pose.pose().set(command.pose());
            pose.normal().set(command.normal());
            for (BedrockCube cube : command.cubes()) {
                cube.compile(pose, consumer, command.light(), command.overlay(),
                        command.red(), command.green(), command.blue(), command.alpha());
            }
        }
    }

    private static PoseStack copyOf(PoseStack source) {
        PoseStack copy = new PoseStack();
        copy.last().pose().set(source.last().pose());
        copy.last().normal().set(source.last().normal());
        return copy;
    }

    public record DrawCommand(Matrix4f pose,
                              Matrix3f normal,
                              List<BedrockCube> cubes,
                              int light,
                              int overlay,
                              float red,
                              float green,
                              float blue,
                              float alpha) {
        public DrawCommand {
            pose = new Matrix4f(pose);
            normal = new Matrix3f(normal);
            cubes = List.copyOf(cubes);
        }
    }

    private static final class Builder {
        private final ItemDisplayContext displayContext;
        private final int overlay;
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;
        private final List<DrawCommand> commands = new ArrayList<>();
        private final List<IFunctionalSubmitter.SubmitTask> functionalTasks = new ArrayList<>();
        private int skippedFunctionalNodes;

        private Builder(ItemDisplayContext displayContext,
                        int overlay,
                        float red,
                        float green,
                        float blue,
                        float alpha) {
            this.displayContext = displayContext;
            this.overlay = overlay;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        /**
         * <b>현재 행렬</b> 아래에서 part 자신과 하위 트리의 형상을 모은다.
         * capturePart와의 차이: part 자신에게 translateAndRotateAndScale을 다시 적용하지 않고
         * (호출 지점에서 이미 적용함) 자식 노드로 재귀할 때만 적용한다.
         */
        private void captureGeometry(BedrockPart part, PoseStack poseStack, int inheritedLight) {
            if (!part.visible) {
                return;
            }
            int partLight = part.illuminated ? 15728880 : inheritedLight;
            if (!part.cubes.isEmpty()) {
                PoseStack.Pose current = poseStack.last();
                this.commands.add(new DrawCommand(
                        current.pose(), current.normal(), part.cubes,
                        partLight, this.overlay, this.red, this.green, this.blue, this.alpha));
            }
            for (BedrockPart child : part.children) {
                capturePart(child, poseStack, partLight);
            }
        }

        private void capturePart(BedrockPart part, PoseStack poseStack, int inheritedLight) {
            int partLight = part.illuminated ? 15728880 : inheritedLight;

            // FunctionalBedrockPart는 visible이 false여도 provider를 항상 평가한다.
            // null을 돌려주는 provider는 표시/상태 훅이라 평소처럼 스냅숏할 수 있다.
            // renderer를 돌려주는 provider는 collector 대응 A3 구현이 필요해 여기서는 실행하지 않는다.
            IFunctionalRenderer legacyFunctionalRenderer = null;
            if (part instanceof FunctionalBedrockPart functional && functional.functionalRenderer != null) {
                legacyFunctionalRenderer = functional.functionalRenderer.apply(part);
            }

            poseStack.pushPose();
            part.translateAndRotateAndScale(poseStack);

            if (legacyFunctionalRenderer != null) {
                if (legacyFunctionalRenderer instanceof IFunctionalSubmitter submitter) {
                    submitter.extract(new IFunctionalSubmitter.ExtractionContext(
                            poseStack,
                            this.displayContext,
                            partLight,
                            this.overlay,
                            this.functionalTasks::add
                    ));
                    poseStack.popPose();
                    return;
                } else if (legacyFunctionalRenderer instanceof IMirrorGeometry mirror) {
                    // 이 노드의 변환 아래에서 먼저 자신(하위 트리 포함)을 그리고, 거울로 지정한 노드도 한 번 더 그린다.
                    // additional_magazine에 쓴다: 총몸의 탄창과 손을 따라가는 탄창이 같은 메시를 함께 쓴다.
                    // 이 경로를 타면 총몸과 같은 RenderType / DrawCommand 묶음을 써서 재질과 순서가 맞는다.
                    if (part.visible) {
                        captureGeometry(part, poseStack, partLight);
                        BedrockPart mirrored = mirror.getMirroredPart();
                        if (mirrored != null && mirrored.visible) {
                            captureGeometry(mirrored, poseStack, partLight);
                        }
                    }
                    poseStack.popPose();
                    return;
                } else {
                    this.skippedFunctionalNodes++;
                    poseStack.popPose();
                    return;
                }
            }

            if (part.visible) {
                if (!part.cubes.isEmpty()) {
                    PoseStack.Pose current = poseStack.last();
                    this.commands.add(new DrawCommand(
                            current.pose(),
                            current.normal(),
                            part.cubes,
                            partLight,
                            this.overlay,
                            this.red,
                            this.green,
                            this.blue,
                            this.alpha
                    ));
                }
                for (BedrockPart child : part.children) {
                    capturePart(child, poseStack, partLight);
                }
            }

            poseStack.popPose();
        }
    }
}
