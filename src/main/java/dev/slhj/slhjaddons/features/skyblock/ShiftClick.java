package dev.slhj.slhjaddons.features.skyblock;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;

import java.util.ArrayList;
import java.util.List;

public final class ShiftClick extends Feature {

    private static final String DEFAULT_CONTAINERS = "Trades,Your Equipment and Stats";

    private final Setting.TextSetting containersSetting;

    public ShiftClick() {
        setLabel("Shift Click");
        category(Category.SKYBLOCK);
        containersSetting = text(
                "shift_click.containers",
                "Containers",
                DEFAULT_CONTAINERS,
                "Containers to Shift Click, e.g. Trades,Your Equipment and Stats");
    }

    public static final String id = "shift_click";
    @Override public String id() { return id; }

    @Override
    public void init() {
    }

    public List<String> containers() {
        List<String> result = new ArrayList<>();
        for (String title : containersSetting.value().get().split(",")) {
            String trimmed = title.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }
}