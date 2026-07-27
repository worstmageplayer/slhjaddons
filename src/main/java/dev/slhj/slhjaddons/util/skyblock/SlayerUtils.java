package dev.slhj.slhjaddons.util.skyblock;

import java.util.List;

public class SlayerUtils {
    public enum Slayer {
        ZOMBIE("Revenant Horror"),
        SPIDER("Tarantula Broodfather"),
        BLAZE("Inferno Demon"),
        ENDERMAN("Voidgloom Seraph"),
        VAMPIRE("Bloodfiend");

        private final String bossName;

        Slayer(String bossName) {
            this.bossName = bossName;
        }

        public String bossName() {
            return bossName;
        }
    }

    public static boolean isSlayerActive(Slayer slayer) {
        List<String> lines = ScoreboardUtils.lines();
        boolean boss = false, slay = false;
        for (String l : lines) {
            if (l.contains(slayer.bossName())) boss = true;
            if (l.contains("Slay the boss!")) slay = true;
        }
        return boss && slay;
    }
}
