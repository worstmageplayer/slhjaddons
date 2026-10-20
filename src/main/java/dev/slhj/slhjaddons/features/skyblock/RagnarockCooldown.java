package dev.slhj.slhjaddons.features.skyblock;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.hud.HudAnchor;
import dev.slhj.slhjaddons.hud.HudContent;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudPlacement;
import dev.slhj.slhjaddons.util.client.ChatUtils;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import dev.slhj.slhjaddons.util.skyblock.CooldownUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class RagnarockCooldown extends Feature {
    private static final long BASE_COOLDOWN_MS = 20_000L;

    private final RagnarockCooldownTimer timer = new RagnarockCooldownTimer(
            () -> Math.round(BASE_COOLDOWN_MS * CooldownUtils.getCooldownMultiplier()));

    private final Setting.TextSetting readyMessage;
    private final Setting.ToggleSetting playSoundOnReady;
    private final Setting.SliderSetting readyVolume;

    public RagnarockCooldown() {
        setLabel("Ragnarock Cooldown Timer");
        setDescription("Track Ragnarock cooldown, including dungeon Mage reductions.");
        setCategory(Category.SKYBLOCK);
        readyMessage = text("rag_cooldown.message", "Ready Message", "&r&5&lRagnarock &aReady!");
        playSoundOnReady = toggle("rag_cooldown.play_sound", "Play Sound On Ready", false);
        readyVolume = intSlider("rag_cooldown.sound_volume", "Ready Sound Volume (%)", 0, 100, 50);
    }

    public static final String ID = "rag_cooldown";
    @Override public String id() { return ID; }

    @Override
    public void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!isEnabled() || !overlay) return;
            String text = ChatFormatting.stripFormatting(message.getString());
            timer.onActionBar(text);
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> update());
        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> timer.reset());

        ClientPlayConnectionEvents.JOIN.register((h, s, c) -> timer.reset());

        SlhjAddons.hud().register(new HudElement(this, "rag_cooldown", "Ragnarock Cooldown",
                new HudPlacement(HudAnchor.TOP_LEFT, HudAnchor.TOP_LEFT, 10, 10, 1), this::content,
                () -> HudContent.text("Rag Axe: 12.3", 0xFFFFFFFF)));
    }

    private void update() {
        if (!isEnabled() || !timer.tick()) return;
        if (playSoundOnReady.value().get() && ClientUtils.player() != null) {
            ClientUtils.mc().getSoundManager().play(SimpleSoundInstance.forUI(
                    SoundEvents.NOTE_BLOCK_PLING.value(), 1f, readyVolume.value().get().floatValue() / 100f));
        }
        ChatUtils.chat(readyMessage.value().get());
    }

    private HudContent content() {
        long remaining = timer.remainingMs();
        if (remaining <= 0) return null;
        return HudContent.text("Rag Axe: " + String.format("%.1f", remaining / 1000.0), 0xFFFFFFFF);
    }

    @Override
    protected void onDisable() {
        timer.reset();
    }
}
