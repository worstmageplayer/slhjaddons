package dev.slhj.slhjaddons.features.dungeons;

import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.hud.*;
import dev.slhj.slhjaddons.util.client.ContainerScreenPosition;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class PartyFinderHelper extends Feature {

    public static final String id = "party_finder_helper";

    private static final String PARTY_FINDER_TITLE = "Party Finder";
    private static final int PARTY_SIZE = 5;
    private static final int HIGHLIGHT_COLOR = 0x7000FF00;

    private static final Pattern MEMBER = Pattern.compile("^\\s*\\w{1,16}: (Archer|Berserk|Mage|Healer|Tank) \\(.*?\\d+.*?\\)\\s*$");
    private static final Pattern INELIGIBLE = Pattern.compile("^(Requires .*|You don't meet the requirement!|Complete previous floor first!)$");
    private static final Pattern CLASS_MENU_ITEM = Pattern.compile("^View and select a dungeon class\\.$");
    private static final Pattern CURRENTLY_SELECTED = Pattern.compile("^Currently Selected: (Archer|Berserk|Mage|Healer|Tank)$");
    private static final Pattern SELECTED_CHAT = Pattern.compile("^You have selected the (Archer|Berserk|Mage|Healer|Tank) Dungeon Class!$");

    private enum DungeonClass {
        ARCHER("Archer", ChatFormatting.GOLD),
        BERSERK("Berserk", ChatFormatting.DARK_RED),
        MAGE("Mage", ChatFormatting.AQUA),
        HEALER("Healer", ChatFormatting.LIGHT_PURPLE),
        TANK("Tank", ChatFormatting.DARK_GREEN);

        final String displayName;
        final ChatFormatting color;

        DungeonClass(String displayName, ChatFormatting color) {
            this.displayName = displayName;
            this.color = color;
        }

        String colored() { return color + displayName + ChatFormatting.WHITE; }

        static DungeonClass fromName(String name) {
            for (DungeonClass c : values()) if (c.displayName.equals(name)) return c;
            return null;
        }
    }

    private DungeonClass selectedClass;
    private AbstractContainerScreen<?> activeScreen;
    private final List<Slot> highlighted = new ArrayList<>();
    private final EnumMap<DungeonClass, Integer> wantedCounts = new EnumMap<>(DungeonClass.class);
    private int openParties;
    private HudElement overlay;

    public PartyFinderHelper() {
        setLabel("Party Finder Helper");
        setDescription("Highlight parties missing your class and show the most and least wanted classes.");
        category(Category.DUNGEONS);
    }

    @Override public String id() { return id; }

    @Override
    public void init() {
        overlay = new HudElement(this, id, label(),
                new HudPlacement(HudAnchor.TOP_LEFT, HudAnchor.TOP_LEFT, 10, 10, 1),
                () -> isPartyFinderOpen() ? HudContent.text(buildOverlayText(), 0xFFFFFFFF) : null,
                () -> HudContent.text("Your Class: " + DungeonClass.MAGE.colored()
                        + "\nMost Wanted: " + DungeonClass.MAGE.colored()
                        + "\nLeast Wanted: " + DungeonClass.ARCHER.colored() + ", " + DungeonClass.BERSERK.colored(), 0xFFFFFFFF),
                HudElement.RenderLayer.SCREEN);
        SlhjAddons.hud().register(overlay);

        ClientReceiveMessageEvents.GAME.register((message, actionBar) -> {
            if (actionBar) return;
            Matcher m = SELECTED_CHAT.matcher(clean(message));
            if (m.matches()) selectedClass = DungeonClass.fromName(m.group(1));
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> container)) return;
            ScreenEvents.afterTick(screen).register(s -> onTick(container));

            if (!PARTY_FINDER_TITLE.equals(screen.getTitle().getString())) return;
            activeScreen = container;

            ScreenEvents.afterForeground(screen).register((s, graphics, mx, my, delta) -> {
                if (!isPartyFinderOpen() || activeScreen != s) return;
                ContainerScreenPosition position = (ContainerScreenPosition) container;
                int left = position.slhj$getLeftPos();
                int top = position.slhj$getTopPos();
                for (Slot slot : highlighted) {
                    int x = left + slot.x;
                    int y = top + slot.y;
                    graphics.fill(x, y, x + 16, y + 16, HIGHLIGHT_COLOR);
                }
                var frame = overlay.prepare(false);
                if (frame != null) frame.draw(graphics);
            });

            ScreenEvents.remove(screen).register(s -> {
                if (activeScreen == s) {
                    activeScreen = null;
                    clearResults();
                }
            });
        });
    }

    @Override protected void onDisable() { clearResults(); }

    private void onTick(AbstractContainerScreen<?> screen) {
        if (!isEnabled() || ClientUtils.mc().gui.screen() != screen) return;
        detectSelectedClass(screen);
        if (screen == activeScreen) analyzePartyFinder(screen);
    }

    private void detectSelectedClass(AbstractContainerScreen<?> screen) {
        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof Inventory) continue;
            List<String> lore = cleanLore(slot.getItem());
            if (lore.isEmpty() || !CLASS_MENU_ITEM.matcher(lore.getFirst()).matches()) continue;
            for (String line : lore) {
                Matcher m = CURRENTLY_SELECTED.matcher(line);
                if (m.matches()) {
                    selectedClass = DungeonClass.fromName(m.group(1));
                    return;
                }
            }
        }
    }

    private void analyzePartyFinder(AbstractContainerScreen<?> screen) {
        clearResults();

        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof Inventory) continue;
            ItemStack item = slot.getItem();
            if (item.isEmpty() || !clean(item.getHoverName()).endsWith("'s Party")) continue;

            EnumSet<DungeonClass> present = EnumSet.noneOf(DungeonClass.class);
            int members = 0;
            boolean ineligible = false;

            for (String line : cleanLore(item)) {
                Matcher m = MEMBER.matcher(line);
                if (m.matches()) {
                    members++;
                    present.add(DungeonClass.fromName(m.group(1)));
                } else if (INELIGIBLE.matcher(line).matches()) {
                    ineligible = true;
                }
            }

            if (members == 0 || members >= PARTY_SIZE) continue;

            openParties++;
            EnumSet<DungeonClass> missing = EnumSet.complementOf(present);
            for (DungeonClass c : missing) wantedCounts.merge(c, 1, Integer::sum);

            if (!ineligible && selectedClass != null && missing.contains(selectedClass)) highlighted.add(slot);
        }
    }

    private String buildOverlayText() {
        String yourClass = "Your Class: " + (selectedClass != null ? selectedClass.colored() : "Unknown (open Catacombs Gate)");
        if (openParties == 0) return yourClass + "\nNo open parties";

        int max = Collections.max(wantedCounts.values());
        int min = Collections.min(wantedCounts.values());
        String most = max > 0 ? classesWithCount(max) : "None";
        String least = min < max ? classesWithCount(min) : "None";
        return yourClass + "\nMost Wanted: " + most + "\nLeast Wanted: " + least;
    }

    private String classesWithCount(int count) {
        return wantedCounts.entrySet().stream()
                .filter(e -> e.getValue() == count)
                .map(e -> e.getKey().colored())
                .collect(Collectors.joining(", "));
    }

    private boolean isPartyFinderOpen() {
        return isEnabled() && activeScreen != null && ClientUtils.mc().gui.screen() == activeScreen;
    }

    private void clearResults() {
        highlighted.clear();
        wantedCounts.clear();
        for (DungeonClass c : DungeonClass.values()) wantedCounts.put(c, 0);
        openParties = 0;
    }

    private static String clean(Component component) {
        String stripped = ChatFormatting.stripFormatting(component.getString());
        return stripped == null ? "" : stripped.trim();
    }

    private static List<String> cleanLore(ItemStack item) {
        ItemLore lore = item.get(DataComponents.LORE);
        if (lore == null) return List.of();
        List<String> lines = new ArrayList<>(lore.lines().size());
        for (Component line : lore.lines()) lines.add(clean(line));
        return lines;
    }
}