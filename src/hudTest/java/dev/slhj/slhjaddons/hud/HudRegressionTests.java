package dev.slhj.slhjaddons.hud;

import com.google.gson.Gson;
import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.core.Feature;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Dependency-free regression suite, run by Gradle's hudTest task and check lifecycle. */
public final class HudRegressionTests {
    private static int checks;
    private static final float SW = 1000, SH = 800, W = 60, H = 40;

    public static void main(String[] args) {
        anchorCombinations();
        anchorsPreservePosition();
        sizeChangesFollowElementAnchor();
        constraintsAndResize();
        legacyMigrationAndPersistence();
        overlapSelection();
        draggingAndAvailability();
        sharedContentAndEnablement();
        timerReadsHaveNoSideEffects();
        System.out.println("HUD regression checks passed: " + checks);
    }
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    private static void close(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < .001f, message + ": expected " + expected + ", got " + actual);
    }
    private static HudPlacement defaults() {
        return new HudPlacement(HudAnchor.TOP_LEFT, HudAnchor.TOP_LEFT, 10, 10, 1);
    }
    private static void anchorCombinations() {
        for (HudAnchor screen : HudAnchor.values()) for (HudAnchor element : HudAnchor.values()) {
            HudPlacement p = new HudPlacement(screen, element, 0, 0, 2);
            p.moveTo(240, 190, SW, SH, W, H);
            var b = p.bounds(SW, SH, W, H);
            close(b.x(), 240, screen + "/" + element + " left");
            close(b.y(), 190, screen + "/" + element + " top");
            close(b.x() + b.width() * element.x, SW * screen.x + p.x, "attached X point");
            close(b.y() + b.height() * element.y, SH * screen.y + p.y, "attached Y point");
        }
        var p = new HudPlacement(HudAnchor.BOTTOM_CENTER, HudAnchor.TOP_LEFT, 0, -100, 1);
        close(p.bounds(SW, SH, W, H).x(), 500, "Ragnarock example X");
        close(p.bounds(SW, SH, W, H).y(), 700, "Ragnarock example Y");
    }
    private static void anchorsPreservePosition() {
        HudPlacement p = defaults();
        p.moveTo(123, 81, SW, SH, W, H);
        for (HudAnchor screen : HudAnchor.values()) for (HudAnchor element : HudAnchor.values()) {
            p.anchors(screen, element, SW, SH, W, H);
            var b = p.bounds(SW, SH, W, H);
            close(b.x(), 123, "changing anchors preserves left");
            close(b.y(), 81, "changing anchors preserves top");
        }
    }
    private static void sizeChangesFollowElementAnchor() {
        var p = new HudPlacement(HudAnchor.BOTTOM_RIGHT, HudAnchor.BOTTOM_RIGHT, -20, -30, 1);
        var first = p.bounds(SW, SH, W, H);
        p.scale = 2;
        var scaled = p.bounds(SW, SH, W, H);
        close(first.x() + first.width(), scaled.x() + scaled.width(), "scale holds right anchor");
        close(first.y() + first.height(), scaled.y() + scaled.height(), "scale holds bottom anchor");
        var longer = p.bounds(SW, SH, W * 3, H * 2);
        close(longer.x() + longer.width(), 980, "longer live content holds right edge");
        close(longer.y() + longer.height(), 770, "extra lines hold bottom edge");
    }
    private static void constraintsAndResize() {
        var p = defaults();
        p.moveTo(999, -20, SW, SH, W, H);
        close(p.x, 940, "drag clamps full width");
        close(p.y, 0, "drag clamps top");
        p.x = 900; p.y = 700;
        var small = p.bounds(320, 240, W, H);
        close(small.x(), 260, "resize keeps element on screen");
        close(small.y(), 200, "resize clamps full height");
        close(p.x, 900, "resize preserves intended offset");
        close(p.bounds(SW, SH, W, H).x(), 900, "restoring viewport restores placement");
        var huge = p.bounds(30, 20, W, H);
        close(huge.x(), 0, "oversized element left remains accessible");
        close(huge.y(), 0, "oversized element top remains accessible");
        p = new HudPlacement(HudAnchor.BOTTOM_CENTER, HudAnchor.TOP_LEFT, 0, 0, 1);
        p.constrain(SW, SH, W, H);
        close(p.y, -H, "align anchors constrained above bottom edge");
        p.x = Float.NaN; p.y = Float.POSITIVE_INFINITY; p.scale = -3;
        p.screenAnchor = p.elementAnchor = null; p.normalize();
        check(p.screenAnchor == HudAnchor.TOP_LEFT && p.elementAnchor == HudAnchor.TOP_LEFT, "missing anchors normalize");
        close(p.x, 0, "invalid offset normalizes"); close(p.y, 0, "infinite offset normalizes");
        close(p.scale, .1f, "minimum scale");
    }
    private static void legacyMigrationAndPersistence() {
        Gson gson = new Gson();
        SlhjConfig c = gson.fromJson("""
                {"hud":{"rag_cooldown":{"x":123,"y":45,"scale":1.7}},
                 "values":{"clock.right_aligned":"false","clock.padding":"7","clock.scale":"2.5"}}
                """, SlhjConfig.class);
        HudPlacement rag = HudMigration.resolve(c, "rag_cooldown", defaults());
        close(rag.x, 123, "old absolute X preserved"); close(rag.y, 45, "old absolute Y preserved");
        close(rag.scale, 1.7f, "old scale preserved");
        check(rag.screenAnchor == HudAnchor.TOP_LEFT && rag.elementAnchor == HudAnchor.TOP_LEFT, "old coordinates use top left");
        HudPlacement clockDefault = new HudPlacement(HudAnchor.TOP_RIGHT, HudAnchor.TOP_RIGHT, -5, 5, 1);
        HudPlacement clock = HudMigration.resolve(c, "clock", clockDefault);
        check(clock.screenAnchor == HudAnchor.TOP_LEFT, "legacy left-aligned clock");
        close(clock.x, 7, "clock padding becomes X offset"); close(clock.y, 7, "clock Y padding");
        close(clock.scale, 2.5f, "clock scale preserved");
        clock.anchors(HudAnchor.BOTTOM_CENTER, HudAnchor.CENTER, SW, SH, W, H);
        clock.moveTo(350, 500, SW, SH, W, H);
        SlhjConfig roundTrip = gson.fromJson(gson.toJson(c), SlhjConfig.class);
        HudPlacement saved = HudMigration.resolve(roundTrip, "clock", clockDefault);
        check(saved.screenAnchor == HudAnchor.BOTTOM_CENTER && saved.elementAnchor == HudAnchor.CENTER, "anchors persisted");
        close(saved.x, clock.x, "offset persisted");
        check(HudMigration.resolve(roundTrip, "clock", clockDefault) == saved, "migration reuses saved placement");
        c = new SlhjConfig(); c.set("clock.padding", "9");
        clock = HudMigration.resolve(c, "clock", clockDefault);
        check(clock.screenAnchor == HudAnchor.TOP_RIGHT, "missing legacy alignment defaults right");
        close(clock.x, -9, "right clock padding sign");
        check(c.hud.get("clock") == clock, "one persistent placement per element");
        close(clockDefault.x, -5, "migration leaves reset defaults unchanged");
    }
    private static void click(HudSelection<String> selection, List<String> hits) {
        selection.press(hits, 10, 10); selection.release();
    }
    private static void overlapSelection() {
        var s = new HudSelection<String>();
        List<String> hits = List.of("top", "middle", "bottom");
        click(s, hits); check(s.selected().equals("top"), "first click selects topmost");
        click(s, hits); check(s.selected().equals("middle"), "second click cycles");
        click(s, hits); check(s.selected().equals("bottom"), "third click cycles");
        click(s, hits); check(s.selected().equals("top"), "selection wraps");
        click(s, List.of("middle", "bottom")); check(s.selected().equals("middle"), "new hit set starts topmost");
        click(s, List.of()); check(s.selected() == null, "empty click deselects");
        click(s, List.of("only")); click(s, List.of("only"));
        check(s.selected().equals("only"), "single hit remains selected");
    }
    private static void draggingAndAvailability() {
        var s = new HudSelection<String>();
        s.select("bottom");
        s.press(List.of("top", "bottom"), 10, 10);
        check(!s.drag(11, 11), "small cursor jitter is a click");
        check(s.drag(13, 10), "threshold begins drag");
        check(s.selected().equals("bottom"), "drag retains selected overlapping object");
        s.release(); check(s.selected().equals("bottom"), "drag release does not cycle");
        s.press(List.of("top"), 0, 0); s.drag(3, 0);
        check(s.selected().equals("top"), "drag new hit selects it");
        s.retain(value -> !value.equals("top"));
        check(s.selected() == null && s.pressed() == null && !s.dragging(), "disabled dragged object is cleared");
        s.press(List.of("top", "disabled", "bottom"), 0, 0);
        s.retain(value -> !value.equals("disabled")); s.release();
        check(s.selected().equals("top"), "available topmost selected");
        s.press(List.of("top", "disabled", "bottom"), 0, 0);
        s.retain(value -> !value.equals("disabled")); s.release();
        check(s.selected().equals("bottom"), "disabled overlaps excluded from cycling");
        s.press(List.of("top"), 0, 0); s.cancel(); s.release();
        check(s.selected().equals("bottom"), "cancel preserves prior selection");
    }
    private static void sharedContentAndEnablement() {
        Feature owner = new Feature() { @Override public String id() { return "test"; } };
        AtomicInteger reads = new AtomicInteger();
        HudContent example = HudContent.text("Example", 0xFFFFFFFF);
        HudContent actual = HudContent.text("Actual", 0xFFFFFFFF);
        HudContent[] live = {null};
        var element = new HudElement(owner, "test", "Test", defaults(), () -> {
            reads.incrementAndGet(); return live[0];
        }, () -> example, new SlhjConfig());
        check(element.content(true) == null && reads.get() == 0, "disabled feature never queried or drawn");
        owner.setEnabled(true);
        check(element.content(false) == null, "inactive gameplay stays hidden");
        check(element.content(true) == example, "inactive editor uses example on same element");
        live[0] = actual;
        check(element.content(true) == actual && element.content(false) == actual, "live data shared across modes");
        live[0] = null;
        check(element.content(true) == example, "expired live data falls back without replacing element");
        var placement = element.placement(); placement.x = 500; element.reset();
        check(element.placement() == placement, "reset keeps placement identity");
        close(placement.x, 10, "reset restores defaults");
        owner.setEnabled(false);
        check(element.prepare(true) == null, "disabled preparation skips measurement and rendering");
        var manager = new HudManager();
        manager.register(element);
        check(manager.prepare(true).isEmpty(), "manager excludes disabled elements in editor");
        check(manager.prepare(false).isEmpty(), "manager excludes disabled elements in gameplay");
        try {
            manager.register(element);
            throw new AssertionError("duplicate element ID accepted");
        } catch (IllegalArgumentException expected) {
            check(true, "manager rejects duplicate element registrations");
        }
        check(HudContent.text("first\nsecond\n", 0).lines().equals(List.of("first", "second", "")), "multiline content preserves empty lines");
    }
    private static void timerReadsHaveNoSideEffects() {
        AtomicLong now = new AtomicLong(1000);
        var alert = new TimedHudAlert(now::get);
        check(!alert.isActive(5000) && alert.remainingMs(5000) == 0, "never-triggered timer inactive");
        alert.show(); now.set(3000);
        check(alert.isActive(5000), "triggered timer active");
        close(alert.remainingMs(5000), 3000, "timer remaining time");
        now.set(7000);
        check(!alert.isActive(5000) && alert.remainingMs(5000) == 0, "expired timer hidden");
        now.set(3000);
        check(alert.isActive(5000), "visibility reads do not mutate timer state");
        alert.reset(); check(!alert.isActive(5000), "explicit cleanup resets timer");
    }
}
