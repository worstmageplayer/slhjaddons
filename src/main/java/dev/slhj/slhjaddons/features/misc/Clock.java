package dev.slhj.slhjaddons.features.misc;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.hud.HudAnchor;
import dev.slhj.slhjaddons.hud.HudContent;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudPlacement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Clock extends Feature {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("H : mm");

    private static final int DEFAULT_COLOR = 0xFFFFFFFF;

    private final Setting.HexSetting colorSetting;

    public Clock() {
        setLabel("Clock");
        category(Category.MISC);
        colorSetting = hex("clock.color", "Clock Color", DEFAULT_COLOR);
    }

    public static final String id = "clock";
    @Override public String id() { return id; }

    @Override
    public void init() {
        SlhjAddons.hud().register(new HudElement(this, "clock", "Clock",
                new HudPlacement(HudAnchor.TOP_RIGHT, HudAnchor.TOP_RIGHT, -5, 5, 1), this::content, this::content));
    }

    private HudContent content() {
        return HudContent.text(LocalTime.now().format(FMT), colorSetting.value().get());
    }
}
