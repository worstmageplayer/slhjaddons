package dev.slhj.slhjaddons.core;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.features.Commands;
import dev.slhj.slhjaddons.features.ChatCalc;
import dev.slhj.slhjaddons.features.misc.QuickCommands;
import dev.slhj.slhjaddons.features.misc.AutoClick;

public final class CoreRegressionTests {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    private static Setting.SliderSetting slider(Feature feature, String key) {
        return feature.settings().stream().filter(s -> s.key().equals(key)).map(s -> (Setting.SliderSetting) s).findFirst().orElseThrow();
    }
    public static void run() {
        SlhjConfig config = new SlhjConfig();
        check(config.getBool("missing", true), "missing legacy boolean respects true default");
        config.set("bad", "maybe");
        check(config.getBool("bad", true), "malformed boolean respects default");
        config.set("bad", "false");
        check(!config.getBool("bad", true), "explicit false preserved");
        config.set("rate", "40");
        var rate = new ConfigValue<>("rate", 10.0, Double::parseDouble, String::valueOf,
                value -> ConfigValue.normalizeNumber(value, 10, 0, 20, true), config);
        check(rate.get() == 20 && config.raw("rate").equals("20.0"), "saved excessive rate clamped and repaired");
        rate.set(-10.0);
        check(rate.get() == 0, "assignment also clamps lower bound");
        for (String invalid : new String[]{"NaN", "Infinity", "-Infinity", "oops", "2.5"}) {
            config.set("rate", invalid); rate.reload();
            check(rate.get() == 10, "invalid saved integer uses default: " + invalid);
        }
        rate.set(Double.NaN);
        check(rate.get() == 10, "non-finite assignments use default");

        // Exercise real feature construction and coupled ConfigValues without a game client.
        try {
            var configField = SlhjAddons.class.getDeclaredField("config");
            configField.setAccessible(true);
            Object previous = configField.get(null);
            configField.set(null, config);
            try {
                config.set("auto_click.cps", "40");
                check(slider(new AutoClick(), "auto_click.cps").value().get() == 20, "real clicker normalizes saved 40 CPS");
                config.set("quick_commands.inner_radius", "200");
                config.set("quick_commands.outer_radius", "30");
                QuickCommands wheel = new QuickCommands();
                var inner = slider(wheel, "quick_commands.inner_radius").value();
                var outer = slider(wheel, "quick_commands.outer_radius").value();
                check(inner.get() == 200 && outer.get() == 201, "saved inverted radii repaired");
                inner.set(250.0);
                check(outer.get() == 251, "inner edit raises outer");
                outer.set(10.0);
                check(inner.get() == 9, "outer edit lowers inner");
                inner.set(999.0);
                check(inner.get() == 254 && outer.get() == 255, "upper limits keep a gap");
                outer.set(0.0);
                check(inner.get() == 0 && outer.get() == 1, "lower limits keep a gap");
                var show = (Setting.ToggleSetting) wheel.settings().stream()
                        .filter(s -> s.key().equals("quick_commands.show_string")).findFirst().orElseThrow();
                check(show.value().get(), "existing hover-label option is exposed with same default");
                FeatureManager manager = new FeatureManager();
                Commands commands = new Commands();
                ChatCalc calculator = new ChatCalc();
                manager.register(commands); manager.register(calculator);
                config.setFeatureEnabled("commands", false);
                config.setFeatureEnabled("chat_calc", false);
                manager.syncFromConfig();
                check(commands.isEnabled() && calculator.isEnabled(), "infrastructure stays available with saved OFF toggles");
                check(!commands.toggleable() && !calculator.toggleable(), "infrastructure explicitly non-toggleable");
                commands.setEnabled(false);
                check(commands.isEnabled(), "direct disable cannot switch infrastructure off");
                try {
                    manager.register(new Commands());
                    throw new AssertionError("duplicate feature registration accepted");
                } catch (IllegalArgumentException expected) {
                    check(manager.all().size() == 2 && manager.getById("commands") == commands,
                            "duplicate rejected without partial registration");
                }
                Feature normal = new Feature() { @Override public String id() { return "test"; } };
                normal.setEnabled(true); normal.setEnabled(false);
                check(!normal.isEnabled() && normal.toggleable(), "ordinary features still disable normally");
            } finally { configField.set(null, previous); }
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
        System.out.println("Core regression checks passed: " + checks);
    }
}
