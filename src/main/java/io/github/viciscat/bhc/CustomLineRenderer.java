package io.github.viciscat.bhc;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

public interface CustomLineRenderer {

    void render(TextRenderer textRenderer, DrawContext context, OrderedText text, int x, int y, int textColor, int chatWidth);

    Style getStyleAt(TextRenderer textRenderer, int x, int mouseX, int chatWidth);
}
