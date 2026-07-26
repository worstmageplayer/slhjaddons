package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class CancelEmptyTooltip extends Feature {

    private static final String[] BLACKLIST = {"Spirit Leap", "Teleport to Player"};

    public CancelEmptyTooltip() {
        setLabel("Cancel Empty Tooltip");
        category(Category.SKYBLOCK);
    }

    public static final String id = "cancel_empty_tooltip";
    @Override public String id() { return id; }

    @Override
    public void init() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
            if (!isEnabled()) return;

            String containerName = containerScreen.getTitle().getString();

            for (String blacklisted : BLACKLIST) {
                if (containerName.contains(blacklisted)) return;
            }
        });
    }
}
