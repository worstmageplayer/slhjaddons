package dev.slhj.slhjaddons.gui;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

public final class SlhjSettingsScreen extends Screen {

    private record TextLabel(String text, int x, int baseY) {}
    private record ColorSwatch(int x, int baseY, int size, IntSupplier color) {}

    // This only needs a touch when you add a brand new *category*, not when
    // you add or change a feature. Any category a feature declares that
    // isn't listed here just gets appended at the end.
    private static final List<String> CATEGORY_ORDER =
            List.of("Skyblock", "Dungeons", "Fishing", "Slayers", "Events", "Misc");

    // --- scrolling ---------------------------------------------------------
    private static final int VIEWPORT_TOP = 24;
    private static final int VIEWPORT_BOTTOM_MARGIN = 34;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int HEADER_HEIGHT = 20;
    private static final int CATEGORY_GAP = 14;
    private static final int ROW_HEIGHT = 20;

    private static final float HEADER_SCALE = 1.5f;

    private final List<AbstractWidget> scrollWidgets = new ArrayList<>();
    private final List<Integer> scrollWidgetBaseY = new ArrayList<>();
    private final List<Integer> headerBaseY = new ArrayList<>();
    private final List<String> headerLabel = new ArrayList<>();
    private final List<TextLabel> textLabels = new ArrayList<>();
    private final List<ColorSwatch> colorSwatches = new ArrayList<>();
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private boolean draggingScrollbar = false;

    public SlhjSettingsScreen() {
        super(Component.literal("slhjaddons"));
    }

    @Override
    protected void init() {
        var cfg = SlhjAddons.config();
        var features = SlhjAddons.features().all();

        scrollWidgets.clear();
        scrollWidgetBaseY.clear();
        headerBaseY.clear();
        headerLabel.clear();
        textLabels.clear();
        colorSwatches.clear();

        int sliderWidth = 300;
        int sliderX = this.width / 2 - sliderWidth / 2;
        int settingsIndent = sliderX + 12;
        int settingsWidth = sliderWidth - 12;

        Map<String, List<Feature>> byCategory = new LinkedHashMap<>();
        for (String cat : CATEGORY_ORDER) byCategory.put(cat, new ArrayList<>());
        for (Feature f : features) {
            Feature.Category cat = f.category();
            if (cat == null) continue;
            byCategory.computeIfAbsent(cat.toString(), k -> new ArrayList<>()).add(f);
        }

        int curY = VIEWPORT_TOP + 8;

        for (Map.Entry<String, List<Feature>> entry : byCategory.entrySet()) {
            List<Feature> catFeatures = entry.getValue();
            if (catFeatures.isEmpty()) continue;

            headerBaseY.add(curY);
            headerLabel.add(entry.getKey());
            curY += HEADER_HEIGHT;

            for (Feature f : catFeatures) {
                Checkbox box = Checkbox.builder(Component.literal(f.label()), this.font)
                        .pos(sliderX, curY)
                        .selected(cfg.isFeatureEnabled(f.id()))
                        .onValueChange((checkbox, selected) -> {
                            cfg.setFeatureEnabled(f.id(), selected);
                            SlhjAddons.features().syncFromConfig();
                        })
                        .build();
                addScrollable(box, curY);
                curY += ROW_HEIGHT;

                List<Setting> catSettings = f.settings();
                if (catSettings.isEmpty()) continue;

                for (Setting setting : catSettings) {
                    switch (setting) {
                        case Setting.ToggleSetting toggleSetting -> {
                            Checkbox tbox = Checkbox.builder(Component.literal(toggleSetting.label()), this.font)
                                    .pos(settingsIndent, curY)
                                    .selected(toggleSetting.value().get())
                                    .onValueChange((checkbox, selected) -> toggleSetting.value().set(selected))
                                    .build();
                            addScrollable(tbox, curY);
                            curY += ROW_HEIGHT;
                        }
                        case Setting.SliderSetting slider -> {
                            addScrollable(new ConfigSlider(settingsIndent, curY, settingsWidth, 18, slider), curY);
                            curY += ROW_HEIGHT;
                        }
                        case Setting.TextSetting text -> {
                            textLabels.add(new TextLabel(text.label(), settingsIndent, curY));
                            curY += 12;

                            EditBox editBox = new EditBox(this.font, settingsIndent, curY, settingsWidth, 18,
                                    Component.literal(text.label()));
                            editBox.setMaxLength(512);
                            editBox.setValue(text.value().get());
                            if (!text.hint().isEmpty()) editBox.setHint(Component.literal(text.hint()));
                            editBox.setResponder(value -> text.value().set(value));
                            addScrollable(editBox, curY);
                            curY += ROW_HEIGHT;
                        }
                        case Setting.HexSetting hex -> {
                            textLabels.add(new TextLabel(hex.label(), settingsIndent, curY));
                            curY += 12;

                            int swatchSize = 16;
                            int swatchGap = 6;
                            int boxWidth = settingsWidth - swatchSize - swatchGap;

                            EditBox editBox = new EditBox(this.font, settingsIndent, curY, boxWidth, 18,
                                    Component.literal(hex.label()));
                            editBox.setMaxLength(9);
                            editBox.setValue(String.format("#%08X", hex.value().get()));
                            editBox.setResponder(value -> {
                                Integer parsed = SlhjConfig.parseHexColor(value);
                                if (parsed != null) hex.value().set(parsed);
                            });
                            addScrollable(editBox, curY);

                            int swatchX = settingsIndent + boxWidth + swatchGap;
                            colorSwatches.add(new ColorSwatch(swatchX, curY + 1, swatchSize, () -> hex.value().get()));

                            curY += ROW_HEIGHT;
                        }
                        case Setting.ButtonSetting btn -> {
                            Button b = Button.builder(Component.literal(btn.buttonText()), pressed -> btn.action().run())
                                    .bounds(settingsIndent, curY, settingsWidth, 18)
                                    .build();
                            addScrollable(b, curY);
                            curY += ROW_HEIGHT;
                        }
                    }
                }

                curY += 6;
            }

            curY += CATEGORY_GAP;
        }

        addRenderableWidget(Button.builder(Component.literal("Edit HUD"), b -> HudEditorScreen.openHudEditor())
                .bounds(this.width / 2 - 159, this.height - 28, 78, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 - 75, this.height - 28, 150, 20)
                .build());

        int contentBottom = curY;
        int viewportBottom = this.height - VIEWPORT_BOTTOM_MARGIN;
        maxScroll = Math.max(0, contentBottom - viewportBottom);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
        repositionScrollables();
    }

    private <T extends AbstractWidget> void addScrollable(T widget, int baseY) {
        scrollWidgets.add(widget);
        scrollWidgetBaseY.add(baseY);
        addRenderableWidget(widget);
    }

    private void repositionScrollables() {
        int bottom = this.height - VIEWPORT_BOTTOM_MARGIN;
        for (int i = 0; i < scrollWidgets.size(); i++) {
            AbstractWidget w = scrollWidgets.get(i);
            int y = scrollWidgetBaseY.get(i) - scrollOffset;
            w.setY(y);
            boolean visible = y + w.getHeight() > VIEWPORT_TOP && y < bottom;
            w.visible = visible;
            w.active = visible;
        }
    }

    private void scrollBy(int delta) {
        if (maxScroll <= 0) return;
        int next = Mth.clamp(scrollOffset + delta, 0, maxScroll);
        if (next != scrollOffset) {
            scrollOffset = next;
            repositionScrollables();
        }
    }

    private int scrollbarX() {
        return this.width - 14;
    }

    private int scrollbarThumbHeight(int trackHeight) {
        if (maxScroll <= 0) return trackHeight;
        int contentHeight = trackHeight + maxScroll;
        return Math.max(20, trackHeight * trackHeight / contentHeight);
    }

    private int scrollbarThumbY(int top, int trackHeight, int thumbHeight) {
        if (maxScroll <= 0) return top;
        return top + (int) ((trackHeight - thumbHeight) * (scrollOffset / (double) maxScroll));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scrollBy((int) (-scrollY * 16));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (maxScroll > 0 && event.button() == 0) {
            int top = VIEWPORT_TOP;
            int trackHeight = this.height - VIEWPORT_BOTTOM_MARGIN - top;
            int sbX = scrollbarX();
            if (event.x() >= sbX && event.x() <= sbX + SCROLLBAR_WIDTH && event.y() >= top && event.y() <= top + trackHeight) {
                draggingScrollbar = true;
                jumpScrollbarTo(event.y(), top, trackHeight);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScrollbar) {
            int top = VIEWPORT_TOP;
            int trackHeight = this.height - VIEWPORT_BOTTOM_MARGIN - top;
            jumpScrollbarTo(event.y(), top, trackHeight);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    private void jumpScrollbarTo(double mouseY, int top, int trackHeight) {
        int thumbHeight = scrollbarThumbHeight(trackHeight);
        double ratio = Mth.clamp((mouseY - top - thumbHeight / 2.0) / (trackHeight - thumbHeight), 0.0, 1.0);
        scrollOffset = (int) Math.round(ratio * maxScroll);
        repositionScrollables();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, "slhjaddons settings", this.width / 2, 10, 0xFFFFFFFF);

        int viewportBottom = this.height - VIEWPORT_BOTTOM_MARGIN;
        int headerCenterX = this.width / 2;

        for (int i = 0; i < headerBaseY.size(); i++) {
            int y = headerBaseY.get(i) - scrollOffset;
            if (y + HEADER_HEIGHT <= VIEWPORT_TOP || y >= viewportBottom) continue;

            String label = headerLabel.get(i);

            graphics.pose().pushMatrix();
            graphics.pose().scale(HEADER_SCALE, HEADER_SCALE);
            graphics.centeredText(this.font, label,
                    Math.round(headerCenterX / HEADER_SCALE), Math.round(y / HEADER_SCALE), 0xFFFFD866);
            graphics.pose().popMatrix();

            int lineY = y + HEADER_HEIGHT - 4;
            int lineRight = this.width - 12;
            graphics.fill(12, lineY, lineRight, lineY + 1, 0x55FFFFFF);
        }

        for (TextLabel label : textLabels) {
            int y = label.baseY() - scrollOffset;
            if (y + this.font.lineHeight > VIEWPORT_TOP && y < viewportBottom) {
                graphics.text(this.font, label.text(), label.x(), y, 0xFFFFFFFF);
            }
        }

        for (ColorSwatch swatch : colorSwatches) {
            int y = swatch.baseY() - scrollOffset;
            if (y + swatch.size() <= VIEWPORT_TOP || y >= viewportBottom) continue;
            graphics.fill(swatch.x() - 1, y - 1, swatch.x() + swatch.size() + 1, y + swatch.size() + 1, 0xFFFFFFFF);
            graphics.fill(swatch.x(), y, swatch.x() + swatch.size(), y + swatch.size(), swatch.color().getAsInt());
        }

        if (maxScroll > 0) {
            int top = VIEWPORT_TOP;
            int trackHeight = this.height - VIEWPORT_BOTTOM_MARGIN - top;
            int sbX = scrollbarX();
            int thumbHeight = scrollbarThumbHeight(trackHeight);
            int thumbY = scrollbarThumbY(top, trackHeight, thumbHeight);
            graphics.fill(sbX, top, sbX + SCROLLBAR_WIDTH, top + trackHeight, 0x55000000);
            graphics.fill(sbX, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public void onClose() {
        SlhjAddons.config().save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class ConfigSlider extends AbstractSliderButton {
        private final Setting.SliderSetting spec;

        ConfigSlider(int x, int y, int w, int h, Setting.SliderSetting spec) {
            super(x, y, w, h, Component.empty(), normalize(spec, spec.value().get()));
            this.spec = spec;
            updateMessage();
        }

        private static double normalize(Setting.SliderSetting spec, double v) {
            return (v - spec.min()) / (spec.max() - spec.min());
        }

        private double denormalized() {
            return spec.min() + value * (spec.max() - spec.min());
        }

        @Override
        protected void updateMessage() {
            double v = denormalized();
            String text = spec.isInt() ? String.valueOf((int) Math.round(v)) : String.format("%.2f", v);
            setMessage(Component.literal(spec.label() + ": " + text));
        }

        @Override
        protected void applyValue() {
            double v = denormalized();
            spec.value().set(spec.isInt() ? (double) Math.round(v) : v);
        }
    }
}