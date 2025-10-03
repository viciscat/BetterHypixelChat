package io.github.viciscat.bhc;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

public interface CustomLineRenderer {

    void render(TextRenderer textRenderer, DrawContext context, OrderedText text, int x, int y, int textColor, int availableWidth);
}
