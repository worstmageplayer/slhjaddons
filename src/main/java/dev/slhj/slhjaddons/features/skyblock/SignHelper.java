package dev.slhj.slhjaddons.features.skyblock;

import dev.slhj.slhjaddons.calc.Calculator;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SignHelper extends Feature {

    private final SignInputState input = new SignInputState(expression -> {
        var result = Calculator.calc(expression);
        return new SignInputState.Result(result.format(), result.commas());
    });

    private final Setting.SliderSetting yPos;

    public final static String INPUT_SIGN_MARKER = "^^^^^^^^^^^^^^^";

    public SignHelper() {
        setLabel("Sign Helper");
        setDescription("Evaluate expressions entered into supported SkyBlock input signs.");
        category(Category.SKYBLOCK);
        yPos = intSlider("sign_helper.ypos", "Vertical Position (GUI px)", 0, 100, 55);
    }

    public static final String id = "sign_helper";
    @Override public String id() { return id; }

    @Override
    public void init() {}

    public void render(GuiGraphicsExtractor g) {
        String displayFormula = input.display();
        if (!isEnabled() || displayFormula == null || displayFormula.isEmpty()) return;

        int screenWidth = g.guiWidth();
        int x = (screenWidth - RenderUtils.stringWidth(displayFormula)) / 2;
        RenderUtils.text(g, displayFormula, x, yPos.value().get().intValue(), 0xFFFFFFFF, true);
    }

    public void resetSign() { input.reset(); }

    public void updateFromSignLine(String raw) {
        if (!isEnabled()) { input.reset(); return; }
        input.update(raw);
    }

    public String getResult() { return input.submission(); }

    @Override protected void onDisable() { input.reset(); }
}
