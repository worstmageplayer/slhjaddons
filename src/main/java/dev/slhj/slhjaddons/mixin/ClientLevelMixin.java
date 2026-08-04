package dev.slhj.slhjaddons.mixin;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.features.misc.NoBlockBreakParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void slhj$addDestroyBlockEffect(BlockPos pos, BlockState blockState, CallbackInfo ci) {
        if (!SlhjAddons.isEnabled(NoBlockBreakParticles.class)) return;
        ci.cancel();
    }

    @Inject(method = "addBreakingBlockEffect", at = @At("HEAD"), cancellable = true)
    private void slhj$addBreakingBlockEffect(BlockPos pos, Direction direction, CallbackInfo ci) {
        if (!SlhjAddons.isEnabled(NoBlockBreakParticles.class)) return;
        ci.cancel();
    }

}