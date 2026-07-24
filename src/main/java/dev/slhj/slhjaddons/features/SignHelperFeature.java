package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.calc.Calculator;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class SignHelperFeature extends Feature {

    private String displayFormula = null;

    public SignHelperFeature() {
        setLabel("Sign Helper");
        category(Category.SKYBLOCK);
    }

    @Override public String id() { return "sign_helper"; }

    @Override
    public void init() {}

    public void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || displayFormula == null || displayFormula.isEmpty()) return;

        int screenWidth = g.guiWidth();
        int x = (screenWidth - RenderUtils.stringWidth(displayFormula)) / 2;
        RenderUtils.text(g, displayFormula, x, 55, 0xFFFFFFFF, true);
    }

    public void setDisplayFormula(String formula) {
        this.displayFormula = formula;
    }

    public void updateFromSignLine(String raw) {
        if (!isEnabled() || raw == null || raw.isBlank()) {
            displayFormula = null;
            return;
        }

        String trimmed = raw.trim();
        try {
            var result = Calculator.calc(trimmed);
            String formatted = result.commas();
            displayFormula = formatted.equals(trimmed) ? formatted : trimmed + " = " + formatted;
        } catch (RuntimeException e) {
            displayFormula = null;
        }
    }
}