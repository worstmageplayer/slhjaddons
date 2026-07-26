package dev.slhj.slhjaddons.features;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.ClientUtils;
import dev.slhj.slhjaddons.util.McUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

import java.util.regex.Pattern;

public final class DungeonCommands extends Feature {

    private static final Pattern FLOOR_PATTERN = Pattern.compile("^([mf])([1-7])$", Pattern.CASE_INSENSITIVE);
    private static final long COOLDOWN_MS = 30_000;

    private static final String[] FLOOR_NAMES = {"", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN"};

    private final Setting.ToggleSetting enterUndersizedSetting;

    private long cooldownEnd = 0;
    private boolean addedToQueue = false;

    public DungeonCommands() {
        setLabel("Dungeon Commands");
        category(Category.DUNGEONS);
        enterUndersizedSetting = toggle("dungeon_commands.enter_undersized", "Auto-Enter Undersized", false);
    }

    public static final String id = "dungeon_commands";
    @Override public String id() { return id; }

    @Override
    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
                dispatcher.register(ClientCommands.literal("d")
                        .then(ClientCommands.literal("help")
                                .executes(ctx -> { help(); return 1; }))
                        .executes(ctx -> { handleDungeonCommand(""); return 1; })
                        .then(ClientCommands.argument("floor", StringArgumentType.word())
                                .executes(ctx -> {
                                    handleDungeonCommand(StringArgumentType.getString(ctx, "floor"));
                                    return 1;
                                }))));
    }

    private void help() {
        McUtils.chat("&7Usage: /d [f1-f7 | m1-m7]");
        McUtils.chat("&7  /d f7");
    }

    public void handleDungeonCommand(String arg) {
        if (arg == null || arg.isEmpty()) {
            McUtils.runCommand("warp dungeons");
            return;
        }

        var matcher = FLOOR_PATTERN.matcher(arg);
        if (!matcher.matches()) {
            McUtils.chat("&cInvalid floor: " + arg + ". /dungeon help for usage.");
            return;
        }

        String prefix = matcher.group(1).toLowerCase();
        int floorNum = Integer.parseInt(matcher.group(2));
        String instanceType = prefix.equals("m") ? "MASTER_CATACOMBS" : "CATACOMBS";

        long now = System.currentTimeMillis();
        long timeLeft = Math.max(0, cooldownEnd - now);

        if (timeLeft > 0) {
            if (addedToQueue) {
                McUtils.chat(String.format("Rejoining in %.2fs", timeLeft / 1000.0));
                return;
            }
            addedToQueue = true;
            McUtils.runCommand(String.format("pc Waiting for cooldown, rejoining in %.2fs.", timeLeft / 1000.0));
        }

        long delay = timeLeft + 100;
        McUtils.scheduleTask(() -> {
            McUtils.runCommand(String.format("joininstance %s_FLOOR_%s", instanceType, FLOOR_NAMES[floorNum]));
            enterUndersized();
        }, delay);

        cooldownEnd = System.currentTimeMillis() + COOLDOWN_MS;
    }

    private void enterUndersized() {
        addedToQueue = false;
        if (!enterUndersizedSetting.value().get()) return;

        LocalPlayer player = ClientUtils.player();
        if (player == null) return;
        AbstractContainerMenu menu = player.containerMenu;
        McUtils.MC.gameMode.handleContainerInput(menu.containerId, 13, 0, ContainerInput.PICKUP, player);
    }
}