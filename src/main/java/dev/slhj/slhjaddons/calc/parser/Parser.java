package dev.slhj.slhjaddons.calc.parser;

import dev.slhj.slhjaddons.calc.exceptions.ParserException;
import dev.slhj.slhjaddons.calc.lexer.Token.*;
import dev.slhj.slhjaddons.calc.parser.Node.*;

import java.math.BigDecimal;
import java.util.ArrayList;

import static dev.slhj.slhjaddons.calc.lexer.Token.OperatorToken.getBindingPower;

public class Parser {
    private final TokenType[] tokens;
    private final int tokensLength;
    private int pos = 0;

    private Parser(TokenType[] tokens) {
        this.tokens = tokens;
        this.tokensLength = tokens.length;
    }

    public static NodeType parse(TokenType[] tokens) {
        return new Parser(tokens).parseExpression(0);
    }

    private NodeType prefixNodeSoThatMyIDECanShutUP(PrefixToken prefix) {
        return new PrefixOperationNode(parseExpression(9), prefix);
    }

    private NodeType parseExpression(int minBp) {
        TokenType token = tokens[pos++];
        NodeType lhs;

        switch (token) {
            case NumberToken num -> lhs = new NumberNode(num.value());
            case IdentifierToken id -> {
                if (tokens[pos] == ParenthesisToken.OPEN) {
                    pos++; // Consume '('
                    ArrayList<NodeType> args = new ArrayList<>();

                    while (tokens[pos] != ParenthesisToken.CLOSE) {
                        args.add(parseExpression(0));
                        if (tokens[pos] != ParenthesisToken.CLOSE && tokens[pos++] != CommaToken.COMMA_TOKEN) {
                            throw new ParserException("Expected comma between arguments");
                        }
                    }

                    pos++; // Consume '('
                    lhs = new FunctionNode(id, args);
                } else {
                    lhs = new VariableNode(id);
                }
            }
            case PrefixToken prefix -> lhs = prefixNodeSoThatMyIDECanShutUP(prefix);
            case ParenthesisToken p when p == ParenthesisToken.OPEN -> {
                lhs = parseExpression(0);
                if (tokens[pos] != ParenthesisToken.CLOSE) {
                    throw new ParserException("Expected closing parenthesis");
                }
                pos++;
            }
            default -> throw new ParserException("Unexpected token: " + token);
        }

        loop:
        while (pos < tokensLength) {
            int[] bp;
            OperatorToken implicitMul = OperatorToken.MUL;
            TokenType next = tokens[pos];

            switch (next) {
                case EndToken ignored -> { break loop; }
                case CommaToken ignored -> { break loop; }
                case SemiColonToken ignored -> { break loop; }
                case ParenthesisToken parenthesis -> {
                    if (parenthesis == ParenthesisToken.CLOSE) break loop;

                    bp = getBindingPower(implicitMul);
                    if (bp[0] < minBp) break loop;

                    pos++; // Consume'('
                    NodeType rhs = parseExpression(0);
                    if (tokens[pos] != ParenthesisToken.CLOSE)
                        throw new ParserException("Expected closing parenthesis");
                    pos++; // Consume ')'

                    lhs = new BinaryOperationNode(lhs, rhs, implicitMul);
                }
                case SuffixToken suffix -> {
                    pos++; // Consume 'suffix'
                    lhs = new SuffixOperationNode(lhs, suffix);
                }
                case IdentifierToken id -> {
                    NodeType rhs;
                    pos++; // Consume 'variable'
                    if (tokens[pos] == ParenthesisToken.OPEN) {
                        pos++; // Consume '('
                        ArrayList<NodeType> args = new ArrayList<>();

                        while (tokens[pos] != ParenthesisToken.CLOSE) {
                            args.add(parseExpression(0));
                            if (tokens[pos] != ParenthesisToken.CLOSE && tokens[pos++] != CommaToken.COMMA_TOKEN) {
                                throw new ParserException("Expected comma between arguments");
                            }
                        }

                        pos++; // Consume ')'
                        rhs = new FunctionNode(id, args);
                    } else {
                        rhs = new VariableNode(id);
                    }

                    bp = getBindingPower(implicitMul);
                    if (bp[0] < minBp) break loop;
                    lhs = new BinaryOperationNode(lhs, rhs, implicitMul);
                }
                case OperatorToken operator -> {
                    bp = getBindingPower(operator);
                    if (bp[0] < minBp) break loop;
                    pos++; // Consume 'operator'
                    lhs = new BinaryOperationNode(lhs, parseExpression(bp[1]), operator);
                }
                case NumberToken(BigDecimal value) -> {
                    bp = getBindingPower(implicitMul);
                    if (bp[0] < minBp) break loop;
                    pos++; // Consume 'number'
                    NodeType rhs = new NumberNode(value);
                    lhs = new BinaryOperationNode(lhs, rhs, implicitMul);
                }
                case PrefixToken ignored -> throw new ParserException("This shouldn't be here");
            }
        }
        return lhs;
    }
}