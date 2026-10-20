package dev.slhj.slhjaddons.gui;

import dev.slhj.slhjaddons.core.Feature;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class SettingsMenuState {
    private static final List<Feature.Category> ORDER = List.of(Feature.Category.SKYBLOCK,
            Feature.Category.DUNGEONS, Feature.Category.FISHING, Feature.Category.SLAYERS,
            Feature.Category.PETS, Feature.Category.EVENTS, Feature.Category.MISC, Feature.Category.FORAGING);
    private String query = "";
    private final Map<String, Double> scrollPositions = new HashMap<>();

    String query() { return query; }
    boolean searching() { return !query.isBlank(); }
    void query(String query) { this.query = query; }
    double scroll() { return scrollPositions.getOrDefault(viewKey(), 0.0); }
    void scroll(double position) { scrollPositions.put(viewKey(), position); }
    private String viewKey() {
        return searching() ? "search:" + query.strip().toLowerCase(Locale.ROOT) : "all";
    }
    List<Feature.Category> categories(List<Feature> features) {
        return ORDER.stream().filter(c -> features.stream().anyMatch(f -> f.category() == c)).toList();
    }
    List<Feature> visible(List<Feature> features) {
        return features.stream().filter(f -> f.category() != null)
                .filter(f -> matches(f.label() + " " + f.id() + " " + f.description() + " "
                        + f.category() + " " + String.join(" ", f.settings().stream().map(s -> s.label()).toList()), query))
                .sorted(Comparator.comparing((Feature f) -> ORDER.indexOf(f.category()))
                        .thenComparing(Feature::label, String.CASE_INSENSITIVE_ORDER)).toList();
    }
    static boolean matches(String text, String query) {
        String haystack = text.toLowerCase(Locale.ROOT);
        for (String term : query.strip().toLowerCase(Locale.ROOT).split("\\s+"))
            if (!haystack.contains(term)) return false;
        return true;
    }
}
