package dev.slhj.slhjaddons.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.hud.HudAnchor;
import dev.slhj.slhjaddons.hud.HudElement;
import dev.slhj.slhjaddons.hud.HudPlacement;
import dev.slhj.slhjaddons.hud.HudSelection;
import dev.slhj.slhjaddons.util.render.RenderUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Edits the same persistent HUD objects drawn during gameplay. */
public final class HudEditorScreen extends Screen {
    private static final int WHITE = 0xFFFFFFFF, MUTED = 0xFFB8C0CC;
    private static final int GOLD = 0xFFFFCA64, CYAN = 0xFF78DCE8;
    private static final int PANEL_TOP = 52, CONTENT_HEIGHT = 218;
    private static final String[] ARROWS = {"↖", "↑", "↗", "←", "•", "→", "↙", "↓", "↘"};

    private record PanelWidget(AbstractWidget widget, int y) {}
    private final HudSelection<HudElement> selection = new HudSelection<>();
    private final List<PanelWidget> panelWidgets = new ArrayList<>();
    private final List<Button> screenAnchors = new ArrayList<>(), elementAnchors = new ArrayList<>();
    private List<HudElement.Frame> frames = List.of();
    private boolean expanded, syncing;
    private int panelScroll;
    private double grabX, grabY, pointerX, pointerY;
    private NumberField offsetX, offsetY, scale;
    private Button edit;
    @Nullable private final Screen parent;

    public HudEditorScreen() { this(null); }

    public HudEditorScreen(@Nullable Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
    }

    private int stripWidth() { return Math.min(360, width - 12); }
    private int stripX() { return (width - stripWidth()) / 2; }
    private int panelWidth() { return Math.min(280, width - 12); }
    private int panelX() { return (width - panelWidth()) / 2; }
    private int panelBottom() { return Math.max(PANEL_TOP + 1, Math.min(height - 24, PANEL_TOP + CONTENT_HEIGHT)); }
    private boolean overStrip(double x, double y) {
        return x >= stripX() && x <= stripX() + stripWidth() && y >= 6 && y <= 46;
    }
    private boolean overPanel(double x, double y) {
        return expanded && selection.selected() != null && x >= panelX() && x <= panelX() + panelWidth()
                && y >= PANEL_TOP && y <= panelBottom();
    }

    @Override protected void init() {
        commitFields();
        selection.cancel();
        frames = SlhjAddons.hud().prepare(true);
        rebuildControls();
    }

    private void rebuildControls() {
        commitFields();
        clearFocus();
        setFocused(null);
        clearWidgets();
        offsetX = offsetY = scale = null;
        panelWidgets.clear(); screenAnchors.clear(); elementAnchors.clear();
        addRenderableWidget(Button.builder(Component.literal("<"), b -> cycleSelection(-1))
                .bounds(stripX() + stripWidth() - 148, 16, 22, 20)
                .tooltip(Tooltip.create(Component.literal("Previous HUD element"))).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> cycleSelection(1))
                .bounds(stripX() + stripWidth() - 124, 16, 22, 20)
                .tooltip(Tooltip.create(Component.literal("Next HUD element"))).build());
        edit = addRenderableWidget(Button.builder(Component.literal(expanded ? "Hide" : "Edit"), b -> {
            expanded = !expanded;
            rebuildControls();
        }).bounds(stripX() + stripWidth() - 100, 16, 44, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(stripX() + stripWidth() - 50, 16, 44, 20).build());
        if (expanded && selection.selected() != null) {
            int left = panelX() + 10, right = panelX() + panelWidth() / 2 + 5;
            addAnchorGrid(left, true);
            addAnchorGrid(right, false);
            int fieldWidth = panelWidth() / 2 - 18;
            offsetX = field(left, 126, fieldWidth, "X offset", value -> {
                selection.selected().placement().x = value;
                constrainSelected();
            }, () -> selection.selected().placement().x);
            offsetY = field(right, 126, fieldWidth, "Y offset", value -> {
                selection.selected().placement().y = value;
                constrainSelected();
            }, () -> selection.selected().placement().y);
            scale = field(left, 164, fieldWidth, "Scale", this::setScale, () -> selection.selected().placement().scale);
            panel(Button.builder(Component.literal("Align anchors"), b -> {
                var p = selection.selected().placement();
                p.x = 0; p.y = 0;
                constrainSelected();
                setFocused(null);
            }).bounds(left, 0, fieldWidth, 20).tooltip(Tooltip.create(Component.literal(
                    "Zero offsets, keeping the element inside the screen"))).build(), 192);
            panel(Button.builder(Component.literal("Reset selected"), b -> {
                selection.selected().reset();
                constrainSelected();
                setFocused(null);
            }).bounds(right, 0, fieldWidth, 20).build(), 192);
        } else offsetX = offsetY = scale = null;
        positionPanelWidgets();
        syncControls();
        setFocused(edit);
    }

    private void addAnchorGrid(int x, boolean screen) {
        List<Button> buttons = screen ? screenAnchors : elementAnchors;
        for (HudAnchor anchor : HudAnchor.values()) {
            int index = anchor.ordinal();
            Button button = Button.builder(Component.literal(ARROWS[index]), b -> {
                HudElement.Frame frame = selectedFrame();
                if (frame == null) return;
                HudPlacement p = frame.element().placement();
                p.anchors(screen ? anchor : p.screenAnchor, screen ? p.elementAnchor : anchor,
                        width, height, frame.width(), frame.height());
                setFocused(null);
            }).bounds(x + (index % 3) * 25, 0, 23, 22)
                    .tooltip(Tooltip.create(Component.literal((screen ? "Screen: " : "Element: ") + anchor.label)))
                    .build();
            buttons.add(button);
            panel(button, 24 + (index / 3) * 24);
        }
    }

    private NumberField field(int x, int y, int fieldWidth, String label, Consumer<Float> update, Supplier<Float> read) {
        NumberField field = new NumberField(x, fieldWidth, label, update, read);
        panel(field, y);
        return field;
    }

    private void commitFields() {
        if (offsetX != null) offsetX.commit();
        if (offsetY != null) offsetY.commit();
        if (scale != null) scale.commit();
    }

    private final class NumberField extends EditBox {
        private final HudElement owner = selection.selected();
        private final Consumer<Float> update;
        private final Supplier<Float> read;
        private boolean dirty;

        NumberField(int x, int fieldWidth, String label, Consumer<Float> update, Supplier<Float> read) {
            super(HudEditorScreen.this.font, x, 0, fieldWidth, 18, Component.literal(label));
            this.update = update;
            this.read = read;
            setMaxLength(16);
            setResponder(text -> {
                if (syncing) return;
                dirty = true;
                setTextColor(SettingsInputs.finiteFloat(text) == null ? 0xFFFF7777 : WHITE);
            });
        }

        void commit() {
            if (!dirty || syncing || owner != selection.selected() || !owner.enabled()) return;
            Float value = SettingsInputs.finiteFloat(getValue());
            if (value != null) update.accept(value);
            dirty = false;
            syncing = true;
            setValue(String.format(Locale.ROOT, "%.2f", read.get()));
            setTextColor(WHITE);
            syncing = false;
        }

        @Override public void setFocused(boolean focused) {
            if (!focused && isFocused()) commit();
            super.setFocused(focused);
        }

        @Override public boolean keyPressed(KeyEvent event) {
            if (event.isConfirmation()) { commit(); return true; }
            return super.keyPressed(event);
        }
    }

    private void panel(AbstractWidget widget, int y) {
        panelWidgets.add(new PanelWidget(addRenderableWidget(widget), y));
    }
    private void positionPanelWidgets() {
        panelScroll = Math.clamp(panelScroll, 0, Math.max(0, CONTENT_HEIGHT - (panelBottom() - PANEL_TOP)));
        for (PanelWidget entry : panelWidgets) {
            AbstractWidget w = entry.widget();
            w.setY(PANEL_TOP + entry.y() - panelScroll);
            w.visible = w.getY() >= PANEL_TOP + 2 && w.getY() + w.getHeight() <= panelBottom() - 2;
            w.active = w.visible;
            if (!w.visible && getFocused() == w) setFocused(null);
        }
    }
    private void syncField(EditBox box, float value) {
        if (box != null && !box.isFocused()) {
            box.setValue(String.format(Locale.ROOT, "%.2f", value));
            box.setTextColor(WHITE);
        }
    }
    private void syncControls() {
        edit.active = selection.selected() != null;
        HudElement selected = selection.selected();
        if (selected == null) return;
        syncing = true;
        HudPlacement p = selected.placement();
        syncField(offsetX, p.x); syncField(offsetY, p.y); syncField(scale, p.scale);
        for (int i = 0; i < screenAnchors.size(); i++) {
            screenAnchors.get(i).setMessage(Component.literal(ARROWS[i]).withStyle(
                    p.screenAnchor.ordinal() == i ? ChatFormatting.GOLD : ChatFormatting.WHITE));
            elementAnchors.get(i).setMessage(Component.literal(ARROWS[i]).withStyle(
                    p.elementAnchor.ordinal() == i ? ChatFormatting.GOLD : ChatFormatting.WHITE));
        }
        syncing = false;
    }

    private HudElement.Frame selectedFrame() { return frameFor(selection.selected()); }
    private HudElement.Frame frameFor(HudElement element) {
        for (var frame : frames) if (frame.element() == element && element.enabled()) return frame;
        return null;
    }
    private void constrainSelected() {
        var frame = selectedFrame();
        if (frame != null) frame.constrain(width, height);
    }
    private void setScale(float value) {
        var frame = selectedFrame();
        if (frame == null) return;
        frame.element().placement().scale = Math.clamp(value, .1f, 10f);
        frame.constrain(width, height);
    }
    private List<HudElement> hits(double x, double y) {
        List<HudElement> hits = new ArrayList<>();
        for (int i = frames.size() - 1; i >= 0; i--) {
            var frame = frames.get(i);
            if (frame.element().enabled() && frame.bounds(width, height).contains(x, y, 4)) hits.add(frame.element());
        }
        return hits;
    }
    private void changedSelection(HudElement before) {
        if (selection.selected() != before) {
            panelScroll = 0;
            rebuildControls();
        }
    }
    private void moveDragged() {
        var frame = frameFor(selection.pressed());
        if (selection.dragging() && frame != null)
            frame.move((float) (pointerX - grabX), (float) (pointerY - grabY), width, height);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        pointerX = mouseX; pointerY = mouseY;
        frames = SlhjAddons.hud().prepare(true);
        HudElement before = selection.selected();
        selection.retain(HudElement::enabled);
        changedSelection(before);
        moveDragged();
        g.fill(0, 0, width, height, 0x77000000);
        for (var frame : frames) frame.draw(g);
        boolean hoverHud = !overStrip(mouseX, mouseY) && !overPanel(mouseX, mouseY);
        for (var frame : frames) if (frame.element() != selection.selected()) {
            boolean hovered = hoverHud && frame.bounds(width, height).contains(mouseX, mouseY, 4);
            if (hovered) drawAnchorGuide(g, frame);
            outline(g, frame, 1, hovered ? CYAN : 0x886D819C);
        }
        var selected = selectedFrame();
        if (selected != null) {
            if (expanded || hoverHud && selected.bounds(width, height).contains(mouseX, mouseY, 4))
                drawAnchorGuide(g, selected);
            outline(g, selected, 2, GOLD);
        }
        drawChrome(g, mouseX, mouseY);
        syncControls();
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }

    private void outline(GuiGraphicsExtractor g, HudElement.Frame frame, int thickness, int color) {
        var b = frame.bounds(width, height);
        int left = Math.max(0, (int) Math.floor(b.x()) - 3);
        int top = Math.max(0, (int) Math.floor(b.y()) - 3);
        int right = Math.min(width, (int) Math.ceil(b.x() + b.width()) + 3);
        int bottom = Math.min(height, (int) Math.ceil(b.y() + b.height()) + 3);
        RenderUtils.drawHollowRect(g, left, top, right - left, bottom - top, thickness, color);
    }
    private void drawAnchorGuide(GuiGraphicsExtractor g, HudElement.Frame frame) {
        HudPlacement p = frame.element().placement();
        var b = frame.bounds(width, height);
        int sx = Math.round(width * p.screenAnchor.x), sy = Math.round(height * p.screenAnchor.y);
        int ex = Math.round(b.x() + b.width() * p.elementAnchor.x);
        int ey = Math.round(b.y() + b.height() * p.elementAnchor.y);
        int steps = Math.max(1, Math.max(Math.abs(ex - sx), Math.abs(ey - sy)));
        for (int i = 0; i <= steps; i += 3) {
            int x = Math.round(sx + (ex - sx) * (i / (float) steps));
            int y = Math.round(sy + (ey - sy) * (i / (float) steps));
            g.fill(x, y, x + 1, y + 1, 0xAAFFFFFF);
        }
        marker(g, sx, sy, CYAN);
        marker(g, ex, ey, GOLD);
    }
    private void marker(GuiGraphicsExtractor g, int x, int y, int color) {
        x = Math.clamp(x, 3, Math.max(3, width - 3));
        y = Math.clamp(y, 3, Math.max(3, height - 3));
        g.fill(x - 3, y - 3, x + 3, y + 3, 0xFF111722);
        g.fill(x - 2, y - 2, x + 2, y + 2, color);
    }
    private void drawChrome(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int x = stripX();
        g.fill(x, 6, x + stripWidth(), 46, 0xED101722);
        g.text(font, "HUD Editor", x + 8, 11, MUTED, false);
        String name = selection.selected() == null ? "Click an element to select" : selection.selected().label();
        List<HudElement> under = hits(mouseX, mouseY);
        int index = under.indexOf(selection.selected());
        String overlap = index >= 0 && under.size() > 1 ? " · " + (index + 1) + " of " + under.size() : "";
        name = font.plainSubstrByWidth(name, Math.max(1, stripWidth() - 164 - font.width(overlap))) + overlap;
        g.text(font, name, x + 8, 27,
                selection.selected() == null ? MUTED : GOLD, false);
        String hint = expanded ? "Tab: next control · Enter: apply number" : width >= 430 ? "Click: cycle  |  Drag: move  |  Scroll: scale  |  Tab: next element"
                : "Click: cycle · Drag: move · Tab: next";
        g.centeredText(font, hint, width / 2, height - 15, MUTED);
        if (frames.isEmpty()) {
            g.centeredText(font, "No enabled HUD elements", width / 2, height / 2, WHITE);
            g.centeredText(font, "Enable a HUD feature in settings first", width / 2, height / 2 + 14, MUTED);
        }
        if (!expanded || selection.selected() == null) return;
        int left = panelX() + 10, right = panelX() + panelWidth() / 2 + 5;
        g.fill(panelX(), PANEL_TOP, panelX() + panelWidth(), panelBottom(), 0xF2101722);
        g.enableScissor(panelX(), PANEL_TOP, panelX() + panelWidth(), panelBottom());
        panelLabel(g, "Screen anchor", left, 8);
        panelLabel(g, "Element anchor", right, 8);
        HudPlacement p = selection.selected().placement();
        panelLabel(g, p.screenAnchor.label, left, 100);
        panelLabel(g, p.elementAnchor.label, right, 100);
        panelLabel(g, "X offset", left, 114); panelLabel(g, "Y offset", right, 114);
        panelLabel(g, "Scale", left, 152);
        panelLabel(g, "0.1 – 10", right, 168);
        g.disableScissor();
        if (CONTENT_HEIGHT > panelBottom() - PANEL_TOP) {
            int viewport = panelBottom() - PANEL_TOP;
            int thumbHeight = Math.max(12, viewport * viewport / CONTENT_HEIGHT);
            int thumbY = PANEL_TOP + Math.round((viewport - thumbHeight)
                    * (panelScroll / (float) (CONTENT_HEIGHT - viewport)));
            g.fill(panelX() + panelWidth() - 4, thumbY, panelX() + panelWidth() - 2, thumbY + thumbHeight, MUTED);
        }
    }
    private void panelLabel(GuiGraphicsExtractor g, String text, int x, int y) {
        g.text(font, text, x, PANEL_TOP + y - panelScroll, MUTED, false);
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && getFocused() instanceof NumberField field
                && !field.isMouseOver(event.x(), event.y())) field.commit();
        if (super.mouseClicked(event, doubleClick)) return true;
        if (overStrip(event.x(), event.y()) || overPanel(event.x(), event.y())) return true;
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;
        setFocused(null);
        HudElement before = selection.selected();
        selection.press(hits(event.x(), event.y()), event.x(), event.y());
        var frame = frameFor(selection.pressed());
        if (frame != null) {
            var bounds = frame.bounds(width, height);
            grabX = event.x() - bounds.x(); grabY = event.y() - bounds.y();
        }
        changedSelection(before);
        return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && selection.pressed() != null) {
            pointerX = event.x(); pointerY = event.y();
            HudElement before = selection.selected();
            if (selection.drag(pointerX, pointerY)) moveDragged();
            changedSelection(before);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && selection.pressed() != null) {
            HudElement before = selection.selected();
            selection.release();
            changedSelection(before);
            return true;
        }
        return super.mouseReleased(event);
    }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (overPanel(mouseX, mouseY)) {
            panelScroll += (int) (-scrollY * 18);
            positionPanelWidgets();
            return true;
        }
        if (overStrip(mouseX, mouseY)) return true;
        List<HudElement> under = hits(mouseX, mouseY);
        if (under.isEmpty()) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        commitFields();
        setFocused(null);
        HudElement target = under.contains(selection.selected()) ? selection.selected() : under.getFirst();
        HudElement.Frame frame = frameFor(target);
        target.placement().scale = Math.clamp(target.placement().scale + (float) scrollY * .1f, .1f, 10f);
        frame.constrain(width, height);
        return true;
    }
    private void cycleSelection(int direction) {
        commitFields();
        List<HudElement> enabled = frames.stream().map(HudElement.Frame::element).filter(HudElement::enabled).toList();
        if (enabled.isEmpty()) return;
        HudElement before = selection.selected();
        int current = before == null ? -1 : enabled.indexOf(before);
        int next = current < 0 ? (direction < 0 ? enabled.size() - 1 : 0)
                : Math.floorMod(current + direction, enabled.size());
        selection.select(enabled.get(next));
        changedSelection(before);
    }

    @Override public boolean keyPressed(KeyEvent event) {
        if (event.shortcutKey() == 9) {
            if (!expanded) {
                cycleSelection(event.hasShiftDown() ? -1 : 1);
                return true;
            }
            // Include off-screen panel controls in keyboard navigation, then reveal focus.
            for (PanelWidget entry : panelWidgets) {
                entry.widget().visible = true;
                entry.widget().active = true;
            }
            boolean handled = super.keyPressed(event);
            for (PanelWidget entry : panelWidgets) if (entry.widget() == getFocused()) {
                int top = entry.y() - panelScroll;
                int bottom = top + entry.widget().getHeight();
                if (top < 2) panelScroll = entry.y() - 2;
                else if (bottom > panelBottom() - PANEL_TOP - 2)
                    panelScroll = entry.y() + entry.widget().getHeight() - (panelBottom() - PANEL_TOP) + 2;
            }
            positionPanelWidgets();
            return handled;
        }
        return super.keyPressed(event);
    }
    @Override public void removed() {
        commitFields();
        selection.cancel();
        SlhjAddons.config().save();
        super.removed();
    }
    @Override public boolean isPauseScreen() { return false; }

    @Override public void onClose() { minecraft.gui.setScreen(parent); }

    public static void openHudEditor() {
        openHudEditor(null);
    }

    public static void openHudEditor(@Nullable Screen parent) {
        SlhjAddons.config().save();
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> mc.gui.setScreen(new HudEditorScreen(parent)));
    }
}
