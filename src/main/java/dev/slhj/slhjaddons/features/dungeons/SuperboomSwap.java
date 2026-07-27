package dev.slhj.slhjaddons.features.dungeons;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import dev.slhj.slhjaddons.util.client.InputUtils;
import dev.slhj.slhjaddons.util.skyblock.HypixelUtils;
import dev.slhj.slhjaddons.util.skyblock.SkyblockItemUtils;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class SuperboomSwap extends Feature {

    public SuperboomSwap() {
        setLabel("SuperBoom Auto Swap");
        category(Category.DUNGEONS);
    }

    public static final String id = "superboom_swap";
    @Override public String id() { return id; }

    @Override
    public void init() {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (!isEnabled() || !level.isClientSide()) return InteractionResult.PASS;
            if (!HypixelUtils.inDungeon()) return InteractionResult.PASS;

            BlockState state = level.getBlockState(pos);
            boolean boomBlock = state.is(Blocks.STONE_BRICKS)
                    || state.is(Blocks.CRACKED_STONE_BRICKS)
                    || state.is(Blocks.STONE_SLAB)
                    || state.is(Blocks.SMOOTH_STONE_SLAB);
            if (!boomBlock) return InteractionResult.PASS;

            var inv = ClientUtils.player().getInventory();
            for (int i = 0; i < 9; i++) {
                if (SkyblockItemUtils.idEquals(inv.getItem(i), "SUPERBOOM_TNT")) {
                    InputUtils.setSelectedSlot(i);
                    break;
                }
            }
            return InteractionResult.PASS; // don't eat the click
        });
    }
}
