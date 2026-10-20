package dev.slhj.slhjaddons.features.misc;

import com.mojang.blaze3d.platform.InputConstants;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.client.ClickPacer;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import dev.slhj.slhjaddons.util.client.InputUtils;
import dev.slhj.slhjaddons.util.client.KeyBindingUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;

public final class AutoClick extends Feature {
    private KeyMapping key;
    private final ClickPacer pacer = new ClickPacer();
    private static final int DEFAULT_CPS = 10;

    private final Setting.SliderSetting cpsSetting;
    private final Setting.ToggleSetting entityInRange;

    public AutoClick() {
        setCategory(Category.MISC);
        setLabel("Auto Click");
        setDescription("Click while holding the Auto Click key; hold right mouse to use right clicks. Change the key in Options > Controls > Key Binds.");
        cpsSetting = intSlider("auto_click.cps", "Click Rate (CPS)", 0, 20, DEFAULT_CPS);
        entityInRange = toggle("auto_click.entity_in_range", "Requires Entity In Range", true);
    }

    public static final String ID = "auto_click";
    @Override public String id() { return ID; }

    @Override
    public void init() {
        key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.slhjaddons.auto_click",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_GRAVE,
                KeyBindingUtils.getMainCategory()
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> tick());
        ClientPlayConnectionEvents.DISCONNECT.register((h, mc) -> pacer.reset());
    }

    private void tick() {
        int cps = cpsSetting.value().get().intValue();
        if (pacer.tick(cps, isEnabled() && !ClientUtils.inGui() && ClientUtils.player() != null && key.isDown())) {
            if (isRightMouseDown()) {
                InputUtils.rightClick();
            } else {
                InputUtils.leftClick(entityInRange.value().get());
            }
        }
    }

    private boolean isRightMouseDown() {
        return ClientUtils.mc().mouseHandler.isRightPressed();
    }

    @Override
    protected void onDisable() {
        pacer.reset();
    }
}
