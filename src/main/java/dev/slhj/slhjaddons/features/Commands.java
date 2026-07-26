package dev.slhj.slhjaddons.features;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.gui.HudEditorScreen;
import dev.slhj.slhjaddons.gui.SlhjSettingsScreen;
import dev.slhj.slhjaddons.util.McUtils;
import dev.slhj.slhjaddons.util.SkyblockItemUtils;
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
                        .executes(ctx -> { openGui(); return 1; })
                        .then(ClientCommands.literal("list")
                                .executes(ctx -> { list(); return 1; }))
                        .then(ClientCommands.literal("component_custom_data")
                                .executes(ctx -> copyNBT()))
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

    private int copyNBT() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return 0;

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            McUtils.chat("§cNo item in hand!");
            return 0;
        }

        CustomData customData = held.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            McUtils.chat("§cItem has no custom_data component!");
            return 0;
        }

        CompoundTag tag = customData.copyTag(); // or getUnsafe() if you don't want a copy
        String nbtString = tag.toString();

        Minecraft.getInstance().keyboardHandler.setClipboard(nbtString);
        McUtils.chat("§aCopied custom_data to clipboard! (" + nbtString.length() + " chars)");

        return 1;
    }

    private Integer getAttunement(ItemStack item) {
        CustomData itemCustomData = SkyblockItemUtils.getCustomData(item);
        CompoundTag itemCompoundTag = itemCustomData.copyTag();
        var td_attune_mode = itemCompoundTag.getInt("td_attune_mode");
        if (td_attune_mode.isPresent()) {
            return td_attune_mode.get();
        }

        McUtils.chat("either not a blaze dagger or something else");
        return null;
    }

    private void openGui() {
        McUtils.MC.execute(() -> McUtils.MC.setScreen(new SlhjSettingsScreen()));
    }

    private void list() {
        McUtils.chat("&e&lslhj&raddons &7features:");
        var cfg = SlhjAddons.config();
        for (Feature f : SlhjAddons.features().all()) {
            boolean on = cfg.isFeatureEnabled(f.id());
            McUtils.chat(" &7- &f" + f.id() + " " + (on ? "&aON" : "&cOFF"));
        }
    }

    private void toggle(String id) {
        var cfg = SlhjAddons.config();
        boolean next = !cfg.isFeatureEnabled(id);
        cfg.setFeatureEnabled(id, next);
        cfg.save();
        SlhjAddons.features().syncFromConfig();
        McUtils.chat("&7" + id + " -> " + (next ? "&aON" : "&cOFF"));
    }
}