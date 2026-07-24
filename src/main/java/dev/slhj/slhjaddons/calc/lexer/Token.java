package dev.slhj.slhjaddons.calc.lexer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Token {
    public sealed interface TokenType
            permits NumberToken, OperatorToken, PrefixToken, SuffixToken,
            ParenthesisToken, IdentifierToken, CommaToken, SemiColonToken, EndToken {
        String toString();
    }

    public record NumberToken(BigDecimal value) implements TokenType {
        @Override
        public String toString() {
            return value.toString() + " NumberToken";
        }
    }

    public record IdentifierToken(String identifier) implements TokenType {
        @Override
        public String toString() {
            return identifier + " IdentifierToken";
        }
    }

    public enum OperatorToken implements TokenType {
        ADD('+', 2, 3),
        SUB('-', 2, 3),
        MUL('*', 4, 5),
        DIV('/', 4, 5),
        MOD('%', 4, 5),
        POW('^', 7, 6);

        private final char operatorSymbol;
        private final int leftBindingPower;
        private final int rightBindingPower;

        OperatorToken(char symbol, int left, int right) {
            this.operatorSymbol = symbol;
            this.leftBindingPower = left;
            this.rightBindingPower = right;
        }

        public char getOperatorSymbol() {
            return operatorSymbol;
        }

        public static int[] getBindingPower(OperatorToken op) {
            return new int[]{op.leftBindingPower, op.rightBindingPower};
        }

        private static final Map<Character, OperatorToken> MAP = new HashMap<>();
        static {
            for (OperatorToken op : values()) {
                MAP.put(op.getOperatorSymbol(), op);
            }
        }

        public static OperatorToken fromSymbol(char symbol) {
            return MAP.get(symbol);
        }

        public static boolean isOperator(char c) {
            return MAP.containsKey(c);
        }

        @Override
        public String toString() {
            return operatorSymbol + " OperatorToken";
        }
    }

    public enum PrefixToken implements TokenType {
        PLUS('+'),
        MINUS('-');

        private final char prefixSymbol;

        PrefixToken(char symbol) {
            this.prefixSymbol = symbol;
        }

        public char getPrefixSymbol() {
            return prefixSymbol;
        }

        private static final Map<Character, PrefixToken> MAP = new HashMap<>();
        static {
            for (PrefixToken p : values()) {
                MAP.put(p.getPrefixSymbol(), p);
            }
        }

        public static PrefixToken fromSymbol(char symbol) {
            return MAP.get(symbol);
        }

        public static boolean isPrefix(char c) {
            return MAP.containsKey(c);
        }

        @Override
        public String toString() {
            return prefixSymbol + " PrefixToken";
        }
    }

    public enum SuffixToken implements TokenType {
        THOUSAND('k'),
        MILLION('m'),
        BILLION('b'),
        TRILLION('t'),
        FACTORIAL('!');

        private final char suffixSymbol;

        SuffixToken(char symbol) {
            this.suffixSymbol = symbol;
        }

        public char getSuffixSymbol() {
            return suffixSymbol;
        }

        private static final Map<Character, SuffixToken> MAP = new HashMap<>();
        static {
            for (SuffixToken s : values()) {
                MAP.put(s.getSuffixSymbol(), s);
            }
        }

        public static SuffixToken fromSymbol(char symbol) {
            return MAP.get(symbol);
        }

        public static boolean isSuffix(char c) {
            return MAP.containsKey(c);
        }

        @Override
        public String toString() {
            return suffixSymbol + " SuffixToken";
        }
    }

    public enum ParenthesisToken implements TokenType {
        OPEN('('),
        CLOSE(')');

        private final char parenthesisSymbol;

        ParenthesisToken(char symbol) {
            this.parenthesisSymbol = symbol;
        }

        public char getParenthesisSymbol() {
            return parenthesisSymbol;
        }

        private static final Map<Character, ParenthesisToken> MAP = new HashMap<>();
        static {
            for (ParenthesisToken p : values()) {
                MAP.put(p.getParenthesisSymbol(), p);
            }
        }

        public static ParenthesisToken fromSymbol(char symbol) {
            return MAP.get(symbol);
        }

        public static boolean isParenthesis(char c) {
            return MAP.containsKey(c);
        }

        @Override
        public String toString() {
            return parenthesisSymbol + " ParenthesisToken";
        }
    }

    public enum CommaToken implements TokenType {
        COMMA_TOKEN;

        @Override
        public String toString() {
            return ", CommaToken";
        }
    }

    public enum SemiColonToken implements TokenType {
        SEMI_COLON_TOKEN;

        @Override
        public String toString() {
            return "; SemiColonToken";
        }
    }

    public enum EndToken implements TokenType {
        END_TOKEN;

        @Override
        public String toString() {
            return "END";
        }
    }

    private static final int NUMBER_POOL_MAX_CACHE = 5000;
    private static final int IDENTIFIER_POOL_MAX_CACHE = 50;

    private final static Map<BigDecimal, NumberToken> numberPool = new LinkedHashMap<>(NUMBER_POOL_MAX_CACHE + 1, 1, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<BigDecimal, NumberToken> eldest) {
            return size() > NUMBER_POOL_MAX_CACHE;
        }
    };

    private final static Map<String, IdentifierToken> identifierPool = new LinkedHashMap<>(IDENTIFIER_POOL_MAX_CACHE  + 1, 1, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, IdentifierToken> eldest) {
            return size() > IDENTIFIER_POOL_MAX_CACHE ;
        }
    };

    public static NumberToken numberToken(String value) {
        return numberPool.computeIfAbsent(new BigDecimal(value).stripTrailingZeros(), NumberToken::new);
    }

    public static IdentifierToken identifierToken(String id) {
        return identifierPool.computeIfAbsent(id.intern(), IdentifierToken::new);
    }

    public static OperatorToken operatorToken(char c) {
        return OperatorToken.fromSymbol(c);
    }

    public static PrefixToken prefixToken(char c) {
        return PrefixToken.fromSymbol(c);
    }

    public static SuffixToken suffixToken(char c) {
        return SuffixToken.fromSymbol(c);
    }

    public static ParenthesisToken parenthesisToken(char c) {
        return ParenthesisToken.fromSymbol(c);
    }
}