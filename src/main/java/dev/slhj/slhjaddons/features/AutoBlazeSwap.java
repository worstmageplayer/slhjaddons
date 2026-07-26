package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.McUtils;
import dev.slhj.slhjaddons.util.SkyblockItemUtils;
import dev.slhj.slhjaddons.util.SlayerUtils;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Map;

import static dev.slhj.slhjaddons.util.SlayerUtils.isSlayerActive;

public final class AutoBlazeSwap extends Feature {

    private static final long COOLDOWN_MS = 500;
    private static final Map<String, Integer> ATTUNEMENT_NUMBERS = Map.of(
            "ASHEN", 0,
            "AURIC", 1,
            "SPIRIT", 2,
            "CRYSTAL", 3
    );

    private static final Map<Integer, String> ATTUNEMENT_DAGGER = Map.of(
            0, "HEARTFIRE_DAGGER",
            1, "HEARTFIRE_DAGGER",
            2, "HEARTMAW_DAGGER",
            3, "HEARTMAW_DAGGER"
    );

    private long lastExecutionTime = 0;
    private static final int INVENTORY_OFFSET = 36;

    public AutoBlazeSwap() {
        setLabel("Blaze Slayer Attunement Auto Swap");
        category(Category.SLAYERS);
    }

    public static final String id = "blaze_auto_swap";
    @Override public String id() { return id; }

    @Override
    public void init() {
        AttackEntityCallback.EVENT.register((player, level, hand, pos, direction) -> attack(player));
    }

    private InteractionResult attack(Player player) {
        if (!isEnabled()) return InteractionResult.PASS;
        if (System.currentTimeMillis() - lastExecutionTime < COOLDOWN_MS) {
            return InteractionResult.PASS;
        }

        if (player == null || !isSlayerActive(SlayerUtils.Slayer.BLAZE)) {
            return InteractionResult.PASS;
        }

        Integer blazeAttunement = getBlazeAttunement(player);
        if (blazeAttunement == null) return InteractionResult.PASS;
        //McUtils.chat("Blaze Attunement: " + blazeAttunement.toString());

        ItemStack heldDagger = player.getMainHandItem();
        Integer heldAttunement = isDagger(heldDagger) ? getAttunement(heldDagger) : null;
        //McUtils.chat("Dagger Attunement: " + heldAttunement);

        boolean needsSwap = heldAttunement == null || !heldAttunement.equals(blazeAttunement);
        if (!needsSwap) return InteractionResult.PASS;

        SwapResult result = swapDagger(blazeAttunement, player);
        if (result == null) return InteractionResult.PASS;

        lastExecutionTime = System.currentTimeMillis();

        if (result.attunement() != null && result.attunement().equals(blazeAttunement)) {
            return InteractionResult.PASS;
        }
        rightClickWithCooldown();
        return InteractionResult.PASS;
    }

    private Integer getBlazeAttunement(Player player) {
        var box = player.getBoundingBox().inflate(6);
        var stands = player.level().getEntitiesOfClass(ArmorStand.class, box);

        for (ArmorStand stand : stands) {
            String name = stand.getName().getString();

            for (Map.Entry<String, Integer> entry : ATTUNEMENT_NUMBERS.entrySet()) {
                if (name.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private boolean isDagger(ItemStack stack) {
        String id = SkyblockItemUtils.skyblockId(stack);
        return "HEARTFIRE_DAGGER".equals(id) || "HEARTMAW_DAGGER".equals(id);
    }

    private record SwapResult(int hotbarSlot, Integer attunement) {}

    private SwapResult swapDagger(int blazeAttunementNumber, Player player) {
        String targetId = ATTUNEMENT_DAGGER.get(blazeAttunementNumber);
        if (targetId == null) return null;

        AbstractContainerMenu container = player.containerMenu;
        for (int slot = INVENTORY_OFFSET; slot < INVENTORY_OFFSET + 9; slot++) {
            ItemStack item = container.getSlot(slot).getItem();
            String itemId = SkyblockItemUtils.skyblockId(item);
            if (targetId.equals(itemId)) {
                int hotbarIndex = slot - INVENTORY_OFFSET;
                McUtils.setSelectedSlot(hotbarIndex);
                return new SwapResult(hotbarIndex, getAttunement(item));
            }
        }
        return null;
    }

    private Integer getAttunement(ItemStack item) {
        CustomData itemCustomData = SkyblockItemUtils.getCustomData(item);
        if (itemCustomData == null) return null;

        CompoundTag itemCompoundTag = itemCustomData.copyTag();
        var attuneMode = itemCompoundTag.getInt("td_attune_mode");
        return attuneMode.orElse(null);
    }

    private void rightClickWithCooldown() {
        McUtils.scheduleTask(McUtils::rightClick);
    }
}