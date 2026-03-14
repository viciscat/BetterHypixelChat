package io.github.viciscat.bhc;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface LineRendererProvider {

    Optional<List<Line>> getLineRenderers(Font textRenderer, Component text, String string, String trimmed, int chatWidth);

    default MutableComponent withFont(Component text) {
        return Component.empty().setStyle(ChatConstants.VANILLA_FONT_STYLE).append(text);
    }

    default FormattedCharSequence withFont(FormattedCharSequence text) {
        return TextHelper.styleOverride(text, ChatConstants.VANILLA_FONT_STYLE);
    }


    record Line(FormattedCharSequence text, @Nullable CustomLineRenderer renderer) implements Pair<FormattedCharSequence, CustomLineRenderer> {

        @Override
        public FormattedCharSequence left() {
            return text;
        }

        @Override
        public CustomLineRenderer right() {
            return renderer;
        }
    }
}
