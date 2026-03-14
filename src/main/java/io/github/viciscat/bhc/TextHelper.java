package io.github.viciscat.bhc;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiConsumer;

public final class TextHelper {
    public static String toString(FormattedCharSequence orderedText) {
        StringBuilder stringBuilder = new StringBuilder();
        orderedText.accept((index, style, codePoint) -> {
            stringBuilder.appendCodePoint(codePoint);
            return true;
        });
        return stringBuilder.toString();
    }

    public static FormattedCharSequence trim(FormattedCharSequence orderedText) {
        String s = toString(orderedText);
        int i = StringHelper.findFirstDifferentChar(s, 0, ' ');
        int j = StringHelper.findLastDifferentChar(s, s.length() - 1, ' ');
        if (i < 0 || j < 0) return orderedText;
        return subText(orderedText, i, j);
    }

    public static Component trim(Component text) {
        String s = text.getString();
        int i = StringHelper.findFirstDifferentChar(s, 0, ' ');
        int j = StringHelper.findLastDifferentChar(s, s.length() - 1, ' ');
        if (i < 0 || j < 0) return text;
        return subText(text, i, j);
    }

    /**
     * @param start inclusive
     * @param end inclusive
     */
    public static FormattedCharSequence subText(FormattedCharSequence text, int start, int end) {
        return visitor -> new SubVisitor(visitor, start, end).start(text);
    }

    /**
     * @param start inclusive
     * @param end inclusive
     */
    public static Component subText(Component text, int start, int end) {
        MutableComponent sub = Component.empty();
        text.visit(new SubTextVisitor(start, end, (style, s) -> sub.append(Component.literal(s).setStyle(style))), Style.EMPTY);
        return sub;
    }

    public static FormattedCharSequence styleOverride(FormattedCharSequence text, Style override) {
        return visitor -> text.accept((index, style, codePoint) -> visitor.accept(index, style.applyTo(override), codePoint));
    }

    public static Optional<TextColor> getFirstColor(FormattedCharSequence text) {
        ColorGetterVisitor visitor = new ColorGetterVisitor();
        text.accept(visitor);
        return Optional.ofNullable(visitor.color);
    }

    public static Optional<TextColor> getFirstColor(Component text) {
        return text.visit((style, asString) -> Optional.ofNullable(style.getColor()), Style.EMPTY);
    }

    private static class ColorGetterVisitor implements FormattedCharSink {
        private TextColor color = null;

        @Override
        public boolean accept(int index, Style style, int codePoint) {
            if (style.getColor() != null) {
                color = style.getColor();
                return false;
            }
            return true;
        }
    }

    private static class SubVisitor implements FormattedCharSink {
        private final int start, end;
        private final FormattedCharSink visitor;
        private int i = -1;

        private SubVisitor(FormattedCharSink visitor, int start, int end) {
            this.visitor = visitor;
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean accept(int index, @NotNull Style style, int codePoint) {
            i++;
            if (i < start) return true;
            if (i > end) return false;
            return visitor.accept(index, style, codePoint);
        }

        private boolean start(FormattedCharSequence text) {
            text.accept(this);
            return i > end;
        }
    }

    private static class SubTextVisitor implements FormattedText.StyledContentConsumer<Unit> {
        private final BiConsumer<Style, String> consumer;
        private int start, length;

        private SubTextVisitor(int start, int end, BiConsumer<Style, String> consumer) {
            this.consumer = consumer;
            this.start = start;
            this.length = end - start + 1;
        }

        @Override
        public @NotNull Optional<Unit> accept(@NotNull Style style, String asString) {
            if (asString.length() <= start) {
                start -= asString.length();
                return Optional.empty();
            } else if (start != 0) {
                asString = asString.substring(start);
                start = 0;
            }
            if (asString.length() <= length) {
                length -= asString.length();
                consumer.accept(style, asString);
            } else {
                asString = asString.substring(0, length);
                consumer.accept(style, asString);
                length = 0;
            }
            return length == 0 ? Optional.of(Unit.INSTANCE) : Optional.empty();
        }
    }
}
