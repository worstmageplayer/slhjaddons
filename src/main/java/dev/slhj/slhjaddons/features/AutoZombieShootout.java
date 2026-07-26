package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.ClientUtils;
import dev.slhj.slhjaddons.util.HypixelUtils;
import dev.slhj.slhjaddons.util.McUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class AutoZombieShootout extends Feature {

    private static final long CLICK_COOLDOWN = 200;
    private long lastClick = 0;

    @Override public String id() { return "auto_zombie_shootout"; }

    public AutoZombieShootout() {
        setLabel("Auto Zombie Shootout");
        category(Category.EVENTS);
    }

    @Override
    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> tick());
    }

    private void tick() {
        if (!isEnabled()) return;
        if (!HypixelUtils.inSkyblock()) return;
        if (System.currentTimeMillis() - lastClick < CLICK_COOLDOWN) return;

        LocalPlayer player = ClientUtils.player();
        if (player == null) return;

        String heldItem = McUtils.itemNameContains(player.getMainHandItem(), "Dart") ? "Dart" : "";
        if (heldItem.isEmpty()) return;

        var level = McUtils.MC.level;
        if (level == null) return;

        for (var entity : level.getEntities(null, player.getBoundingBox().inflate(50))) {
            if (entity instanceof LivingEntity && entity != player && entity.getDisplayName().getString().contains("Zombie")) {
                Vec3 lookPos = entity.getEyePosition();
                var rot = player.getRotationVector();
                player.setYRot((float) (Math.atan2(lookPos.z - player.getZ(), lookPos.x - player.getX()) * 180 / Math.PI) - 90);
                player.setXRot((float) -(Math.asin((lookPos.y - player.getEyeY()) / player.distanceTo(entity)) * 180 / Math.PI));
                
                McUtils.rightClick();
                lastClick = System.currentTimeMillis();
                return;
            }
        }
    }
}
