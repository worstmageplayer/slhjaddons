package dev.slhj.slhjaddons.gui;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.util.McUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class HudEditorScreen extends Screen {

    private static final int HANDLE_W = 80;
    private static final int HANDLE_H = 16;
    private static final int HANDLE_COL       = 0xAA2255CC;
    private static final int HANDLE_COL_HOV   = 0xCC4488FF;
    private static final int HANDLE_COL_DRAG  = 0xFFFFAA00;
    private static final int TEXT_COL         = 0xFFFFFFFF;

    @Nullable private HudElement dragging = null;
    private double dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    private static int screenX(HudElement el) { return Math.round(el.x()); }
    private static int screenY(HudElement el) { return Math.round(el.y()); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // Dim background
        g.fill(0, 0, this.width, this.height, 0x88000000);

        // Title + hint
        g.centeredText(this.font, "HUD Editor", this.width / 2, 6, TEXT_COL);
        g.centeredText(this.font, "Drag to move  |  Scroll to scale  |  Esc to save & close",
                this.width / 2, 18, 0xFFAAAAAA);

        for (HudElement el : HudElement.ALL) {
            int hx = screenX(el);
            int hy = screenY(el);
            boolean hovered = mouseX >= hx && mouseX <= hx + HANDLE_W
                    && mouseY >= hy && mouseY <= hy + HANDLE_H;
            boolean active = dragging == el;
            int col = active ? HANDLE_COL_DRAG : hovered ? HANDLE_COL_HOV : HANDLE_COL;

            g.fill(hx, hy, hx + HANDLE_W, hy + HANDLE_H, col);
            g.fill(hx, hy, hx + HANDLE_W, hy + 1, 0xFFFFFFFF);
            g.fill(hx, hy + HANDLE_H - 1, hx + HANDLE_W, hy + HANDLE_H, 0xFFFFFFFF);

            String label = el.label() + " x" + String.format("%.2f", el.scale());
            String clipped = this.font.plainSubstrByWidth(label, HANDLE_W - 4);
            String text = clipped + " " + String.format("%.1f", el.scale()) + "x";
            g.text(this.font, text, hx + 2, hy + (HANDLE_H - this.font.lineHeight) / 2, TEXT_COL, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (HudElement el : HudElement.ALL) {
                int hx = screenX(el), hy = screenY(el);
                if (event.x() >= hx && event.x() <= hx + HANDLE_W
                        && event.y() >= hy && event.y() <= hy + HANDLE_H) {
                    dragging = el;
                    dragOffsetX = event.x() - hx;
                    dragOffsetY = event.y() - hy;
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null && event.button() == 0) {
            float nx = (float) (event.x() - dragOffsetX);
            float ny = (float) (event.y() - dragOffsetY);
            // Clamp to screen so handles can't be lost off-screen
            nx = Math.clamp(nx, 0, this.width - HANDLE_W);
            ny = Math.clamp(ny, 0, this.height - HANDLE_H);
            dragging.move(nx, ny);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && dragging != null) {
            dragging = null;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scale whichever handle the mouse is over
        for (HudElement el : HudElement.ALL) {
            int hx = screenX(el), hy = screenY(el);
            if (mouseX >= hx && mouseX <= hx + HANDLE_W
                    && mouseY >= hy && mouseY <= hy + HANDLE_H) {
                el.setScale(el.scale() + (float) scrollY * 0.1f);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        SlhjAddons.config().save();
        super.onClose();
    }

    @Override public boolean isPauseScreen() { return false; }
}
