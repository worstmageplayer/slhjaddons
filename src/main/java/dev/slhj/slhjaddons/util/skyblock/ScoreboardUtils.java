package dev.slhj.slhjaddons.util.skyblock;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public final class ScoreboardUtils {
    private ScoreboardUtils() {}

    public static List<String> lines() {
        List<String> out = new ArrayList<>();
        var level = Minecraft.getInstance().level;
        if (level == null) return out;

        Scoreboard sb = level.getScoreboard();
        Objective obj = sb.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (obj == null) return out;

        var scores = new ArrayList<>(sb.listPlayerScores(obj));
        scores.sort((a, b) -> Integer.compare(a.value(), b.value()));

        List<String> ordered = new ArrayList<>();
        for (var entry : scores) {
            String owner = entry.owner();
            PlayerTeam team = sb.getPlayersTeam(owner);
            String line = team == null
                    ? owner
                    : team.getPlayerPrefix().getString() + owner + team.getPlayerSuffix().getString();
            ordered.add(line);
        }
        for (int i = ordered.size() - 1; i >= 0; i--) out.add(ordered.get(i));
        return out;
    }

    public static String title() {
        var level = Minecraft.getInstance().level;
        if (level == null) return "";
        Objective obj = level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        return obj == null ? "" : obj.getDisplayName().getString();
    }

    public static List<String> linesNoFormat() {
        List<String> raw = lines();
        List<String> out = new ArrayList<>(raw.size());
        for (String s : raw) out.add(ChatFormatting.stripFormatting(s));
        return out;
    }
}
