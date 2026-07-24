package dev.slhj.slhjaddons.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class KeyBindingUtils {
    private KeyBindingUtils() {}

    private static KeyMapping.Category mainCategory;

    public static KeyMapping.Category getMainCategory() {
        if (mainCategory == null) {
            mainCategory = KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath("slhjaddons", "main"));
        }
        return mainCategory;
    }
}
