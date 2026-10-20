package dev.slhj.slhjaddons.util.skyblock;

import dev.slhj.slhjaddons.util.client.ClientUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jspecify.annotations.Nullable;

public final class CooldownUtils {
    private CooldownUtils() {}

    private static final MageCooldownState STATE = new MageCooldownState();
    @Nullable private static ClientLevel level;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> refresh());
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay && refresh()) STATE.onChat(ChatFormatting.stripFormatting(message.getString()));
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    }

    public static double getCooldownMultiplier() {
        if (!refresh() || ClientUtils.player() == null) return 1.0;
        return STATE.multiplier(ClientUtils.player().getGameProfile().name());
    }

    /** Refresh on demand as well as on ticks so the first cast uses the latest tab data. */
    private static boolean refresh() {
        var currentLevel = ClientUtils.mc().level;
        if (currentLevel != level) {
            STATE.reset();
            level = currentLevel;
        }
        if (currentLevel == null || !HypixelUtils.inDungeon()) {
            STATE.reset();
            return false;
        }
        STATE.updateTab(ClientUtils.tabListPlayers().stream().map(ChatFormatting::stripFormatting).toList());
        return true;
    }

    private static void reset() {
        STATE.reset();
        level = null;
    }
}
