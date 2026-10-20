package dev.slhj.slhjaddons.core;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.config.SlhjConfig;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ConfigValue<T> {
    private final String key;
    private final T defaultValue;
    private final Function<String, T> parser;
    private final Function<T, String> serializer;
    private T cached;
    private final SlhjConfig config;
    private final UnaryOperator<T> normalizer;
    private Consumer<T> changeListener = ignored -> {};

    private ConfigValue(String key, T defaultValue, Function<String, T> parser, Function<T, String> serializer) {
        this(key, defaultValue, parser, serializer, UnaryOperator.identity(), SlhjAddons.config());
    }

    ConfigValue(String key, T defaultValue, Function<String, T> parser, Function<T, String> serializer,
                UnaryOperator<T> normalizer, SlhjConfig config) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.parser = parser;
        this.serializer = serializer;
        this.normalizer = normalizer;
        this.config = config;
        reload();
    }

    public void reload() {
        String raw = config.raw(key);
        if (raw == null) {
            cached = defaultValue;
            return;
        }
        try {
            cached = normalizer.apply(parser.apply(raw));
        } catch (Exception e) {
            cached = defaultValue;
        }

        config.set(key, serializer.apply(cached));
    }

    public String key() { return key; }
    public T defaultValue() { return defaultValue; }
    public T get() { return cached; }

    public void set(T value) {
        T next = normalizer.apply(value);
        boolean different = !Objects.equals(cached, next);
        cached = next;
        config.set(key, serializer.apply(next));
        if (different) changeListener.accept(next);
    }

    public void onChange(Consumer<T> changeListener) { this.changeListener = changeListener; }

    static ConfigValue<Boolean> ofBoolean(String key, boolean defaultValue) {
        return new ConfigValue<>(key, defaultValue, raw -> SlhjConfig.parseBoolean(raw, defaultValue), String::valueOf);
    }

    static ConfigValue<Double> ofDouble(String key, double defaultValue, double min, double max, boolean integer) {
        return new ConfigValue<>(key, defaultValue, Double::parseDouble, String::valueOf,
                value -> normalizeNumber(value, defaultValue, min, max, integer), SlhjAddons.config());
    }

    public static double normalizeNumber(double value, double defaultValue, double min, double max, boolean integer) {
        if (!Double.isFinite(value) || integer && value != Math.rint(value)) return defaultValue;
        return Math.clamp(value, min, max);
    }

    static ConfigValue<String> ofString(String key, String defaultValue) {
        return new ConfigValue<>(key, defaultValue, Function.identity(), Function.identity());
    }

    static ConfigValue<Integer> ofColor(String key, int defaultValue) {
        return new ConfigValue<>(key, defaultValue,
                s -> {
                    Integer v = SlhjConfig.parseHexColor(s);
                    if (v == null) throw new IllegalArgumentException("bad color: " + s);
                    return v;
                },
                argb -> String.format("%08X", argb));
    }
}
