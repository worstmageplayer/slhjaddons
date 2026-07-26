package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;

public final class NoBobHurt extends Feature {
    public NoBobHurt() {
        setLabel("No Bob Hurt");
        category(Category.MISC);
    }
    
    public static final String id = "no_bob_hurt";
    @Override public String id() { return id; }

    @Override public void init() {}
}