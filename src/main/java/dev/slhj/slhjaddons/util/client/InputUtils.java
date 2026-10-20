package dev.slhj.slhjaddons.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.sdl.SDLMouse;

public final class InputUtils {
    private InputUtils() {}

    public static void setSelectedSlot(int index) {
        LocalPlayer p = ClientUtils.player();
        if (p != null && index >= 0 && index < 9) p.getInventory().setSelectedSlot(index);
    }

    public static void centerCursor() {
        var window = ClientUtils.mc().getWindow();
        SDLMouse.SDL_WarpMouseInWindow(window.handle(), window.getScreenWidth() / 2.0f, window.getScreenHeight() / 2.0f);
    }

    public static void rightClick() {
        LocalPlayer p = ClientUtils.player();
        if (p != null && ClientUtils.mc().gameMode != null) {
            ClientUtils.mc().gameMode.useItem(p, InteractionHand.MAIN_HAND);
            p.swing(InteractionHand.MAIN_HAND, p.getMainHandItem().getInteractAnimation(), false);
        }
    }

    public static void leftClick(boolean requireEntityInRange) {
        LocalPlayer p = ClientUtils.player();
        Minecraft mc = ClientUtils.mc();
        if (p == null || mc.gameMode == null) return;

        if (mc.hitResult instanceof EntityHitResult entityHit) {
            mc.gameMode.attack(p, entityHit.getEntity());
        } else if (requireEntityInRange) {
            return;
        }
        p.swing(InteractionHand.MAIN_HAND, p.getMainHandItem().getAttackAnimation(), false);
        p.connection.send(ServerboundPunchPacket.INSTANCE);
    }
}