package com.daqem.jobsplus.client.compat;

import com.daqem.jobsplus.JobsPlus;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.architectury.platform.Platform;
import net.minecraft.client.renderer.RenderPipelines;

import java.lang.reflect.Field;
import java.util.Map;

/** Iris에서 누락된 TTF 표지판 파이프라인만 클라이언트 초기화 시 연결한다. */
public final class IrisTtfCompat
{
    private IrisTtfCompat() {}

    public static void register()
    {
        if (!Platform.isModLoaded("iris")) return;
        try
        {
            // 선택적 연동이므로 Iris를 빌드·실행 필수 의존성으로 추가하지 않는다.
            Class<?> pipelines = Class.forName("net.irisshaders.iris.pipeline.IrisPipelines");
            Map<?, ?> main = getMappings(pipelines, "coreShaderMap");
            Map<?, ?> shadow = getMappings(pipelines, "coreShaderMapShadow");
            RenderPipeline source = RenderPipelines.TEXT_GRAYSCALE;
            RenderPipeline target = RenderPipelines.TEXT_GRAYSCALE_POLYGON_OFFSET;
            if (main.containsKey(target) && shadow.containsKey(target)) return;
            if (!main.containsKey(source) || !shadow.containsKey(source))
            {
                JobsPlus.LOGGER.warn("Could not restore Iris TTF sign rendering: grayscale source mappings are missing.");
                return;
            }
            // Iris의 공개 API가 일반 렌더링과 그림자 렌더링을 함께 등록한다.
            pipelines.getMethod("copyPipeline", RenderPipeline.class, RenderPipeline.class)
                    .invoke(null, source, target);
            JobsPlus.LOGGER.info("Restored Iris TTF sign rendering for the grayscale polygon-offset pipeline.");
        }
        catch (ReflectiveOperationException | RuntimeException | LinkageError exception)
        {
            // Iris 내부 API가 바뀌면 원인을 기록하고 나머지 클라이언트 초기화는 계속한다.
            JobsPlus.LOGGER.warn("Could not restore Iris TTF sign rendering.", exception);
        }
    }

    private static Map<?, ?> getMappings(Class<?> pipelines, String name) throws ReflectiveOperationException
    {
        Field field = pipelines.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<?, ?>) field.get(null);
    }
}
