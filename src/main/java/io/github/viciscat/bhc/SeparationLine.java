package io.github.viciscat.bhc;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.ColorHelper;

public record SeparationLine(int color, int thickness) implements CustomLineRenderer {
    @Override
    public void render(TextRenderer textRenderer, DrawContext context, OrderedText text, int x, int y, int textColor, int availableWidth) {
        int i = y + (textRenderer.fontHeight - thickness) / 2;
        int color1 = ColorHelper.withAlpha(ColorHelper.channelFromFloat(ColorHelper.getAlphaFloat(this.color) * ColorHelper.getAlphaFloat(textColor)), this.color);
        context.fill(x + 2, i + 1, x + availableWidth, i + thickness + 1, ColorHelper.scaleRgb(color1, 0.25f));
        context.fill(x + 1, i, x + availableWidth - 1, i + thickness, color1);
    }
}
