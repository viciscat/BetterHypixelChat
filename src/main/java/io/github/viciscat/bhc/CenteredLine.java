package io.github.viciscat.bhc;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

public record CenteredLine(OrderedText text) implements CustomLineRenderer {
    @Override
    public void render(TextRenderer textRenderer, DrawContext context, OrderedText text, int x, int y, int color, int availableWidth) {
        int center = x + (availableWidth / 2);
        context.drawCenteredTextWithShadow(textRenderer, this.text, center, y, color);
    }
}
