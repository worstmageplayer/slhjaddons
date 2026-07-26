package dev.slhj.slhjaddons.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.features.NoBobHurt;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class HurtMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void slhj$bobHurt(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (SlhjAddons.config().isFeatureEnabled(NoBobHurt.id)) ci.cancel();
    }

}
