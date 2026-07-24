package dev.slhj.slhjaddons.calc.parser;

import dev.slhj.slhjaddons.calc.lexer.Token.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class Node {
    public sealed interface NodeType
            permits NumberNode, BinaryOperationNode, PrefixOperationNode,
            SuffixOperationNode, VariableNode, FunctionNode {
        String toString();
    }

    public record NumberNode(BigDecimal value) implements NodeType {
        @Override
        public String toString() {
            return value.toString();
        }
    }

    public record BinaryOperationNode(NodeType left, NodeType right, OperatorToken operator) implements NodeType {
        @Override
        public String toString() {
            return "(" + operator.toString() + " " + left.toString() + " " + right.toString() + ")";
        }
    }

    public record PrefixOperationNode(NodeType value, PrefixToken prefix) implements NodeType {
        @Override
        public String toString() {
            return "(" + prefix.toString() + " " + value.toString() + ")";
        }
    }

    public record SuffixOperationNode(NodeType value, SuffixToken suffix) implements NodeType {
        @Override
        public String toString() {
            return "(" + suffix.toString() + " " + value.toString() + ")";
        }
    }

    public record VariableNode(IdentifierToken variable) implements NodeType {
        @Override
        public String toString() {
            return variable.identifier();
        }
    }

    public record FunctionNode(IdentifierToken function, List<NodeType> args) implements NodeType {
        @Override
        public String toString() {
            return "(" + function.identifier() + "(" + args.stream().map(NodeType::toString).collect(Collectors.joining(", ")) + "))";
        }
    }
}