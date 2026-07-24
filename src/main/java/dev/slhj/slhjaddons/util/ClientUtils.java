package dev.slhj.slhjaddons.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class ClientUtils {
    private ClientUtils() {}

    public static final Minecraft MC = Minecraft.getInstance();

    public static LocalPlayer player() { return MC.player; }

    public static boolean inGui() { return MC.screen != null; }

    public static int screenWidth() { return MC.getWindow().getGuiScaledWidth(); }
    public static int screenHeight() { return MC.getWindow().getGuiScaledHeight(); }
}
