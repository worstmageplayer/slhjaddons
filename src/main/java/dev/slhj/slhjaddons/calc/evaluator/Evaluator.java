package dev.slhj.slhjaddons.calc.evaluator;

import dev.slhj.slhjaddons.calc.exceptions.EvaluatorException;
import dev.slhj.slhjaddons.calc.identifier.Functions;
import dev.slhj.slhjaddons.calc.identifier.Variables;
import dev.slhj.slhjaddons.calc.lexer.Token;
import dev.slhj.slhjaddons.calc.parser.Node.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;

import static dev.slhj.slhjaddons.calc.identifier.Functions.getFunction;

public class Evaluator {
    private static final ArrayDeque<HashMap<String, BigDecimal>> variablesStack = new ArrayDeque<>();

    public static BigDecimal evaluate(NodeType node) {
        return switch (node) {
            case NumberNode n -> n.value();
            case BinaryOperationNode b -> {
                BigDecimal left = evaluate(b.left());
                BigDecimal right = evaluate(b.right());
                yield switch (b.operator()) {
                    case ADD -> left.add(right);
                    case SUB -> left.subtract(right);
                    case MUL -> left.multiply(right);
                    case DIV -> {
                        if (right.compareTo(BigDecimal.ZERO) == 0) throw new EvaluatorException("Division by zero.");
                        else yield left.divide(right, 20, RoundingMode.HALF_UP);
                    }
                    case MOD -> {
                        if (right.compareTo(BigDecimal.ZERO) == 0) throw new EvaluatorException("Division by zero.");
                        else yield left.remainder(right);
                    }
                    case POW -> {
                        if (right.scale() <= 0) yield left.pow(right.intValue());
                        else yield BigDecimal.valueOf(Math.pow(left.doubleValue(), right.doubleValue()));
                    }
                };
            }
            case PrefixOperationNode p -> {
                BigDecimal value = evaluate(p.value());
                yield switch (p.prefix()) {
                    case Token.PrefixToken.PLUS -> value;
                    case Token.PrefixToken.MINUS -> value.negate();
                };
            }
            case SuffixOperationNode s -> {
                BigDecimal value = evaluate(s.value());
                yield switch (s.suffix()) {
                    case THOUSAND -> value.scaleByPowerOfTen(3);
                    case MILLION -> value.scaleByPowerOfTen(6);
                    case BILLION -> value.scaleByPowerOfTen(9);
                    case TRILLION -> value.scaleByPowerOfTen(12);
                    case FACTORIAL -> factorial(value);
                };
            }
            case VariableNode v -> resolveVariable(v.variable().identifier());
            case FunctionNode f -> {
                Functions.Function func = getFunction(f.function().identifier());
                List<String> params = func.params();
                final int paramsSize = params.size();

                if (f.args().size() != paramsSize) {
                    throw new EvaluatorException("Function '" + f.function().identifier() + "' expects " + paramsSize + " args, got " + f.args().size());
                }

                HashMap<String, BigDecimal> scope = new HashMap<>(paramsSize);
                for (int i = 0; i < paramsSize; i++) {
                    scope.put(params.get(i), evaluate(f.args().get(i)));
                }

                variablesStack.push(scope);
                BigDecimal result =  evaluate(func.body());
                variablesStack.pop();
                yield result;
            }
        };
    }

    private static BigDecimal resolveVariable(String name) {
        HashMap<String, BigDecimal> scope = variablesStack.peek();
        if (scope != null && scope.containsKey(name)) return scope.get(name);

        if (Variables.includes(name)) return Variables.get(name);

        throw new EvaluatorException("Variable not found: " + name);
    }

    private static BigDecimal factorial(BigDecimal value) {
        if (value.signum() < 0 || value.stripTrailingZeros().scale() > 0) {
            throw new ArithmeticException("Factorial only works for positive integers rn");
        }

        if (value.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            throw new EvaluatorException("Factorial too large");
        }

        BigDecimal result = BigDecimal.ONE;
        final int valueExact = value.intValueExact();
        for (int i = 1; i <= valueExact; i++) {
            result = result.multiply(BigDecimal.valueOf(i));
        }
        return result;
    }
}