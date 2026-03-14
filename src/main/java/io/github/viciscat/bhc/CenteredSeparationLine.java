package io.github.viciscat.bhc;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CenteredSeparationLine(FormattedCharSequence text, int lineColor, int middleWidth, Layer layer) implements CustomLineRenderer {
    private static final int THICKNESS = 1;
    @Override
    public void render(ChatGraphics graphics, FormattedCharSequence text, int lineX, int lineY, int lineWidth, int lineHeight, int textY, float textAlpha) {
        int center = lineX + (lineWidth / 2);
        graphics.drawCenteredString(this.text, center, textY, textAlpha);

        int start = lineX + (lineWidth - middleWidth) / 2;
        int end = start + middleWidth;

        int sepY = textY + (graphics.lineHeight() - THICKNESS) / 2 + switch (layer) {
            case CENTER -> 0;
            case TOP -> -Math.ceilDiv(lineHeight, 2);
            case BOTTOM -> Math.floorDiv(lineHeight, 2);
        };
        int color1 = ARGB.color(ARGB.as8BitChannel(ARGB.alphaFloat(this.lineColor) * textAlpha), this.lineColor);
        int color2 = ARGB.scaleRGB(color1, 0.25f);
        graphics.fill(lineX + 2, sepY + 1, start, sepY + THICKNESS + 1, color2);
        graphics.fill(lineX + 1, sepY, start - 1, sepY + THICKNESS, color1);

        graphics.fill(end + 2, sepY + 1, lineX + lineWidth, sepY + THICKNESS + 1, color2);
        graphics.fill(end + 1, sepY, lineX + lineWidth - 1, sepY + THICKNESS, color1);
    }

    //? if <1.21.11 {
    /*@Override
    public net.minecraft.network.chat.Style getStyleAt(Font textRenderer, int x, int mouseX, int chatWidth) {
        double offset = x + (chatWidth - textRenderer.width(this.text)) / 2.0;
        return mouseX < offset ? null : textRenderer.getSplitter().componentStyleAtWidth(this.text, net.minecraft.util.Mth.floor(mouseX - offset));
    }
    *///? }

    public enum Layer {
        TOP,
        CENTER,
        BOTTOM
    }

    public static class Provider implements LineRendererProvider {

        @Override
        public Optional<List<Line>> getLineRenderers(Font textRenderer, Component text, String string, String trimmed, int chatWidth) {
            if (!trimmed.startsWith("-") || !trimmed.endsWith("-") || textRenderer.width(withFont(text)) <= ChatConstants.DEFAULT_WIDTH - 10) return Optional.empty();
            int separationEnd = StringHelper.findFirstDifferentChar(string, 0, '-');
            int separationStart = StringHelper.findLastDifferentChar(string, string.length() - 1, '-');

            if (separationEnd < 0 || separationStart < 0) return Optional.empty();

            Component middleText = TextHelper.subText(text, separationEnd, separationStart);

            int middleWidth = textRenderer.width(withFont(middleText));
            int leadingWidth = textRenderer.width(withFont(TextHelper.subText(text.getVisualOrderText(), 0, separationEnd - 1)));
            int abs = Math.abs(ChatConstants.DEFAULT_WIDTH / 2 - (leadingWidth + middleWidth / 2));
            if (abs > 6) return Optional.empty();

            List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(middleText, chatWidth, textRenderer).stream().map(TextHelper::trim).toList();
            int maxMiddleWidth = list.stream().mapToInt(textRenderer::width).max().orElse(chatWidth);
            List<Line> lines = new ArrayList<>();

            boolean even = list.size() % 2 == 0;
            int middleIndex = list.size() / 2;
            int color = TextHelper.getFirstColor(text).map(textColor -> ARGB.opaque(textColor.getValue())).orElse(-1);
            for (int i = 0; i < list.size(); i++) {
                FormattedCharSequence orderedText = list.get(i);
                if (i < middleIndex - (even ? 1 : 0) || i > middleIndex) {
                    lines.add(new Line(orderedText, new CenteredLine(orderedText, null)));
                } else {
                    lines.add(new Line(orderedText, new CenteredSeparationLine(orderedText, color, maxMiddleWidth, i == middleIndex ? (even ? Layer.TOP : Layer.CENTER) : Layer.BOTTOM)));
                }
            }


            return Optional.of(lines);
        }
    }
}
