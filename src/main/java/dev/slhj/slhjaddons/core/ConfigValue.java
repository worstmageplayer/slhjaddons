package dev.slhj.slhjaddons.core;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.config.SlhjConfig;

import java.util.function.Function;

public final class ConfigValue<T> {
    private final String key;
    private final T def;
    private final Function<String, T> parse;
    private final Function<T, String> serialize;
    private T cached;

    private ConfigValue(String key, T def, Function<String, T> parse, Function<T, String> serialize) {
        this.key = key;
        this.def = def;
        this.parse = parse;
        this.serialize = serialize;
        reload();
    }

    public void reload() {
        String raw = SlhjAddons.config().raw(key);
        if (raw == null) {
            cached = def;
            return;
        }
        try {
            cached = parse.apply(raw);
        } catch (Exception e) {
            cached = def;
        }
    }

    public String key() { return key; }
    public T def() { return def; }
    public T get() { return cached; }

    public void set(T value) {
        cached = value;
        SlhjAddons.config().set(key, serialize.apply(value));
    }

    static ConfigValue<Boolean> ofBool(String key, boolean def) {
        return new ConfigValue<>(key, def, Boolean::parseBoolean, String::valueOf);
    }

    static ConfigValue<Double> ofDouble(String key, double def) {
        return new ConfigValue<>(key, def, Double::parseDouble, String::valueOf);
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