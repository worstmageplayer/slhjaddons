package dev.slhj.slhjaddons.util;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlayerStatsUtils {
    private PlayerStatsUtils() {}

    private static final Pattern HEALTH = Pattern.compile("([\\d,]+)/([\\d,]+)\u2764");
    private static final Pattern MANA = Pattern.compile("([\\d,]+)/([\\d,]+)\u270e");
    private static final Pattern OVERFLOW = Pattern.compile("([\\d,]+)\u02ac");
    private static final Pattern DEFENSE = Pattern.compile("([\\d,]+)\u2748 Defense");

    private static volatile Integer currentHealth, maxHealth, currentMana, maxMana, currentDefense, overflowMana, effectiveHP;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) return;
            String text = ChatFormatting.stripFormatting(message.getString());

            Matcher h = HEALTH.matcher(text);
            if (h.find()) {
                currentHealth = parse(h.group(1));
                maxHealth = parse(h.group(2));
            }
            Matcher m = MANA.matcher(text);
            if (m.find()) {
                currentMana = parse(m.group(1));
                maxMana = parse(m.group(2));
            }
            Matcher o = OVERFLOW.matcher(text);
            overflowMana = o.find() ? parse(o.group(1)) : 0;

            Matcher d = DEFENSE.matcher(text);
            if (d.find()) currentDefense = parse(d.group(1));

            if (currentHealth != null && currentDefense != null) {
                effectiveHP = (int) (currentHealth * (1 + currentDefense / 100.0));
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> reset());
    }

    private static int parse(String s) {
        try { return Integer.parseInt(s.replace(",", "")); }
        catch (Exception e) { return 0; }
    }

    private static void reset() {
        currentHealth = maxHealth = currentMana = maxMana = currentDefense = overflowMana = effectiveHP = null;
    }

    public static Integer currentHealth() { return currentHealth; }
    public static Integer maxHealth() { return maxHealth; }
    public static Integer currentMana() { return currentMana; }
    public static Integer maxMana() { return maxMana; }
    public static Integer currentDefense() { return currentDefense; }
    public static Integer overflowMana() { return overflowMana; }
    public static Integer effectiveHP() { return effectiveHP; }
}