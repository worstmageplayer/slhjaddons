package dev.slhj.slhjaddons.gui;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets the player drag/scale every HUD-editable feature's on-screen element.
 * A feature only shows up here by implementing {@link HudRenderer} - nothing
 * else needs to know about the editor. Each entry renders the feature's own
 * example preview (its real draw call, fed placeholder data) instead of a
 * generic box, so what you see here is what you'll see in-game.
 */
public final class HudEditorScreen extends Screen {

    private static final int OUTLINE_COL      = 0xAA2255CC;
    private static final int OUTLINE_COL_HOV  = 0xCC4488FF;
    private static final int OUTLINE_COL_DRAG = 0xFFFFAA00;
    private static final int TEXT_COL         = 0xFFFFFFFF;
    private static final int TAG_BG_COL       = 0xAA000000;

    /** Padding (screen px) added around a preview's text bounds for grabbing/hovering. */
    private static final int HITBOX_PADDING = 4;

    private final List<HudRenderer> renderers = new ArrayList<>();

    @Nullable private HudElement dragging = null;
    private double dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
        for (Feature feature : SlhjAddons.features().all()) {
            if (feature instanceof HudRenderer renderer) {
                renderers.add(renderer);
            }
        }
    }

    private static int elX(HudRenderer r) { return Math.round(r.hudElement().x()); }
    private static int elY(HudRenderer r) { return Math.round(r.hudElement().y()); }

    /** Preview's rendered width in real screen pixels, already accounting for its scale. */
    private int previewWidth(HudRenderer r) {
        return Math.round(this.font.width(r.hudPreviewText()) * r.hudElement().scale());
    }

    /** Preview's rendered height in real screen pixels, already accounting for its scale. */
    private int previewHeight(HudRenderer r) {
        return Math.round(this.font.lineHeight * r.hudElement().scale());
    }

    private boolean isInHitbox(HudRenderer r, double mouseX, double mouseY) {
        int hx = elX(r), hy = elY(r);
        int hw = previewWidth(r), hh = previewHeight(r);
        return mouseX >= hx - HITBOX_PADDING && mouseX <= hx + hw + HITBOX_PADDING
                && mouseY >= hy - HITBOX_PADDING && mouseY <= hy + hh + HITBOX_PADDING;
    }

    @Nullable
    private HudRenderer rendererAt(double mouseX, double mouseY) {
        for (HudRenderer r : renderers) {
            if (isInHitbox(r, mouseX, mouseY)) return r;
        }
        return null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // Dim background
        g.fill(0, 0, this.width, this.height, 0x88000000);

        // Title + hint
        g.centeredText(this.font, "HUD Editor", this.width / 2, 6, TEXT_COL);
        g.centeredText(this.font, "Drag to move  |  Scroll to scale  |  Esc to save & close",
                this.width / 2, 18, 0xFFAAAAAA);

        for (HudRenderer r : renderers) {
            HudElement el = r.hudElement();
            int hx = elX(r), hy = elY(r);
            int hw = previewWidth(r), hh = previewHeight(r);

            boolean hovered = isInHitbox(r, mouseX, mouseY);
            boolean active = dragging == el;
            int col = active ? OUTLINE_COL_DRAG : hovered ? OUTLINE_COL_HOV : OUTLINE_COL;

            // Hollow outline around the real preview (rather than a solid box
            // covering it up) so the element stays grabbable at any scale.
            int ox = hx - HITBOX_PADDING, oy = hy - HITBOX_PADDING;
            int ow = hw + HITBOX_PADDING * 2, oh = hh + HITBOX_PADDING * 2;
            g.fill(ox, oy, ox + ow, oy + 1, col);
            g.fill(ox, oy + oh - 1, ox + ow, oy + oh, col);
            g.fill(ox, oy, ox + 1, oy + oh, col);
            g.fill(ox + ow - 1, oy, ox + ow, oy + oh, col);

            // The feature's own placeholder/example render, scaled by el.scale().
            r.renderHudPreview(g);

            // Small caption tag so it's clear which feature this is while editing,
            // placed above the preview (or below, if that would go off-screen).
            String tag = el.label() + "  x" + String.format("%.2f", el.scale());
            int tagW = this.font.width(tag);
            int tagY = oy - this.font.lineHeight - 2;
            if (tagY < 32) tagY = oy + oh + 2;
            g.fill(ox, tagY - 1, ox + tagW + 4, tagY + this.font.lineHeight + 1, TAG_BG_COL);
            g.text(this.font, tag, ox + 2, tagY, TEXT_COL, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            HudRenderer r = rendererAt(event.x(), event.y());
            if (r != null) {
                dragging = r.hudElement();
                dragOffsetX = event.x() - elX(r);
                dragOffsetY = event.y() - elY(r);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null && event.button() == 0) {
            float nx = (float) (event.x() - dragOffsetX);
            float ny = (float) (event.y() - dragOffsetY);
            // Clamp to screen so elements can't be lost off-screen
            nx = Math.clamp(nx, 0, this.width);
            ny = Math.clamp(ny, 0, this.height);
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
        // Scale whichever element the mouse is over
        HudRenderer r = rendererAt(mouseX, mouseY);
        if (r != null) {
            HudElement el = r.hudElement();
            el.setScale(el.scale() + (float) scrollY * 0.1f);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        SlhjAddons.config().save();
        super.onClose();
    }

    @Override public boolean isPauseScreen() { return false; }

    public static void openHudEditor() {
        SlhjAddons.config().save();
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.setScreen(new HudEditorScreen()));
    }
}