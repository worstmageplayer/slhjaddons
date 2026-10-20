package dev.slhj.slhjaddons.hud;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.client.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/** One persistent element, used unchanged by gameplay and the editor. */
public final class HudElement {
    public enum RenderLayer { HUD, SCREEN }
    private final RenderLayer renderLayer;
    private final Feature owner;
    private final String id, label;
    private final HudPlacement placement, defaults;
    private final Supplier<@Nullable HudContent> live;
    private final Supplier<HudContent> example;

    public HudElement(Feature owner, String id, String label, HudPlacement defaults,
                      Supplier<@Nullable HudContent> live, Supplier<HudContent> example) {
        this(owner, id, label, defaults, live, example, SlhjAddons.config());
    }

    public HudElement(Feature owner, String id, String label, HudPlacement defaults,
                      Supplier<@Nullable HudContent> live, Supplier<HudContent> example, RenderLayer layer) {
        this(owner, id, label, defaults, live, example, SlhjAddons.config(), layer);
    }

    HudElement(Feature owner, String id, String label, HudPlacement defaults,
               Supplier<@Nullable HudContent> live, Supplier<HudContent> example,
               dev.slhj.slhjaddons.config.SlhjConfig config) {
        this(owner, id, label, defaults, live, example, config, RenderLayer.HUD);
    }

    HudElement(Feature owner, String id, String label, HudPlacement defaults,
               Supplier<@Nullable HudContent> live, Supplier<HudContent> example,
               dev.slhj.slhjaddons.config.SlhjConfig config, RenderLayer layer) {
        this.renderLayer = layer;
        this.owner = owner; this.id = id; this.label = label;
        this.defaults = defaults.copy();
        this.placement = HudMigration.resolve(config, id, defaults);
        this.live = live; this.example = example;
    }
    public RenderLayer renderLayer() { return renderLayer; }
    public String id() { return id; }
    public String label() { return label; }
    public boolean enabled() { return owner.isEnabled(); }
    public HudPlacement placement() { return placement; }
    public void reset() { placement.copyFrom(defaults); }

    @Nullable HudContent content(boolean editor) {
        if (!enabled()) return null;
        HudContent content = live.get();
        return content == null && editor ? example.get() : content;
    }

    @Nullable public Frame prepare(boolean editor) {
        HudContent content = content(editor);
        if (content == null) return null;
        List<String> lines = content.lines().stream().map(ChatUtils::colorize).toList();
        var font = Minecraft.getInstance().font;
        int shadow = content.shadow() ? 1 : 0;
        int width = lines.stream().mapToInt(font::width).max().orElse(0) + content.padding() * 2 + shadow;
        int height = lines.size() * font.lineHeight + Math.max(0, lines.size() - 1) * content.lineGap()
                + content.padding() * 2 + shadow;
        return new Frame(this, content, lines, Math.max(1, width), Math.max(1, height));
    }

    public record Frame(HudElement element, HudContent content, List<String> lines, int width, int height) {
        public HudPlacement.Bounds bounds(int sw, int sh) { return element.placement.bounds(sw, sh, width, height); }
        public void move(float x, float y, int sw, int sh) { element.placement.moveTo(x, y, sw, sh, width, height); }
        public void constrain(int sw, int sh) { element.placement.constrain(sw, sh, width, height); }
        public void draw(GuiGraphicsExtractor g) {
            var bounds = bounds(g.guiWidth(), g.guiHeight());
            var font = Minecraft.getInstance().font;
            g.pose().pushMatrix();
            g.pose().translate(bounds.x(), bounds.y());
            g.pose().scale(element.placement.scale, element.placement.scale);
            if ((content.background() >>> 24) != 0) g.fill(0, 0, width, height, content.background());
            int y = content.padding();
            for (String line : lines) {
                g.text(font, line, content.padding(), y, content.color(), content.shadow());
                y += font.lineHeight + content.lineGap();
            }
            g.pose().popMatrix();
        }
    }
}
