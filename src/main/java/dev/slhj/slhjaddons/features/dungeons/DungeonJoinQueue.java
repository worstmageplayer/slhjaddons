package dev.slhj.slhjaddons.features.dungeons;

import java.util.function.LongSupplier;

final class DungeonJoinQueue {
    private final LongSupplier clock;
    private long cooldownEnd, due;
    private String pending;

    DungeonJoinQueue(LongSupplier clock) { this.clock = clock; }

    boolean enqueue(String command) {
        if (pending != null) return false;
        pending = command;
        due = Math.max(clock.getAsLong(), cooldownEnd) + 100;
        return true;
    }

    long remainingMs() { return Math.max(0, cooldownEnd - clock.getAsLong()); }

    String poll() {
        if (pending == null || clock.getAsLong() < due) return null;
        String command = pending;
        pending = null;
        cooldownEnd = clock.getAsLong() + 30_000;
        return command;
    }

    void reset() { pending = null; cooldownEnd = due = 0; }
}
