package dev.slhj.slhjaddons.gui;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.core.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Color and alpha editing using Minecraft's standard sliders and text field. */
final class ColorPickerScreen extends Screen {
    private final Screen parent;
    private final Setting.HexSetting setting;
    private final int original;
    private final List<ChannelSlider> channels = new ArrayList<>();
    private EditBox hex;
    private boolean syncing, invalid, accepted;

    ColorPickerScreen(Screen parent, Setting.HexSetting setting) {
        super(Component.literal(setting.label()));
        this.parent = parent;
        this.setting = setting;
        original = setting.value().get();
    }

    private int controlWidth() { return Math.min(300, width - 24); }
    private int left() { return (width - controlWidth()) / 2; }

    @Override protected void init() {
        invalid = false;
        channels.clear();
        String[] names = {"Red", "Green", "Blue", "Opacity"};
        int[] shifts = {16, 8, 0, 24};
        for (int i = 0; i < names.length; i++) {
            ChannelSlider slider = new ChannelSlider(left(), 54 + i * 24, controlWidth(), names[i], shifts[i]);
            channels.add(addRenderableWidget(slider));
        }
        hex = addRenderableWidget(new EditBox(font, left(), 154, controlWidth(), 20, Component.literal("Hex color")));
        hex.setMaxLength(9);
        hex.setValue(SettingsInputs.hex(setting.value().get()));
        hex.setResponder(text -> {
            if (syncing) return;
            Integer parsed = SlhjConfig.parseHexColor(text);
            invalid = parsed == null;
            hex.setTextColor(invalid ? 0xFFFF7777 : 0xFFFFFFFF);
            if (parsed != null) {
                setting.value().set(parsed);
                channels.forEach(ChannelSlider::refresh);
            }
        });
        int gap = 6, buttonWidth = (controlWidth() - 2 * gap) / 3;
        addRenderableWidget(Button.builder(Component.literal("Default"), b -> {
            setting.value().set(setting.value().def());
            refresh();
        }).bounds(left(), height - 28, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose()).bounds(left() + buttonWidth + gap, height - 28, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> { accepted = true; minecraft.gui.setScreen(parent); })
                .bounds(left() + (buttonWidth + gap) * 2, height - 28, buttonWidth, 20).build());
    }

    private void refresh() {
        syncing = true;
        invalid = false;
        channels.forEach(ChannelSlider::refresh);
        hex.setValue(SettingsInputs.hex(setting.value().get()));
        hex.setTextColor(0xFFFFFFFF);
        syncing = false;
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
        ColorSwatchButton.preview(g, left(), 32, controlWidth(), 14, setting.value().get());
        g.centeredText(font, invalid ? "Use #RRGGBB or #AARRGGBB" : "Alpha first: 00 transparent, FF opaque",
                width / 2, 180, invalid ? 0xFFFF7777 : 0xFFAAAAAA);
    }

    @Override public void onClose() { accepted = false; minecraft.gui.setScreen(parent); }
    @Override public void removed() {
        if (!accepted) setting.value().set(original);
        SlhjAddons.config().save();
        super.removed();
    }
    @Override public boolean isPauseScreen() { return false; }

    private final class ChannelSlider extends AbstractSliderButton {
        private final String name;
        private final int shift;

        ChannelSlider(int x, int y, int width, String name, int shift) {
            super(x, y, width, 20, Component.empty(), ((setting.value().get() >>> shift) & 255) / 255.0);
            this.name = name;
            this.shift = shift;
            updateMessage();
        }
        void refresh() {
            value = ((setting.value().get() >>> shift) & 255) / 255.0;
            updateMessage();
        }
        @Override protected void updateMessage() {
            int channel = (int) Math.round(value * 255);
            setMessage(Component.literal(name + ": " + (shift == 24 ? Math.round(channel * 100f / 255) + "%" : channel)));
        }
        @Override protected void applyValue() {
            int mask = 255 << shift;
            setting.value().set((setting.value().get() & ~mask) | ((int) Math.round(value * 255) << shift));
            ColorPickerScreen.this.refresh();
        }
    }
}
