package dev.slhj.slhjaddons.util.client;

public final class ClickPacer {
    private int credit;
    private boolean running;

    public boolean tick(int cps, boolean active) {
        cps = Math.clamp(cps, 0, 20);
        if (!active || cps == 0) { reset(); return false; }
        if (!running) {
            running = true;
            credit = 0;
            return true;
        }
        credit += cps;
        if (credit < 20) return false;
        credit -= 20;
        return true;
    }

    public void reset() { running = false; credit = 0; }
}
