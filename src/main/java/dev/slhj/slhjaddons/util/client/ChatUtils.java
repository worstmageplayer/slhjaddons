package dev.slhj.slhjaddons.util.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class ChatUtils {
    private ChatUtils() {}

    public static void chat(String message) {
        LocalPlayer player = ClientUtils.player();
        if (player != null) player.sendSystemMessage(Component.literal(colorize(message)));
    }

    public static void runCommand(String command) {
        command = normalizeCommand(command);
        if (command.isEmpty()) return;
        LocalPlayer player = ClientUtils.player();
        if (player != null) player.connection.sendCommand(command);
    }

    public static String normalizeCommand(String command) {
        if (command == null) return "";
        String normalized = command.strip();
        return normalized.startsWith("/") ? normalized.substring(1).stripLeading() : normalized;
    }

    @Nullable
    public static String colorize(String text) {
        return text == null ? null : text.replace('&', '§');
    }
}
