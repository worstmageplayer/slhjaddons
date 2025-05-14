const functions = [
    { name: 'cube', params: ['x'], body: 'x^3' },
    { name: 'sqrt', params: ['x'], body: 'x^0.5' },
    { name: 'power', params: ['x', 'y'], body: 'x^y' },
];

const variables = {
    one: 1, two: 2, three: 3, four: 4, five: 5,
    six: 6, seven: 7, eight: 8, nine: 9, ten: 10,
    pi: Math.PI, e: Math.E,
};

const isFunction = name => functions.some(fn => fn.name === name);
const getFunction = name => functions.find(fn => fn.name === name);
const isVariable = name => Object.hasOwn(variables, name);
const getVariableValue = name => variables[name];

const tokenTypes = {
    number: ['0','1','2','3','4','5','6','7','8','9','.'],
    operator: ['+','-','*','/'],
    exponent: ['^'],
    parenthesis: ['(',')'],
    comma: [','],
    equals: ['='],
};

const nonIdentifierChars = [
    ...tokenTypes.number,
    ...tokenTypes.operator,
    ...tokenTypes.exponent,
    ...tokenTypes.parenthesis,
    ...tokenTypes.comma,
    ...tokenTypes.equals
];

class Token {
    constructor(type, value) {
        if (!type || value == null)
            throw new Error(`Token requires type and value, got: ${type}, ${value}`);
        this.type = type;
        this.value = value;
    }
}

/**
 * Tokenizes a mathematical expression string into an array of tokens.
 *
 * Recognized tokens include:
 * - Numbers (e.g., 42, 3.14)
 * - Operators (+, -, *, /, ^)
 * - Parentheses ((, ))
 * - Identifiers (e.g., variable or function names like x, sin)
 * - Commas (for function argument separation)
 *
 * Throws errors for:
 * - Invalid characters
 * - Malformed numbers (e.g., multiple decimal points or standalone '.')
 *
 * @function tokeninator9000
 * @param {string} input - The mathematical expression to tokenize.
 * @returns {Token[]} Array of token objects representing the parsed components of the input.
 * @throws {Error} If an invalid or malformed token is encountered.
 */
const tokeninator9000 = (str) => {
    str = str.replace(/\s+/g, '');
    const tokens = [];
    let i = 0;

    while (i < str.length) {
        let char = str[i];
        
        switch (true) {
            case tokenTypes.number.includes(char): {
                let num = '', dotCount = 0;
                while (i < str.length && tokenTypes.number.includes(str[i])) {
                    if (str[i] === '.' && ++dotCount > 1) {
                        throw new Error(`Multiple dots in number at ${i}`);
                    }
                    num += str[i++];
                }

                if (Number.isNaN(parseFloat(num))) {
                    throw new Error(`Invalid number '${num}'`);
                }

                tokens.push(new Token('number', num));
                break;
            }

            case tokenTypes.operator.includes(char):
                tokens.push(new Token('operator', char));
                i++;
                break;

            case tokenTypes.exponent.includes(char):
                tokens.push(new Token('exponent', char));
                i++;
                break;

            case tokenTypes.parenthesis.includes(char):
                tokens.push(new Token('parenthesis', char));
                i++;
                break;

            case tokenTypes.comma.includes(char):
                tokens.push(new Token('comma', char));
                i++;
                break;

            case tokenTypes.equals.includes(char):
                tokens.push(new Token('equals', char));
                i++;
                break;

            default:
                let id = '';
                while (i < str.length && !nonIdentifierChars.includes(str[i])) {
                    id += str[i++];
                }
                tokens.push(new Token('identifier', id));
                break;
        }
    }
    return tokens;
};

// AST Nodes
/**
 * Base class for all AST (Abstract Syntax Tree) nodes.
 */
class ASTNode {
    /**
     * @param {string} type - The type of the AST node.
     */
    constructor(type) {
        /** @type {string} */
        this.type = type;
    }
}

/**
 * Represents a numeric literal in the AST.
 */
class Literal extends ASTNode {
    /**
     * @param {number} value - The numeric value of the literal.
     */
    constructor(value) {
        super('Literal');
        /** @type {number} */
        this.value = value;
    }
}

/**
 * Represents a binary operation (e.g., +, -, *, /, ^) in the AST.
 */
class BinaryOperation extends ASTNode {
    /**
     * @param {ASTNode} left - The left-hand side of the operation.
     * @param {string} operator - The operator (e.g., '+', '-', '*', '/', '^').
     * @param {ASTNode} right - The right-hand side of the operation.
     */
    constructor(left, operator, right) {
        super('BinaryOperation');
        /** @type {ASTNode} */
        this.left = left;
        /** @type {string} */
        this.operator = operator;
        /** @type {ASTNode} */
        this.right = right;
    }
}

/**
 * Represents a unary operation (e.g., +, -) in the AST.
 */
class UnaryOperation extends ASTNode {
    /**
     * @param {string} operator - The unary operator ('+' or '-').
     * @param {ASTNode} argument - The operand the operator is applied to.
     */
    constructor(operator, argument) {
        super('UnaryOperation');
        /** @type {string} */
        this.operator = operator;
        /** @type {ASTNode} */
        this.argument = argument;
    }
}

/**
 * Represents a function call (e.g., f(x), g(a, b)) in the AST.
 */
class FunctionCall extends ASTNode {
    /**
     * @param {string} name - The name of the function being called.
     * @param {ASTNode[]} args - The arguments passed to the function.
     */
    constructor(name, args) {
        super('FunctionCall');
        /** @type {string} */
        this.name = name;
        /** @type {ASTNode[]} */
        this.args = args;
    }
}

/**
 * Represents a variable reference (e.g., x, y) in the AST.
 */
class Variable extends ASTNode {
    /**
     * @param {string} name - The name of the variable.
     */
    constructor(name) {
        super('Variable');
        /** @type {string} */
        this.name = name;
    }
}

/**
 * Parses a tokenized mathematical expression into an abstract syntax tree (AST).
 *
 * This function implements a recursive descent parser that supports:
 * - Binary operations: +, -, *, /, ^
 * - Unary operations: +, - (prefix only)
 * - Grouped expressions with parentheses
 * - Literals (numbers)
 * - Variables and function calls (with comma-separated arguments)
 *
 * The resulting AST consists of instances of:
 * - `Literal`
 * - `Variable`
 * - `UnaryOperation`
 * - `BinaryOperation`
 * - `FunctionCall`
 *
 * The function expects a well-formed array of tokens and will throw if any unexpected
 * token or malformed structure is encountered.
 *
 * @function parseinator
 * @param {Token[]} tokens - An array of tokens representing a mathematical expression.
 * @returns {ASTNode} The root node of the parsed abstract syntax tree.
 * @throws {Error} If the token stream is malformed or contains unexpected tokens.
 */
const parseinator = (tokens) => {
    let i = 0;

    const peek = () => tokens[i];
    const consume = () => {
        if (i >= tokens.length) throw new Error("Unexpected end of input");
        return tokens[i++];
    };

    const parseExpression = () => {
        let node = parseTerm();
        while (peek()?.type === 'operator' && ['+', '-'].includes(peek().value)) {
            let operator = consume().value;
            let right = parseTerm();
            node = new BinaryOperation(node, operator, right);
        }
        return node;
    };

    const parseTerm = () => {
        let node = parseUnary();
        while (peek()?.type === 'operator' && ['*', '/'].includes(peek().value)) {
            let operator = consume().value;
            let right = parseUnary();
            node = new BinaryOperation(node, operator, right);
        }
        return node;
    };

    const parseUnary = () => {
        const token = peek();
        if (token?.type === 'operator' && ['+', '-'].includes(token.value)) {
            const operator = consume().value;
            const argument = parseUnary();
            return new UnaryOperation(operator, argument);
        }
        return parseExponent();
    };

    const parseExponent = () => {
        let node = parsePrimary();
        if (peek()?.type === 'exponent') {
            const operator = consume().value;
            const right = parseUnary();
            node = new BinaryOperation(node, operator, right);
        }
        return node;
    };

    const parsePrimary = () => {
        const token = peek();
        if (!token) throw new Error("Unexpected end of input");

        if (token.type === 'number') {
            return new Literal(parseFloat(consume().value));
        }

        if (token.type === 'parenthesis' && token.value === '(') {
            consume(); // consume '('
            const expr = parseExpression();
            if (consume().value !== ')') throw new Error("Expected ')'");
            return expr;
        }

        if (token.type === 'identifier') {
            return parseVariableOrFunction();
        }

        throw new Error(`Unexpected token: ${token.type} (${token.value})`);
    };

    const parseVariableOrFunction = () => {
        const name = consume().value;
        const next = peek();

        if (isFunction(name) && next?.type === 'parenthesis' && next.value === '(') {
            consume(); // consume '('
            const args = [];

            while (peek() && peek().value !== ')') {
                args.push(parseExpression());
                if (peek()?.type === 'comma') consume();
            }

            if (consume().value !== ')') throw new Error("Expected ')'");
            return new FunctionCall(name, args);
        }

        if (isVariable(name)) {
            return new Variable(name);
        }

        throw new Error(`Unknown identifier: '${name}'`);
    };

    const result = parseExpression();
    if (i < tokens.length) {
        const leftover = tokens.slice(i).map(t => t.value).join('');
        throw new Error(`Unexpected token(s): '${leftover}'`);
    }

    return result;
};

/**
 * Recursively evaluates an AST (Abstract Syntax Tree) node and returns its numeric result.
 *
 * @param {ASTNode} node - The root of the AST to evaluate. This can be any subclass of ASTNode:
 * Literal, UnaryOperation, BinaryOperation, FunctionCall, or Variable.
 * 
 * @returns {number} The evaluated result of the expression represented by the AST.
 * 
 * @throws {Error} If division by zero occurs, an unknown operator or variable is encountered,
 * or a function call is invalid (e.g., not found or given wrong number of arguments).
 */
const evaluateinator = (node) => {
    if (node instanceof Literal) return node.value;

    if (node instanceof UnaryOperation) {
        const val = evaluateinator(node.argument);
        return node.operator === '-' ? -val : val;
    }

    if (node instanceof BinaryOperation) {
        const left = evaluateinator(node.left);
        const right = evaluateinator(node.right);
        switch (node.operator) {
            case '+': return left + right;
            case '-': return left - right;
            case '*': return left * right;
            case '/': if (right === 0) throw new Error('Division by zero'); return left / right;
            case '^': return Math.pow(left, right);
            default: throw new Error(`Unknown binary operator: ${node.operator}`);
        }
    }

    if (node instanceof FunctionCall) {
        const fn = getFunction(node.name);
        if (!fn) throw new Error(`Function '${node.name}' not found`);
        if (fn.params.length !== node.args.length)
            throw new Error(`Function '${fn.name}' expects ${fn.params.length} args`);

        const argValues = node.args.map(arg => evaluateinator(arg));

        let body = fn.body;
        fn.params.forEach((param, j) => {
            const re = new RegExp(`\\b${param}\\b`, 'g');
            body = body.replace(re, argValues[j]);
        });

        const tokens = tokeninator9000(body);
        const parsed = parseinator(tokens);
        return evaluateinator(parsed);
    }

    if (node instanceof Variable) {
        if (isVariable(node.name)) return getVariableValue(node.name);
        throw new Error(`Unknown variable: ${node.name}`);
    }

    throw new Error(`Unknown AST node type: ${node.type}`);
};

export const calculator = (input) => {
    const tokens = tokeninator9000(input);
    // console.log(JSON.stringify(tokens, null, 2))
    const tree = parseinator(tokens);
    // console.log(JSON.stringify(tree, null, 2))
    return evaluateinator(tree);
};

function parseInputFunction(tokens) {
    const eqIndex = tokens.indexOf('=');
    if (eqIndex === -1) throw new Error("Missing '=' in function definition");

    const header = tokens.slice(0, eqIndex);
    const body = tokens.slice(eqIndex + 1).join('');

    if (header.length < 4 || header[1] !== '(' || header[header.length - 1] !== ')') {
        throw new Error("Invalid function header format");
    }

    const name = header.slice(0, header.indexOf('(')).join('');

    const params = header.slice(2, -1).join('').split(',').map(p => p.trim()).filter(Boolean);

    return { name, params, body};
}

function defineFunction(name, params, body) {
    const validParamRegex = /^[a-zA-Z_]\w*$/;
    for (const param of params) {
        if (!validParamRegex.test(param)) {
            throw new Error(`Invalid parameter name: '${param}'`);
        }
    }

    const existingIndex = functions.findIndex(fn => fn.name === name);

    if (existingIndex !== -1) {
        functions[existingIndex] = { name, params, body };
    } else {
        functions.push({ name, params, body });
    }
}

register('Command', (...args) => {
    try {
        if (!args || args.join('').toLowerCase() === 'help') {
            ChatLib.chat(`Usage: /define name(params)=expression\nExample: /define f(x,y)=x^2+y^2\nDefines a custom function.`);
            return;
        }
        if (args.join('').toLowerCase() === 'show') {
            if (functions.length === 0) {
                ChatLib.chat("No functions defined.");
            } else {
                ChatLib.chat("Defined functions:");
                functions.forEach(fn => {
                    ChatLib.chat(`${fn.name}(${fn.params.join(', ')}) = ${fn.body}`);
                });
            }
            return;
        }
        const argsarray = [...args].join('').split('');
        const { name, params, body } = parseInputFunction(argsarray);
        defineFunction(name, params, body);
        // console.log('Defined Function:', JSON.stringify({ name, params, body }));
        ChatLib.chat(`Defined function ${name}(${params.join(', ')}) = ${body}`);
    } catch (e) {
        console.log(e);
        ChatLib.chat("Invalid Input");
    }
}).setName('define').setAliases('let');

register('Command', (...args) => {
    try {
        if (!args || args.join('').toLowerCase() === 'help') {
            ChatLib.chat(`Usage: /calc expression\nExample: /calc 2^3+4`);
            return;
        }
        let expression = args.join('');
        const result = calculator(expression);
        const text = new TextComponent(result);
        text.setHover('show_text', `${expression} = ${result}`);
        ChatLib.chat(text);
    } catch (e) {
        console.log(e);
        ChatLib.chat("Invalid Input");
    }
}).setName('calc');