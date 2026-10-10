package com.tacz.guns.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.function.Consumer;

/**
 * 예전 IFunctionalRenderer를 대신하는 collector 대응 구현.
 *
 * <p>구현체는 추출 단계에서 실행되어 변하지 않는 작업을 내보낸다. 작업이 실행될 때 변할 수 있는
 * PoseStack을 붙잡거나 변할 수 있는 BedrockPart/GunDisplayInstance 상태를 읽으면 안 된다.</p>
 */
public interface IFunctionalSubmitter extends IFunctionalRenderer {
    void extract(ExtractionContext context);

    /** 예전 같은 버퍼 즉시 렌더링 경로에서 실수로 쓰지 못하게 막는다. */
    @Override
    default void render(PoseStack poseStack,
                        VertexConsumer vertexBuffer,
                        ItemDisplayContext transformType,
                        int light,
                        int overlay) {
        // collector 전용 구현.
    }

    @FunctionalInterface
    interface SubmitTask {
        void submit(SubmitNodeCollector collector);
    }

    record ExtractionContext(PoseStack poseStack,
                             ItemDisplayContext displayContext,
                             int light,
                             int overlay,
                             Consumer<SubmitTask> output) {
        public ExtractionContext {
            PoseStack frozen = new PoseStack();
            frozen.last().pose().set(poseStack.last().pose());
            frozen.last().normal().set(poseStack.last().normal());
            poseStack = frozen;
        }

        public void add(SubmitTask task) {
            output.accept(task);
        }
    }
}
