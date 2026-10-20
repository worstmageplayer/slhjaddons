package dev.slhj.slhjaddons.features.dungeons;

import java.util.concurrent.atomic.AtomicLong;

public final class DungeonJoinRegressionTests {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void run() {
        AtomicLong now = new AtomicLong(1000);
        var queue = new DungeonJoinQueue(now::get);
        check(queue.enqueue("f7"), "first join accepted");
        check(!queue.enqueue("m7"), "duplicate cannot replace first queued floor");
        check(queue.poll() == null, "initial delay respected");
        now.set(1100);
        check("f7".equals(queue.poll()), "first queued floor dispatched");
        check(queue.poll() == null, "dispatch occurs only once");
        now.set(2000);
        check(queue.remainingMs() == 29100, "cooldown starts at actual dispatch");
        check(queue.enqueue("m7"), "next floor can queue during cooldown");
        now.set(31_199);
        check(queue.poll() == null, "queued request waits until cooldown plus send delay");
        now.set(31_200);
        check("m7".equals(queue.poll()), "queued request dispatched after cooldown");
        check(queue.remainingMs() == 30_000, "queued dispatch starts a full cooldown");
        queue.enqueue("f1");
        queue.reset();
        now.set(100_000);
        check(queue.poll() == null, "disable/disconnect cancels pending request");
        check(queue.remainingMs() == 0, "new session has no stale cooldown");
        check(queue.enqueue("f2"), "queue works again after cancellation");
        System.out.println("Dungeon queue regression checks passed: " + checks);
    }
}
