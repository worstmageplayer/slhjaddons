package dev.slhj.slhjaddons.features.misc;

import dev.slhj.slhjaddons.core.Feature;

public final class NoBlockBreakParticles extends Feature {
    public NoBlockBreakParticles() {
        setLabel("No Block Break Particles");
        category( Category.MISC);
    }

    @Override
    public String id() {
        return "no_block_break_particles";
    }
}