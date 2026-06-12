package io.github.viciscat.bhc.mixin.chatgraphics;

import org.spongepowered.asm.mixin.Mixin;


import io.github.viciscat.bhc.GuiGraphicsSupplier;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import org.jspecify.annotations.Nullable;

import java.util.function.UnaryOperator;
@Mixin(ChatComponent.ChatGraphicsAccess.class)
public interface ChatGraphicsAccessMixin extends GuiGraphicsSupplier, ChatComponent.ChatGraphicsAccess {

    @Override
    default @Nullable GuiGraphicsExtractor bhc$getGuiGraphics() {
        return null;
    }

    @Override
    default void bhc$applyParameters(UnaryOperator<ActiveTextCollector.Parameters> consumer) {

    }
}
