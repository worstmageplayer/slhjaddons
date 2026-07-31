package dev.slhj.slhjaddons.mixin;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.features.skyblock.SignHelper;
import static dev.slhj.slhjaddons.features.skyblock.SignHelper.INPUT_SIGN_MARKER;
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

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin {

    @Final @Shadow private String[] messages;

    @Inject(method = "keyPressed", at = @At("RETURN"))
    private void slhj$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        pushToFeature();
    }

    @Inject(method = "charTyped", at = @At("TAIL"))
    private void slhj$onCharTyped(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
        pushToFeature();
    }

    @Inject(method = "onDone", at = @At("HEAD"))
    private void slhj$onDone(CallbackInfo ci) {
        if (!SlhjAddons.isEnabled(SignHelper.class)) return;
        if (messages == null || messages.length == 0) return;
        if (messages.length < 2 || !INPUT_SIGN_MARKER.equals(messages[1])) return;

        SignHelper feature = SlhjAddons.features().get(SignHelper.class);
        if (feature == null) return;

        messages[0] = feature.getResult();
    }

    @Unique
    private void pushToFeature() {
        if (!SlhjAddons.isEnabled(SignHelper.class)) return;
        SignHelper feature = SlhjAddons.features().get(SignHelper.class);
        if (feature == null || messages == null || messages.length == 0) return;

        if (messages.length < 2 || !INPUT_SIGN_MARKER.equals(messages[1])) {
            feature.setDisplayFormula(null);
            return;
        }
        feature.updateFromSignLine(messages[0]);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void slhj$extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!SlhjAddons.isEnabled(SignHelper.class)) return;
        SignHelper feature = SlhjAddons.features().get(SignHelper.class);
        if (feature != null) feature.render(graphics);
    }
}