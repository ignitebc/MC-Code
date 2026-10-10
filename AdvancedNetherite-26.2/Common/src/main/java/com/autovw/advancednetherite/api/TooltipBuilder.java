package com.autovw.advancednetherite.api;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.api.annotation.Internal;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * 번역 가능한 툴팁을 유연하게 만드는 빌더.
 * MC 1.19에서 제거된 Tooltips API를 대신하며, 만들어지는 툴팁을 더 세밀하게 다룰 수 있다.
 *
 * @since 1.12.0
 * @author Autovw
 */
public class TooltipBuilder
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private TooltipBuilder()
    {
    }

    /**
     * 번역 가능한 툴팁을 만든다.
     * 모드 ID를 지정하지 않으면 툴팁이 기본 <i>minecraft</i> 네임스페이스로 등록된다.
     * 툴팁 이름이 비어 있으면 {@link #build(Identifier, Object...)}가 {@link IllegalStateException}을 던진다.
     *
     * 툴팁에 다른 하위 요소를 넣어야 하면 {@link #create(Identifier, Object...)}를 쓴다.
     *
     * @param key 툴팁 이름
     * @return MutableComponent
     */
    public static MutableComponent create(Identifier key)
    {
        return build(key, (Object) null);
    }

    /**
     * 번역 가능한 툴팁을 만든다.
     * 모드 ID를 지정하지 않으면 툴팁이 기본 <i>minecraft</i> 네임스페이스로 등록된다.
     * 툴팁 이름이 비어 있으면 {@link #build(Identifier, Object...)}가 {@link IllegalStateException}을 던진다.
     *
     * @param key 툴팁 이름
     * @param args 하위 요소
     * @return MutableComponent
     */
    public static MutableComponent create(Identifier key, Object... args)
    {
        return build(key, args);
    }

    /**
     * 내부 용도로만 쓰는 빌더.
     */
    @Internal
    private static MutableComponent build(Identifier key, @Nullable Object... args)
    {
        String content = "tooltip." + key.getNamespace() + "." + key.getPath();
        if (!content.endsWith("."))
        {
            if (makeArgs(args))
            {
                return Component.translatable(content, args);
            }
            return Component.translatable(content);
        }
        else
        {
            LOGGER.error("Cannot build tooltip ending with a dot (" + content + ")");
            if (!AdvancedNetherite.getPlatformHelper().isProduction())
            {
                throw new IllegalStateException("Tried to build tooltip with incomplete name!");
            }
            return Component.empty();
        }
    }

    @Internal
    private static boolean makeArgs(Object... args)
    {
        // 가변 인자에는 항상 최소 한 개의 항목이 들어 있다
        // 그래서 (args != null)은 항상 true가 되어 검사로 쓸 수 없다
        // 가변 인자를 돌면서 실제 값이 있는지 확인한다
        for (Object arg : args)
        {
            if (arg != null)
            {
                return true;
            }
        }
        return false;
    }
}
