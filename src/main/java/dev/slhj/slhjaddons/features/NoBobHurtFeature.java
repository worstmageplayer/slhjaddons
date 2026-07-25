package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;

public final class NoBobHurtFeature extends Feature {
    public NoBobHurtFeature() {
        setLabel("No Bob Hurt");
        category(Category.MISC);
    }
    @Override public String id() { return "no_bob_hurt"; }
    @Override public void init() {}
}