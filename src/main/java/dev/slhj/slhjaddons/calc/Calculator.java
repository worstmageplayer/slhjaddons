package dev.slhj.slhjaddons.calc;

import dev.slhj.slhjaddons.calc.exceptions.CalculatorException;
import dev.slhj.slhjaddons.calc.identifier.Variables;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static dev.slhj.slhjaddons.calc.evaluator.Evaluator.evaluate;
import static dev.slhj.slhjaddons.calc.lexer.Lexer.tokenize;
import static dev.slhj.slhjaddons.calc.parser.Parser.parse;

public class Calculator {
    public static class CalcResult {
        private final BigDecimal value;

        private CalcResult(BigDecimal value) {
            this.value = value;
        }

        public String format() {
            return value.stripTrailingZeros().scale() <= 0
                    ? value.toBigInteger().toString()
                    : value.stripTrailingZeros().toPlainString();
        }

        public String commas() {
            String formatted = format();
            boolean negative = formatted.startsWith("-");
            if (negative) formatted = formatted.substring(1);

            int dotIndex = formatted.indexOf('.');
            String intPart = (dotIndex >= 0) ? formatted.substring(0, dotIndex) : formatted;
            String decPart = (dotIndex >= 0) ? formatted.substring(dotIndex) : "";

            StringBuilder result = new StringBuilder();

            for (int i = intPart.length() - 1, count = 0; i >= 0; i--, count++) {
                if (count > 0 && count % 3 == 0) {
                    result.append(',');
                }
                result.append(intPart.charAt(i));
            }

            result.reverse();

            if (negative) result.insert(0, '-');
            result.append(decPart);

            return result.toString();
        }

        public BigDecimal raw() {
            return value;
        }

        public String round(int places) {
            if (places < 0) throw new CalculatorException("Decimal places must be non-negative");
            return value.setScale(places, RoundingMode.HALF_UP).toPlainString();
        }

        public String scientific() {
            return value.stripTrailingZeros().toEngineeringString();
        }

        @Override
        public String toString() {
            return format();
        }
    }

    public static CalcResult calc(String input) {
        CalcResult result = new CalcResult(evaluate(parse(tokenize(input))));
        Variables.set("ans", result.raw());
        return result;
    }
}