package moe.caramel.chat.controller;

import moe.caramel.chat.wrapper.WrapperEditBox;
import net.minecraft.client.gui.components.EditBox;

/**
 * EditBox 컨트롤러
 */
public interface EditBoxController {

    /**
     * EditBox 래퍼를 가져온다.
     *
     * @param box EditBox 객체
     * @return 래퍼 객체
     */
    static WrapperEditBox getWrapper(final EditBox box) {
        return ((EditBoxController) box).caramelChat$wrapper();
    }

    /**
     * EditBox 래퍼를 가져온다.
     *
     * @return 래퍼 객체
     */
    WrapperEditBox caramelChat$wrapper();
}
