package io.github.viciscat.bhc;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.math.MathHelper;

public record CenteredLine(OrderedText text) implements CustomLineRenderer {
    @Override
    public void render(TextRenderer textRenderer, DrawContext context, OrderedText text, int x, int y, int textColor, int chatWidth) {
        int center = x + (chatWidth / 2);
        context.drawCenteredTextWithShadow(textRenderer, this.text, center, y, textColor);
    }

    @Override
    public Style getStyleAt(TextRenderer textRenderer, int x, int mouseX, int chatWidth) {
        double offset = x + (chatWidth - textRenderer.getWidth(text)) / 2.0;
        return mouseX < offset ? null : textRenderer.getTextHandler().getStyleAt(text, MathHelper.floor(mouseX - offset));
    }
}
