package dev.slhj.slhjaddons.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class McUtils {
    private McUtils() {}

    public static final Minecraft MC = Minecraft.getInstance();

    public static void chat(String msg) {
        LocalPlayer p = ClientUtils.player();
        if (p != null) p.sendSystemMessage(Component.literal(colorize(msg)));
    }

    public static void runCommand(String cmd) {
        LocalPlayer p = ClientUtils.player();
        if (p != null) p.connection.sendCommand(cmd);
    }

    public static List<String> getTabListPlayers() {
        if (MC.getConnection() == null) return List.of();
        List<String> names = new ArrayList<>();
        for (var info : MC.getConnection().getListedOnlinePlayers()) {
            var display = info.getTabListDisplayName();
            names.add(display != null ? display.getString() : info.getProfile().name());
        }
        return names;
    }

    public static int selectedSlot() {
        LocalPlayer p = ClientUtils.player();
        return p == null ? 0 : p.getInventory().getSelectedSlot();
    }

    public static void setSelectedSlot(int index) {
        LocalPlayer p = ClientUtils.player();
        if (p != null && index >= 0 && index < 9) p.getInventory().setSelectedSlot(index);
    }

    public static void centerCursor() {
        var window = MC.getWindow();
        GLFW.glfwSetCursorPos(window.handle(), window.getWidth() / 2.0, window.getHeight() / 2.0);
    }

    public static void rightClick() {
        LocalPlayer p = ClientUtils.player();
        if (p != null && MC.gameMode != null) MC.gameMode.useItem(p, InteractionHand.MAIN_HAND);
    }

    public static void leftClick(boolean requireEntityInRange) {
        LocalPlayer p = ClientUtils.player();
        if (p == null || MC.gameMode == null) return;

        if (requireEntityInRange) {
            HitResult hit = MC.hitResult;
            if (hit instanceof EntityHitResult entityHit) {
                Entity target = entityHit.getEntity();
                MC.gameMode.attack(p, target);
                p.swing(InteractionHand.MAIN_HAND);
            }
        } else {
            p.swing(InteractionHand.MAIN_HAND);
        }
    }

    public static void scheduleTask(Runnable task) {
        MC.executeBlocking(task);
    }

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "slhjaddons-scheduler");
                t.setDaemon(true);
                return t;
            });

    public static void scheduleTask(Runnable task, long delayMs) {
        SCHEDULER.schedule(() -> MC.executeBlocking(task), delayMs, TimeUnit.MILLISECONDS);
    }

    public static boolean itemNameContains(ItemStack item, String text) {
        if (item.isEmpty()) return false;
        String name = item.getHoverName().getString();
        return name.contains(text);
    }

    public static List<String> getSidebarLines() {
        return ScoreboardUtils.lines();
    }

    public static String colorize(String s) {
        return s == null ? "" : s.replace('&', '\u00a7');
    }
}
