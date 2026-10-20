package dev.slhj.slhjaddons.util.skyblock;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ScoreboardUtils {
    private ScoreboardUtils() {}

    public static List<String> lines() {
        List<String> lines = new ArrayList<>();
        var level = Minecraft.getInstance().level;
        if (level == null) return lines;

        Scoreboard scoreboard = level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return lines;

        var scores = new ArrayList<>(scoreboard.listPlayerScores(objective));
        scores.sort((a, b) -> Integer.compare(a.value(), b.value()));

        List<String> ordered = new ArrayList<>();
        for (var entry : scores) {
            String owner = entry.owner();
            PlayerTeam team = scoreboard.getPlayersTeam(owner);
            String line = team == null
                    ? owner
                    : team.getPlayerPrefix().getString() + owner + team.getPlayerSuffix().getString();
            ordered.add(line);
        }
        for (int i = ordered.size() - 1; i >= 0; i--) lines.add(ordered.get(i));
        return lines;
    }

    @Nullable
    public static String title() {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        Objective objective = level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        return objective == null ? null : objective.getDisplayName().getString();
    }

    public static List<String> linesNoFormat() {
        List<String> raw = lines();
        List<String> lines = new ArrayList<>(raw.size());
        for (String s : raw) lines.add(ChatFormatting.stripFormatting(s));
        return lines;
    }
}
