package io.github.viciscat.bhc;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextBuilder implements CharacterVisitor {
    private final List<MutableText> texts = new ArrayList<>();
    private Style style = null;
    private final StringBuilder stringBuilder = new StringBuilder();

    public TextBuilder() {
        texts.add(Text.empty());
    }

    @Override
    public boolean accept(int index, Style style, int codePoint) {
        if (this.style == null) this.style = style;

        if (!this.style.equals(style)) {
            if (!stringBuilder.isEmpty()) {
                texts.getLast().append(Text.literal(stringBuilder.toString()).setStyle(this.style));
                stringBuilder.setLength(0);
            }
            this.style = style;
        }
        if (codePoint == '\n') {
            if (!stringBuilder.isEmpty()) {
                texts.getLast().append(Text.literal(stringBuilder.toString()).setStyle(this.style));
                stringBuilder.setLength(0);
            }
            texts.add(Text.empty());
            return true;
        }
        stringBuilder.append(Character.toChars(codePoint));
        return true;
    }

    public Collection<MutableText> getTexts() {
        if (!stringBuilder.isEmpty()) {
            texts.getLast().append(Text.literal(stringBuilder.toString()).setStyle(this.style));
        }
        return texts;
    }
}
