package dev.slhj.slhjaddons.features.dungeons;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.hud.HudAnchor;
import dev.slhj.slhjaddons.hud.HudContent;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudPlacement;
import dev.slhj.slhjaddons.hud.TimedHudAlert;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;

public final class BloodWarpTimer extends Feature {
    private static final long DOOR_TIMER_MS = 20_000;
    private final TimedHudAlert alert = new TimedHudAlert();

    public BloodWarpTimer() {
        setLabel("Blood Warp Timer");
        setDescription("Show elapsed time after the Blood Door opens.");
        setCategory(Category.DUNGEONS);
    }

    public static final String ID = "blood_warp_timer";
    @Override public String id() { return ID; }

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

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!alert.isActive(DOOR_TIMER_MS)) alert.reset();
        });
        SlhjAddons.hud().register(new HudElement(this, "blood_warp_timer", "Blood Warp Timer",
                new HudPlacement(HudAnchor.CENTER, HudAnchor.CENTER, 0, -5, 1), this::content,
                () -> HudContent.text("5.0", 0xFFFFAA00)));
    }

    private HudContent content() {
        if (!alert.isActive(DOOR_TIMER_MS)) return null;
        double seconds = alert.elapsedMs() / 1000.0;
        int color = seconds < 5 ? 0xFF00AA00 : seconds < 7.5 ? 0xFFFFAA00 : 0xFFAA0000;
        return HudContent.text(String.format("%.1f", seconds), color);
    }
}
