package dev.slhj.slhjaddons.features.pets;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.hud.HudAnchor;
import dev.slhj.slhjaddons.hud.HudContent;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudPlacement;
import dev.slhj.slhjaddons.hud.TimedHudAlert;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;

public final class AutoPetRules extends Feature {

    private static final Pattern PET_PATTERN =
            Pattern.compile("Autopet equipped your (\\[Lvl (\\d+)]) (.+?)! VIEW RULE");
    private static final long DURATION_MS = 5000;
    private static final double FADE_START = 0.7;

    private final TimedHudAlert alert = new TimedHudAlert();
    private String label = null;

    private static final String PREVIEW_LABEL = "§7[Lvl 100] §r§dBlack Cat";

    public AutoPetRules() {
        setLabel("Auto Pet Rules");
        category(Category.PETS);
    }

    public static final String id = "auto_pet_rules";
    @Override public String id() { return id; }

    @Override
    public void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!isEnabled() || overlay) return;
            String raw = message.getString();
            String stripped = ChatFormatting.stripFormatting(raw);

            var matcher = PET_PATTERN.matcher(stripped);
            if (matcher.find()) {
                int[] rawIndex = strippedToRawIndex(raw);
                int rawStart = rawIndex[matcher.start(1)];
                int rawEnd = rawIndex[matcher.end(3)];
                label = raw.substring(rawStart, rawEnd);
                alert.show();
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (label != null && !alert.isActive(DURATION_MS)) label = null;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> {
            label = null;
            alert.reset();
        });
        SlhjAddons.hud().register(new HudElement(this, "auto_pet_rules", "Auto Pet Rules",
                new HudPlacement(HudAnchor.TOP_LEFT, HudAnchor.TOP_LEFT, 10, 100, 1), this::content,
                () -> HudContent.text(PREVIEW_LABEL, 0xFFFFFFFF)));
    }

    private static int[] strippedToRawIndex(String raw) {
        int[] map = new int[raw.length() + 1];
        int stripped = 0;
        int i = 0;
        while (i < raw.length()) {
            char c = raw.charAt(i);
            if (c == '§' && i + 1 < raw.length() && ChatFormatting.getByCode(raw.charAt(i + 1)) != null) {
                i += 2;
                continue;
            }
            map[stripped++] = i;
            i++;
        }
        map[stripped] = raw.length();
        return map;
    }

    private HudContent content() {
        if (label == null || !alert.isActive(DURATION_MS)) return null;
        int alpha = alert.alpha(DURATION_MS, FADE_START);
        return alpha <= 0 ? null : HudContent.text(label, (alpha << 24) | 0xFFFFFF);
    }
}
