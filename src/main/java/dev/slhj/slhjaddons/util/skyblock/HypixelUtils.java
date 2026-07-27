package dev.slhj.slhjaddons.util.skyblock;

public final class HypixelUtils {
    private HypixelUtils() {}

    public static boolean inSkyblock() {
        String title = ScoreboardUtils.title();
        if (title == null) return false;
        return title.toUpperCase().contains("SKYBLOCK");
    }

    public static boolean inDungeon() {
        if (!inSkyblock()) return false;
        for (String line : ScoreboardUtils.linesNoFormat()) {
            if (line.contains("The Catacombs")) return true;
        }
        return false;
    }
}
