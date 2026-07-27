package dev.slhj.slhjaddons;

import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.core.FeatureManager;
import dev.slhj.slhjaddons.features.*;
import dev.slhj.slhjaddons.features.dungeons.BloodWarpTimer;
import dev.slhj.slhjaddons.features.dungeons.DungeonCommands;
import dev.slhj.slhjaddons.features.dungeons.PartyFinderHelper;
import dev.slhj.slhjaddons.features.dungeons.SuperboomSwap;
import dev.slhj.slhjaddons.features.events.AutoZombieShootout;
import dev.slhj.slhjaddons.features.fishing.AutoFish;
import dev.slhj.slhjaddons.features.fishing.GoldenFishAlert;
import dev.slhj.slhjaddons.features.misc.AutoClick;
import dev.slhj.slhjaddons.features.misc.Clock;
import dev.slhj.slhjaddons.features.misc.NoBobHurt;
import dev.slhj.slhjaddons.features.misc.QuickCommands;
import dev.slhj.slhjaddons.features.pets.AutoPetRules;
import dev.slhj.slhjaddons.features.skyblock.*;
import dev.slhj.slhjaddons.features.slayers.AutoBlazeSwap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client entry point. Replaces index.js: imports every feature and registers the command. */
public final class SlhjAddons implements ClientModInitializer {

    public static final String MOD_ID = "slhjaddons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static SlhjConfig config;
    private static final FeatureManager FEATURES = new FeatureManager();

    public static SlhjConfig config() { return config; }
    public static FeatureManager features() { return FEATURES; }

    @Override
    public void onInitializeClient() {
        config = SlhjConfig.load();

        FEATURES.register(new Commands());
        FEATURES.register(new Clock());
        FEATURES.register(new CustomScoreboard());
        FEATURES.register(new RagnarockCooldown());
        FEATURES.register(new AutoFish());
        FEATURES.register(new SuperboomSwap());
        FEATURES.register(new QuickCommands());
        FEATURES.register(new AutoPetRules());
        FEATURES.register(new AutoZombieShootout());
        FEATURES.register(new AutoBlazeSwap());
        FEATURES.register(new BloodWarpTimer());
        FEATURES.register(new CancelEmptyTooltip());
        FEATURES.register(new CancelSlotHighlight());
        FEATURES.register(new DungeonCommands());
        FEATURES.register(new GoldenFishAlert());
        FEATURES.register(new PartyFinderHelper());
        FEATURES.register(new ShiftClick());
        FEATURES.register(new SignHelper());
        FEATURES.register(new ChatCalc());
        FEATURES.register(new AutoClick());
        FEATURES.register(new NoBobHurt());

        FEATURES.bootstrap();

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> config.save());
        LOGGER.info("slhjaddons loaded {} features", FEATURES.all().size());
    }
}
