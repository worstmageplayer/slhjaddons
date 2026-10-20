package dev.slhj.slhjaddons.gui;

import dev.slhj.slhjaddons.core.Feature;

import java.util.List;

public final class SettingsMenuRegressionTests {
    private static int checks;

    public static void run() {
        categoriesAndSearch();
        searchAndScroll();
        numericValidation();
        hudNumericValidation();
        System.out.println("Settings menu regression checks passed: " + checks);
    }

    private static void check(boolean condition, String description) {
        checks++;
        if (!condition) throw new AssertionError(description);
    }

    private static Feature feature(String id, String label, Feature.Category category, String description) {
        return new Feature() {
            { setLabel(label); category(category); setDescription(description); }
            @Override public String id() { return id; }
        };
    }

    private static void categoriesAndSearch() {
        Feature clock = feature("clock", "Clock", Feature.Category.MISC, "Current local time");
        Feature fish = feature("golden_fish_alert", "Golden Fish Alert", Feature.Category.FISHING, "Lava fishing alert");
        Feature autoFish = feature("auto_fish", "Auto Fish", Feature.Category.FISHING, "Reel in and cast after a delay");
        Feature pets = feature("auto_pet_rules", "Auto Pet Rules", Feature.Category.PETS, "Equipped pet notifications");
        Feature command = feature("commands", "Commands", null, "Internal command registration");
        List<Feature> all = List.of(clock, fish, autoFish, pets, command);
        var state = new SettingsMenuState();
        check(state.visible(all).equals(List.of(autoFish, fish, pets, clock)), "all categories use predictable category/name order");
        check(state.categories(all).equals(List.of(Feature.Category.FISHING, Feature.Category.PETS, Feature.Category.MISC)),
                "only populated categories appear, including Pets");
        check(!fish.isEnabled() && state.visible(all).contains(fish), "disabled features remain configurable");
        state.query("LOCAL TIME");
        check(state.visible(all).equals(List.of(clock)), "global search crosses categories and matches descriptions");
        state.query("golden alert");
        check(state.visible(all).equals(List.of(fish)), "search matches separate words in feature labels");
        state.query("auto_pet_rules");
        check(state.visible(all).equals(List.of(pets)), "feature IDs can be searched");
        state.query("no such feature");
        check(state.visible(all).isEmpty(), "no matches yields an empty result");
        state.query("");
        check(state.visible(all).equals(List.of(autoFish, fish, pets, clock)), "clearing search restores the full list across categories");
        check(SettingsMenuState.matches("Golden Fish Alert Alert Duration Color", "fish duration"),
                "search supports feature and setting-label terms together");
        check(SettingsMenuState.matches("Any feature", "   "), "whitespace query matches everything");
    }

    private static void searchAndScroll() {
        var state = new SettingsMenuState();
        check(state.scroll() == 0, "full list starts at the top");
        state.scroll(120);
        state.query("clock");
        check(state.scroll() == 0, "search starts at the top independently of the full list");
        state.scroll(30);
        state.query("");
        check(state.scroll() == 120, "clearing search restores the single full-list position");
        state.scroll(240);
        state.query(" CLOCK ");
        check(state.scroll() == 30, "same search restores its position regardless of case or surrounding spaces");
        state.query("other");
        check(state.scroll() == 0, "different search has its own scroll position");
        state.query("   ");
        check(state.scroll() == 240, "blank search uses the shared full-list position");
    }

    private static void hudNumericValidation() {
        check(SettingsInputs.finiteFloat("100") == 100f, "finite out-of-range HUD draft allowed until commit");
        for (String invalid : List.of("", "-", "NaN", "Infinity", "1e100", "text"))
            check(SettingsInputs.finiteFloat(invalid) == null, "invalid HUD draft rejected: " + invalid);
        var placement = new dev.slhj.slhjaddons.hud.HudPlacement();
        placement.scale = Math.clamp(SettingsInputs.finiteFloat("100"), .1f, 10f);
        placement.x = SettingsInputs.finiteFloat("10000");
        placement.constrain(320, 240, 10, 10);
        check(placement.scale == 10 && placement.x == 220, "committed values constrained to actual visible placement");
    }

    private static void numericValidation() {
        check(SettingsInputs.number("0", 0, 40, true) == 0, "minimum integer accepted");
        check(SettingsInputs.number("40", 0, 40, true) == 40, "maximum integer accepted");
        check(SettingsInputs.number("2.5", 0, 10, false) == 2.5, "decimal settings retain decimal support");
        for (String invalid : List.of("", "-", "1e", "NaN", "Infinity", "-Infinity", "41", "-1", "1.5", "text"))
            check(SettingsInputs.number(invalid, 0, 40, true) == null, "invalid or incomplete integer edit: " + invalid);
        check(SettingsInputs.numberText(12, true).equals("12"), "integer entry formats without decimals");
        check(SettingsInputs.numberText(1.25, false).equals("1.25"), "decimal entry formats using a stable decimal point");
    }
}
