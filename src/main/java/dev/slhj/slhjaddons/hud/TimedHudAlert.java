package dev.slhj.slhjaddons.hud;

import java.util.function.LongSupplier;

public final class TimedHudAlert {

    private final LongSupplier clock;
    private long shownAt = 0;

    public TimedHudAlert() { this(System::currentTimeMillis); }
    TimedHudAlert(LongSupplier clock) { this.clock = clock; }

    public void show() {
        shownAt = clock.getAsLong();
    }

    public void reset() {
        shownAt = 0;
    }

    public boolean isActive(long durationMs) {
        return shownAt != 0 && elapsedMs() < durationMs;
    }

    public long elapsedMs() {
        return shownAt == 0 ? 0 : clock.getAsLong() - shownAt;
    }

    public long remainingMs(long durationMs) {
        return shownAt == 0 ? 0 : Math.max(0, durationMs - elapsedMs());
    }

    public double remainingSeconds(long durationMs) {
        return remainingMs(durationMs) / 1000.0;
    }

    public double progress(long durationMs) {
        return Math.min(1.0, elapsedMs() / (double) durationMs);
    }

    public int alpha(long durationMs, double fadeStartFraction) {
        double t = progress(durationMs);
        if (fadeStartFraction >= 1.0 || t < fadeStartFraction) return 255;
        double phase = (t - fadeStartFraction) / (1 - fadeStartFraction);
        return (int) (255 * (1 - Math.pow(phase, 5)));
    }
}