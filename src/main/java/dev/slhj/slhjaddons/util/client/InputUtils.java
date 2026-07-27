package dev.slhj.slhjaddons.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class InputUtils {
    private InputUtils() {}

    public static void setSelectedSlot(int index) {
        LocalPlayer p = ClientUtils.player();
        if (p != null && index >= 0 && index < 9) p.getInventory().setSelectedSlot(index);
    }

    public static void centerCursor() {
        var window = ClientUtils.mc().getWindow();
        GLFW.glfwSetCursorPos(window.handle(), window.getWidth() / 2.0, window.getHeight() / 2.0);
    }

    public static void rightClick() {
        LocalPlayer p = ClientUtils.player();
        if (p != null && ClientUtils.mc().gameMode != null) {
            ClientUtils.mc().gameMode.useItem(p, InteractionHand.MAIN_HAND);
            p.swing(InteractionHand.MAIN_HAND);
        }
    }

    public static void leftClick(boolean requireEntityInRange) {
        LocalPlayer p = ClientUtils.player();
        Minecraft mc = ClientUtils.mc();
        if (p == null || mc.gameMode == null) return;

        if (requireEntityInRange) {
            if (mc.hitResult instanceof EntityHitResult entityHit) {
                Entity target = entityHit.getEntity();
                mc.gameMode.attack(p, target);
                p.swing(InteractionHand.MAIN_HAND);
            }
        } else {
            p.swing(InteractionHand.MAIN_HAND);
        }
    }
}