package dev.slhj.slhjaddons.features.fishing;

import java.util.regex.Pattern;

/** A pending recast belongs only to an uninterrupted fishing interaction. */
final class FishingCycle {
    private static final Pattern TIMER = Pattern.compile("^\\d\\.\\d$");
    private boolean armed;
    private int ticksSinceCatch = -1;

    boolean tick(boolean allowed, int delay) {
        if (!allowed) { reset(); return false; }
        if (ticksSinceCatch < 0 || ++ticksSinceCatch < delay) return false;
        ticksSinceCatch = -1;
        return true;
    }

    boolean observe(String name) {
        if (ticksSinceCatch >= 0) return false;
        if (TIMER.matcher(name).matches()) armed = true;
        else if (armed && name.equals("!!!")) {
            armed = false;
            ticksSinceCatch = 0;
            return true;
        }
        return false;
    }

    void reset() { armed = false; ticksSinceCatch = -1; }
}
