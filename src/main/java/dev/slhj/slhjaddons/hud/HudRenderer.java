package dev.slhj.slhjaddons.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface HudRenderer {

    HudElement hudElement();

    String hudPreviewText();

    void renderHudPreview(GuiGraphicsExtractor g);
}