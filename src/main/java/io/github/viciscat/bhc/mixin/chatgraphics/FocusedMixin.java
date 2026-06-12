package io.github.viciscat.bhc.mixin.chatgraphics;


import org.spongepowered.asm.mixin.Mixin;


import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import io.github.viciscat.bhc.GuiGraphicsSupplier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.UnaryOperator;
@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingFocusedGraphicsAccess")
public abstract class FocusedMixin implements GuiGraphicsSupplier {

    @Shadow
    @Final
    private GuiGraphicsExtractor graphics;

    @Shadow
    private ActiveTextCollector.Parameters parameters;

    @Override
    public @Nullable GuiGraphicsExtractor bhc$getGuiGraphics() {
        return graphics;
    }

    @Override
    public void bhc$applyParameters(UnaryOperator<ActiveTextCollector.Parameters> operator) {
        parameters = operator.apply(this.parameters);
    }
}

