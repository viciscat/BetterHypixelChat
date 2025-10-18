package io.github.viciscat.bhc;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public record CenteredSeparationLine(OrderedText text, int lineColor) implements CustomLineRenderer {
    private static final int THICKNESS = 1;
    @Override
    public void render(TextRenderer textRenderer, DrawContext context, OrderedText text, int x, int y, int textColor, int chatWidth) {
        int center = x + (chatWidth / 2);
        context.drawCenteredTextWithShadow(textRenderer, this.text, center, y, textColor);

        int width = textRenderer.getWidth(this.text);
        int start = x + (chatWidth - width) / 2;
        int end = start + width;

        int lineY = y + (textRenderer.fontHeight - THICKNESS) / 2;
        int color1 = ColorHelper.withAlpha(ColorHelper.channelFromFloat(ColorHelper.getAlphaFloat(this.lineColor) * ColorHelper.getAlphaFloat(textColor)), this.lineColor);
        int color2 = ColorHelper.scaleRgb(color1, 0.25f);
        context.fill(x + 2, lineY + 1, start, lineY + THICKNESS + 1, color2);
        context.fill(x + 1, lineY, start - 1, lineY + THICKNESS, color1);

        context.fill(end + 2, lineY + 1, x + chatWidth, lineY + THICKNESS + 1, color2);
        context.fill(end + 1, lineY, x + chatWidth - 1, lineY + THICKNESS, color1);

    }

    @Override
    public Style getStyleAt(TextRenderer textRenderer, int x, int mouseX, int chatWidth) {
        double offset = x + (chatWidth - textRenderer.getWidth(this.text)) / 2.0;
        return mouseX < offset ? null : textRenderer.getTextHandler().getStyleAt(this.text, MathHelper.floor(mouseX - offset));
    }
}
