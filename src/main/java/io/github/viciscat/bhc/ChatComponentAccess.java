package io.github.viciscat.bhc;

import net.minecraft.client.GuiMessage;
import org.jetbrains.annotations.Nullable;

public interface ChatComponentAccess {

    int bhc$getScaledWidth();
    @Nullable CustomLineRenderer bhc$getCustomLineRenderer(GuiMessage.Line line);
}
