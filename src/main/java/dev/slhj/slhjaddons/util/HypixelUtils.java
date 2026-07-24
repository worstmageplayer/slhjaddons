package dev.slhj.slhjaddons.util;

public final class HypixelUtils {
    private HypixelUtils() {}

    public static boolean inSkyblock() {
        return ScoreboardUtils.title().toUpperCase().contains("SKYBLOCK");
    }

    public static boolean inDungeon() {
        if (!inSkyblock()) return false;
        for (String line : ScoreboardUtils.linesNoFormat()) {
            if (line.contains("The Catacombs")) return true;
        }
        return false;
    }
}
