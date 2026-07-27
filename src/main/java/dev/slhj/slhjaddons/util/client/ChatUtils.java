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
        LocalPlayer p = ClientUtils.player();
        if (p != null) p.connection.sendCommand(cmd);
    }

    @Nullable
    public static String colorize(String s) {
        return s == null ? null : s.replace('&', '§');
    }
}