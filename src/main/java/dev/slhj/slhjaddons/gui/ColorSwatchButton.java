package dev.slhj.slhjaddons.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.IntSupplier;

/** Standard button chrome with a checkerboard showing the color's actual opacity. */
final class ColorSwatchButton extends Button {
    private final IntSupplier color;

    ColorSwatchButton(IntSupplier color, Runnable openPicker) {
        super(0, 0, 20, 20, Component.literal("Choose color"), b -> openPicker.run(), DEFAULT_NARRATION);
        this.color = color;
        setTooltip(Tooltip.create(Component.literal("Choose color and opacity")));
    }

    @Override protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        extractDefaultSprite(g);
        preview(g, getX() + 4, getY() + 4, getWidth() - 8, getHeight() - 8, color.getAsInt());
    }

    static void preview(GuiGraphicsExtractor g, int x, int y, int width, int height, int color) {
        for (int dy = 0; dy < height; dy += 4) for (int dx = 0; dx < width; dx += 4)
            g.fill(x + dx, y + dy, x + Math.min(width, dx + 4), y + Math.min(height, dy + 4),
                    ((dx + dy) / 4) % 2 == 0 ? 0xFFAAAAAA : 0xFF666666);
        g.fill(x, y, x + width, y + height, color);
    }
}
