package dev.slhj.slhjaddons.features.fishing;

public final class FishingRegressionTests {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void run() {
        var cycle = new FishingCycle();
        check(!cycle.observe("!!!"), "unarmed alert cannot reel");
        check(!cycle.observe("1.2"), "timer arms without clicking");
        check(cycle.observe("!!!"), "alert reels once");
        check(!cycle.observe("!!!"), "duplicate alert cannot reel twice");
        for (int i = 1; i < 20; i++) check(!cycle.tick(true, 20), "recast waits full delay");
        check(cycle.tick(true, 20), "recast after configured delay");
        check(!cycle.tick(true, 20), "recast occurs once");
        cycle.observe("0.1"); cycle.observe("!!!");
        check(!cycle.tick(false, 0), "invalid context cancels even a due recast");
        for (int i = 0; i < 30; i++) check(!cycle.tick(true, 20), "closing menu cannot revive cancelled recast");
        cycle.observe("0.5"); cycle.reset();
        check(!cycle.observe("!!!"), "disable/disconnect also clears armed state");
        cycle.observe("0.5"); cycle.observe("!!!");
        check(cycle.tick(true, 0), "zero delay recasts at next tick");
        System.out.println("Fishing regression checks passed: " + checks);
    }
}
