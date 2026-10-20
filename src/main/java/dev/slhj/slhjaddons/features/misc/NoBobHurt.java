package dev.slhj.slhjaddons.features.misc;

import dev.slhj.slhjaddons.core.Feature;

public final class NoBobHurt extends Feature {
    public NoBobHurt() {
        setLabel("No Bob Hurt");
        setDescription("Disable the camera shake when taking damage.");
        setCategory(Category.MISC);
    }

    public static final String ID = "no_bob_hurt";
    @Override public String id() { return ID; }
}
