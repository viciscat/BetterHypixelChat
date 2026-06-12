package io.github.viciscat.bhc;

import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.jspecify.annotations.Nullable;

public interface ChatComponentAccess {

    int bhc$getScaledWidth();
    @Nullable CustomLineRenderer bhc$getCustomLineRenderer(GuiMessage.Line line);
}
