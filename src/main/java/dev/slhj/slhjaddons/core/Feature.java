package dev.slhj.slhjaddons.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Feature {

    public enum Category {
        SKYBLOCK("Skyblock"),
        SLAYERS("Slayers"),
        PETS("Pets"),
        MISC("Misc"),
        EVENTS("Events"),
        DUNGEONS("Dungeons"),
        FISHING("Fishing"),
        FORAGING("Foraging");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private boolean enabled;
    private Category category;
    private String label;
    private final List<Setting> settings = new ArrayList<>();

    public abstract String id();

    public String label() {
        return label;
    }

    public void init() {}

    protected void onEnable() {}
    protected void onDisable() {}

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable(); else onDisable();
    }

    public final Category category() {
        return category;
    }

    public final List<Setting> settings() {
        return Collections.unmodifiableList(settings);
    }

    protected final void category(Category category) {
        this.category = category;
    }

    protected final void setLabel(String label) {
        this.label = label;
    }

    protected final String header() {
        return this.label;
    }

    protected final Setting.SliderSetting slider(String key, String label, double min, double max, double def) {
        var value = ConfigValue.ofDouble(key, def);
        var s = new Setting.SliderSetting(value, label, min, max, false);
        settings.add(s);
        return s;
    }

    protected final Setting.SliderSetting intSlider(String key, String label, int min, int max, int def) {
        var value = ConfigValue.ofDouble(key, def);
        var s = new Setting.SliderSetting(value, label, min, max, true);
        settings.add(s);
        return s;
    }

    protected final Setting.TextSetting text(String key, String label, String def) {
        return text(key, label, def, "");
    }

    protected final Setting.TextSetting text(String key, String label, String def, String hint) {
        var value = ConfigValue.ofString(key, def);
        var s = new Setting.TextSetting(value, label, hint);
        settings.add(s);
        return s;
    }

    protected final Setting.HexSetting hex(String key, String label, int def) {
        var value = ConfigValue.ofColor(key, def);
        var s = new Setting.HexSetting(value, label);
        settings.add(s);
        return s;
    }

    protected final Setting.ButtonSetting button(String key, String label, String buttonText, Runnable action) {
        var s = new Setting.ButtonSetting(key, label, buttonText, action);
        settings.add(s);
        return s;
    }

    protected final Setting.ToggleSetting toggle(String key, String label, boolean def) {
        var value = ConfigValue.ofBool(key, def);
        var s = new Setting.ToggleSetting(value, label);
        settings.add(s);
        return s;
    }
}