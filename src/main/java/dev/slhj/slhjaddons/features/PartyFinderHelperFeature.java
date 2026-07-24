package dev.slhj.slhjaddons.features;

import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.McUtils;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PartyFinderHelperFeature extends Feature {

    private static final Pattern CLASS_LINE = Pattern.compile(": ?\\(?([ABMHT])\\)?");
    private static final List<String> REQUIRED = List.of("A", "B", "M", "H", "T");

    private Map<String, Integer> classCounts = null;
    private String mostWanted = "None";
    private String leastWanted = "None";
    private boolean inPartyFinder = false;

    public PartyFinderHelperFeature() {
        setLabel("Public Finder Helper");
        category(Category.DUNGEONS);
    }

    @Override public String id() { return "party_finder_helper"; }

    @Override
    public void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
            if (!"Party Finder".equals(containerScreen.getTitle().getString())) return;

            inPartyFinder = true;
            analyzePartyFinder(containerScreen);

            ScreenMouseEvents.afterMouseClick(screen).register((s, mouseX, mouseY) -> {
                McUtils.scheduleTask(() -> analyzePartyFinder(containerScreen), 150);
                return false;
            });

            ScreenEvents.remove(screen).register(s -> {
                inPartyFinder = false;
                classCounts = null;
            });
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "party_finder_helper"),
                (graphics, delta) -> render(graphics));
    }

    private void analyzePartyFinder(AbstractContainerScreen<?> screen) {
        if (!isEnabled()) return;

        classCounts = new HashMap<>();
        for (String cls : REQUIRED) classCounts.put(cls, 0);

        for (Slot slot : screen.getMenu().slots) {
            ItemStack item = slot.getItem();
            if (item.isEmpty()) continue;

            String name = item.getHoverName().getString();
            if (!name.endsWith("'s Party")) continue;

            for (String cls : getMissingClasses(item)) {
                classCounts.merge(cls, 1, Integer::sum);
            }
        }

        int max = classCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int min = classCounts.values().stream().mapToInt(Integer::intValue).min().orElse(0);

        StringBuilder mostB = new StringBuilder();
        StringBuilder leastB = new StringBuilder();
        for (String cls : REQUIRED) {
            int count = classCounts.getOrDefault(cls, 0);
            if (count == max && max > 0) {
                if (!mostB.isEmpty()) mostB.append(", ");
                mostB.append(cls);
            }
            if (count == min) {
                if (!leastB.isEmpty()) leastB.append(", ");
                leastB.append(cls);
            }
        }
        mostWanted = !mostB.isEmpty() ? mostB.toString() : "None";
        leastWanted = !leastB.isEmpty() ? leastB.toString() : "None";
    }

    private List<String> getMissingClasses(ItemStack item) {
        ItemLore lore = item.get(DataComponents.LORE);
        if (lore == null) return List.of();

        List<Component> lines = lore.lines();
        Set<String> found = new HashSet<>();
        for (int i = 4; i < lines.size() - 1; i++) {
            String line = ChatFormatting.stripFormatting(lines.get(i).getString());
            Matcher m = CLASS_LINE.matcher(line);
            if (m.find()) found.add(m.group(1));
        }

        List<String> missing = new ArrayList<>();
        for (String cls : REQUIRED) if (!found.contains(cls)) missing.add(cls);
        return missing;
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || !inPartyFinder || classCounts == null) return;
        String text = String.format("Most Wanted: %s\nLeast Wanted: %s", mostWanted, leastWanted);
        RenderUtils.text(g, text, 10, 10, 0xFFFFFFFF, true);
    }
}