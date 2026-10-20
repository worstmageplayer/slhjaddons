package dev.slhj.slhjaddons.util.skyblock;

import java.util.List;

public final class MageCooldownRegressionTests {
    private static int checks;

    public static void run() {
        documentedTabFormats();
        cooldownPercentages();
        duplicatesAndMissingPlayers();
        deathsAndIncompleteTab();
        serverBonusAndReset();
        invalidLevels();
        System.out.println("Mage cooldown regression checks passed: " + checks);
    }

    private static void check(boolean condition, String description) {
        checks++;
        if (!condition) throw new AssertionError(description);
    }

    private static void multiplier(MageCooldownState state, String name, double expected, String description) {
        double actual = state.multiplier(name);
        check(Math.abs(actual - expected) < .000001, description + ": expected " + expected + ", got " + actual);
    }

    private static void documentedTabFormats() {
        String[][] fixtures = {
                {"[319] Empa_ α (Mage XXXIV)", "Empa_", "0.33"},
                {"[393] [YOUTUBE] Remittal Σ♲ (Mage XL)", "Remittal", "0.30"},
                {"[273] Ovi_1 Ӄ (Mage XXXVI)", "Ovi_1", "0.32"},
                {"[400] [MVP++] Player_1 ♲ (Mage L)", "Player_1", "0.25"},
                {"[1] NewMage (Mage 0)", "NewMage", "0.50"},
                {"[20] Player_1 (Mage XX)", "Player_1", "0.40"},
                {"  [20]   Player_1   (Mage xl)  ", "player_1", "0.30"},
                {"[400] Abcdefghijklmno_ (Mage XLIX)", "Abcdefghijklmno_", "0.26"}
        };
        for (String[] fixture : fixtures) {
            var state = new MageCooldownState();
            state.updateTab(List.of(fixture[0]));
            multiplier(state, fixture[1], Double.parseDouble(fixture[2]), fixture[0]);
        }
    }

    private static void cooldownPercentages() {
        double[][] fixtures = {
                {0, .50, .75}, {1, .50, .75}, {2, .49, .74}, {20, .40, .65},
                {39, .31, .56}, {40, .30, .55}, {49, .26, .51}, {50, .25, .50}
        };
        for (double[] fixture : fixtures) {
            int level = (int) fixture[0];
            var state = new MageCooldownState();
            state.updateTab(List.of("[100] You (Mage " + level + ")", "[100] Tanky (Tank L)"));
            multiplier(state, "You", fixture[1], "solo Mage " + level);
            state.updateTab(List.of("[100] Other (Mage I)"));
            multiplier(state, "You", fixture[2], "duplicate Mage " + level);
        }
        var state = new MageCooldownState();
        state.updateTab(List.of("[100] You (Mage XXXIX)"));
        check(Math.round(20_000 * state.multiplier("You")) == 6200, "odd Mage level rounds to exact milliseconds");
    }

    private static void duplicatesAndMissingPlayers() {
        var state = new MageCooldownState();
        multiplier(state, "You", 1, "missing player has no reduction");
        state.updateTab(List.of("[100] You (Mage XL)", "[100] You (Mage XL)"));
        multiplier(state, "You", .30, "self and repeated tab entry are not duplicate Mages");
        state.updateTab(List.of("[100] YOU (Mage XL)", "[100] Healer_ ♲ (Healer L)"));
        multiplier(state, "you", .30, "name matching is case-insensitive");
        state.updateTab(List.of("[100] [MVP+] Other α (Mage XL)"));
        multiplier(state, "You", .55, "another Mage removes solo bonus");
        multiplier(state, "Healer_", 1, "a Mage teammate does not grant reduction to other classes");
        multiplier(state, "Missing", 1, "unknown local player does not inherit another player's class");
        state.updateTab(List.of("[100] You (Archer XL)"));
        multiplier(state, "You", 1, "local class changes update the roster");
    }

    private static void deathsAndIncompleteTab() {
        var state = new MageCooldownState();
        state.updateTab(List.of("[100] You (Mage XL)", "[100] Other (Mage L)"));
        state.updateTab(List.of("[100] You (Mage XL)", "[100] Other (DEAD)"));
        multiplier(state, "You", .55, "dead duplicate Mage retains their class");
        state.updateTab(List.of("[100] You (Mage XL)"));
        multiplier(state, "You", .55, "missing teammate does not change the run's solo bonus");
        state.updateTab(List.of());
        multiplier(state, "You", .55, "transient empty tab retains known class data");
        state.updateTab(List.of("[100] You (DEAD)"));
        multiplier(state, "You", .55, "local death retains known class level");

        state = new MageCooldownState();
        state.updateTab(List.of("[100] You (Mage XL)", "[100] Other (DEAD)"));
        multiplier(state, "You", .55, "unknown dead teammate does not imply solo Mage");
        state.updateTab(List.of("[100] Other (Tank L)"));
        multiplier(state, "You", .30, "revived teammate resolves their previously unknown class");
    }

    private static void serverBonusAndReset() {
        var state = new MageCooldownState();
        state.updateTab(List.of("[100] You (Mage XL)", "[100] Other (DEAD)"));
        state.onChat("Party > Someone: Your Mage stats are doubled because you are the only player using this class!");
        multiplier(state, "You", .55, "player chat cannot claim a server solo bonus");
        state.onChat("Your Mage stats are doubled because you are the only player using this class!");
        multiplier(state, "You", .30, "server bonus resolves an incomplete roster");
        state.updateTab(List.of("[100] Other (Mage L)"));
        multiplier(state, "You", .30, "server-confirmed run bonus takes precedence over tab inference");
        state.reset();
        multiplier(state, "You", 1, "world or dungeon exit clears local class");
        state.updateTab(List.of("[100] You (Mage XL)", "[100] Other (Mage L)"));
        multiplier(state, "You", .55, "solo bonus cannot leak into a new run");
        state.onChat("Your Tank stats are doubled because you are the only player using this class!");
        multiplier(state, "You", 1, "server-reported non-Mage class prevents stale Mage reduction");
    }

    private static void invalidLevels() {
        for (String level : List.of("51", "999999999999999999", "IIII", "IL", "IC", "XXXX", "LI", "M")) {
            var state = new MageCooldownState();
            state.updateTab(List.of("[100] You (Mage " + level + ")"));
            multiplier(state, "You", 1, "invalid class level " + level + " has no reduction");
        }
        var state = new MageCooldownState();
        state.updateTab(List.of("[100] You (Mage XL) unexpected suffix", "You (Mage XL)", "Players (5)"));
        multiplier(state, "You", 1, "unrecognized lines do not create a local Mage");
    }
}
