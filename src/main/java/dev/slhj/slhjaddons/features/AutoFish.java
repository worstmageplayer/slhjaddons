package dev.slhj.slhjaddons.features;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import dev.slhj.slhjaddons.util.client.InputUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
import java.util.regex.Pattern;
public final class AutoFish extends Feature {
    private static final Pattern TIMER = Pattern.compile("^\\d\\.\\d$");
    private boolean armed = false;

    private int frameCounter = 0;
    private int ticksSinceCatch = -1;

    private final int DEFAULT_DELAY = 20;
    private final Setting.SliderSetting delaySetting;

    public AutoFish() {
        category(Category.FISHING);
        setLabel("Auto Fish");
        delaySetting = intSlider("auto_fish.delay", "Auto Fish Delay (ticks)", 0, 50, DEFAULT_DELAY);
    }

    public static final String id = "auto_fish";
    @Override public String id() { return id; }

    @Override
    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> tick());
    }

    private void tick() {
        frameCounter++;

        if (ticksSinceCatch >= 0) {
            ticksSinceCatch++;
            if (ticksSinceCatch >= delaySetting.value().get().intValue()) {
                InputUtils.rightClick();
                ticksSinceCatch = -1;
            }
        }

        if (!isEnabled() || ClientUtils.inGui()) return;
        if (frameCounter % 4 != 0) return;
        LocalPlayer p = ClientUtils.player();
        if (p == null || !p.getMainHandItem().is(Items.FISHING_ROD)) return;
        var box = p.getBoundingBox().inflate(8.0);
        for (ArmorStand stand : p.level().getEntitiesOfClass(ArmorStand.class, box)) {
            String name = ChatFormatting.stripFormatting(stand.getName().getString());
            if (!armed && TIMER.matcher(name).matches()) {
                armed = true;
            } else if (armed && name.equals("!!!")) {
                InputUtils.rightClick();
                ticksSinceCatch = 0;
                armed = false;
            }
        }
    }
}