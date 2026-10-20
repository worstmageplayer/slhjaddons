package dev.slhj.slhjaddons.features.skyblock;

import java.util.function.Function;

/** Cache belongs to one sign session, never to a previously opened sign. */
final class SignInputState {
    record Result(String value, String formatted) {}
    private final Function<String, Result> evaluate;
    private String raw, display;
    private Result result;

    SignInputState(Function<String, Result> evaluate) { this.evaluate = evaluate; }

    void reset() { raw = display = null; result = null; }

    void update(String input) {
        input = input == null ? "" : input;
        if (input.equals(raw)) return;
        raw = input;
        result = null;
        display = null;
        if (input.isBlank()) return;
        String expression = input.trim();
        if (expression.startsWith("=")) expression = expression.substring(1);
        try {
            result = evaluate.apply(expression);
            display = result.formatted().equals(expression) ? result.formatted()
                    : expression + " = " + result.formatted();
        } catch (RuntimeException ignored) {
            // An unsuccessful calculation must never replace the user's text.
        }
    }

    String display() { return display; }
    String submission() { return result != null ? result.value() : raw == null ? "" : raw; }
}
