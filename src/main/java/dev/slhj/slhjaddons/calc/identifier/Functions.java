package dev.slhj.slhjaddons.calc.identifier;

import dev.slhj.slhjaddons.calc.parser.Node.NodeType;

import java.util.*;

import static dev.slhj.slhjaddons.calc.lexer.Lexer.tokenize;
import static dev.slhj.slhjaddons.calc.parser.Parser.parse;

public class Functions {
    public record Function(List<String> params, NodeType body) {
    }

    private static final Map<String, Function> functionMap = new HashMap<>();

    static {
        add("cube", List.of("x"), "x^3");
        add("sqrt", List.of("x"), "x^0.5");
        add("power", List.of("x", "y"), "x^y");
        add("sum", List.of("x", "y"), "x+y");
        add("test", List.of("x", "y", "z"), "x^2-3*y+4z+0.5");
    }

    private static List<String> parseParams(String paramString) {
        List<String> result = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < paramString.length(); i++) {
            char c = paramString.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            } else if (c == ',') {
                result.add(buffer.toString());
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
        }

        if (!buffer.isEmpty()) {
            result.add(buffer.toString());
        }

        return result;
    }

    public static void add(String name, String paramsString, String body) {
        List<String> params = parseParams(paramsString);
        add(name, params, body);
    }

    public static void add(String name, List<String> params, String body) {
        if (includes(name)) {
            throw new RuntimeException(String.format("Function %s already defined", name));
        }
        NodeType bodyTree = parse(tokenize(body));
        functionMap.put(name, new Function(params, bodyTree));
    }

    public static void set(String name, List<String> params, String body) {
        if (!includes(name)) {
            throw new RuntimeException(String.format("Function %s has not been defined", name));
        }
        NodeType bodyTree = parse(tokenize(body));
        functionMap.replace(name, new Function(params, bodyTree));
    }

    public static boolean includes(String name) {
        return functionMap.containsKey(name);
    }

    public static Function getFunction(String name) {
        Function f = functionMap.get(name);
        if (f == null) throw new RuntimeException("Function not found: " + name);
        return f;
    }

    public static Collection<Function> getFunctions() {
        return functionMap.values();
    }
}