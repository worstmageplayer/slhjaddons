package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;

public final class CancelSlotHighlight extends Feature {

    public CancelSlotHighlight() {
        setLabel("Cancel Slot Highlight");
        category(Category.SKYBLOCK);
    }

    @Override public String id() { return "cancel_slot_highlight"; }

    @Override
    public void init() {
    }
}
