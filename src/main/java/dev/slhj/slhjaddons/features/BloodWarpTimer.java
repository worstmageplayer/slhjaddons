package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.hud.TimedHudAlert;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class BloodWarpTimer extends Feature {

    private static final long DOOR_TIMER_MS = 20_000;
    private final TimedHudAlert alert = new TimedHudAlert();

    public BloodWarpTimer() {
        setLabel("Blood Warp Timer");
        category(Category.DUNGEONS);
    }

    public static final String id = "blood_warp_timer";
    @Override public String id() { return id; }

    @Override
    public void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!isEnabled()) return;
            String text = ChatFormatting.stripFormatting(message.getString());
            if (text.contains("The BLOOD DOOR has been opened!")) {
                alert.show();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> alert.reset());

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "blood_warp_timer"),
                (graphics, delta) -> render(graphics));
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || !alert.isActive(DOOR_TIMER_MS)) return;

        double seconds = alert.elapsedMs() / 1000.0;
        String text = String.format("%.1f", seconds);
        int color = seconds < 5 ? 0xFF00AA00 : seconds < 7.5 ? 0xFFFFAA00 : 0xFFAA0000;

        int screenWidth = g.guiWidth();
        int screenHeight = g.guiHeight();
        int x = (screenWidth - RenderUtils.stringWidth(text)) / 2;
        int y = (screenHeight / 2) - 10;
        RenderUtils.text(g, text, x, y, color, true);
    }
}