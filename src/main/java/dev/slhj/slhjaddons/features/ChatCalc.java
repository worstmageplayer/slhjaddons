package dev.slhj.slhjaddons.features;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.slhj.slhjaddons.calc.Calculator;
import dev.slhj.slhjaddons.calc.identifier.Functions;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.util.client.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatCalc extends Feature {

    private static final Pattern DEFINE_PATTERN =
            Pattern.compile("^([a-zA-Z_]\\w*)\\(([^()=]*)\\)=(.+)$");

    public static final String id = "chat_calc";
    @Override public String id() { return id; }

    @Override
    public void init() {
        setEnabled(true);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(ClientCommands.literal("calc")
                    .executes(ctx -> { help("calc"); return 1; })
                    .then(ClientCommands.argument("expression", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                runCalc(StringArgumentType.getString(ctx, "expression"));
                                return 1;
                            })));

            dispatcher.register(ClientCommands.literal("define")
                    .executes(ctx -> { help("define"); return 1; })
                    .then(ClientCommands.argument("definition", StringArgumentType.greedyString())
                            .executes(ctx -> {
                                runDefine(StringArgumentType.getString(ctx, "definition"));
                                return 1;
                            })));
        });
    }

    private void help(String which) {
        if (which.equals("calc")) {
            ChatUtils.chat("&7Usage: /calc <expression>  e.g. /calc 2^3+4");
        } else {
            ChatUtils.chat("&7Usage: /define name(params)=expression  e.g. /define f(x,y)=x^2+y^2");
            ChatUtils.chat("&7/define show - lists defined functions");
        }
    }

    private void runCalc(String expression) {
        if (expression == null || expression.isBlank()) { help("calc"); return; }
        try {
            var result = Calculator.calc(expression);
            ChatUtils.chat("&7" + expression + " &f= &a" + result.commas());
        } catch (RuntimeException e) {
            ChatUtils.chat("&cInvalid input: " + e.getMessage());
        }
    }

    private void runDefine(String definition) {
        if (definition == null || definition.isBlank() || definition.equalsIgnoreCase("help")) {
            help("define");
            return;
        }
        if (definition.equalsIgnoreCase("show")) {
            var funcs = Functions.getFunctions();
            if (funcs.isEmpty()) { ChatUtils.chat("&7No functions defined."); return; }
            ChatUtils.chat("&7Defined functions:");
            for (var f : funcs) {
                ChatUtils.chat(" &f- params=" + f.params() + " body=" + f.body());
            }
            return;
        }

        Matcher m = DEFINE_PATTERN.matcher(definition.replace(" ", ""));
        if (!m.matches()) {
            ChatUtils.chat("&cInvalid function format. /define help for help.");
            return;
        }

        String name = m.group(1);
        List<String> params = m.group(2).isEmpty() ? List.of() : List.of(m.group(2).split(","));
        String body = m.group(3);

        try {
            if (Functions.includes(name)) {
                Functions.set(name, params, body);
            } else {
                Functions.add(name, params, body);
            }
            ChatUtils.chat("&aDefined function " + name + "(" + String.join(", ", params) + ") = " + body);
        } catch (RuntimeException e) {
            ChatUtils.chat("&cInvalid input: " + e.getMessage());
        }
    }
}