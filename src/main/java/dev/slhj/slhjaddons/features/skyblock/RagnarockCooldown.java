package dev.slhj.slhjaddons.features.skyblock;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.hud.HudAnchor;
import dev.slhj.slhjaddons.hud.HudContent;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudPlacement;
import dev.slhj.slhjaddons.hud.TimedHudAlert;
import dev.slhj.slhjaddons.util.client.ChatUtils;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import dev.slhj.slhjaddons.util.skyblock.CooldownUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;

public final class RagnarockCooldown extends Feature {

    private static final long BASE_COOLDOWN_MS = 20_000L;

    private final TimedHudAlert alert = new TimedHudAlert();
    private long cooldownMs = BASE_COOLDOWN_MS;
    private boolean onCooldown = false;

    private final Setting.TextSetting readyMessage;
    private final Setting.ToggleSetting playSoundOnReady;

    public RagnarockCooldown() {
        setLabel("Ragnarock Cooldown Timer");
        category(Category.SKYBLOCK);
        readyMessage = text("rag_cooldown.message", "Ready Message", "&r&5&lRagnarock &aReady!");
        playSoundOnReady = toggle("rag_cooldown.play_sound", "Play Sound On Ready", false);
    }

    public static final String id = "rag_cooldown";
    @Override public String id() { return id; }

    @Override
    public void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!isEnabled() || !overlay) return;
            String text = ChatFormatting.stripFormatting(message.getString());
            if (text.contains("CASTING") && !onCooldown) startCooldown();
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> update());
        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> reset());

        // worldLoad -> reset
        ClientPlayConnectionEvents.JOIN.register((h, s, c) -> reset());

        SlhjAddons.hud().register(new HudElement(this, "rag_cooldown", "Ragnarock Cooldown",
                new HudPlacement(HudAnchor.TOP_LEFT, HudAnchor.TOP_LEFT, 10, 10, 1), this::content,
                () -> HudContent.text("Rag Axe: 12.3", 0xFFFFFFFF)));
    }

    private void startCooldown() {
        cooldownMs = (long) (BASE_COOLDOWN_MS * CooldownUtils.getCooldownMultiplier());
        alert.show();
        onCooldown = true;
    }

    private void reset() {
        onCooldown = false;
        cooldownMs = BASE_COOLDOWN_MS;
        alert.reset();
    }

    private void update() {
        if (!isEnabled() || !onCooldown || alert.remainingMs(cooldownMs) > 0) return;
        onCooldown = false;
        if (playSoundOnReady.value().get() && ClientUtils.player() != null) {
            // Preserve the existing sound setting; sound playback is not implemented yet.
        }
        ChatUtils.chat(readyMessage.value().get());
    }

    private HudContent content() {
        long remaining = alert.remainingMs(cooldownMs);
        if (!onCooldown || remaining <= 0) return null;
        return HudContent.text("Rag Axe: " + String.format("%.1f", remaining / 1000.0), 0xFFFFFFFF);
    }
}
