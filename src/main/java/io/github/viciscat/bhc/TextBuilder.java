package io.github.viciscat.bhc;

import net.minecraft.util.FormattedCharSink;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TextBuilder implements FormattedCharSink {
    private final List<MutableComponent> texts = new ArrayList<>();
    private Style style = null;
    private final StringBuilder stringBuilder = new StringBuilder();
    private final int start;
    private final int end;
    private int i = -1;

    public TextBuilder(int start, int end) {
        this.start = start;
        this.end = end;
        texts.add(Component.empty());
    }

    public TextBuilder() {
        this(0, Integer.MAX_VALUE);
    }

    @Override
    public boolean accept(int index, Style style, int codePoint) {
        i++;
        if (i < start || i > end) return true;
        if (this.style == null) this.style = style;

        if (!this.style.equals(style)) {
            if (!stringBuilder.isEmpty()) {
                texts.getLast().append(Component.literal(stringBuilder.toString()).setStyle(this.style));
                stringBuilder.setLength(0);
            }
            this.style = style;
        }
        if (codePoint == '\n') {
            if (!stringBuilder.isEmpty()) {
                texts.getLast().append(Component.literal(stringBuilder.toString()).setStyle(this.style));
                stringBuilder.setLength(0);
            }
            texts.add(Component.empty());
            return true;
        }
        stringBuilder.append(Character.toChars(codePoint));
        return true;
    }

    public List<MutableComponent> getTexts() {
        if (!stringBuilder.isEmpty()) {
            texts.getLast().append(Component.literal(stringBuilder.toString()).setStyle(this.style));
        }
        return texts;
    }
}
