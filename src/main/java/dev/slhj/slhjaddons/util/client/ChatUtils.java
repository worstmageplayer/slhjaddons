package dev.slhj.slhjaddons.util.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class ChatUtils {
    private ChatUtils() {}

    public static void chat(String msg) {
        LocalPlayer p = ClientUtils.player();
        if (p != null) p.sendSystemMessage(Component.literal(colorize(msg)));
    }

    public static void runCommand(String cmd) {
        cmd = normalizeCommand(cmd);
        if (cmd.isEmpty()) return;
        LocalPlayer p = ClientUtils.player();
        if (p != null) p.connection.sendCommand(cmd);
    }

    public static String normalizeCommand(String cmd) {
        if (cmd == null) return "";
        String normalized = cmd.strip();
        return normalized.startsWith("/") ? normalized.substring(1).stripLeading() : normalized;
    }

    @Nullable
    public static String colorize(String s) {
        return s == null ? null : s.replace('&', '§');
    }
}