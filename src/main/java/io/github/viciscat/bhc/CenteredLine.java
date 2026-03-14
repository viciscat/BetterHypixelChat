package io.github.viciscat.bhc;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
//? if <1.21.11
//import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CenteredLine(FormattedCharSequence text, @Nullable SideDecorations sideDecorations) implements CustomLineRenderer {
    private static final int SPACING = 4;
    private static final char[] CHEVRONS = new char[]{'<', '>'};
    private static final CharPredicate IS_CHEVRON = c -> c == '<' || c == '>';
    @Override
    public void render(ChatGraphics graphics, FormattedCharSequence text, int lineX, int lineY, int lineWidth, int lineHeight, int textY, float textAlpha) {
        int center = lineX + (lineWidth / 2);
        graphics.drawCenteredString(this.text, center, textY, textAlpha);
        if (sideDecorations != null) {
            int leftX = lineX + (lineWidth - sideDecorations.centerWidth) / 2 - graphics.width(sideDecorations.left) - SPACING;
            int decorationY = switch (sideDecorations.layer) {
                case TOP -> textY - Math.ceilDiv(lineHeight, 2);
                case BOTTOM -> textY + Math.floorDiv(lineHeight, 2);
                case CENTER -> textY;
            };
            graphics.drawString(sideDecorations.left, leftX, decorationY, textAlpha);
            graphics.drawString(sideDecorations.right, lineX + (lineWidth + sideDecorations.centerWidth) / 2 + SPACING, decorationY, textAlpha);
        }
    }

    //? if <1.21.11 {
    /*@Override
    public Style getStyleAt(Font textRenderer, int lineX, int mouseX, int lineWidth) {
        if (sideDecorations != null) {
            int leftX = lineX + (lineWidth - sideDecorations.centerWidth) / 2 - textRenderer.width(sideDecorations.left) - SPACING;
            int rightX = lineX + (lineWidth + sideDecorations.centerWidth) / 2 + SPACING;
            if (mouseX >= leftX) {
                Style style = textRenderer.getSplitter().componentStyleAtWidth(sideDecorations.left, mouseX - leftX);
                if (style != null) return style;
            }
            if (mouseX >= rightX) {
                Style style = textRenderer.getSplitter().componentStyleAtWidth(sideDecorations.right, mouseX - rightX);
                if (style != null) return style;
            }
        }
        double textX = lineX + (lineWidth - textRenderer.width(text)) / 2.0;
        return mouseX < textX ? null : textRenderer.getSplitter().componentStyleAtWidth(text, net.minecraft.util.Mth.floor(mouseX - textX));
    }
    *///? }

    public record SideDecorations(FormattedCharSequence left, FormattedCharSequence right, int centerWidth, Layer layer) {
        public enum Layer {
            TOP,
            CENTER,
            BOTTOM
        }
    }

    public static class Provider implements LineRendererProvider {

        @Override
        public Optional<List<Line>> getLineRenderers(Font textRenderer, Component text, String string, String trimmed, int chatWidth) {
            if (!string.startsWith(" ") || trimmed.isEmpty()) return Optional.empty();
            int firstNonSpaceChar = StringHelper.findFirstDifferentChar(string, 0, ' ');
            int lastNonSpaceChar = StringHelper.findLastDifferentChar(string, string.length() - 1, ' ');

            // Shouldn't happen but you never know
            if (firstNonSpaceChar < 0 || lastNonSpaceChar < 0) return Optional.empty();

            int middleTextStart = firstNonSpaceChar;
            int middleTextEnd = lastNonSpaceChar;

            Pair<FormattedCharSequence, FormattedCharSequence> chevrons = null;
            boolean hasChevronLeft = IS_CHEVRON.test(string.charAt(firstNonSpaceChar));
            boolean hasChevronRight = IS_CHEVRON.test(string.charAt(lastNonSpaceChar));
            FormattedCharSequence leftChevron;
            FormattedCharSequence rightChevron;
            if (hasChevronLeft) {
                int chevronEnd = StringHelper.findFirstDifferentChar(string, firstNonSpaceChar, CHEVRONS);
                if (chevronEnd < 0 || Math.abs(chevronEnd - firstNonSpaceChar) < 2) leftChevron = null; // Invalid.
                else {
                    int j = StringHelper.findFirstDifferentChar(string, chevronEnd, ' ');
                    if (j < 0) leftChevron = null;
                    else {
                        leftChevron = TextHelper.subText(text.getVisualOrderText(), firstNonSpaceChar, chevronEnd - 1);
                        middleTextStart = j;
                    }
                }
            }
            else leftChevron = FormattedCharSequence.EMPTY;

            if (hasChevronRight) {
                int chevronEnd = StringHelper.findLastDifferentChar(string, lastNonSpaceChar, CHEVRONS);
                if (chevronEnd < 0 || Math.abs(chevronEnd - lastNonSpaceChar) < 2) rightChevron = null; // Invalid.
                else {
                    int j = StringHelper.findLastDifferentChar(string, chevronEnd, ' ');
                    if (j < 0) rightChevron = null;
                    else {
                        rightChevron = TextHelper.subText(text.getVisualOrderText(), chevronEnd + 1, lastNonSpaceChar);
                        middleTextEnd = j;
                    }
                }
            }
            else rightChevron = FormattedCharSequence.EMPTY;

            if ((hasChevronLeft || hasChevronRight) && leftChevron != null && rightChevron != null)
                chevrons = Pair.of(leftChevron, rightChevron);

            int leadingSpaceWidth;
            if (chevrons != null) {
                leadingSpaceWidth = textRenderer.width(withFont(FormattedCharSequence.composite(Language.getInstance().getVisualOrder(FormattedText.of(" ".repeat(firstNonSpaceChar))), chevrons.left())));
            } else {
                leadingSpaceWidth = textRenderer.width(withFont(Component.literal(" ".repeat(firstNonSpaceChar))));
            }

            Component middleText = TextHelper.subText(text, middleTextStart, middleTextEnd);
            int middleWidth = textRenderer.width(withFont(middleText));
            int abs = Math.abs(ChatConstants.DEFAULT_WIDTH / 2 - (leadingSpaceWidth + middleWidth / 2));
            //System.out.println("abs: " + abs + " chevrons: " + leftChevron + " " + rightChevron + " middleText: " + middleText + " middleWidth: " + middleWidth);
            if (abs > 6) return Optional.empty();
            List<Line> lines = new ArrayList<>();
            // WE ARE CENTERED BABY
            int decorationsWidth;
            if (chevrons != null) {
                decorationsWidth = Math.max(textRenderer.width(chevrons.left()), textRenderer.width(chevrons.right())) * 2 + SPACING * 2;
            } else decorationsWidth = 0;
            //System.out.println("chatWidth - decorations: " + (chatWidth - decorationsWidth));
            List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(middleText, chatWidth - decorationsWidth, textRenderer).stream()
                    .map(TextHelper::trim)
                    .toList();
            int maxMiddleWidth = list.stream().mapToInt(textRenderer::width).max().orElse(middleWidth);
            boolean even = list.size() % 2 == 0;
            int middleIndex = list.size() / 2;
            for (int i = 0; i < list.size(); i++) {
                FormattedCharSequence orderedText = list.get(i);
                SideDecorations decorations;
                if (chevrons != null) {
                    if (i < middleIndex - (even ? 1 : 0)) decorations = null;
                    else if (i > middleIndex) decorations = null;
                    else {
                        decorations = new SideDecorations(chevrons.left(), chevrons.right(), maxMiddleWidth, i == middleIndex ? (even ? SideDecorations.Layer.TOP : SideDecorations.Layer.CENTER) : SideDecorations.Layer.BOTTOM);
                    }
                } else decorations = null;
                lines.add(new Line(orderedText, new CenteredLine(orderedText, decorations)));
            }

            return Optional.of(lines);
        }
    }
}
