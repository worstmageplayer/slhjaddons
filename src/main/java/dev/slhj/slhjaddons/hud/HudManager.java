package dev.slhj.slhjaddons.hud;

import dev.slhj.slhjaddons.gui.HudEditorScreen;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class HudManager {
    private final List<HudElement> elements = new ArrayList<>();
    public void register(HudElement element) {
        if (elements.stream().anyMatch(e -> e.id().equals(element.id())))
            throw new IllegalArgumentException("Duplicate HUD id: " + element.id());
        elements.add(element);
    }
    public List<HudElement.Frame> prepare(boolean editor) {
        List<HudElement.Frame> frames = new ArrayList<>();
        for (HudElement element : elements) {
            if (!editor && element.renderLayer() == HudElement.RenderLayer.SCREEN) continue;
            HudElement.Frame frame = element.prepare(editor);
            if (frame != null) frames.add(frame);
        }
        return frames;
    }
    public void init() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("slhjaddons", "hud"), (g, delta) -> {
            if (Minecraft.getInstance().gui.screen() instanceof HudEditorScreen) return;
            for (HudElement.Frame frame : prepare(false)) frame.draw(g);
        });
    }
}
