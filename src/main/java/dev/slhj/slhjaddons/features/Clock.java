package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ClockFeature extends Feature {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("H : mm");

    private static final double DEFAULT_SCALE = 1.0;
    private static final int DEFAULT_PADDING = 5;
    private static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static final boolean DEFAULT_RIGHT_ALIGNED = true;

    private final Setting.SliderSetting scaleSetting;
    private final Setting.SliderSetting paddingSetting;
    private final Setting.HexSetting colorSetting;
    private final Setting.ToggleSetting rightAlignedSetting;

    public ClockFeature() {
        setLabel("Clock");
        category(Category.MISC);
        scaleSetting = slider("clock.scale", "Clock Scale", 0, 10, DEFAULT_SCALE);
        paddingSetting = intSlider("clock.padding", "Clock Padding", 0, 10, DEFAULT_PADDING);
        colorSetting = hex("clock.color", "Clock Color", DEFAULT_COLOR);
        rightAlignedSetting = toggle("clock.right_aligned", "Right Aligned", DEFAULT_RIGHT_ALIGNED);
    }

    @Override public String id() { return "clock"; }

    @Override
    public void init() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "clock"),
                (graphics, delta) -> render(graphics));
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled()) return;

        String time = LocalTime.now().format(FMT);
        float scale = scaleSetting.value().get().floatValue();
        int padding = paddingSetting.value().get().intValue();
        int color = colorSetting.value().get();
        boolean right = rightAlignedSetting.value().get();

        int screenW = g.guiWidth();
        int w = RenderUtils.stringWidth(time);

        int x = right ? (int) ((screenW - w * scale - padding) / scale) : (int) (padding / scale);
        int y = (int) (padding / scale);

        RenderUtils.pushScale(g, scale);
        RenderUtils.text(g, time, x, y, color, true);
        RenderUtils.pop(g);
    }
}