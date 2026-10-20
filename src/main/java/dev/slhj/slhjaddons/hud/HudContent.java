package dev.slhj.slhjaddons.hud;

import java.util.Arrays;
import java.util.List;

public record HudContent(List<String> lines, int color, int background, boolean shadow, int padding, int lineGap) {
    public HudContent {
        lines = lines.stream().flatMap(line -> Arrays.stream(line.split("\\n", -1))).toList();
        padding = Math.max(0, padding);
        lineGap = Math.max(0, lineGap);
    }
    public static HudContent text(String text, int color) {
        return new HudContent(List.of(text), color, 0, true, 0, 0);
    }
}
