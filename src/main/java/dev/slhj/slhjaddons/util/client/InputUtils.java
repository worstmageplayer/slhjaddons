package dev.slhj.slhjaddons.util.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.sdl.SDLMouse;

public final class InputUtils {
    private InputUtils() {}

    public static void setSelectedSlot(int index) {
        LocalPlayer player = ClientUtils.player();
        if (player != null && index >= 0 && index < 9) player.getInventory().setSelectedSlot(index);
    }

    public static void centerCursor() {
        var window = ClientUtils.mc().getWindow();
        SDLMouse.SDL_WarpMouseInWindow(window.handle(), window.getScreenWidth() / 2.0f, window.getScreenHeight() / 2.0f);
    }

    public static void rightClick() {
        LocalPlayer player = ClientUtils.player();
        if (player != null && ClientUtils.mc().gameMode != null) {
            ClientUtils.mc().gameMode.useItem(player, InteractionHand.MAIN_HAND);
            player.swing(InteractionHand.MAIN_HAND, player.getMainHandItem().getInteractAnimation(), false);
        }
    }

    public static void leftClick(boolean requireEntityInRange) {
        LocalPlayer player = ClientUtils.player();
        Minecraft mc = ClientUtils.mc();
        if (player == null || mc.gameMode == null) return;

        if (mc.hitResult instanceof EntityHitResult entityHit) {
            mc.gameMode.attack(player, entityHit.getEntity());
        } else if (requireEntityInRange) {
            return;
        }
        player.swing(InteractionHand.MAIN_HAND, player.getMainHandItem().getAttackAnimation(), false);
        player.connection.send(ServerboundPunchPacket.INSTANCE);
    }
}
