package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;

public final class ShiftClickFeature extends Feature {

    private static final String[] ALLOWED_CONTAINERS = {"Trades", "Your Equipment and Stats"};

    public ShiftClickFeature() {
        setLabel("Shift Click");
        category(Category.SKYBLOCK);
    }
    @Override public String id() { return "shift_click"; }

    @Override
    public void init() {
        // TODO: Implement with appropriate Fabric screen event hooks and correct ClickType API
    }
}
