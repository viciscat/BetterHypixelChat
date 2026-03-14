package io.github.viciscat.bhc;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public interface CustomLineRenderer {

    void render(ChatGraphics graphics, FormattedCharSequence text, int lineX, int lineY, int lineWidth, int lineHeight, int textY, float textAlpha);

    default @Nullable Style getStyleAt(Font textRenderer, int lineX, int mouseX, int lineWidth) {
        return null;
    }
}
