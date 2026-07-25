package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudRenderer;
import dev.slhj.slhjaddons.hud.TimedHudAlert;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.regex.Pattern;

public final class AutoPetRulesFeature extends Feature implements HudRenderer {

    private static final Pattern PET_PATTERN =
            Pattern.compile("Autopet equipped your (\\[Lvl (\\d+)\\]) (.+?)! VIEW RULE");
    private static final long DURATION_MS = 5000;
    private static final double FADE_START = 0.7;

    private final TimedHudAlert alert = new TimedHudAlert();
    private String label = null;

    private static final String PREVIEW_LABEL = "§7[Lvl 100] §r§dBlack Cat";

    private final HudElement hud = new HudElement("auto_pet_rules", "Auto Pet Rules", 10, 100);

    public AutoPetRulesFeature() {
        setLabel("Auto Pet Rules");
        category(Category.PETS);
    }

    @Override public String id() { return "auto_pet_rules"; }

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

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "auto_pet_rules"),
                (graphics, delta) -> render(graphics));
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

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || label == null) return;
        if (!alert.isActive(DURATION_MS)) { label = null; return; }

        int alpha = alert.alpha(DURATION_MS, FADE_START);
        if (alpha <= 0) return;

        int color = (alpha << 24) | 0xFFFFFF;
        RenderUtils.pushScale(g, hud.scale());
        RenderUtils.text(g, label, (int) (hud.x() / hud.scale()), (int) (hud.y() / hud.scale()), color, true);
        RenderUtils.pop(g);
    }

    private double fadeOut(double t) {
        if (t < FADE_START) return 0;
        double phase = (t - FADE_START) / (1 - FADE_START);
        return Math.pow(phase, 5);
    }

    @Override
    public HudElement hudElement() {
        return hud;
    }

    @Override
    public String hudPreviewText() {
        return PREVIEW_LABEL;
    }

    @Override
    public void renderHudPreview(GuiGraphicsExtractor g) {
        RenderUtils.pushScale(g, hud.scale());
        RenderUtils.text(g, PREVIEW_LABEL, (int) (hud.x() / hud.scale()), (int) (hud.y() / hud.scale()), 0xFFFFFFFF, true);
        RenderUtils.pop(g);
    }
}