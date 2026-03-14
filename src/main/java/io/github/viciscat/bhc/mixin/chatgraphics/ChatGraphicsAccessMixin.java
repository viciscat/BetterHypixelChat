package io.github.viciscat.bhc.mixin.chatgraphics;

import org.spongepowered.asm.mixin.Mixin;


//? if >=1.21.11 {

import io.github.viciscat.bhc.GuiGraphicsSupplier;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;
@Mixin(ChatComponent.ChatGraphicsAccess.class)
public interface ChatGraphicsAccessMixin extends GuiGraphicsSupplier, ChatComponent.ChatGraphicsAccess {

    @Override
    default @Nullable GuiGraphics bhc$getGuiGraphics() {
        return null;
    }

    @Override
    default void bhc$applyParameters(UnaryOperator<ActiveTextCollector.Parameters> consumer) {

    }
}
//? } else {
/*@Mixin(net.minecraft.client.Minecraft.class)
public class ChatGraphicsAccessMixin {}

*///? }
