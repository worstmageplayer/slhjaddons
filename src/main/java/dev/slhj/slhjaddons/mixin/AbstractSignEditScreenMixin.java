package dev.slhj.slhjaddons.mixin;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.features.skyblock.SignHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.slhj.slhjaddons.features.skyblock.SignHelper.INPUT_SIGN_MARKER;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin {
    @Final @Shadow private String[] messages;
    @Unique private boolean slhj$initialized;

    @Inject(method = "keyPressed", at = @At("RETURN"))
    private void slhj$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        pushToFeature();
    }

    @Inject(method = "charTyped", at = @At("TAIL"))
    private void slhj$onCharTyped(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
        pushToFeature();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void slhj$init(CallbackInfo ci) {
        SignHelper feature = SlhjAddons.features().get(SignHelper.class);
        if (feature != null && !slhj$initialized) feature.resetSign();
        slhj$initialized = true;
        pushToFeature();
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void slhj$submit(CallbackInfo ci) {
        if (!SlhjAddons.isEnabled(SignHelper.class) || !supportedSign()) return;
        SignHelper feature = SlhjAddons.features().get(SignHelper.class);
        feature.updateFromSignLine(messages[0]);
        messages[0] = feature.getResult();
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void slhj$removed(CallbackInfo ci) {
        SignHelper feature = SlhjAddons.features().get(SignHelper.class);
        if (feature != null) feature.resetSign();
    }

    @Unique
    private boolean supportedSign() {
        return messages != null && messages.length >= 2 && INPUT_SIGN_MARKER.equals(messages[1]);
    }

    @Unique
    private void pushToFeature() {
        if (!SlhjAddons.isEnabled(SignHelper.class)) return;
        SignHelper feature = SlhjAddons.features().get(SignHelper.class);
        if (feature == null || messages == null || messages.length == 0) return;

        if (!supportedSign()) {
            feature.resetSign();
            return;
        }
        feature.updateFromSignLine(messages[0]);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void slhj$extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!SlhjAddons.isEnabled(SignHelper.class) || !supportedSign()) return;
        SignHelper feature = SlhjAddons.features().get(SignHelper.class);
        if (feature != null) feature.render(graphics);
    }
}
