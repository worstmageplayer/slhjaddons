package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class GoldenFishFeature extends Feature {

    private final HudElement hud = new HudElement("golden_fish_alert", "Golden Fish Alert", 10, 10);
    private long alertShowTime = 0;
    private static final long DEFAULT_ALERT_DURATION_MS = 3000;

    private final Setting.HexSetting colorSetting;
    private final Setting.SliderSetting durationSetting;

    public GoldenFishFeature() {
        setLabel("Golden Fish Alert");
        category(Category.FISHING);
        colorSetting = hex("golden_fish_alert.color", "Alert Color", 0xFFFFAA00);
        durationSetting = intSlider("golden_fish_alert.duration", "Alert Duration (ms)", 500, 8000, (int) DEFAULT_ALERT_DURATION_MS);
    }

    @Override public String id() { return "golden_fish_alert"; }

    @Override
    public void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!isEnabled() || overlay) return;
            String text = ChatFormatting.stripFormatting(message.getString());
            if (text.contains("You spot a Golden Fish surface from beneath the lava!")) {
                alertShowTime = System.currentTimeMillis();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            alertShowTime = 0;
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "golden_fish_alert"),
                (graphics, delta) -> render(graphics));
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || alertShowTime == 0) return;

        long elapsed = System.currentTimeMillis() - alertShowTime;
        if (elapsed > durationSetting.value().get().intValue()) {
            alertShowTime = 0;
            return;
        }

        String text = "Golden Fish!";
        int screenWidth = g.guiWidth();
        int screenHeight = g.guiHeight();
        int textWidth = RenderUtils.stringWidth(text);
        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight / 2);
        int color = colorSetting.value().get();

        RenderUtils.pushScale(g, 5.0f);
        RenderUtils.text(g, text, x / 5, y / 5, color, true);
        RenderUtils.pop(g);
    }
}