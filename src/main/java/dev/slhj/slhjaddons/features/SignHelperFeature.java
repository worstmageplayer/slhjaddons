package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.calc.Calculator;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SignHelperFeature extends Feature {

    private static final int MAX_SIGN_LINE_LENGTH = 15;

    private String displayFormula = null;
    private String lastRawInput = null;
    private String lastResult = null;

    private final Setting.SliderSetting yPos;
    private final Setting.ToggleSetting requireEquals;

    public SignHelperFeature() {
        setLabel("Sign Helper");
        category(Category.SKYBLOCK);
        yPos = slider("sign_helper.ypos", "Y-Pos", 0.0, 100, 55);
        requireEquals = toggle("sign_helper.require_equals", "Require '=' prefix", false);
    }

    @Override public String id() { return "sign_helper"; }

    @Override
    public void init() {}

    public void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || displayFormula == null || displayFormula.isEmpty()) return;

        int screenWidth = g.guiWidth();
        int x = (screenWidth - RenderUtils.stringWidth(displayFormula)) / 2;
        RenderUtils.text(g, displayFormula, x, yPos.value().get().intValue(), 0xFFFFFFFF, true);
    }

    public void setDisplayFormula(String formula) {
        this.displayFormula = formula;
    }

    public void updateFromSignLine(String raw) {
        if (!isEnabled() || raw == null || raw.isBlank()) {
            displayFormula = null;
            lastRawInput = raw;
            lastResult = null;
            return;
        }

        String trimmed = raw.trim();

        if (requireEquals.value().get() && !trimmed.startsWith("=")) {
            displayFormula = null;
            lastRawInput = raw;
            lastResult = null;
            return;
        }
        String expression = trimmed.startsWith("=") ? trimmed.substring(1) : trimmed;

        if (raw.equals(lastRawInput)) return;
        lastRawInput = raw;

        try {
            var result = Calculator.calc(expression);
            String formatted = result.commas();
            lastResult = result.format();
            displayFormula = formatted.equals(expression) ? formatted : expression + " = " + formatted;
        } catch (RuntimeException e) {
            lastResult = null;
            displayFormula = null;
        }
    }

    public String getResult() {
        if (lastResult == null) {
            return lastRawInput == null ? "" : lastRawInput;
        }
        String value = lastResult;
        if (value.length() > MAX_SIGN_LINE_LENGTH) {
            value = value.substring(0, MAX_SIGN_LINE_LENGTH);
        }
        return value;
    }
}