package dev.slhj.slhjaddons.core;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.config.SlhjConfig;

import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.Objects;

public final class ConfigValue<T> {
    private final String key;
    private final T def;
    private final Function<String, T> parse;
    private final Function<T, String> serialize;
    private T cached;
    private final SlhjConfig config;
    private final UnaryOperator<T> normalize;
    private Consumer<T> changed = ignored -> {};

    private ConfigValue(String key, T def, Function<String, T> parse, Function<T, String> serialize) {
        this(key, def, parse, serialize, UnaryOperator.identity(), SlhjAddons.config());
    }

    ConfigValue(String key, T def, Function<String, T> parse, Function<T, String> serialize,
                UnaryOperator<T> normalize, SlhjConfig config) {
        this.key = key;
        this.def = def;
        this.parse = parse;
        this.serialize = serialize;
        this.normalize = normalize;
        this.config = config;
        reload();
    }

    public void reload() {
        String raw = config.raw(key);
        if (raw == null) {
            cached = def;
            return;
        }
        try {
            cached = normalize.apply(parse.apply(raw));
        } catch (Exception e) {
            cached = def;
        }
        // Persist repaired values on the next normal save.
        config.set(key, serialize.apply(cached));
    }

    public String key() { return key; }
    public T def() { return def; }
    public T get() { return cached; }

    public void set(T value) {
        T next = normalize.apply(value);
        boolean different = !Objects.equals(cached, next);
        cached = next;
        config.set(key, serialize.apply(next));
        if (different) changed.accept(next);
    }

    public void onChange(Consumer<T> changed) { this.changed = changed; }

    static ConfigValue<Boolean> ofBool(String key, boolean def) {
        return new ConfigValue<>(key, def, raw -> SlhjConfig.parseBoolean(raw, def), String::valueOf);
    }

    static ConfigValue<Double> ofDouble(String key, double def, double min, double max, boolean integer) {
        return new ConfigValue<>(key, def, Double::parseDouble, String::valueOf,
                value -> normalizeNumber(value, def, min, max, integer), SlhjAddons.config());
    }

    public static double normalizeNumber(double value, double def, double min, double max, boolean integer) {
        if (!Double.isFinite(value) || integer && value != Math.rint(value)) return def;
        return Math.clamp(value, min, max);
    }

    static ConfigValue<String> ofString(String key, String def) {
        return new ConfigValue<>(key, def, Function.identity(), Function.identity());
    }

    static ConfigValue<Integer> ofColor(String key, int def) {
        return new ConfigValue<>(key, def,
                s -> {
                    Integer v = SlhjConfig.parseHexColor(s);
                    if (v == null) throw new IllegalArgumentException("bad color: " + s);
                    return v;
                },
                argb -> String.format("%08X", argb));
    }
}
