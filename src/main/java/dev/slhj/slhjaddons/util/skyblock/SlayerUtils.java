package dev.slhj.slhjaddons.util.skyblock;

import com.ibm.icu.impl.Pair;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SlayerUtils {
    public enum Slayer {
        ZOMBIE("Revenant Horror"),
        SPIDER("Tarantula Broodfather"),
        BLAZE("Inferno Demon"),
        ENDERMAN("Voidgloom Seraph"),
        VAMPIRE("Bloodfiend");

        private final String bossName;

        Slayer(String bossName) {
            this.bossName = bossName;
        }

        public String bossName() {
            return bossName;
        }
    }

    public static boolean isSlayerActive(Slayer slayer) {
        List<String> lines = ScoreboardUtils.lines();
        boolean boss = false, slay = false;
        for (String l : lines) {
            if (l.contains(slayer.bossName())) boss = true;
            if (l.contains("Slay the boss!")) slay = true;
        }
        return boss && slay;
    }

    public enum BlazeAttunements {
        ASHEN("ASHEN ♨", Set.of("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"), 0),
        AURIC("AURIC ♨", Set.of("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"), 1),
        SPIRIT("SPIRIT ♨", Set.of("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"), 2),
        CRYSTAL("CRYSTAL ♨", Set.of("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"), 3);

        private final String displayName;
        private final Set<String> daggers;
        private final int attunementNumber;

        BlazeAttunements(String displayName, Set<String> daggers, int attunementNumber) {
            this.displayName = displayName;
            this.daggers = daggers;
            this.attunementNumber = attunementNumber;
        }

        public String displayName() { return displayName; }
        public Set<String> daggers() { return daggers; }
        public int attunementNumber() { return attunementNumber; }

        public boolean isDagger(ItemStack stack) {
            String id = SkyblockItemUtils.skyblockId(stack);
            return id != null && daggers.contains(id);
        }

        @Nullable
        public static BlazeAttunements fromAttunementNumber(int number) {
            for (BlazeAttunements a : values()) {
                if (a.attunementNumber == number) return a;
            }
            return null;
        }

        @Nullable
        public static BlazeAttunements fromStack(ItemStack stack) {
            for (BlazeAttunements a : values()) {
                if (a.isDagger(stack)) return a;
            }
            return null;
        }

        @Nullable
        public static BlazeAttunements fromArmorStandName(String name) {
            for (BlazeAttunements a : values()) {
                if (name.contains(a.displayName)) return a;
            }
            return null;
        }
    }
}
