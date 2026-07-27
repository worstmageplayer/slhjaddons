package dev.slhj.slhjaddons.features.skyblock;

import dev.slhj.slhjaddons.core.Feature;

public final class ShiftClick extends Feature {

    private static final String[] ALLOWED_CONTAINERS = {"Trades", "Your Equipment and Stats"};

    public ShiftClick() {
        setLabel("Shift Click");
        category(Category.SKYBLOCK);
    }

    public static final String id = "shift_click";
    @Override public String id() { return id; }

    @Override
    public void init() {
        // TODO: Implement with appropriate Fabric screen event hooks and correct ClickType API
    }
}
