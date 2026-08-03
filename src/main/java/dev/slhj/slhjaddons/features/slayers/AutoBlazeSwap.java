package dev.slhj.slhjaddons.features.slayers;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.client.ChatUtils;
import dev.slhj.slhjaddons.util.client.SchedulerUtils;
import dev.slhj.slhjaddons.util.skyblock.SkyblockItemUtils;
import dev.slhj.slhjaddons.util.skyblock.SlayerUtils;
import dev.slhj.slhjaddons.util.skyblock.SlayerUtils.BlazeAttunements;
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

import java.util.Optional;

import static dev.slhj.slhjaddons.util.skyblock.SlayerUtils.isSlayerActive;

public final class AutoBlazeSwap extends Feature {

    private static final long COOLDOWN_MS = 500;

    private long lastExecutionTime = 0;
    private static final int INVENTORY_OFFSET = 36;

    public AutoBlazeSwap() {
        setLabel("Blaze Slayer Dagger Auto Swap");
        category(Category.SLAYERS);
    }

    public static final String id = "blaze_auto_swap";
    @Override public String id() { return id; }

    @Override
    public void init() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, entityHitResult) -> attack(player, entity));
    }

    private void autoSwap(Player player, Entity blaze) {
        if (!isEnabled()) return;
        if (System.currentTimeMillis() - lastExecutionTime < COOLDOWN_MS) return;
        if (player == null || !isSlayerActive(SlayerUtils.Slayer.BLAZE)) return;

        BlazeAttunements attunement = getBlazeAttunement(blaze);
        if (attunement == null) return;

        ItemStack heldItem = player.getMainHandItem();
        BlazeAttunements heldAttunement = attunement.isDagger(heldItem) ? getAttunement(heldItem) : null;
        if (heldAttunement == attunement) return;

        BlazeAttunements result = swapDagger(attunement, player);
        if (result == null) return;

        lastExecutionTime = System.currentTimeMillis();

        if (result == attunement) return;
        SchedulerUtils.run(InputUtils::rightClick);
    }
    private InteractionResult attack(Player player, Entity entity) {
        autoSwap(player, entity);
        return InteractionResult.PASS;
    }

    @Nullable
    private BlazeAttunements getBlazeAttunement(Entity blaze) {
        var box = blaze.getBoundingBox().inflate(2);
        var stands = blaze.level().getEntitiesOfClass(ArmorStand.class, box);
        for (ArmorStand stand : stands) {
            BlazeAttunements attunement = BlazeAttunements.fromArmorStandName(stand.getName().getString());
            if (attunement != null) return attunement;
        }
        return null;
    }

    @Nullable
    private BlazeAttunements swapDagger(BlazeAttunements target, Player player) {
        AbstractContainerMenu container = player.containerMenu;
        for (int slot = INVENTORY_OFFSET; slot < INVENTORY_OFFSET + 9; slot++) {
            ItemStack item = container.getSlot(slot).getItem();
            if (!target.isDagger(item)) continue;

            int hotbarIndex = slot - INVENTORY_OFFSET;
            InputUtils.setSelectedSlot(hotbarIndex);
            return getAttunement(item);
        }
        return null;
    }

    @Nullable
    private BlazeAttunements getAttunement(ItemStack item) {
        CustomData itemCustomData = SkyblockItemUtils.getCustomData(item);
        if (itemCustomData == null) return null;

        CompoundTag itemCompoundTag = itemCustomData.copyTag();
        Optional<Integer> attuneMode = itemCompoundTag.getInt("td_attune_mode");

        return attuneMode.map(BlazeAttunements::fromAttunementNumber).orElse(null);
    }
}