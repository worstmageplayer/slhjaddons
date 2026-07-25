package dev.slhj.slhjaddons.hud;

public final class TimedHudAlert {

    private long shownAt = 0;

    public void show() {
        shownAt = System.currentTimeMillis();
    }

    public void reset() {
        shownAt = 0;
    }

    public boolean isActive(long durationMs) {
        if (shownAt == 0) return false;
        if (elapsedMs() >= durationMs) {
            shownAt = 0;
            return false;
        }
        return true;
    }

    public long elapsedMs() {
        return shownAt == 0 ? 0 : System.currentTimeMillis() - shownAt;
    }

    public long remainingMs(long durationMs) {
        return Math.max(0, durationMs - elapsedMs());
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