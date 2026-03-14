package io.github.viciscat.bhc;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class ChatConstants {
    public static final int DEFAULT_WIDTH = 320;
    /**
     * This is the font Hypixel assumes
     */
    public static final Identifier VANILLA_FONT = Identifier.withDefaultNamespace("vanilla_default");

    public static final Style VANILLA_FONT_STYLE = Style.EMPTY.withFont(new FontDescription.Resource(VANILLA_FONT));
}
