package io.github.viciscat.bhc;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class TextBuilder implements CharacterVisitor {
    private final MutableText text = Text.empty();
    private Style style = null;
    private final StringBuilder stringBuilder = new StringBuilder();


    @Override
    public boolean accept(int index, Style style, int codePoint) {
        if (this.style == null) {
            this.style = style;
            this.stringBuilder.append(Character.toChars(codePoint));
            return true;
        }
        if (!this.style.equals(style)) {
            text.append(Text.literal(stringBuilder.toString()).setStyle(this.style));
            stringBuilder.setLength(0);
            this.style = style;
        }
        stringBuilder.append(Character.toChars(codePoint));
        return true;
    }

    public MutableText getText() {
        if (!stringBuilder.isEmpty()) {
            text.append(Text.literal(stringBuilder.toString()).setStyle(this.style));
        }
        return text;
    }
}
