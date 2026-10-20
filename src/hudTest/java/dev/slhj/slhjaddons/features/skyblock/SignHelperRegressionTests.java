package dev.slhj.slhjaddons.features.skyblock;

import java.util.concurrent.atomic.AtomicInteger;

public final class SignHelperRegressionTests {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void run() {
        AtomicInteger evaluations = new AtomicInteger();
        var sign = new SignInputState(expression -> {
            evaluations.incrementAndGet();
            if (expression.equals("bad")) throw new IllegalArgumentException();
            return new SignInputState.Result("1000000000000000", "1,000,000,000,000,000");
        });
        check(sign.submission().equals(""), "untouched fresh sign never submits null");
        sign.update("=10^15");
        check(sign.submission().equals("1000000000000000"), "long result never truncated");
        check(sign.display().equals("10^15 = 1,000,000,000,000,000"), "preview matches result");
        sign.update("=10^15");
        check(evaluations.get() == 1, "unchanged input is not reevaluated on submit or resize");
        sign.reset();
        check(sign.display() == null && sign.submission().isEmpty(), "new or unsupported sign cannot reuse prior result");
        sign.update("bad");
        check(sign.submission().equals("bad") && sign.display() == null, "failed evaluation preserves current text");
        sign.update("");
        check(sign.submission().isEmpty(), "clearing input clears cached result");
        sign.update(null);
        check(sign.submission().isEmpty(), "null input normalized");
        sign.reset(); sign.update("=10^15");
        check(evaluations.get() == 3, "new session evaluates same expression independently");
        System.out.println("Sign helper regression checks passed: " + checks);
    }
}
