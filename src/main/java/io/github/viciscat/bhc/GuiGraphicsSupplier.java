package io.github.viciscat.bhc;

//? if >=1.21.11
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public interface GuiGraphicsSupplier {

    @Nullable GuiGraphics bhc$getGuiGraphics();

    //? if >=1.21.11 {
    void bhc$applyParameters(UnaryOperator<ActiveTextCollector.Parameters> consumer);

    static GuiGraphicsSupplier of(ChatComponent.ChatGraphicsAccess chatGraphicsAccess) {
        return (GuiGraphicsSupplier) chatGraphicsAccess;
    }
    //? }
}
