package dev.slhj.slhjaddons.features.skyblock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class RagnarockCooldownRegressionTests {
    private static int checks;

    public static void run() {
        repeatedAndBuffMessages();
        nextCastUsesLatestMultiplier();
        newCastBetweenTicks();
        resetAndCancellation();
        System.out.println("Ragnarock cooldown regression checks passed: " + checks);
    }

    private static void check(boolean condition, String description) {
        checks++;
        if (!condition) throw new AssertionError(description);
    }

    private static void repeatedAndBuffMessages() {
        AtomicLong now = new AtomicLong();
        AtomicInteger starts = new AtomicInteger();
        var timer = new RagnarockCooldownTimer(() -> {
            starts.incrementAndGet();
            return 5000;
        }, now::get);
        check(timer.remainingMs() == 0 && !timer.tick(), "idle timer never emits ready");
        timer.onActionBar("CASTING 10s");
        timer.onActionBar("BROADCASTING IN 3s");
        timer.onActionBar("1,000/1,000❤ 100 Defense");
        check(starts.get() == 0, "only a casting-start message reads Mage cooldown");
        timer.onActionBar("1,000/1,000❤ CASTING IN 3s 1,000 Mana");
        check(timer.remainingMs() == 5000 && starts.get() == 1, "channel starts one Mage-adjusted cooldown");
        now.set(1000);
        timer.onActionBar("CASTING IN 2s");
        check(timer.remainingMs() == 4000 && starts.get() == 1, "channel updates cannot restart cooldown");
        now.set(3000);
        timer.onActionBar("CASTING 10s");
        check(timer.remainingMs() == 2000, "buff activation does not restart cooldown");
        now.set(5000);
        check(timer.remainingMs() == 0 && timer.remainingMs() == 0, "render reads do not finish cooldown");
        check(timer.tick(), "ready emitted at cooldown deadline");
        check(!timer.tick(), "ready emitted only once");
        now.set(6000);
        timer.onActionBar("CASTING 7s");
        check(timer.remainingMs() == 0 && !timer.tick() && starts.get() == 1,
                "remaining buff messages cannot create a second timer after short Mage cooldown");
    }

    private static void nextCastUsesLatestMultiplier() {
        AtomicLong now = new AtomicLong(1000), cooldown = new AtomicLong(6000);
        var timer = new RagnarockCooldownTimer(cooldown::get, now::get);
        timer.onActionBar("CASTING IN 3s");
        cooldown.set(11_000);
        now.set(2000);
        check(timer.remainingMs() == 5000, "an active cooldown retains its starting multiplier");
        now.set(7000);
        check(timer.tick(), "first cast completes");
        timer.onActionBar("CASTING IN 3s");
        check(timer.remainingMs() == 11_000, "new cast uses updated Mage/party state");
    }

    private static void newCastBetweenTicks() {
        AtomicLong now = new AtomicLong(1000);
        var timer = new RagnarockCooldownTimer(() -> 5000, now::get);
        timer.onActionBar("CASTING IN 3s");
        now.set(6001);
        timer.onActionBar("CASTING IN 3s");
        check(timer.remainingMs() == 5000, "a new cast just after expiry is accepted before the next tick");
        check(!timer.tick(), "old completion cannot emit ready during a new cast");
    }

    private static void resetAndCancellation() {
        AtomicLong now = new AtomicLong(1000);
        var timer = new RagnarockCooldownTimer(() -> 20_000, now::get);
        timer.onActionBar("CASTING IN 3s");
        now.set(4000);
        timer.onActionBar("CANCELLED");
        check(timer.remainingMs() == 17_000, "cancelled channel retains the original ability cooldown");
        timer.reset();
        now.set(30_000);
        check(timer.remainingMs() == 0 && !timer.tick(), "disconnect or disable resets without a stale ready message");
        timer.onActionBar("CASTING 7s");
        check(timer.remainingMs() == 0, "reset does not turn buff updates into activations");
        timer.onActionBar("CASTING IN 3s");
        check(timer.remainingMs() == 20_000, "fresh activation works after reset");
    }
}
