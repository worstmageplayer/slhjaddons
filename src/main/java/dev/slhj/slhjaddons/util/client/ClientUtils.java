package dev.slhj.slhjaddons.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

public final class ClientUtils {
    private ClientUtils() {}

    public static Minecraft mc() { return Minecraft.getInstance(); }

    public static LocalPlayer player() { return mc().player; }

    public static boolean inGui() { return mc().screen != null; }

    public static int screenWidth() { return mc().getWindow().getGuiScaledWidth(); }

    public static int screenHeight() { return mc().getWindow().getGuiScaledHeight(); }

    public static int selectedSlot() {
        LocalPlayer p = player();
        return p == null ? 0 : p.getInventory().getSelectedSlot();
    }

    public static String hoverName(ItemStack item) {
        if (item.isEmpty()) return null;
        return item.getHoverName().getString();
    }

    public static java.util.List<String> tabListPlayers() {
        if (mc().getConnection() == null) return java.util.List.of();
        var names = new java.util.ArrayList<String>();
        for (var info : mc().getConnection().getListedOnlinePlayers()) {
            var display = info.getTabListDisplayName();
            names.add(display != null ? display.getString() : info.getProfile().name());
        }
        return names;
    }
}