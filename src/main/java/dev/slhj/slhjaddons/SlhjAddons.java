package dev.slhj.slhjaddons;

import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.hud.HudManager;
import dev.slhj.slhjaddons.core.FeatureManager;
import dev.slhj.slhjaddons.features.*;
import dev.slhj.slhjaddons.features.dungeons.BloodWarpTimer;
import dev.slhj.slhjaddons.features.dungeons.DungeonCommands;
import dev.slhj.slhjaddons.features.dungeons.PartyFinderHelper;
import dev.slhj.slhjaddons.features.dungeons.SuperboomSwap;
import dev.slhj.slhjaddons.features.events.AutoZombieShootout;
import dev.slhj.slhjaddons.features.fishing.AutoFish;
import dev.slhj.slhjaddons.features.fishing.GoldenFishAlert;
import dev.slhj.slhjaddons.features.misc.*;
import dev.slhj.slhjaddons.features.pets.AutoPetRules;
import dev.slhj.slhjaddons.features.skyblock.*;
import dev.slhj.slhjaddons.features.slayers.AutoBlazeSwap;
import dev.slhj.slhjaddons.util.skyblock.CooldownUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SlhjAddons implements ClientModInitializer {

    public static final String MOD_ID = "slhjaddons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static SlhjConfig config;
    private static final HudManager HUD = new HudManager();
    public static HudManager hud() { return HUD; }

    private static final FeatureManager FEATURES = new FeatureManager();

    public static SlhjConfig config() { return config; }
    public static FeatureManager features() { return FEATURES; }

    public static boolean isEnabled(Class<? extends Feature> type) {
        return FEATURES.isEnabled(type);
    }

    @Override
    public void onInitializeClient() {
        config = SlhjConfig.load();

        FEATURES.register(new Commands());
        FEATURES.register(new Clock());
        FEATURES.register(new RagnarockCooldown());
        FEATURES.register(new AutoFish());
        FEATURES.register(new SuperboomSwap());
        FEATURES.register(new QuickCommands());
        FEATURES.register(new AutoPetRules());
        FEATURES.register(new AutoZombieShootout());
        FEATURES.register(new AutoBlazeSwap());
        FEATURES.register(new BloodWarpTimer());
        FEATURES.register(new DungeonCommands());
        FEATURES.register(new GoldenFishAlert());
        FEATURES.register(new PartyFinderHelper());
        FEATURES.register(new ShiftClick());
        FEATURES.register(new SignHelper());
        FEATURES.register(new ChatCalc());
        FEATURES.register(new AutoClick());
        FEATURES.register(new NoBobHurt());
        FEATURES.register(new NoFog());
        FEATURES.register(new NoBlockBreakParticles());

        CooldownUtils.init();
        FEATURES.bootstrap();
        HUD.init();

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> config.save());
        LOGGER.info("slhjaddons loaded {} features", FEATURES.all().size());
    }
}
