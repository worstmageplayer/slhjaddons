package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.ClientUtils;
import dev.slhj.slhjaddons.util.McUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

import java.util.regex.Pattern;

public final class DungeonCommandsFeature extends Feature {

    private static final Pattern FLOOR_PATTERN = Pattern.compile("^([mf])([1-7])$", Pattern.CASE_INSENSITIVE);
    private static final long COOLDOWN_MS = 30_000;

    private long cooldownEnd = 0;
    private boolean addedToQueue = false;

    public DungeonCommandsFeature() {
        setLabel("Dungeon Commands");
        category(Category.DUNGEONS);
    }

    @Override public String id() { return "dungeon_commands"; }

    @Override
    public void init() {
        // Command registration would happen through CommandsFeature
    }

    public void handleDungeonCommand(String arg) {
        if (arg == null || arg.isEmpty()) {
            McUtils.runCommand("warp dungeons");
            return;
        }

        var matcher = FLOOR_PATTERN.matcher(arg);
        if (!matcher.matches()) return;

        String prefix = matcher.group(1).toLowerCase();
        int floorNum = Integer.parseInt(matcher.group(2));

        String[] floorNames = {"", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN"};
        String instanceType = prefix.equals("m") ? "MASTER_CATACOMBS" : "CATACOMBS";

        if (floorNum < 1 || floorNum > 7) return;

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
            McUtils.runCommand(String.format("joininstance %s_FLOOR_%s", instanceType, floorNames[floorNum]));
            enterUndersized();
        }, delay);
    }

    private void enterUndersized() {
        addedToQueue = false;
        if (!SlhjAddons.config().isFeatureEnabled("enter_undersized")) return;

        LocalPlayer player = ClientUtils.player();
        if (player == null) return;
        AbstractContainerMenu menu = player.containerMenu;
        McUtils.MC.gameMode.handleContainerInput(menu.containerId, 13, 0, ContainerInput.PICKUP, player);
    }
}
