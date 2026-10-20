package dev.slhj.slhjaddons.util.skyblock;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

final class MageCooldownState {
    private static final Pattern TAB_ENTRY = Pattern.compile(
            "^\\[\\d+]\\s+(?:\\[[^]]+]\\s+)?(?<name>[A-Za-z0-9_]{1,16})"
                    + "(?:\\s+[^()]*)?\\s+\\((?:(?<class>Archer|Berserk|Healer|Mage|Tank)"
                    + "\\s+(?<level>[IVXLCDM]+|\\d+)|(?<dead>DEAD))\\)\\s*$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ROMAN_LEVEL = Pattern.compile("L|(?:XL|X{0,3})(?:IX|IV|V?I{0,3})");
    private static final Pattern UNIQUE_CLASS = Pattern.compile(
            "^Your (?<class>Mage|Archer|Berserk|Healer|Tank) stats are doubled"
                    + " because you are the only player using this class!$");
    private static final Map<Character, Integer> ROMAN = Map.of('I', 1, 'V', 5, 'X', 10, 'L', 50);

    private record Member(@Nullable String dungeonClass, @Nullable Integer level) {}
    private final Map<String, Member> members = new HashMap<>();
    @Nullable private String uniqueClass;

    void updateTab(List<String> lines) {
        for (String line : lines) {
            var matcher = TAB_ENTRY.matcher(line.strip());
            if (!matcher.matches()) continue;
            String name = matcher.group("name").toLowerCase(Locale.ROOT);
            if (matcher.group("dead") != null) {
                members.putIfAbsent(name, new Member(null, null));
            } else {
                members.put(name, new Member(matcher.group("class"), decodeLevel(matcher.group("level"))));
            }
        }
    }

    void onChat(String message) {
        var matcher = UNIQUE_CLASS.matcher(message.strip());
        if (matcher.matches()) uniqueClass = matcher.group("class");
    }

    double multiplier(String playerName) {
        String name = playerName.toLowerCase(Locale.ROOT);
        Member player = members.get(name);
        if (player == null || !"Mage".equalsIgnoreCase(player.dungeonClass()) || player.level() == null)
            return 1.0;
        if (uniqueClass != null && !uniqueClass.equals("Mage")) return 1.0;

        boolean otherMage = members.entrySet().stream().anyMatch(entry -> !entry.getKey().equals(name)
                && "Mage".equalsIgnoreCase(entry.getValue().dungeonClass()));
        boolean unknownClass = members.values().stream().anyMatch(member -> member.dungeonClass() == null);
        boolean soloMage = "Mage".equals(uniqueClass) || (!otherMage && !unknownClass);

        int reduction = (soloMage ? 50 : 25) + player.level() / 2;
        return (100 - reduction) / 100.0;
    }

    void reset() {
        members.clear();
        uniqueClass = null;
    }

    @Nullable private static Integer decodeLevel(String text) {
        if (text.matches("\\d{1,2}")) {
            int level = Integer.parseInt(text);
            return level <= 50 ? level : null;
        }
        String numeral = text.toUpperCase(Locale.ROOT);
        if (numeral.isEmpty() || !ROMAN_LEVEL.matcher(numeral).matches()) return null;
        int total = 0, previous = 0;
        for (int i = numeral.length() - 1; i >= 0; i--) {
            int value = ROMAN.get(numeral.charAt(i));
            total += value < previous ? -value : value;
            previous = value;
        }
        return total;
    }
}
