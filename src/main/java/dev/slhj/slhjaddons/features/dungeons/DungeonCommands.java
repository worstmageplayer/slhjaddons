package dev.slhj.slhjaddons.features.dungeons;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.client.ChatUtils;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.Locale;
import java.util.regex.Pattern;

public final class DungeonCommands extends Feature {
    private static final Pattern FLOOR_PATTERN = Pattern.compile("^([mf])([1-7])$", Pattern.CASE_INSENSITIVE);

    private static final String[] FLOOR_NAMES = {"", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN"};

    private final DungeonJoinQueue queue = new DungeonJoinQueue(System::currentTimeMillis);

    public DungeonCommands() {
        setLabel("Dungeon Commands");
        setDescription("Use /d to warp to dungeons or /d f1-f7 and /d m1-m7 to queue a floor.");
        setCategory(Category.DUNGEONS);
    }

    public static final String ID = "dungeon_commands";
    @Override public String id() { return ID; }

    @Override
    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (!isEnabled() || mc.player == null || mc.getConnection() == null) {
                queue.reset();
                return;
            }
            String command = queue.poll();
            if (command != null) ChatUtils.runCommand(command);
        });
        ClientPlayConnectionEvents.JOIN.register((h, sender, mc) -> queue.reset());
        ClientPlayConnectionEvents.DISCONNECT.register((h, mc) -> queue.reset());
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
        ChatUtils.chat("&7Usage: /d [f1-f7 | m1-m7]");
        ChatUtils.chat("&7  /d f7");
    }

    @Override protected void onDisable() { queue.reset(); }

    public void handleDungeonCommand(String arg) {
        if (!isEnabled()) {
            ChatUtils.chat("&cDungeon Commands is disabled. Enable it in /slhj settings.");
            return;
        }
        if (ClientUtils.player() == null) return;
        if (arg == null || arg.isEmpty()) {
            ChatUtils.runCommand("warp dungeons");
            return;
        }

        var matcher = FLOOR_PATTERN.matcher(arg);
        if (!matcher.matches()) {
            ChatUtils.chat("&cInvalid floor: " + arg + ". /d help for usage.");
            return;
        }

        String prefix = matcher.group(1).toLowerCase(Locale.ROOT);
        int floorNum = Integer.parseInt(matcher.group(2));
        String instanceType = prefix.equals("m") ? "MASTER_CATACOMBS" : "CATACOMBS";

        long timeLeft = queue.remainingMs();
        String command = "joininstance " + instanceType + "_FLOOR_" + FLOOR_NAMES[floorNum];
        if (!queue.enqueue(command)) {
            ChatUtils.chat("&7A dungeon join is already queued.");
            return;
        }
        if (timeLeft > 0) {
            ChatUtils.runCommand(String.format(Locale.ROOT,
                    "pc Waiting for cooldown, rejoining in %.2fs.", timeLeft / 1000.0));
        }
    }
}
