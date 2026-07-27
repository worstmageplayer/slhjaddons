package dev.slhj.slhjaddons.features.skyblock;

import dev.slhj.slhjaddons.core.Feature;

public final class CancelSlotHighlight extends Feature {

    public CancelSlotHighlight() {
        setLabel("Cancel Slot Highlight");
        category(Category.SKYBLOCK);
    }

    public static final String id = "cancel_slot_highlight";
    @Override public String id() { return id; }

    @Override
    public void init() {
    }
}
