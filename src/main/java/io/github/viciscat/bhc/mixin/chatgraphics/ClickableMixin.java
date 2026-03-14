package io.github.viciscat.bhc.mixin.chatgraphics;

import io.github.viciscat.bhc.GuiGraphicsSupplier;
import org.spongepowered.asm.mixin.Mixin;


//? if >=1.21.11 {
import net.minecraft.client.gui.ActiveTextCollector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.UnaryOperator;
@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$ClickableTextOnlyGraphicsAccess")
public abstract class ClickableMixin implements GuiGraphicsSupplier {

    @Shadow
    @Final
    private ActiveTextCollector output;

    @Override
    public void bhc$applyParameters(UnaryOperator<ActiveTextCollector.Parameters> operator) {
        output.defaultParameters(operator.apply(output.defaultParameters()));
    }
}
//? } else {
/*@Mixin(net.minecraft.client.Minecraft.class)
public class ClickableMixin {}

*///? }
