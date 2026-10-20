package dev.slhj.slhjaddons.features.misc;

import dev.slhj.slhjaddons.core.Feature;

public final class NoFog extends Feature {
    public NoFog() {
        setLabel("No Fog");
        setDescription("Hide distance and environmental fog.");
        category( Category.MISC);
    }

    @Override
    public String id() {
        return "no_fog";
    }
}
