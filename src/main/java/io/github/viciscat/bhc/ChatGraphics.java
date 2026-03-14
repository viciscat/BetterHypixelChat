package io.github.viciscat.bhc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

//? if<1.21.11 {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;*/
//? }
public class ChatGraphics {


    private final Font font;
    //? if >=1.21.11 {
    private final ChatComponent.ChatGraphicsAccess graphics;
    public boolean hovered = false;
    public ChatGraphics(ChatComponent.ChatGraphicsAccess chatGraphicsAccess) {
        this.graphics = chatGraphicsAccess;
        this.font = Minecraft.getInstance().font;
    }
    //?} else {
    /*private final GuiGraphics graphics;
    public ChatGraphics(GuiGraphics graphics, Font font) {
        this.graphics = graphics;
        this.font = font;
    }
    *///?}


    public void drawString(FormattedCharSequence component, int x, int y, float alpha) {
        //? if >=1.21.11 {
            graphics.updatePose(m -> m.translate(x, y));
            hovered |= graphics.handleMessage(0, alpha, component);
            graphics.updatePose(m -> m.translate(-x, -y));
        //?} else {
             /*graphics.drawString(font, component, x, y, ARGB.white(alpha));
        *///?}
    }

    public void drawString(Component component, int x, int y, float alpha) {
        drawString(component.getVisualOrderText(), x, y, alpha);
    }

    public void drawCenteredString(Component component, int centerX, int y, float alpha) {
        drawString(component, centerX - Minecraft.getInstance().font.width(component) / 2, y, alpha);
    }

    public void drawCenteredString(FormattedCharSequence component, int centerX, int y, float alpha) {
        drawString(component, centerX - Minecraft.getInstance().font.width(component) / 2, y, alpha);
    }

    public void fill(int x0, int y0, int x1, int y1, int color) {
        graphics.fill(x0, y0, x1, y1, color);
    }

    public int width(Component component) {
        return font.width(component);
    }

    public int width(FormattedCharSequence component) {
        return font.width(component);
    }

    public int lineHeight() {
        return font.lineHeight;
    }


}
