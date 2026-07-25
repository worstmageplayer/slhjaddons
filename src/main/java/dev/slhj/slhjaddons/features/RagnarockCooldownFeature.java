package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudRenderer;
import dev.slhj.slhjaddons.util.ClientUtils;
import dev.slhj.slhjaddons.util.CooldownUtils;
import dev.slhj.slhjaddons.util.McUtils;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class RagnarockCooldownFeature extends Feature implements HudRenderer {

    private static final long BASE_COOLDOWN_MS = 20_000L;

    private final HudElement hud = new HudElement("rag_cooldown", "Rag Cooldown", 10, 10);
    private long castTime = 0;
    private long cooldownMs = BASE_COOLDOWN_MS;
    private boolean onCooldown = false;

    private final Setting.TextSetting readyMessage;
    private final Setting.ToggleSetting playSoundOnReady;

    public RagnarockCooldownFeature() {
        setLabel("Ragnarok Cooldown Timer");
        category(Category.SKYBLOCK);
        readyMessage = text("rag_cooldown.message", "Ready Message", "&r&5&lRagnarock &aReady!");
        playSoundOnReady = toggle("rag_cooldown.play_sound", "Play Sound On Ready", false);
    }

    @Override public String id() { return "rag_cooldown"; }

    @Override
    public void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!isEnabled() || !overlay) return;
            String text = ChatFormatting.stripFormatting(message.getString());
            if (text.contains("CASTING") && !onCooldown) startCooldown();
        });

        // worldLoad -> reset
        ClientPlayConnectionEvents.JOIN.register((h, s, c) -> reset());

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "rag_cooldown"),
                (graphics, delta) -> render(graphics));
    }

    private void startCooldown() {
        castTime = System.currentTimeMillis();
        cooldownMs = (long) (BASE_COOLDOWN_MS * CooldownUtils.getCooldownMultiplier());
        onCooldown = true;
    }

    private void reset() {
        onCooldown = false;
        cooldownMs = BASE_COOLDOWN_MS;
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || !onCooldown) return;

        long remaining = cooldownMs - (System.currentTimeMillis() - castTime);
        if (remaining <= 0) {
            onCooldown = false;
            if (playSoundOnReady.value().get() && ClientUtils.player() != null) {
                // World.playSound('random.successful_hit') -> level.playLocalSound(...)
            }
            McUtils.chat(readyMessage.value().get());
            return;
        }

        String label = "Rag Axe: " + String.format("%.1f", remaining / 1000.0);
        RenderUtils.pushScale(g, hud.scale());
        RenderUtils.text(g, label, (int)(hud.x() / hud.scale()), (int)(hud.y() / hud.scale()), 0xFFFFFFFF, true);
        RenderUtils.pop(g);
    }

    @Override
    public HudElement hudElement() {
        return hud;
    }

    @Override
    public String hudPreviewText() {
        return "Rag Axe: 12.3";
    }

    @Override
    public void renderHudPreview(GuiGraphicsExtractor g) {
        RenderUtils.pushScale(g, hud.scale());
        RenderUtils.text(g, hudPreviewText(), (int) (hud.x() / hud.scale()), (int) (hud.y() / hud.scale()), 0xFFFFFFFF, true);
        RenderUtils.pop(g);
    }
}