package dev.slhj.slhjaddons.calc.parser;

import dev.slhj.slhjaddons.calc.exceptions.ParserException;
import dev.slhj.slhjaddons.calc.lexer.Token.CommaToken;
import dev.slhj.slhjaddons.calc.lexer.Token.EndToken;
import dev.slhj.slhjaddons.calc.lexer.Token.IdentifierToken;
import dev.slhj.slhjaddons.calc.lexer.Token.NumberToken;
import dev.slhj.slhjaddons.calc.lexer.Token.OperatorToken;
import dev.slhj.slhjaddons.calc.lexer.Token.ParenthesisToken;
import dev.slhj.slhjaddons.calc.lexer.Token.PrefixToken;
import dev.slhj.slhjaddons.calc.lexer.Token.SemicolonToken;
import dev.slhj.slhjaddons.calc.lexer.Token.SuffixToken;
import dev.slhj.slhjaddons.calc.lexer.Token.TokenType;
import dev.slhj.slhjaddons.calc.parser.Node.BinaryOperationNode;
import dev.slhj.slhjaddons.calc.parser.Node.FunctionNode;
import dev.slhj.slhjaddons.calc.parser.Node.NodeType;
import dev.slhj.slhjaddons.calc.parser.Node.NumberNode;
import dev.slhj.slhjaddons.calc.parser.Node.PrefixOperationNode;
import dev.slhj.slhjaddons.calc.parser.Node.SuffixOperationNode;
import dev.slhj.slhjaddons.calc.parser.Node.VariableNode;

import java.math.BigDecimal;
import java.util.ArrayList;

import static dev.slhj.slhjaddons.calc.lexer.Token.OperatorToken.getBindingPower;

public class Parser {
    private final TokenType[] tokens;
    private final int tokenCount;
    private int position = 0;

    private Parser(TokenType[] tokens) {
        this.tokens = tokens;
        this.tokenCount = tokens.length;
    }

    public static NodeType parse(TokenType[] tokens) {
        return new Parser(tokens).parseExpression(0);
    }

    private NodeType parsePrefix(PrefixToken prefix) {
        return new PrefixOperationNode(parseExpression(9), prefix);
    }

    private NodeType parseIdentifier(IdentifierToken identifier) {
        if (tokens[position] != ParenthesisToken.OPEN) return new VariableNode(identifier);
        position++;
        ArrayList<NodeType> arguments = new ArrayList<>();
        while (tokens[position] != ParenthesisToken.CLOSE) {
            arguments.add(parseExpression(0));
            if (tokens[position] != ParenthesisToken.CLOSE && tokens[position++] != CommaToken.COMMA_TOKEN) {
                throw new ParserException("Expected comma between arguments");
            }
        }
        position++;
        return new FunctionNode(identifier, arguments);
    }

    private NodeType parseExpression(int minimumBindingPower) {
        TokenType token = tokens[position++];
        NodeType left;

        switch (token) {
            case NumberToken num -> left = new NumberNode(num.value());
            case IdentifierToken identifier -> left = parseIdentifier(identifier);
            case PrefixToken prefix -> left = parsePrefix(prefix);
            case ParenthesisToken p when p == ParenthesisToken.OPEN -> {
                left = parseExpression(0);
                if (tokens[position] != ParenthesisToken.CLOSE) {
                    throw new ParserException("Expected closing parenthesis");
                }
                position++;
            }
            default -> throw new ParserException("Unexpected token: " + token);
        }

        loop:
        while (position < tokenCount) {
            int[] bindingPower;
            OperatorToken implicitMultiply = OperatorToken.MUL;
            TokenType next = tokens[position];

            switch (next) {
                case EndToken ignored -> { break loop; }
                case CommaToken ignored -> { break loop; }
                case SemicolonToken ignored -> { break loop; }
                case ParenthesisToken parenthesis -> {
                    if (parenthesis == ParenthesisToken.CLOSE) break loop;

                    bindingPower = getBindingPower(implicitMultiply);
                    if (bindingPower[0] < minimumBindingPower) break loop;

                    position++;
                    NodeType right = parseExpression(0);
                    if (tokens[position] != ParenthesisToken.CLOSE)
                        throw new ParserException("Expected closing parenthesis");
                    position++;

                    left = new BinaryOperationNode(left, right, implicitMultiply);
                }
                case SuffixToken suffix -> {
                    position++;
                    left = new SuffixOperationNode(left, suffix);
                }
                case IdentifierToken identifier -> {
                    position++;
                    NodeType right = parseIdentifier(identifier);
                    bindingPower = getBindingPower(implicitMultiply);
                    if (bindingPower[0] < minimumBindingPower) break loop;
                    left = new BinaryOperationNode(left, right, implicitMultiply);
                }
                case OperatorToken operator -> {
                    bindingPower = getBindingPower(operator);
                    if (bindingPower[0] < minimumBindingPower) break loop;
                    position++;
                    left = new BinaryOperationNode(left, parseExpression(bindingPower[1]), operator);
                }
                case NumberToken(BigDecimal value) -> {
                    bindingPower = getBindingPower(implicitMultiply);
                    if (bindingPower[0] < minimumBindingPower) break loop;
                    position++;
                    NodeType right = new NumberNode(value);
                    left = new BinaryOperationNode(left, right, implicitMultiply);
                }
                case PrefixToken ignored -> throw new ParserException("This shouldn't be here");
            }
        }
        return left;
    }
}
