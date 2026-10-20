package dev.slhj.slhjaddons.features.fishing;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.hud.HudAnchor;
import dev.slhj.slhjaddons.hud.HudContent;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudPlacement;
import dev.slhj.slhjaddons.hud.TimedHudAlert;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;

public final class GoldenFishAlert extends Feature {

    private static final long DEFAULT_ALERT_DURATION_MS = 3000;

    private final Setting.HexSetting colorSetting;
    private final Setting.SliderSetting durationSetting;
    private final Setting.TextSetting alertText;

    private final TimedHudAlert alert = new TimedHudAlert();

    public GoldenFishAlert() {
        setLabel("Golden Fish Alert");
        category(Category.FISHING);
        colorSetting = hex("golden_fish_alert.color", "Alert Color", 0xFFFFAA00);
        durationSetting = intSlider("golden_fish_alert.duration", "Alert Duration (ms)", 500, 8000, (int) DEFAULT_ALERT_DURATION_MS);
        alertText = text("golden_fish_alert.text", "Alert Text", "Golden Fish!");
    }

    public static final String id = "golden_fish_alert";
    @Override public String id() { return id; }

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

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!alert.isActive(durationSetting.value().get().longValue())) alert.reset();
        });
        SlhjAddons.hud().register(new HudElement(this, "golden_fish_alert", "Golden Fish Alert",
                new HudPlacement(HudAnchor.TOP_LEFT, HudAnchor.TOP_LEFT, 10, 10, 1),
                () -> alert.isActive(durationSetting.value().get().longValue()) ? content() : null,
                this::content));
    }

    private HudContent content() {
        return HudContent.text(alertText.value().get(), colorSetting.value().get());
    }
}
