package dev.slhj.slhjaddons.util.skyblock;

import dev.slhj.slhjaddons.util.client.ClientUtils;
import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CooldownUtils {
    private CooldownUtils() {}

    private static final Pattern TAB_ENTRY =
            Pattern.compile("^\\[(\\d+)] (\\w+) \\((\\w+) (\\w+)\\)$");
    private static final Pattern SCOREBOARD_CLASS = Pattern.compile("^\\[(\\w)] (\\w+)");

    public static double getCooldownMultiplier() {
        if (!HypixelUtils.inDungeon()) return 1.0;

        String you = ClientUtils.player() == null ? null : ClientUtils.player().getGameProfile().name();
        if (you == null) return 1.0;

        Integer classLevel = null;
        for (String line : ClientUtils.tabListPlayers()) {
            Matcher m = TAB_ENTRY.matcher(ChatFormatting.stripFormatting(line));
            if (!m.matches()) continue;
            if (!"Mage".equals(m.group(3))) continue;
            if (!you.equals(m.group(2))) continue;
            classLevel = decodeNumeral(m.group(4));
        }
        if (classLevel == null) return 1.0;

        double reduction = Math.floor(classLevel / 2.0) / 100.0;
        return 1 - 0.25 - reduction - (hasDuplicateMage() ? 0 : 0.25);
    }

    private static boolean hasDuplicateMage() {
        for (String line : ScoreboardUtils.linesNoFormat()) {
            Matcher m = SCOREBOARD_CLASS.matcher(line);
            if (m.find() && m.group(1).contains("M")) return true;
        }
        return false;
    }

    private static final java.util.Map<Character, Integer> ROMAN = java.util.Map.of(
            'I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100, 'D', 500, 'M', 1000);

    @Nullable
    private static Integer decodeNumeral(String numeral) {
        String s = numeral.trim().toUpperCase();
        if (!s.matches("^[IVXLCDM]+$")) return null;
        int total = 0, prev = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            int val = ROMAN.get(s.charAt(i));
            total += val < prev ? -val : val;
            prev = val;
        }
        return total;
    }
}