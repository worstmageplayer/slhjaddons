package dev.slhj.slhjaddons.hud;

import java.util.List;
import java.util.function.Predicate;

public final class HudSelection<T> {
    private T selected, pressed;
    private List<T> pressHits = List.of();
    private double pressX, pressY;
    private boolean dragging;

    public T selected() { return selected; }
    public T pressed() { return pressed; }
    public boolean dragging() { return dragging; }
    public void select(T element) { cancel(); selected = element; }
    public void cancel() { pressed = null; pressHits = List.of(); dragging = false; }
    public void press(List<T> hits, double x, double y) {
        cancel();
        if (hits.isEmpty()) { selected = null; return; }
        pressHits = List.copyOf(hits);
        pressed = selected != null && hits.contains(selected) ? selected : hits.getFirst();
        pressX = x; pressY = y;
    }
    public boolean drag(double x, double y) {
        if (pressed == null) return false;
        if (!dragging && Math.hypot(x - pressX, y - pressY) >= 3) {
            dragging = true; selected = pressed;
        }
        return dragging;
    }
    public void retain(Predicate<T> available) {
        if (selected != null && !available.test(selected)) selected = null;
        if (pressed != null && !available.test(pressed)) cancel();
        else pressHits = pressHits.stream().filter(available).toList();
    }
    public void release() {
        if (pressed != null && !dragging) {
            int previous = selected == null ? -1 : pressHits.indexOf(selected);
            selected = pressHits.get((previous + 1) % pressHits.size());
        }
        cancel();
    }
}
