package dev.slhj.slhjaddons.hud;

import dev.slhj.slhjaddons.config.SlhjConfig;

public final class HudMigration {
    private HudMigration() {}
    public static HudPlacement resolve(SlhjConfig config, String id, HudPlacement defaults) {
        HudPlacement existing = config.hud.get(id);
        if (existing != null) { existing.normalize(); return existing; }
        HudPlacement placement = defaults.copy();
        if (id.equals("clock")) {
            boolean right = config.raw("clock.right_aligned") == null
                    || config.getBoolean("clock.right_aligned", true);
            float padding = (float) config.getDouble("clock.padding", 5);
            placement.screenAnchor = placement.elementAnchor = right ? HudAnchor.TOP_RIGHT : HudAnchor.TOP_LEFT;
            placement.x = right ? -padding : padding;
            placement.y = padding;
            placement.scale = (float) config.getDouble("clock.scale", 1);
        }
        placement.normalize();
        config.hud.put(id, placement);
        return placement;
    }
}
