package dev.slhj.slhjaddons.util.render;

import dev.slhj.slhjaddons.util.client.ChatUtils;
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

    public static void text(GuiGraphicsExtractor graphics, String text, int x, int y, int argb, boolean shadow) {
        graphics.text(Minecraft.getInstance().font, ChatUtils.colorize(text), x, y, argb, shadow);
    }

    public static void drawHollowRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int thickness, int argb) {
        fill(graphics, x, y, width, thickness, argb);
        fill(graphics, x, y + height - thickness, width, thickness, argb);
        fill(graphics, x, y + thickness, thickness, height - 2 * thickness, argb);
        fill(graphics, x + width - thickness, y + thickness, thickness, height - 2 * thickness, argb);
    }

    public static void fill(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int argb) {
        graphics.fill(x, y, x + width, y + height, argb);
    }

    public static void pushScale(GuiGraphicsExtractor graphics, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
    }

    public static void pop(GuiGraphicsExtractor graphics) {
        graphics.pose().popMatrix();
    }
}
