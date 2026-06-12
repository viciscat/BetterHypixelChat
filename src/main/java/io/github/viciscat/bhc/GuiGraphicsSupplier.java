package io.github.viciscat.bhc;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import org.jspecify.annotations.Nullable;

import java.util.function.UnaryOperator;

public interface GuiGraphicsSupplier {

    @Nullable GuiGraphicsExtractor bhc$getGuiGraphics();

    void bhc$applyParameters(UnaryOperator<ActiveTextCollector.Parameters> consumer);

    static GuiGraphicsSupplier of(ChatComponent.ChatGraphicsAccess chatGraphicsAccess) {
        return (GuiGraphicsSupplier) chatGraphicsAccess;
    }
}
