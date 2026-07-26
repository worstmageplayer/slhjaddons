package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudRenderer;
import dev.slhj.slhjaddons.hud.TimedHudAlert;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class GoldenFishAlert extends Feature implements HudRenderer {

    private static final long DEFAULT_ALERT_DURATION_MS = 3000;

    private final Setting.HexSetting colorSetting;
    private final Setting.SliderSetting durationSetting;

    private final HudElement hud = new HudElement("golden_fish_alert", "Golden Fish Alert", 10, 10);
    private final TimedHudAlert alert = new TimedHudAlert();

    public GoldenFishAlert() {
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
                alert.show();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> alert.reset());

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "golden_fish_alert"),
                (graphics, delta) -> render(graphics));
    }

    private void render(GuiGraphicsExtractor g) {
        long duration = durationSetting.value().get().intValue();
        if (!isEnabled() || !alert.isActive(duration)) return;

        String text = "Golden Fish!";
        int color = colorSetting.value().get();
        RenderUtils.pushScale(g, hud.scale());
        RenderUtils.text(g, text, (int) (hud.x() / hud.scale()), (int) (hud.y() / hud.scale()), color, true);
        RenderUtils.pop(g);
    }

    @Override
    public HudElement hudElement() {
        return hud;
    }

    @Override
    public String hudPreviewText() {
        return "Golden Fish!";
    }

    @Override
    public void renderHudPreview(GuiGraphicsExtractor g) {
        int color = colorSetting.value().get();
        RenderUtils.pushScale(g, hud.scale());
        RenderUtils.text(g, hudPreviewText(), (int) (hud.x() / hud.scale()), (int) (hud.y() / hud.scale()), color, true);
        RenderUtils.pop(g);
    }
}