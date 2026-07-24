package dev.slhj.slhjaddons.calc.identifier;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Variables {
    private static final Map<String, BigDecimal> variableMap = new HashMap<>();
    static {
        add("pi", "3.14159265358979323846264338327950288419716939937510");
        add("phi", "1.61803398874989484820458683436563811772030917980576");
        add("e", "2.71828182845904523536028747135266249775724709369995");
        add("g", "9.81");
        add("one", "1");
        add("two", "2");
        add("three", "3");
        add("four", "4");
        add("five", "5");
        add("six", "6");
        add("seven", "7");
        add("eight", "8");
        add("nine", "9");
        add("ten", "10");
        add("hundred", "100");
        add("thousand", "1000");
        add("million", "1000000");
        add("billion", "1000000000");
        add("trillion", "1000000000000");
        add("ans", "0");
    }

    public static void add(String name, String value) {
        if (includes(name)) {
            throw new RuntimeException(String.format("Variable %s already defined", name));
        }
        variableMap.put(name, new BigDecimal(value));
    }

    public static void add(String name, BigDecimal value) {
        if (includes(name)) {
            throw new RuntimeException(String.format("Variable %s already defined", name));
        }
        variableMap.put(name, value);
    }

    public static void set(String name, String value) {
        if (!includes(name)) {
            throw new RuntimeException(String.format("Variable %s has not been defined", name));
        }
        variableMap.replace(name, new BigDecimal(value));
    }

    public static void set(String name, BigDecimal value) {
        if (!includes(name)) {
            throw new RuntimeException(String.format("Variable %s has not been defined", name));
        }
        variableMap.replace(name, value);
    }

    public static boolean includes(String name) {
        return variableMap.containsKey(name);
    }

    public static BigDecimal get(String name) {
        return variableMap.get(name);
    }

    public static Map<String, BigDecimal> getVariableMap() {
        return variableMap;
    }
}