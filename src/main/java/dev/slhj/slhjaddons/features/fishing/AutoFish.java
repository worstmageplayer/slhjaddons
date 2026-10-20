package dev.slhj.slhjaddons.features.fishing;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import dev.slhj.slhjaddons.util.client.InputUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Items;
public final class AutoFish extends Feature {
    private final FishingCycle cycle = new FishingCycle();
    private int frameCounter;

    private final static int DEFAULT_DELAY = 20;
    private final Setting.SliderSetting delaySetting;

    public AutoFish() {
        category(Category.FISHING);
        setLabel("Auto Fish");
        setDescription("Reel in on a fishing alert, then cast again after a delay.");
        delaySetting = intSlider("auto_fish.delay", "Recast Delay (ticks)", 0, 50, DEFAULT_DELAY);
    }

    public static final String id = "auto_fish";
    @Override public String id() { return id; }

    @Override
    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> tick());
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.BEFORE_INIT.register((mc, screen, w, h) -> reset());
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((h, sender, mc) -> reset());
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((h, mc) -> reset());
    }

    private void reset() { cycle.reset(); frameCounter = 0; }
    @Override protected void onDisable() { reset(); }

    private void tick() {
        LocalPlayer p = ClientUtils.player();
        if (!isEnabled() || ClientUtils.inGui() || p == null || !p.getMainHandItem().is(Items.FISHING_ROD)) {
            reset();
            return;
        }
        frameCounter++;

        if (cycle.tick(true, delaySetting.value().get().intValue())) {
            InputUtils.rightClick();
            return;
        }

        if (frameCounter % 4 != 0) return;
        var box = p.getBoundingBox().inflate(8.0);
        for (ArmorStand stand : p.level().getEntitiesOfClass(ArmorStand.class, box)) {
            String name = ChatFormatting.stripFormatting(stand.getName().getString());
            if (cycle.observe(name)) {
                InputUtils.rightClick();
                break;
            }
        }
    }
}
