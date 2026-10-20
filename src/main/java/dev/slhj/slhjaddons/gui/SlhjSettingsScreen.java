package dev.slhj.slhjaddons.gui;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.core.ConfigValue;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.client.ChatUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;

public final class SlhjSettingsScreen extends Screen {
    private static final int BODY_TOP = 72, WHITE = 0xFFFFFFFF, MUTED = 0xFFAAAAAA, ERROR = 0xFFFF7777;
    private final SettingsMenuState state = new SettingsMenuState();
    private final Map<String, String> drafts = new HashMap<>();
    private final Map<Feature.Category, Integer> categoryOffsets = new EnumMap<>(Feature.Category.class);
    private SettingsList categories, options;
    private EditBox search;
    private boolean rebuildPending, syncingSearch;
    private double categoryScroll;
    private int visibleCount;
    private Feature.Category activeCategory, pendingJump;

    public SlhjSettingsScreen() { super(Component.literal("slhjaddons settings")); }

    private int menuWidth() { return Math.min(760, width - 16); }
    private int left() { return (width - menuWidth()) / 2; }
    private int sidebarWidth() { return width < 420 ? 92 : 112; }
    private int contentLeft() { return left() + sidebarWidth() + 8; }
    private int contentWidth() { return menuWidth() - sidebarWidth() - 8; }
    private int bodyHeight() { return Math.max(40, height - BODY_TOP - 42); }

    @Override protected void init() {
        rememberScroll();
        if (categories != null) categoryScroll = categories.scrollAmount();
        categories = addRenderableWidget(new SettingsList(left(), sidebarWidth()));
        for (Feature.Category category : state.categories(SlhjAddons.features().all())) addCategory(category);
        categories.setScrollAmount(categoryScroll);

        search = addRenderableWidget(new EditBox(font, contentLeft() + 8, 30, contentWidth() - 36, 20,
                Component.literal("Search all settings")));
        search.setMaxLength(128);
        search.setHint(Component.literal("Search all settings..."));
        search.setValue(state.query());
        search.setResponder(query -> {
            if (syncingSearch) return;
            rememberScroll();
            state.query(query);
            pendingJump = null;
            rebuildPending = true;
        });
        addRenderableWidget(Button.builder(Component.literal("X"), b -> {
            search.setValue("");
            setFocused(search);
        }).bounds(contentLeft() + contentWidth() - 24, 30, 20, 20)
                .tooltip(Tooltip.create(Component.literal("Clear search"))).build());
        addRenderableWidget(Button.builder(Component.literal("Edit HUD"), b -> HudEditorScreen.openHudEditor(this))
                .bounds(width / 2 - 154, height - 28, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(width / 2 + 4, height - 28, 150, 20).build());
        rebuildOptions();
    }

    private void addCategory(Feature.Category category) {
        Row row = new Row();
        Button button = Button.builder(Component.literal(category.toString()), b -> {
            rememberScroll();
            boolean wasSearching = state.searching();
            state.query("");
            syncingSearch = true;
            search.setValue("");
            syncingSearch = false;
            pendingJump = category;
            if (wasSearching || rebuildPending) rebuildPending = true;
            else jumpToCategory();
        }).bounds(0, 0, categories.getRowWidth() - 4, 20).build();
        button.setOverrideRenderHighlightedSprite(() -> !state.searching() && activeCategory == category
                || button.isHoveredOrFocused());
        row.widget(button, 0, 0);
        categories.add(row, 24);
    }

    private void rememberScroll() {
        if (options != null && !rebuildPending) state.scroll(options.scrollAmount());
    }

    private void rebuildOptions() {
        if (options != null) removeWidget(options);
        options = addRenderableWidget(new SettingsList(contentLeft(), contentWidth()));
        categoryOffsets.clear();
        List<Feature> features = state.visible(SlhjAddons.features().all());
        visibleCount = features.size();
        Feature.Category previous = null;
        for (Feature feature : features) {
            if (feature.category() != previous) {
                Row heading = new Row();
                heading.label(feature.category().toString(), 0, 3, MUTED);
                heading.drawers.add((g, row) -> g.fill(row.getContentX(), row.getContentY() + 15,
                        row.getContentX() + rowWidth(), row.getContentY() + 16, 0xFF555555));
                categoryOffsets.put(feature.category(), options.getNextY() - options.getY() - 2);
                options.add(heading, 19);
                previous = feature.category();
            }
            addFeature(feature);
        }
        if (features.isEmpty()) {
            Row empty = new Row();
            empty.label("No matching features", 0, 4, WHITE);
            empty.label("Try a different search.", 0, 18, MUTED);
            options.add(empty, 36);
        }
        options.setScrollAmount(state.scroll());
        rebuildPending = false;
        if (pendingJump != null) jumpToCategory();
    }

    private void jumpToCategory() {
        options.setScrollAmount(categoryOffsets.getOrDefault(pendingJump, 0));
        state.scroll(options.scrollAmount());
        pendingJump = null;
    }

    private int rowWidth() { return options.getRowWidth() - 4; }

    private void addFeature(Feature feature) {
        Row header = new Row();
        header.drawers.add((g, row) -> g.text(font, font.plainSubstrByWidth(feature.label(), rowWidth() - 54),
                row.getContentX(), row.getContentY() + 6, WHITE, false));
        Button toggle = Button.builder(toggleMessage(feature.isEnabled()), b -> {
            var cfg = SlhjAddons.config();
            cfg.setFeatureEnabled(feature.id(), !feature.isEnabled());
            SlhjAddons.features().syncFromConfig();
            cfg.save();
            b.setMessage(toggleMessage(feature.isEnabled()));
        }).bounds(0, 0, 48, 20).tooltip(Tooltip.create(Component.literal("Toggle " + feature.label()))).build();
        header.widget(toggle, rowWidth() - 48, 0);
        options.add(header, 24);
        if (!feature.description().isEmpty()) {
            Row description = new Row();
            var lines = font.split(Component.literal(feature.description()), rowWidth() - 8);
            description.drawers.add((g, row) -> {
                for (int i = 0; i < lines.size(); i++)
                    g.text(font, lines.get(i), row.getContentX() + 4, row.getContentY() + i * font.lineHeight, MUTED, false);
            });
            options.add(description, lines.size() * font.lineHeight + 8);
        }
        for (Setting setting : feature.settings()) addSetting(setting);
        if (!feature.settings().isEmpty()) {
            Row reset = new Row();
            reset.widget(Button.builder(Component.literal("Reset settings"), b -> {
                for (Setting setting : feature.settings()) {
                    drafts.remove(setting.key());
                    switch (setting) {
                        case Setting.ToggleSetting s -> reset(s.value());
                        case Setting.SliderSetting s -> reset(s.value());
                        case Setting.TextSetting s -> reset(s.value());
                        case Setting.ColorSetting s -> reset(s.value());
                        case Setting.ButtonSetting ignored -> { }
                    }
                }
                SlhjAddons.config().save();
                rememberScroll();
                rebuildPending = true;
            }).bounds(0, 0, Math.min(120, rowWidth()), 20)
                    .tooltip(Tooltip.create(Component.literal("Restore this feature's settings; keep its ON/OFF state"))).build(), 0, 0);
            options.add(reset, 30);
        }
    }

    private static <T> void reset(ConfigValue<T> value) { value.set(value.defaultValue()); }
    private static Component toggleMessage(boolean enabled) {
        return Component.literal(enabled ? "ON" : "OFF");
    }

    private void addSetting(Setting setting) {
        switch (setting) {
            case Setting.ToggleSetting toggle -> {
                Row row = new Row();
                row.widget(Button.builder(Component.literal(toggle.label() + ": " + (toggle.value().get() ? "ON" : "OFF")), b -> {
                    toggle.value().set(!toggle.value().get());
                    b.setMessage(Component.literal(toggle.label() + ": " + (toggle.value().get() ? "ON" : "OFF")));
                }).bounds(0, 0, rowWidth(), 20).build(), 0, 0);
                options.add(row, 26);
            }
            case Setting.SliderSetting slider -> addNumber(slider);
            case Setting.TextSetting text -> addText(text);
            case Setting.ColorSetting hex -> addColor(hex);
            case Setting.ButtonSetting button -> {
                Row row = new Row();
                row.widget(Button.builder(Component.literal(button.buttonText()), b -> button.action().run())
                        .bounds(0, 0, rowWidth(), 20).build(), 0, 0);
                options.add(row, 26);
            }
        }
    }

    private void addNumber(Setting.SliderSetting setting) {
        Row row = new Row();
        row.label(setting.label(), 0, 0, WHITE);
        int fieldWidth = Math.min(70, rowWidth() / 3);
        EditBox number = new EditBox(font, 0, 0, fieldWidth, 20, Component.literal(setting.label()));
        number.setMaxLength(16);
        number.setValue(drafts.getOrDefault(setting.key(), SettingsInputs.numberText(setting.value().get(), setting.integer())));
        number.setTextColor(SettingsInputs.number(number.getValue(), setting.min(), setting.max(), setting.integer()) == null ? ERROR : WHITE);
        ConfigSlider slider = new ConfigSlider(rowWidth() - fieldWidth - 6, setting, value -> {
            drafts.remove(setting.key());
            number.setValue(SettingsInputs.numberText(value, setting.integer()));
        });
        number.setResponder(text -> {
            drafts.put(setting.key(), text);
            Double value = SettingsInputs.number(text, setting.min(), setting.max(), setting.integer());
            number.setTextColor(value == null ? ERROR : WHITE);
            if (value != null) { setting.value().set(value); slider.refresh(); }
        });
        row.widget(slider, 0, 13);
        row.widget(number, rowWidth() - fieldWidth, 13);
        row.drawers.add((g, entry) -> {
            if (!number.isFocused()) {
                String actual = SettingsInputs.numberText(setting.value().get(), setting.integer());
                if (!actual.equals(number.getValue()) && SettingsInputs.number(number.getValue(), setting.min(), setting.max(), setting.integer()) != null) {
                    drafts.remove(setting.key());
                    number.setValue(actual);
                    slider.refresh();
                }
            }
            boolean valid = SettingsInputs.number(number.getValue(), setting.min(), setting.max(), setting.integer()) != null;
            String range = SettingsInputs.numberText(setting.min(), setting.integer()) + " - "
                    + SettingsInputs.numberText(setting.max(), setting.integer());
            g.text(font, (valid ? "Range: " : "Enter ") + range + (valid || !setting.integer() ? "" : " (whole numbers)"),
                    entry.getContentX(), entry.getContentY() + 35, valid ? MUTED : ERROR, false);
        });
        options.add(row, 49);
    }

    private void addText(Setting.TextSetting setting) {
        Row row = new Row();
        row.label(setting.label(), 0, 0, WHITE);
        EditBox text = new EditBox(font, 0, 0, rowWidth(), 20, Component.literal(setting.label()));
        text.setMaxLength(512);
        text.setValue(setting.value().get());
        if (!setting.hint().isEmpty()) {
            text.setHint(Component.literal(setting.hint()));
            text.setTooltip(Tooltip.create(Component.literal(setting.hint())));
        }
        text.setResponder(setting.value()::set);
        row.widget(text, 0, 13);
        boolean preview = setting.key().endsWith(".message") || setting.key().endsWith(".text");
        if (preview) row.drawers.add((g, entry) -> g.text(font,
                font.plainSubstrByWidth("Preview: " + ChatUtils.colorize(setting.value().get()), rowWidth()),
                entry.getContentX(), entry.getContentY() + 36, WHITE, true));
        options.add(row, preview ? 51 : 40);
    }

    private void addColor(Setting.ColorSetting setting) {
        Row row = new Row();
        row.label(setting.label(), 0, 0, WHITE);
        EditBox hex = new EditBox(font, 0, 0, rowWidth() - 26, 20, Component.literal(setting.label()));
        hex.setMaxLength(9);
        hex.setValue(drafts.getOrDefault(setting.key(), SettingsInputs.hex(setting.value().get())));
        hex.setTextColor(SlhjConfig.parseHexColor(hex.getValue()) == null ? ERROR : WHITE);
        hex.setResponder(text -> {
            drafts.put(setting.key(), text);
            Integer color = SlhjConfig.parseHexColor(text);
            hex.setTextColor(color == null ? ERROR : WHITE);
            if (color != null) setting.value().set(color);
        });
        row.widget(hex, 0, 13);
        row.widget(new ColorSwatchButton(() -> setting.value().get(), () -> {
            drafts.remove(setting.key());
            minecraft.gui.setScreen(new ColorPickerScreen(this, setting));
        }), rowWidth() - 20, 13);
        row.drawers.add((g, entry) -> {
            boolean valid = SlhjConfig.parseHexColor(hex.getValue()) != null;
            g.text(font, valid ? "#AARRGGBB (alpha first)" : "Use 6 or 8 hex digits", entry.getContentX(),
                    entry.getContentY() + 35, valid ? MUTED : ERROR, false);
        });
        options.add(row, 49);
    }

    @Override public void tick() {
        super.tick();
        if (rebuildPending) rebuildOptions();
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        activeCategory = null;
        for (Feature.Category category : state.categories(SlhjAddons.features().all())) {
            Integer offset = categoryOffsets.get(category);
            if (offset != null && offset <= options.scrollAmount() + 1) activeCategory = category;
        }
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(font, title, width / 2, 10, WHITE);
        g.text(font, "Categories", left() + 8, 58, MUTED, false);
        String heading = state.searching() ? "Search results" : "All features";
        g.text(font, heading + " (" + visibleCount + ")", contentLeft() + 8, 58, WHITE, false);
        if (width >= 480) g.text(font, "Changes apply immediately", left(), height - 40, MUTED, false);
    }

    @Override public void removed() {
        rememberScroll();
        if (categories != null) categoryScroll = categories.scrollAmount();
        SlhjAddons.config().save();
        super.removed();
    }
    @Override public boolean isPauseScreen() { return false; }

    private final class SettingsList extends ContainerObjectSelectionList<Row> {
        SettingsList(int x, int width) {
            super(SlhjSettingsScreen.this.minecraft, width, bodyHeight(), BODY_TOP, 24);
            centerListVertically = false;
            setX(x);
        }
        @Override public int getRowWidth() { return getWidth() - 16; }
        void add(Row row, int height) { addEntry(row, height); }
    }

    private record WidgetPlacement(AbstractWidget widget, int x, int y) {}

    private final class Row extends ContainerObjectSelectionList.Entry<Row> {
        private final List<WidgetPlacement> widgets = new ArrayList<>();
        private final List<BiConsumer<GuiGraphicsExtractor, Row>> drawers = new ArrayList<>();
        void widget(AbstractWidget widget, int x, int y) { widgets.add(new WidgetPlacement(widget, x, y)); }
        void label(String text, int x, int y, int color) {
            drawers.add((g, row) -> g.text(font, font.plainSubstrByWidth(text, getWidth() - x - 4),
                    getContentX() + x, getContentY() + y, color, false));
        }
        @Override public void extractContent(GuiGraphicsExtractor g, int mouseX, int mouseY, boolean hovered, float partialTick) {
            for (var drawer : drawers) drawer.accept(g, this);
            for (WidgetPlacement placement : widgets) {
                placement.widget().setPosition(getContentX() + placement.x(), getContentY() + placement.y());
                placement.widget().extractRenderState(g, mouseX, mouseY, partialTick);
            }
        }
        @Override public List<? extends GuiEventListener> children() { return widgets.stream().map(WidgetPlacement::widget).toList(); }
        @Override public List<? extends NarratableEntry> narratables() { return widgets.stream().map(WidgetPlacement::widget).toList(); }
    }

    private static final class ConfigSlider extends AbstractSliderButton {
        private final Setting.SliderSetting setting;
        private final DoubleConsumer changed;
        ConfigSlider(int width, Setting.SliderSetting setting, DoubleConsumer changed) {
            super(0, 0, width, 20, Component.empty(), normalize(setting));
            this.setting = setting;
            this.changed = changed;
            updateMessage();
        }
        private static double normalize(Setting.SliderSetting setting) {
            return Math.clamp((setting.value().get() - setting.min()) / (setting.max() - setting.min()), 0, 1);
        }
        void refresh() { value = normalize(setting); updateMessage(); }
        @Override protected void updateMessage() {
            double actual = setting.min() + value * (setting.max() - setting.min());
            setMessage(Component.literal(SettingsInputs.numberText(actual, setting.integer())));
        }
        @Override protected MutableComponent createNarrationMessage() {
            return Component.literal(setting.label() + ": " + getMessage().getString());
        }
        @Override protected void applyValue() {
            double actual = setting.min() + value * (setting.max() - setting.min());
            if (setting.integer()) actual = Math.rint(actual);
            setting.value().set(actual);
            changed.accept(actual);
        }
    }
}
