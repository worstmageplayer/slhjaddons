package dev.slhj.slhjaddons.mixin;

import com.mojang.blaze3d.platform.InputConstants;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.features.skyblock.ShiftClick;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import dev.slhj.slhjaddons.util.client.ContainerScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements ContainerScreenPosition {

    @Shadow protected Slot hoveredSlot;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow public abstract AbstractContainerMenu getMenu();

    @Override public int slhj$getLeftPos() { return leftPos; }
    @Override public int slhj$getTopPos() { return topPos; }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void slhj$shiftClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (handleShiftClick(event)) cir.setReturnValue(true);
    }

    @Unique
    private boolean handleShiftClick(MouseButtonEvent event) {
        ShiftClick feature = SlhjAddons.features().get(ShiftClick.class);
        if (feature == null || !feature.isEnabled()) return false;
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || hoveredSlot == null) return false;

        Object self = this;
        String title = ((AbstractContainerScreen<?>) self).getTitle().getString();
        if (!feature.containers().contains(title)) return false;
        var player = ClientUtils.player();
        if (player == null || hoveredSlot.container != player.getInventory()) return false;

        MultiPlayerGameMode multiplayerGameMode =  ClientUtils.mc().gameMode;
        if (multiplayerGameMode == null) return false;
        multiplayerGameMode.handleContainerInput(
                getMenu().containerId,
                hoveredSlot.index,
                0,
                ContainerInput.QUICK_MOVE,
                player);
        return true;
    }
}