package dev.slhj.slhjaddons.calc.lexer;

import dev.slhj.slhjaddons.calc.exceptions.LexerException;
import dev.slhj.slhjaddons.calc.lexer.Token.TokenType;

import java.math.BigDecimal;
import java.util.ArrayList;

import static dev.slhj.slhjaddons.calc.lexer.Token.*;
import static dev.slhj.slhjaddons.calc.lexer.Token.OperatorToken.isOperator;
import static dev.slhj.slhjaddons.calc.lexer.Token.ParenthesisToken.isParenthesis;
import static dev.slhj.slhjaddons.calc.lexer.Token.PrefixToken.isPrefix;
import static dev.slhj.slhjaddons.calc.lexer.Token.SuffixToken.isSuffix;

public class Lexer {
    public static TokenType[] tokenize(String input) {
        final int inputLength = input.length();
        if (input.trim().isEmpty()) return new NumberToken[]{new NumberToken(BigDecimal.ZERO)};
        int i = 0;

        final ArrayList<TokenType> tokens = new ArrayList<>(inputLength);

        while (i < inputLength) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                final int start = i;
                boolean dot = false;

                while (i < inputLength) {
                    c = input.charAt(i);

                    if (Character.isDigit(c)) {
                        i++;
                    } else if (c == '.') {
                        if (dot) throw new LexerException("Multiple dots in number");
                        dot = true;
                        i++;
                    } else break;
                }

                String number = input.substring(start, i);
                if (number.equals(".")) throw new LexerException("A single dot is not a valid number.");

                tokens.add(numberToken(number));
                continue;
            }

            if (isPrefixChar(c, tokens)) {
                tokens.add(prefixToken(c));
                i++;
                continue;
            }

            if (isSuffixChar(c, tokens) && (i + 1 >= inputLength || !Character.isLetterOrDigit(input.charAt(i + 1)))) {
                tokens.add(suffixToken(c));
                i++;
                continue;
            }

            if (isOperator(c)) {
                tokens.add(operatorToken(c));
                i++;
                continue;
            }

            if (isParenthesis(c)) {
                tokens.add(parenthesisToken(c));
                i++;
                continue;
            }

            if (Character.isLetter(c)) {
                final int start = i++;
                while (i < inputLength) {
                    c = input.charAt(i);
                    if (!Character.isLetterOrDigit(c) && c != '_') break;
                    i++;
                }
                tokens.add(identifierToken(input.substring(start, i)));
                continue;
            }

            if (c == ',') {
                tokens.add(CommaToken.COMMA_TOKEN);
                i++;
                continue;
            }

            if (c == ';') {
                tokens.add(SemiColonToken.SEMI_COLON_TOKEN);
                i++;
                continue;
            }

            throw new LexerException("Invalid char: " + c);
        }

        tokens.add(EndToken.END_TOKEN);
        return tokens.toArray(TokenType[]::new);
    }

    private static boolean isPrefixChar(char c, ArrayList<TokenType> tokenList) {
        if (!isPrefix(c)) return false;
        if (tokenList.isEmpty()) return true;

        TokenType last = tokenList.getLast();

        return switch (last) {
            case PrefixToken ignored -> true;
            case OperatorToken ignored -> true;
            case CommaToken ignored -> true;
            case ParenthesisToken p when p == ParenthesisToken.OPEN -> true;
            default -> false;
        };
    }

    private static boolean isSuffixChar(char c, ArrayList<TokenType> tokenList) {
        if (!isSuffix(c)) return false;
        if (tokenList.isEmpty()) return false;

        TokenType last = tokenList.getLast();

        return switch (last) {
            case NumberToken ignored -> true;
            case IdentifierToken ignored -> true;
            case ParenthesisToken p when p == ParenthesisToken.CLOSE -> true;
            default -> false;
        };
    }
}