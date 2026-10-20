package dev.slhj.slhjaddons.gui;

import java.util.Locale;

final class SettingsInputs {
    private SettingsInputs() {}

    /** Invalid intermediate edits never replace the last valid setting. */
    static Double number(String text, double min, double max, boolean integer) {
        try {
            double value = Double.parseDouble(text);
            return Double.isFinite(value) && value >= min && value <= max && (!integer || value == Math.rint(value))
                    ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
    static Float finiteFloat(String text) {
        try {
            float value = Float.parseFloat(text);
            return Float.isFinite(value) ? value : null;
        } catch (NumberFormatException ignored) { return null; }
    }
    static String numberText(double value, boolean integer) {
        return integer ? Long.toString(Math.round(value)) : String.format(Locale.ROOT, "%.2f", value);
    }
    static String hex(int color) { return String.format(Locale.ROOT, "#%08X", color); }
}
