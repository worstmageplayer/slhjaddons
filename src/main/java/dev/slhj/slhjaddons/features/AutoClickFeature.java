package dev.slhj.slhjaddons.features;

import com.mojang.blaze3d.platform.InputConstants;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.ClientUtils;
import dev.slhj.slhjaddons.util.KeyBindingUtils;
import dev.slhj.slhjaddons.util.McUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class AutoClickFeature extends Feature {

    private KeyMapping key;
    private int tickCounter = 0;
    private final int DEFAULT_CPS = 10;

    private final Setting.SliderSetting cpsSetting;

    public AutoClickFeature() {
        category(Category.MISC);
        setLabel("Auto Click");
        cpsSetting = intSlider("auto_click.cps", "Auto Click CPS", 0, 40, DEFAULT_CPS);
    }

    @Override public String id() { return "auto_click"; }

    @Override
    public void init() {
        key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.slhjaddons.auto_click",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                KeyBindingUtils.getMainCategory()
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> tick());
    }

    private void tick() {
        if (!isEnabled() || ClientUtils.inGui()) return;
        if (!key.isDown()) return;

        int cps = cpsSetting.value().get().intValue();
        int interval = Math.max(1, 20 / Math.max(1, cps));

        if (tickCounter++ % interval == 0) {
            McUtils.leftClick(true);
        }
    }

    @Override
    protected void onDisable() {
        tickCounter = 0;
    }
}