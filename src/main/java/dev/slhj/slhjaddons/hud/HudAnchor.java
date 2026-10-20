package dev.slhj.slhjaddons.hud;

public enum HudAnchor {
    TOP_LEFT("Top left", 0, 0), TOP_CENTER("Top center", .5f, 0), TOP_RIGHT("Top right", 1, 0),
    MIDDLE_LEFT("Middle left", 0, .5f), CENTER("Center", .5f, .5f), MIDDLE_RIGHT("Middle right", 1, .5f),
    BOTTOM_LEFT("Bottom left", 0, 1), BOTTOM_CENTER("Bottom center", .5f, 1), BOTTOM_RIGHT("Bottom right", 1, 1);

    public final String label;
    public final float x, y;
    HudAnchor(String label, float x, float y) { this.label = label; this.x = x; this.y = y; }
}
