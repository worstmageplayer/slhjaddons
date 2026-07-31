package dev.slhj.slhjaddons.features.skyblock;

import dev.slhj.slhjaddons.calc.Calculator;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

public final class SignHelper extends Feature {

    private static final int MAX_SIGN_LINE_LENGTH = 15;

    private String displayFormula = null;
    private String lastRawInput = null;
    private String lastResult = null;

    private final Setting.SliderSetting yPos;

    public final static String INPUT_SIGN_MARKER = "^^^^^^^^^^^^^^^";

    public SignHelper() {
        setLabel("Sign Helper");
        category(Category.SKYBLOCK);
        yPos = intSlider("sign_helper.ypos", "Y-Pos", 0, 100, 55);
    }

    public static final String id = "sign_helper";
    @Override public String id() { return id; }

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

    @Nullable
    public String getResult() {
        if (lastResult == null) {
            return lastRawInput == null ? null : lastRawInput;
        }
        String value = lastResult;
        if (value.length() > MAX_SIGN_LINE_LENGTH) {
            value = value.substring(0, MAX_SIGN_LINE_LENGTH);
        }
        return value;
    }
}