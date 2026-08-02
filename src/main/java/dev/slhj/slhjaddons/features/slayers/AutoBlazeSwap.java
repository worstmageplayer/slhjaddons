package dev.slhj.slhjaddons.features.slayers;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.client.SchedulerUtils;
import dev.slhj.slhjaddons.util.skyblock.SkyblockItemUtils;
import dev.slhj.slhjaddons.util.skyblock.SlayerUtils;
import dev.slhj.slhjaddons.util.client.InputUtils;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.slhj.slhjaddons.util.skyblock.SlayerUtils.isSlayerActive;

public final class AutoBlazeSwap extends Feature {

    private static final long COOLDOWN_MS = 500;
    private static final Map<String, Integer> ATTUNEMENT_NUMBERS = Map.of(
            "ASHEN", 0,
            "AURIC", 1,
            "SPIRIT", 2,
            "CRYSTAL", 3
    );

    private static final Map<Integer, Set<String>> ATTUNEMENT_DAGGERS = Map.of(
            0, Set.of("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"),
            1, Set.of("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"),
            2, Set.of("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"),
            3, Set.of("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER")
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
        AttackEntityCallback.EVENT.register((player, level, hand, entity, direction) -> attack(player, entity));
    }

    private void autoSwap(Player player, Entity blaze) {
        if (!isEnabled()) return;
        if (System.currentTimeMillis() - lastExecutionTime < COOLDOWN_MS) return;
        if (player == null || !isSlayerActive(SlayerUtils.Slayer.BLAZE)) return;

        Integer blazeAttunement = getBlazeAttunement(blaze);
        if (blazeAttunement == null) return;

        ItemStack heldDagger = player.getMainHandItem();
        Integer heldAttunement = isDagger(heldDagger) ? getAttunement(heldDagger) : null;

        boolean needsSwap = heldAttunement == null || !heldAttunement.equals(blazeAttunement);
        if (!needsSwap) return;

        SwapResult result = swapDagger(blazeAttunement, player);
        if (result == null) return;

        lastExecutionTime = System.currentTimeMillis();

        if (result.attunement() != null && result.attunement().equals(blazeAttunement)) return;
        rightClickWithCooldown();
    }
    private InteractionResult attack(Player player, Entity entity) {
        autoSwap(player, entity);
        return InteractionResult.PASS;
    }

    @Nullable
    private Integer getBlazeAttunement(Entity blaze) {
        var box = blaze.getBoundingBox().inflate(1);
        var stands = blaze.level().getEntitiesOfClass(ArmorStand.class, box);

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
        return ATTUNEMENT_DAGGERS.values().stream().anyMatch(set -> set.contains(id));
    }

    private record SwapResult(int hotbarSlot, Integer attunement) {}

    @Nullable
    private SwapResult swapDagger(int blazeAttunementNumber, Player player) {
        Set<String> targetIds = ATTUNEMENT_DAGGERS.get(blazeAttunementNumber);
        if (targetIds == null) return null;

        AbstractContainerMenu container = player.containerMenu;
        for (int slot = INVENTORY_OFFSET; slot < INVENTORY_OFFSET + 9; slot++) {
            ItemStack item = container.getSlot(slot).getItem();
            String itemId = SkyblockItemUtils.skyblockId(item);
            if (targetIds.contains(itemId)) {
                int hotbarIndex = slot - INVENTORY_OFFSET;
                InputUtils.setSelectedSlot(hotbarIndex);
                return new SwapResult(hotbarIndex, getAttunement(item));
            }
        }
        return null;
    }

    @Nullable
    private Integer getAttunement(ItemStack item) {
        CustomData itemCustomData = SkyblockItemUtils.getCustomData(item);
        if (itemCustomData == null) return null;

        CompoundTag itemCompoundTag = itemCustomData.copyTag();
        Optional<Integer> attuneMode = itemCompoundTag.getInt("td_attune_mode");
        return attuneMode.orElse(null);
    }


    private void rightClickWithCooldown() {
        SchedulerUtils.run(InputUtils::rightClick);
    }
}