package dev.slhj.slhjaddons.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.slhj.slhjaddons.SlhjAddons;
import net.fabricmc.loader.api.FabricLoader;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class SlhjConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("slhjaddons.json");

    public Map<String, Boolean> toggles = new HashMap<>();
    public Map<String, String> values = new HashMap<>();
    public Map<String, HudPos> hud = new HashMap<>();

    public static final class HudPos {
        public float x, y, scale = 1f;
        public HudPos() {}
        public HudPos(float x, float y) { this.x = x; this.y = y; }
    }

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
        try { return Boolean.parseBoolean(values.get(key)); }
        catch (Exception e) { return def; }
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

    public HudPos hud(String id, float defX, float defY) {
        return hud.computeIfAbsent(id, k -> new HudPos(defX, defY));
    }

    public static SlhjConfig load() {
        try {
            if (Files.exists(PATH)) {
                SlhjConfig c = GSON.fromJson(Files.readString(PATH), SlhjConfig.class);
                if (c != null) return c;
            }
        } catch (Exception e) {
            SlhjAddons.LOGGER.error("Could not read config, using defaults", e);
        }
        return new SlhjConfig();
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException e) {
            SlhjAddons.LOGGER.error("Could not save config", e);
        }
    }
}