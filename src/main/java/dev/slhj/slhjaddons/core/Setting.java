package dev.slhj.slhjaddons.core;

public sealed interface Setting
        permits Setting.ButtonSetting, Setting.HexSetting, Setting.SliderSetting, Setting.TextSetting, Setting.ToggleSetting {

    String key();
    String label();

    record SliderSetting(ConfigValue<Double> value, String label, double min, double max, boolean isInt)
            implements Setting {
        @Override public String key() { return value.key(); }
    }

    record TextSetting(ConfigValue<String> value, String label, String hint)
            implements Setting {
        @Override public String key() { return value.key(); }
    }

    record HexSetting(ConfigValue<Integer> value, String label)
            implements Setting {
        @Override public String key() { return value.key(); }
    }

    record ButtonSetting(String key, String label, String buttonText, Runnable action)
            implements Setting {}

    record ToggleSetting(ConfigValue<Boolean> value, String label)
            implements Setting {
        @Override public String key() { return value.key(); }
    }
}