package dev.slhj.slhjaddons.features.misc;

import dev.slhj.slhjaddons.core.Feature;

public final class NoBlockBreakParticles extends Feature {
    public static final String ID = "no_block_break_particles";

    public NoBlockBreakParticles() {
        setLabel("No Block Break Particles");
        setDescription("Hide block breaking and destruction particles.");
        setCategory(Category.MISC);
    }

    @Override
    public String id() {
        return ID;
    }
}
