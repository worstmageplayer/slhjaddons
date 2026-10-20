package dev.slhj.slhjaddons.features.misc;

/** Holding a key across cancellation never reopens or executes the previous selection. */
final class CommandWheelSession {
    enum Action { NONE, OPEN, RELEASE, CANCEL }
    private boolean wasDown, open;

    Action update(boolean down, boolean available) {
        boolean pressed = down && !wasDown;
        wasDown = down;
        if (!available) {
            boolean wasOpen = open;
            open = false;
            return wasOpen ? Action.CANCEL : Action.NONE;
        }
        if (pressed) { open = true; return Action.OPEN; }
        if (open && !down) { open = false; return Action.RELEASE; }
        return Action.NONE;
    }

    boolean isOpen() { return open; }
    void cancel() { open = false; }
}
