package dev.slhj.slhjaddons.features.misc;

import dev.slhj.slhjaddons.util.client.ChatUtils;
import dev.slhj.slhjaddons.util.client.ClickPacer;

public final class InputRegressionTests {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void run() {
        for (int cps = 0; cps <= 20; cps++) {
            ClickPacer pacer = new ClickPacer();
            int clicks = 0;
            for (int tick = 0; tick < 200; tick++) if (pacer.tick(cps, true)) clicks++;
            check(clicks == cps * 10, "accurate rate over ten seconds: " + cps);
        }
        ClickPacer pacer = new ClickPacer();
        check(pacer.tick(6, true), "initial click is immediate");
        check(!pacer.tick(6, false), "GUI, key release or disable prevents clicks");
        check(pacer.tick(6, true), "resume has no old fractional delay");
        check(!pacer.tick(0, true), "zero stops an active clicker");

        var wheel = new CommandWheelSession();
        check(wheel.update(true, false) == CommandWheelSession.Action.NONE, "empty wheel does nothing");
        check(wheel.update(true, true) == CommandWheelSession.Action.NONE, "held key cannot open on availability change");
        wheel.update(false, true);
        check(wheel.update(true, true) == CommandWheelSession.Action.OPEN, "fresh press opens wheel");
        check(wheel.isOpen(), "opened wheel visible");
        check(wheel.update(true, true) == CommandWheelSession.Action.NONE, "holding does not reopen");
        wheel.cancel();
        check(!wheel.isOpen(), "screen change immediately cancels");
        check(wheel.update(false, true) == CommandWheelSession.Action.NONE, "release after screen change does not execute");
        wheel.update(true, true);
        check(wheel.update(false, true) == CommandWheelSession.Action.RELEASE, "normal release commits once");
        check(!wheel.isOpen(), "session closes before command dispatch");
        check(wheel.update(false, true) == CommandWheelSession.Action.NONE, "no repeat dispatch");
        wheel.update(true, true);
        check(wheel.update(true, false) == CommandWheelSession.Action.CANCEL, "disable cancels an open wheel");
        check(wheel.update(false, true) == CommandWheelSession.Action.NONE, "disable cannot leave a pending command");
        check(ChatUtils.normalizeCommand(" /warp hub ").equals("warp hub"), "server command slash and outer whitespace normalized");
        check(ChatUtils.normalizeCommand("slhj hud").equals("slhj hud"), "local command preserved");
        check(ChatUtils.normalizeCommand("/slhj").equals("slhj"), "local slash normalized");
        check(ChatUtils.normalizeCommand("/").isEmpty(), "empty slash ignored");
        check(ChatUtils.normalizeCommand(null).isEmpty(), "null ignored");
        System.out.println("Input regression checks passed: " + checks);
    }
}
