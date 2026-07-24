package dev.slhj.slhjaddons;

import dev.slhj.slhjaddons.config.SlhjConfig;
import dev.slhj.slhjaddons.core.FeatureManager;
import dev.slhj.slhjaddons.features.*;
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

        FEATURES.register(new CommandsFeature());
        FEATURES.register(new ClockFeature());
        FEATURES.register(new CustomScoreboardFeature());
        FEATURES.register(new RagnarockCooldownFeature());
        FEATURES.register(new AutoFishFeature());
        FEATURES.register(new SuperboomSwapFeature());
        FEATURES.register(new QuickCommandsFeature());
        FEATURES.register(new AutoPetRulesFeature());
        FEATURES.register(new AutoZombieShootoutFeature());
        FEATURES.register(new BlazeSwapFeature());
        FEATURES.register(new BloodWarpTimerFeature());
        FEATURES.register(new CancelEmptyTooltipFeature());
        FEATURES.register(new CancelSlotHighlightFeature());
        FEATURES.register(new DungeonCommandsFeature());
        FEATURES.register(new GoldenFishFeature());
        FEATURES.register(new PartyFinderHelperFeature());
        FEATURES.register(new ShiftClickFeature());
        FEATURES.register(new SignHelperFeature());
        FEATURES.register(new ChatCalcFeature());
        FEATURES.register(new AutoClickFeature());

        FEATURES.bootstrap();

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> config.save());
        LOGGER.info("slhjaddons loaded {} features", FEATURES.all().size());
    }
}
