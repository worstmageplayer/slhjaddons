package dev.slhj.slhjaddons.hud;

/** Persistent placement in GUI units. Legacy x/y fields become top-left offsets unchanged. */
public final class HudPlacement {
    public HudAnchor screenAnchor = HudAnchor.TOP_LEFT;
    public HudAnchor elementAnchor = HudAnchor.TOP_LEFT;
    public float x, y, scale = 1;

    public record Bounds(float x, float y, float width, float height) {
        public boolean contains(double px, double py, float padding) {
            return px >= x - padding && py >= y - padding
                    && px <= x + width + padding && py <= y + height + padding;
        }
    }

    public HudPlacement() {}
    public HudPlacement(HudAnchor screenAnchor, HudAnchor elementAnchor, float x, float y, float scale) {
        this.screenAnchor = screenAnchor; this.elementAnchor = elementAnchor;
        this.x = x; this.y = y; this.scale = scale;
        normalize();
    }
    public HudPlacement copy() { return new HudPlacement(screenAnchor, elementAnchor, x, y, scale); }
    public void copyFrom(HudPlacement other) {
        screenAnchor = other.screenAnchor; elementAnchor = other.elementAnchor;
        x = other.x; y = other.y; scale = other.scale;
        normalize();
    }
    public void normalize() {
        if (screenAnchor == null) screenAnchor = HudAnchor.TOP_LEFT;
        if (elementAnchor == null) elementAnchor = HudAnchor.TOP_LEFT;
        if (!Float.isFinite(x)) x = 0;
        if (!Float.isFinite(y)) y = 0;
        scale = Float.isFinite(scale) ? Math.clamp(scale, .1f, 10f) : 1;
    }
    public Bounds bounds(float screenWidth, float screenHeight, float width, float height) {
        float w = width * scale, h = height * scale;
        return new Bounds(clamp(screenWidth * screenAnchor.x + x - w * elementAnchor.x, screenWidth, w),
                clamp(screenHeight * screenAnchor.y + y - h * elementAnchor.y, screenHeight, h), w, h);
    }
    private static float clamp(float coordinate, float screen, float size) {
        return Math.clamp(coordinate, 0, Math.max(0, screen - size));
    }
    public void moveTo(float left, float top, float screenWidth, float screenHeight, float width, float height) {
        float w = width * scale, h = height * scale;
        x = clamp(left, screenWidth, w) + w * elementAnchor.x - screenWidth * screenAnchor.x;
        y = clamp(top, screenHeight, h) + h * elementAnchor.y - screenHeight * screenAnchor.y;
    }
    public void constrain(float sw, float sh, float w, float h) {
        Bounds b = bounds(sw, sh, w, h);
        moveTo(b.x, b.y, sw, sh, w, h);
    }
    public void anchors(HudAnchor screen, HudAnchor element, float sw, float sh, float w, float h) {
        Bounds before = bounds(sw, sh, w, h);
        screenAnchor = screen; elementAnchor = element;
        moveTo(before.x, before.y, sw, sh, w, h);
    }
}
