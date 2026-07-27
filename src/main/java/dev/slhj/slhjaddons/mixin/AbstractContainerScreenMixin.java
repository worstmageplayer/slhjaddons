package dev.slhj.slhjaddons.mixin;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.features.skyblock.CancelSlotHighlight;
import dev.slhj.slhjaddons.features.skyblock.ShiftClick;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow protected Slot hoveredSlot;
    @Shadow public abstract AbstractContainerMenu getMenu();

    @Unique
    private static final String[] SHIFT_MENUS = {"Trades", "Your Equipment and Stats"};

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void slhj$shiftClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!SlhjAddons.isEnabled(ShiftClick.class)) return;
        if (event.button() != 0 || hoveredSlot == null) return;

        Object self = this;
        String title = ((AbstractContainerScreen<?>) self).getTitle().getString();
        boolean found = false;
        for (String menu : SHIFT_MENUS) {
            if (menu.equals(title)) {
                found = true;
                break;
            }
        }
        if (!found) return;
        if (hoveredSlot.index < 54) return;

        MultiPlayerGameMode multiplayerGameMode =  ClientUtils.mc().gameMode;
        if (multiplayerGameMode == null) return;
        multiplayerGameMode.handleContainerInput(
                getMenu().containerId, hoveredSlot.index, 0, ContainerInput.QUICK_MOVE, ClientUtils.player());
        cir.setReturnValue(true);
    }

    @Inject(method = "extractSlotHighlightFront", at = @At("HEAD"), cancellable = true)
    private void slhj$extractSlotHighlightFront(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (!SlhjAddons.isEnabled(CancelSlotHighlight.class)) return;
        ci.cancel();
    }
}