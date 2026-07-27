package dev.slhj.slhjaddons.util.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class SchedulerUtils {
    private SchedulerUtils() {}

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "slhjaddons-scheduler");
                t.setDaemon(true);
                return t;
            });

    public static void run(Runnable task) {
        ClientUtils.mc().executeBlocking(task);
    }

    public static void runLater(Runnable task, long delayMs) {
        SCHEDULER.schedule(() -> ClientUtils.mc().executeBlocking(task), delayMs, TimeUnit.MILLISECONDS);
    }
}