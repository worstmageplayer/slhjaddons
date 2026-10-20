package dev.slhj.slhjaddons.features.skyblock;

import java.util.function.LongSupplier;
import java.util.regex.Pattern;

/** Tracks one activation; buff-phase CASTING messages are not new casts. */
final class RagnarockCooldownTimer {
    private static final Pattern CAST_START = Pattern.compile("\\bCASTING IN\\b");
    private final LongSupplier cooldownDuration, clock;
    private long startedAt, duration;
    private boolean running;

    RagnarockCooldownTimer(LongSupplier cooldownDuration) { this(cooldownDuration, System::currentTimeMillis); }
    RagnarockCooldownTimer(LongSupplier cooldownDuration, LongSupplier clock) {
        this.cooldownDuration = cooldownDuration;
        this.clock = clock;
    }

    void onActionBar(String text) {
        if (!CAST_START.matcher(text).find() || running && remainingMs() > 0) return;
        duration = Math.max(0, cooldownDuration.getAsLong());
        startedAt = clock.getAsLong();
        running = true;
    }

    long remainingMs() {
        return running ? Math.max(0, duration - Math.max(0, clock.getAsLong() - startedAt)) : 0;
    }

    /** True once per activation, independently of how often the HUD is drawn. */
    boolean tick() {
        if (!running || remainingMs() > 0) return false;
        running = false;
        return true;
    }

    void reset() {
        running = false;
        startedAt = duration = 0;
    }
}
