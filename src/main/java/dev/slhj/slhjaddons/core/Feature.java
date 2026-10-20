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
    private String description = "";
    private final List<Setting> settings = new ArrayList<>();

    public abstract String id();

    public boolean isToggleable() { return true; }

    public String label() {
        return label;
    }

    public String description() { return description; }

    protected final void setDescription(String description) { this.description = description; }

    public void init() {}

    protected void onEnable() {}
    protected void onDisable() {}

    public final boolean isEnabled() {
        return !isToggleable() || enabled;
    }

    public final void setEnabled(boolean value) {
        value = !isToggleable() || value;
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

    protected final void setCategory(Category category) {
        this.category = category;
    }

    protected final void setLabel(String label) {
        this.label = label;
    }

    protected final Setting.SliderSetting slider(String key, String label, double min, double max, double defaultValue) {
        var value = ConfigValue.ofDouble(key, defaultValue, min, max, false);
        return addSetting(new Setting.SliderSetting(value, label, min, max, false));
    }

    protected final Setting.SliderSetting intSlider(String key, String label, int min, int max, int defaultValue) {
        var value = ConfigValue.ofDouble(key, defaultValue, min, max, true);
        return addSetting(new Setting.SliderSetting(value, label, min, max, true));
    }

    protected final Setting.TextSetting text(String key, String label, String defaultValue) {
        return text(key, label, defaultValue, "");
    }

    protected final Setting.TextSetting text(String key, String label, String defaultValue, String hint) {
        var value = ConfigValue.ofString(key, defaultValue);
        return addSetting(new Setting.TextSetting(value, label, hint));
    }

    protected final Setting.ColorSetting color(String key, String label, int defaultValue) {
        var value = ConfigValue.ofColor(key, defaultValue);
        return addSetting(new Setting.ColorSetting(value, label));
    }

    protected final Setting.ButtonSetting button(String key, String label, String buttonText, Runnable action) {
        return addSetting(new Setting.ButtonSetting(key, label, buttonText, action));
    }

    protected final Setting.ToggleSetting toggle(String key, String label, boolean defaultValue) {
        var value = ConfigValue.ofBoolean(key, defaultValue);
        return addSetting(new Setting.ToggleSetting(value, label));
    }

    private <T extends Setting> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }
}
