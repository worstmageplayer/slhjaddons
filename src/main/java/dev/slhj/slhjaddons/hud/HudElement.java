package dev.slhj.slhjaddons.hud;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.util.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HudElement {

    private final String id;
    private final String label;
    private final SlhjConfig.HudPos pos;

    public HudElement(String id, String label, float defaultX, float defaultY) {
        this.id = id;
        this.label = label;
        this.pos = SlhjAddons.config().hud(id, defaultX, defaultY);
    }

    public String id()    { return id; }
    public String label() { return label; }
    public float x()      { return pos.x; }
    public float y()      { return pos.y; }
    public float scale()  { return pos.scale; }

    public void move(float x, float y) {
        pos.x = x;
        pos.y = y;
    }

    public void setScale(float s) {
        pos.scale = Math.clamp(s, 0.25f, 4f);
    }

    public void renderText(GuiGraphicsExtractor g, String text, int color) {
        RenderUtils.pushScale(g, scale());
        RenderUtils.text(g, text, (int) (x() / scale()), (int) (y() / scale()), color, true);
        RenderUtils.pop(g);
    }
}