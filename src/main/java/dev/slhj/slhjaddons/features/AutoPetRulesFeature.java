package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudRenderer;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.regex.Pattern;

public final class AutoPetRulesFeature extends Feature implements HudRenderer {

    private static final Pattern PET_PATTERN =
            Pattern.compile("Autopet equipped your \\[Lvl (\\d+)\\] (.+?)! VIEW RULE");
    private static final int DURATION_MS = 5000;
    private static final double FADE_START = 0.7;

    private final HudElement hud = new HudElement("auto_pet_rules", "Auto Pet Rules", 10, 100);
    private String petName = null;
    private String petLevel = null;
    private long fadeStartTime = 0;

    public AutoPetRulesFeature() {
        setLabel("Auto Pet Rules");
        category(Category.PETS);
    }

    @Override public String id() { return "auto_pet_rules"; }

    @Override
    public void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!isEnabled() || overlay) return;
            String text = ChatFormatting.stripFormatting(message.getString());

            var matcher = PET_PATTERN.matcher(text);
            if (matcher.find()) {
                petLevel = matcher.group(1);
                petName = matcher.group(2).trim();
                fadeStartTime = System.currentTimeMillis();
            }
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "auto_pet_rules"),
                (graphics, delta) -> render(graphics));
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || petName == null) return;

        long elapsed = System.currentTimeMillis() - fadeStartTime;
        if (elapsed > DURATION_MS) {
            petName = null;
            petLevel = null;
            return;
        }

        double progress = (double) elapsed / DURATION_MS;
        int alpha = (int) (255 * (1 - fadeOut(progress)));
        if (alpha <= 0) return;

        String label = String.format("%s [%s]", petName, petLevel);
        int color = (alpha << 24) | 0xFFFFFF;

        RenderUtils.pushScale(g, hud.scale());
        RenderUtils.text(g, label, (int)(hud.x() / hud.scale()), (int)(hud.y() / hud.scale()), color, true);
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
        return "Pet [Lvl 100]";
    }

    @Override
    public void renderHudPreview(GuiGraphicsExtractor g) {
        RenderUtils.pushScale(g, hud.scale());
        RenderUtils.text(g, hudPreviewText(), (int) (hud.x() / hud.scale()), (int) (hud.y() / hud.scale()), 0xFFFFFFFF, true);
        RenderUtils.pop(g);
    }
}