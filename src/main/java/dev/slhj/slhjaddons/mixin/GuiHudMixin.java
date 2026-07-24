package dev.slhj.slhjaddons.mixin;

import dev.slhj.slhjaddons.SlhjAddons;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiHudMixin {
    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void slhj$hideVanillaScoreboard(CallbackInfo ci) {
        if (SlhjAddons.config().isFeatureEnabled("custom_scoreboard")) ci.cancel();
    }
}
