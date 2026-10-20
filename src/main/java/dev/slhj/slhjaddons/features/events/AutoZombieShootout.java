package dev.slhj.slhjaddons.features.events;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import dev.slhj.slhjaddons.util.client.InputUtils;
import dev.slhj.slhjaddons.util.skyblock.HypixelUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class AutoZombieShootout extends Feature {
    private static final long CLICK_COOLDOWN_MS = 200;
    private long lastClickAt = 0;

    public static final String ID = "auto_zombie_shootout";
    @Override public String id() { return ID; }

    public AutoZombieShootout() {
        setLabel("Auto Zombie Shootout");
        setDescription("Aim at zombies and use a held Dart during Zombie Shootout.");
        setCategory(Category.EVENTS);
    }

    @Override
    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> tick());
    }

    private void tick() {
        if (!isEnabled()) return;
        if (!HypixelUtils.inSkyblock()) return;
        if (System.currentTimeMillis() - lastClickAt < CLICK_COOLDOWN_MS) return;

        LocalPlayer player = ClientUtils.player();
        if (player == null) return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty()) return;

        String heldItem = ClientUtils.hoverName(player.getMainHandItem());
        if (heldItem != null && !heldItem.contains("Dart")) return;

        var level = ClientUtils.mc().level;
        if (level == null) return;

        for (var entity : level.getEntities(null, player.getBoundingBox().inflate(50))) {
            if (entity instanceof LivingEntity && entity != player && entity.getDisplayName().getString().contains("Zombie")) {
                Vec3 lookPos = entity.getEyePosition();
                player.setYRot((float) (Math.atan2(lookPos.z - player.getZ(), lookPos.x - player.getX()) * 180 / Math.PI) - 90);
                player.setXRot((float) -(Math.asin((lookPos.y - player.getEyeY()) / player.distanceTo(entity)) * 180 / Math.PI));

                InputUtils.rightClick();
                lastClickAt = System.currentTimeMillis();
                return;
            }
        }
    }
}
