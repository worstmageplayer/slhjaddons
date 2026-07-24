package dev.slhj.slhjaddons.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class SkyblockItemUtils {
    private SkyblockItemUtils() {}

    public static CustomData getHeldCustomData(Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            McUtils.chat("§cNo item in hand!");
            return null;
        }

        return held.get(DataComponents.CUSTOM_DATA);
    }

    public static CustomData getCustomData(ItemStack item) {
        return item.get(DataComponents.CUSTOM_DATA);
    }

    public static String skyblockId(CustomData customData) {
        CompoundTag tag = customData.copyTag();
        var id = tag.getString("id");
        if (id.isPresent()) {
            return id.get();
        }
        McUtils.chat("Error, report this or something");
        return "";
    }

    public static String skyblockId(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return skyblockId(customData);
    }

    public static boolean idEquals(ItemStack stack, String id) {
        return id.equals(skyblockId(stack));
    }
}
