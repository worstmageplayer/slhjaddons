package dev.slhj.slhjaddons.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class RenderUtils {
    private RenderUtils() {}

    public static int stringWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    public static int fontHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    /** color is 0xAARRGGBB. */
    public static void text(GuiGraphicsExtractor g, String text, int x, int y, int color, boolean shadow) {
        g.text(Minecraft.getInstance().font, McUtils.colorize(text), x, y, color, shadow);
    }

    public static void drawHollowRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int thickness, int argb) {
        fill(g, x, y, w, thickness, argb);                                   // top
        fill(g, x, y + h - thickness, w, thickness, argb);                   // bottom
        fill(g, x, y + thickness, thickness, h - 2 * thickness, argb);       // left
        fill(g, x + w - thickness, y + thickness, thickness, h - 2 * thickness, argb); // right
    }

    public static void fill(GuiGraphicsExtractor g, int x, int y, int w, int h, int argb) {
        g.fill(x, y, x + w, y + h, argb);
    }

    public static void pushScale(GuiGraphicsExtractor g, float scale) {
        g.pose().pushMatrix();
        g.pose().scale(scale, scale);
    }

    public static void pop(GuiGraphicsExtractor g) {
        g.pose().popMatrix();
    }
}
