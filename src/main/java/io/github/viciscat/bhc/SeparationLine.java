package io.github.viciscat.bhc;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.List;
import java.util.Optional;

public record SeparationLine(int color, int thickness) implements CustomLineRenderer {
    @Override
    public void render(ChatGraphics graphics, FormattedCharSequence text, int lineX, int lineY, int lineWidth, int lineHeight, int textY, float textAlpha) {
        int i = textY + (graphics.lineHeight() - thickness) / 2;
        int color1 = ARGB.color(ARGB.as8BitChannel(ARGB.alphaFloat(this.color) * textAlpha), this.color);
        graphics.fill(lineX + 2, i + 1, lineX + lineWidth, i + thickness + 1, ARGB.scaleRGB(color1, 0.25f));
        graphics.fill(lineX + 1, i, lineX + lineWidth - 1, i + thickness, color1);
    }


    public static class Provider implements LineRendererProvider {

        @Override
        public Optional<List<Line>> getLineRenderers(Font textRenderer, Component text, String string, String trimmed, int chatWidth) {
            if (trimmed.length() < 5) return Optional.empty();
            boolean wide = false;
            if (!(trimmed.chars().allMatch(c -> c == '-' || c == '—') || (wide = trimmed.chars().allMatch(c -> c == '▬')))) return Optional.empty();
            return Optional.of(List.of(new Line(
                    text.getVisualOrderText(),
                    new SeparationLine(TextHelper.getFirstColor(text).map(color -> ARGB.opaque(color.getValue())).orElse(-1), wide ? 3 : 1)
                    )));
        }
    }
}
