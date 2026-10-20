package dev.slhj.slhjaddons.features.misc;

import dev.slhj.slhjaddons.core.Feature;

public final class NoFog extends Feature {
    public static final String ID = "no_fog";

    public NoFog() {
        setLabel("No Fog");
        setDescription("Hide distance and environmental fog.");
        setCategory(Category.MISC);
    }

    @Override
    public String id() {
        return ID;
    }
}
