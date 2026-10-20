package dev.slhj.slhjaddons.hud;

import java.util.List;

/** Prepared data shared by measurement and drawing, never a second HUD element. */
public record HudContent(List<String> lines, int color, int background, boolean shadow, int padding, int lineGap) {
    public HudContent {
        lines = lines.stream().flatMap(line -> java.util.Arrays.stream(line.split("\\n", -1))).toList();
        padding = Math.max(0, padding);
        lineGap = Math.max(0, lineGap);
    }
    public static HudContent text(String text, int color) {
        return new HudContent(List.of(text), color, 0, true, 0, 0);
    }
}
