package dev.slhj.slhjaddons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.hud.HudPlacement;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class SlhjConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("slhjaddons.json"); }

    public Map<String, Boolean> toggles = new HashMap<>();
    public Map<String, String> values = new HashMap<>();
    public Map<String, HudPlacement> hud = new HashMap<>();

    public boolean isFeatureEnabled(String id) {
        return toggles.getOrDefault(id, false);
    }

    public void setFeatureEnabled(String id, boolean on) {
        toggles.put(id, on);
    }

    /** Raw string for a key, or null if unset. Prefer ConfigValue<T> over this for new code. */
    public String raw(String key) {
        return values.get(key);
    }

    public String getString(String key, String def) {
        return values.getOrDefault(key, def);
    }

    public boolean getBool(String key, boolean def) {
        return parseBoolean(values.get(key), def);
    }

    public static boolean parseBoolean(String raw, boolean def) {
        if ("true".equalsIgnoreCase(raw)) return true;
        if ("false".equalsIgnoreCase(raw)) return false;
        return def;
    }

    public int getInt(String key, int def) {
        try { return Integer.parseInt(values.get(key)); }
        catch (Exception e) { return def; }
    }

    public double getDouble(String key, double def) {
        try { return Double.parseDouble(values.get(key)); }
        catch (Exception e) { return def; }
    }

    public void set(String key, String value) {
        values.put(key, value);
    }

    public int getColor(String key, int def) {
        String v = values.get(key);
        if (v == null) return def;
        Integer parsed = parseHexColor(v);
        return parsed != null ? parsed : def;
    }

    public void setColor(String key, int argb) {
        values.put(key, String.format("%08X", argb));
    }

    @Nullable
    public static Integer parseHexColor(String raw) {
        if (raw == null) return null;
        String s = raw.startsWith("#") ? raw.substring(1) : raw;
        if (s.length() != 6 && s.length() != 8) return null;
        try {
            int rgb = Integer.parseUnsignedInt(s.length() == 8 ? s.substring(2) : s, 16);
            int a = s.length() == 8 ? Integer.parseUnsignedInt(s.substring(0, 2), 16) : 0xFF;
            return (a << 24) | rgb;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static SlhjConfig load() {
        try {
            Path configPath = path();
            if (Files.exists(configPath)) {
                SlhjConfig c = GSON.fromJson(Files.readString(configPath), SlhjConfig.class);
                if (c != null) {
                    if (c.hud == null) c.hud = new HashMap<>();
                    if (c.values == null) c.values = new HashMap<>();
                    if (c.toggles == null) c.toggles = new HashMap<>();
                    return c;
                }
            }
        } catch (Exception e) {
            SlhjAddons.LOGGER.error("Could not read config, using defaults", e);
        }
        return new SlhjConfig();
    }

    public void save() {
        try {
            Path configPath = path();
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(this));
        } catch (IOException e) {
            SlhjAddons.LOGGER.error("Could not save config", e);
        }
    }
}