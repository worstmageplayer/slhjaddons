package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.RenderUtils;
import dev.slhj.slhjaddons.util.ScoreboardUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class CustomScoreboard extends Feature {

    private static final String HIDDEN = "\u00a7ewww.hypixel.ne\ud83c\udf82\u00a7et"; // the promo line CT filtered

    private static final String DEFAULT_HEADER = "\u00a7e\u00a7lSKYBLOCK";
    private static final String DEFAULT_FOOTER = "\u00a7lslhj\u00a7raddons\u00a7r";
    private static final double DEFAULT_SCALE = 1.0;
    private static final int DEFAULT_PADDING = 2;
    private static final int DEFAULT_OFFSET = 1;
    private static final int DEFAULT_COLOR = 0x55000000;
    private static final boolean DEFAULT_SHADOW = true;

    private final Setting.TextSetting headerSetting;
    private final Setting.TextSetting footerSetting;
    private final Setting.SliderSetting scaleSetting;
    private final Setting.SliderSetting paddingSetting;
    private final Setting.SliderSetting offsetSetting;
    private final Setting.HexSetting colorSetting;
    private final Setting.ToggleSetting shadowSetting;

    public CustomScoreboard() {
        setLabel("Custom Scoreboard");
        category(Category.SKYBLOCK);
        headerSetting = text("scoreboard.header", "Scoreboard Header", DEFAULT_HEADER);
        footerSetting = text("scoreboard.footer", "Scoreboard Footer", DEFAULT_FOOTER);
        scaleSetting = slider("scoreboard.scale", "Scoreboard Scale", 0, 10, DEFAULT_SCALE);
        paddingSetting = intSlider("scoreboard.padding", "Scoreboard Padding", 0, 10, DEFAULT_PADDING);
        offsetSetting = intSlider("scoreboard.offset", "Scoreboard Offset", 0, 20, DEFAULT_OFFSET);
        colorSetting = hex("scoreboard.color", "Scoreboard Background Color", DEFAULT_COLOR);
        shadowSetting = toggle("scoreboard.shadow", "Text Shadow", DEFAULT_SHADOW);
    }

    public static final String id = "custom_scoreboard";
    @Override public String id() { return id; }

    @Override
    public void init() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "scoreboard"),
                (graphics, delta) -> render(graphics));
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled()) return;

        List<String> lines = new ArrayList<>();
        String title = ScoreboardUtils.title();
        boolean sb = title.toUpperCase().contains("SKYBLOCK");
        lines.add(sb ? headerSetting.value().get() : title);
        for (String l : ScoreboardUtils.lines()) {
            if (!l.equals(HIDDEN)) lines.add(l);
        }
        lines.add(footerSetting.value().get());

        float scale = scaleSetting.value().get().floatValue();
        int pad = paddingSetting.value().get().intValue();
        int offset = offsetSetting.value().get().intValue();
        boolean shadow = shadowSetting.value().get();
        int bg = colorSetting.value().get();

        int width = 0;
        for (String l : lines) width = Math.max(width, RenderUtils.stringWidth(l));
        int lineH = RenderUtils.fontHeight() + 1;
        int height = lines.size() * lineH;

        int screenW = (int) (g.guiWidth() / scale);
        int screenH = (int) (g.guiHeight() / scale);
        int x = screenW - offset - width;
        int y = (screenH - height) / 2;

        RenderUtils.pushScale(g, scale);
        RenderUtils.fill(g, x - pad, y - pad, width + 2 * pad + offset, height + 2 * pad, bg);
        int cy = y;
        for (String l : lines) {
            RenderUtils.text(g, l, x, cy, 0xFFFFFFFF, shadow);
            cy += lineH;
        }
        RenderUtils.pop(g);
    }
}