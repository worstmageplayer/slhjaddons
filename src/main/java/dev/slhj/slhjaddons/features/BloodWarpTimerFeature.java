package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class BloodWarpTimerFeature extends Feature {

    private long bloodOpenTime = 0;

    public BloodWarpTimerFeature() {
        setLabel("Blood Warp Timer");
        category(Category.DUNGEONS);
    }

    @Override public String id() { return "blood_warp_timer"; }

    @Override
    public void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!isEnabled()) return;
            String text = ChatFormatting.stripFormatting(message.getString());
            if (text.contains("The BLOOD DOOR has been opened!")) {
                bloodOpenTime = System.currentTimeMillis();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            bloodOpenTime = 0;
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "blood_warp_timer"),
                (graphics, delta) -> render(graphics));
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || bloodOpenTime == 0) return;

        long elapsed = System.currentTimeMillis() - bloodOpenTime;
        double seconds = elapsed / 1000.0;

        if (seconds > 20) {
            bloodOpenTime = 0;
            return;
        }

        String text = String.format("%.1f", seconds);
        int color;
        if (seconds < 5) {
            color = 0xFF00AA00;
        } else if (seconds < 7.5) {
            color = 0xFFFFAA00;
        } else {
            color = 0xFFAA0000;
        }

        int screenWidth = g.guiWidth();
        int screenHeight = g.guiHeight();
        int x = (screenWidth - RenderUtils.stringWidth(text)) / 2;
        int y = (screenHeight / 2) - 10;
        RenderUtils.text(g, text, x, y, color, true);
    }
}
