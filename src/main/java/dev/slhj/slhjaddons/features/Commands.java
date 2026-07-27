package dev.slhj.slhjaddons.features;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.gui.HudEditorScreen;
import dev.slhj.slhjaddons.gui.SlhjSettingsScreen;
import dev.slhj.slhjaddons.util.client.ChatUtils;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class Commands extends Feature {

    public static final String id = "commands";
    @Override public String id() { return id; }

    @Override
    public void init() {
        setEnabled(true);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
                dispatcher.register(ClientCommands.literal("slhj")
                        .executes(ctx -> {
                            openGui();
                            return 1;
                        })
                        .then(ClientCommands.literal("list")
                                .executes(ctx -> {
                                    list();
                                    return 1;
                                }))
                        .then(ClientCommands.literal("component_custom_data")
                                .executes(ctx -> {
                                    copyNBT();
                                    return 1;
                                }))
                        .then(ClientCommands.literal("hud")
                                .executes(ctx -> {
                                    HudEditorScreen.openHudEditor();
                                    return 1;
                                }))
                        .then(ClientCommands.literal("toggle")
                                .then(ClientCommands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> {
                                            toggle(StringArgumentType.getString(ctx, "id"));
                                            return 1;
                                        })))));
    }

    private void copyNBT() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            ChatUtils.chat("§cNo item in hand!");
            return;
        }

        CustomData customData = held.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            ChatUtils.chat("§cItem has no custom_data component!");
            return;
        }

        CompoundTag tag = customData.copyTag();
        String nbtString = tag.toString();

        Minecraft.getInstance().keyboardHandler.setClipboard(nbtString);
        ChatUtils.chat("§aCopied custom_data to clipboard! (" + nbtString.length() + " chars)");
    }

    private void openGui() {
        ClientUtils.mc().execute(() -> ClientUtils.mc().setScreen(new SlhjSettingsScreen()));
    }

    private void list() {
        ChatUtils.chat("&e&lslhj&raddons &7features:");
        SlhjConfig cfg = SlhjAddons.config();
        for (Feature f : SlhjAddons.features().all()) {
            boolean on = cfg.isFeatureEnabled(f.id());
            ChatUtils.chat(" &7- &f" + f.id() + " " + (on ? "&aON" : "&cOFF"));
        }
    }

    private void toggle(String id) {
        SlhjConfig cfg = SlhjAddons.config();
        boolean next = !cfg.isFeatureEnabled(id);
        cfg.setFeatureEnabled(id, next);
        cfg.save();
        SlhjAddons.features().syncFromConfig();
        ChatUtils.chat("&7" + id + " -> " + (next ? "&aON" : "&cOFF"));
    }
}