package dev.slhj.slhjaddons.features.misc;

import dev.slhj.slhjaddons.core.Feature;

public final class NoFog extends Feature {
    public NoFog() {
        setLabel("No Fog");
        category( Category.MISC);
    }

    @Override
    public String id() {
        return "no_fog";
    }
}
