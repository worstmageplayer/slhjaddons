const suffixes = { k: 1e3, m: 1e6, b: 1e9, t: 1e12 };
const knownConstants = { 
    pi: Math.PI, 
    e: Math.E 
};

const userFunctions = {};

/**
 * Converts a math expression string into an array of token objects.
 * Tokens include numbers, operators, parentheses, identifiers, and commas.
 * 
 * @param {string} input - The raw math expression as a string.
 * @returns {Array<Object>} An array of token objects, each with `type` and `value`.
 * 
 * Token types include:
 * - 'number' for numeric values (e.g. 3.14)
 * - 'identifier' for variables or function names (e.g. sin, x)
 * - 'operator' for math symbols (+, -, *, /, ^)
 * - 'paren' for open or close parentheses ('(', ')')
 * - 'comma' for separating function arguments
 */
const tokenize = (string) => {
    const result = [];
    let i = 0;

    while (i < string.length) {
        let char = string[i];

        switch (true) {
            case char >= '0' && char <= '9': {
                let num = '';
                while (i < string.length && ((string[i] >= '0' && string[i] <= '9') || string[i] === '.')) {
                    num += string[i++];
                }
                let suffix = '';
                if (i < string.length && Object.keys(suffixes).includes(string[i])) {
                    suffix = string[i++];
                }
                result.push({ number: parseFloat(num), suffix });

                if (i < string.length && string[i] === '(') {
                    result.push('*');
                }
                break;
            }

            case ['+', '-', '*', '/', '(', ')', '^', ','].includes(char):
                result.push(char);
                i++;
                break;

            default: {
                let str = '';
                while (
                    i < string.length &&
                    !['+', '-', '*', '/', '(', ')', '^', ',', '.', ...Object.keys(suffixes)].includes(string[i]) &&
                    !(string[i] >= '0' && string[i] <= '9')
                ) {
                    str += string[i++];
                }
                result.push(str);
                break;
            }
        }
    }

    return result;
};

/**
 * Converts a list of tokens into an abstract syntax tree (AST).
 * The AST represents the structure of the mathematical expression.
 * 
 * @param {Array<Object>} tokens - Array of token objects produced by `tokenize()`.
 * @returns {Object} The root node of the generated AST.
 * 
 * AST node types include:
 * - 'NumberLiteral': { type, value }
 * - 'Identifier': { type, name }
 * - 'BinaryExpression': { type, operator, left, right }
 * - 'CallExpression': { type, name, args }
 * - 'UnaryExpression': { type, operator, argument }
 */
const parse = (tokens) => {
    let i = 0;

    const peek = () => tokens[i];
    const peekNext = () => tokens[i + 1];
    const consume = () => tokens[i++];

    const parsePrimary = () => {
        const unaryStack = [];
        while (peek() === '+' || peek() === '-') {
            unaryStack.push(consume());
        }

        const token = peek();

        if (typeof token === 'string' && !['+', '-', '*', '/', '(', ')', '^', ','].includes(token)) {
            const name = consume();

            if (peek() === '(') {
                consume(); // consume '('
                const args = [];
                while (peek() !== ')') {
                    args.push(parseExpression());
                    if (peek() === ',') consume();
                }
                consume(); // consume ')'
                const node = { type: 'FunctionCall', name, arguments: args };
                return applyUnaryStack(node, unaryStack);
            }

            if (knownConstants.hasOwnProperty(name)) {
                const node = { type: 'Literal', value: knownConstants[name] };
                return applyUnaryStack(node, unaryStack);
            }

            throw new Error("Unknown identifier: " + name);
        }

        if (token === '(') {
            consume();
            const expr = parseExpression();
            if (consume() !== ')') throw new Error("Expected ')'");
            return applyUnaryStack(expr, unaryStack);
        }

        if (typeof token === 'object' && token.number !== undefined) {
            const { number, suffix } = consume();
            const multiplier = suffix ? suffixes[suffix] : 1;
            const node = { type: 'Literal', value: number * multiplier };
            return applyUnaryStack(node, unaryStack);
        }

        throw new Error("Unexpected token: " + token);
    };

    const applyUnaryStack = (node, stack) => {
        for (let i = stack.length - 1; i >= 0; i--) {
            node = {
                type: 'UnaryExpression',
                operator: stack[i],
                argument: node
            };
        }
        return node;
    };

    const parseExponent = () => {
        let left = parsePrimary();
        if (peek() === '^') {
            consume();
            const right = parseExponent();
            left = { type: 'BinaryExpression', operator: '^', left, right };
        }
        return left;
    };

    const parseTerm = () => {
        let left = parseExponent();
        while (peek() === '*' || peek() === '/') {
            let op = consume();
            let right = parseExponent();
            left = { type: 'BinaryExpression', operator: op, left, right };
        }
        return left;
    };

    const parseExpression = () => {
        let left = parseTerm();
        while (peek() === '+' || peek() === '-') {
            let op = consume();
            let right = parseTerm();
            left = { type: 'BinaryExpression', operator: op, left, right };
        }
        return left;
    };

    const ast = parseExpression();
    if (i < tokens.length) throw new Error("Unexpected token at end: " + peek());
    return ast;
};

/**
 * Recursively evaluates an abstract syntax tree (AST) to compute the final result.
 * Supports built-in math functions, constants, variables, and user-defined functions.
 * 
 * @param {Object} node - The root of the AST generated by `parse()`.
 * @param {Object} [context={}] - Optional evaluation context containing variable values or user-defined functions.
 * @returns {number|string} The result of evaluating the AST, or an error message.
 * 
 * The context object may contain:
 * - Named constants (e.g. { pi: Math.PI })
 * - Variables for custom function calls (e.g. { x: 5 })
 * - Functions (e.g. { sin: Math.sin })
 */
const evaluate = (ast, context = {}) => {
    switch (ast.type) {
        case 'Literal':
            return ast.value;

        case 'BinaryExpression':
            const leftValue = evaluate(ast.left, context);
            const rightValue = evaluate(ast.right, context);
            switch (ast.operator) {
                case '+':
                    return leftValue + rightValue;
                case '-':
                    return leftValue - rightValue;
                case '*':
                    return leftValue * rightValue;
                case '/':
                    if (rightValue === 0) throw new Error("Division by zero");
                    return leftValue / rightValue;
                case '^':
                    return Math.pow(leftValue, rightValue);
                default:
                    throw new Error(`Unknown operator: ${ast.operator}`);
            }

        case 'UnaryExpression':
            const val = evaluate(ast.argument, context);
            switch (ast.operator) {
                case '+': return +val;
                case '-': return -val;
                default: throw new Error(`Unknown unary operator: ${ast.operator}`);
            }

        case 'FunctionCall': {
            const args = ast.arguments.map(arg => evaluate(arg, context));

            if (userFunctions.hasOwnProperty(ast.name)) {
                const func = userFunctions[ast.name];

                if (args.length !== func.params.length) {
                    throw new Error(`Function '${ast.name}' expects ${func.params.length} arguments, got ${args.length}`);
                }

                let replaced = func.body;
                for (let i = 0; i < func.params.length; i++) {
                    const param = func.params[i];
                    const value = args[i];
                    replaced = replaced.replace(new RegExp(`\\b${param}\\b`, 'g'), value);
                }

                const tokens = tokenize(replaced);
                const subAst = parse(tokens);
                return evaluate(subAst, context);
            }

            if (typeof Math[ast.name] === 'function') {
                return Math[ast.name](...args);
            }

            throw new Error(`Unknown function: ${ast.name}`);
        }

        default:
            throw new Error(`Unknown AST node type: ${ast.type}`);
    }
};

/**
 * Defines a custom user function with a name, parameters, and a body expression.
 *
 * @param {string} name - The name of the function (e.g., "f" for f(x)).
 * @param {string[]} params - An array of parameter names (e.g., ["x", "y"]).
 * @param {string} body - A string representing the function's body expression, which can use parameters and other functions.
 *
 * @example
 * defineFunction("square", ["x"], "x * x");
 * defineFunction("add", ["a", "b"], "a + b");
 */
const defineFunction = (name, params, body) => {
    userFunctions[name] = { params, body };
}

const parseInputFunction = (tokens) => {
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

export const calculator = (input) => {
    if (typeof input !== "string") return null;
    // console.log(input)
    const tokens = tokenize(input);
    // console.log("Tokens:", JSON.stringify(tokens, null, 2));
    const ast = parse(tokens);
    // console.log("AST:", JSON.stringify(ast, null, 2));
    const result = evaluate(ast);
    // console.log("Result:", result);
    return result
}

register('Command', (...args) => {
    try {
        if (!args || args.join('').toLowerCase() === 'help') {
            ChatLib.chat(`Usage: /define name(params)=expression\nExample: /define f(x,y)=x^2+y^2\nDefines a custom function.`);
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