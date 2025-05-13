const suffixes = { k: 1e3, m: 1e6, b: 1e9, t: 1e12 };
const knownConstants = { 
    pi: Math.PI, 
    e: Math.E 
};

const userFunctions = {};

const tokenize = (string) => {
    const maxLength = string.length;
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

                // Optional suffix like 'k', 'm'
                if (i < string.length && Object.keys(suffixes).includes(string[i])) {
                    num += string[i++];
                }

                result.push({ type: 'number', value: parseFloat(num) });

                // Implicit multiplication
                if (i < string.length && string[i] === '(') {
                    result.push({ type: 'operator', value: '*' });
                }
                break;
            }

            case char === ')': {
                result.push({ type: 'paren', value: ')' });
                i++;
                break;
            }

            case char === '(': {
                result.push({ type: 'paren', value: '(' });
                i++;
                break;
            }

            case ['+', '-', '*', '/', '^'].includes(char): {
                result.push({ type: 'operator', value: char });
                i++;
                break;
            }

            case char === ',': {
                result.push({ type: 'comma', value: ',' });
                i++;
                break;
            }

            default: {
                let str = '';
                while (
                    i < string.length &&
                    !['+', '-', '*', '/', '(', ')', '^', ',', '.', ...Object.keys(suffixes)].includes(string[i]) &&
                    !(string[i] >= '0' && string[i] <= '9')
                ) {
                    str += string[i++];
                }
                result.push({ type: 'identifier', value: str });
                break;
            }
        }
    }

    return result;
};

const parse = (tokens) => {
    let i = 0;

    const peek = () => tokens[i];
    const peekNext = () => tokens[i + 1];
    const consume = () => tokens[i++];

    function parsePrimary() {
        let token = consume();

        if (!token) throw new Error("Unexpected end of input");

        // Handle numbers (e.g., 2, 3.14)
        if (token.type === "number") {
            return { type: "NumberLiteral", value: token.value };
        }

        // Handle function calls like sin(2)
        if (token.type === "identifier") {
            let next = peek();
            if (next && next.type === "paren" && next.value === "(") {
                consume(); // consume '('
                let args = [];

                // Parse arguments inside the function
                if (peek() && !(peek().type === "paren" && peek().value === ")")) {
                    do {
                        args.push(parseExpression());
                    } while (peek() && peek().type === "comma" && consume());
                }

                if (!peek() || peek().type !== "paren" || peek().value !== ")") {
                    throw new Error("Expected ')' after function arguments");
                }
                consume(); // consume ')'

                return { type: "FunctionCall", name: token.value, args };
            }

            return { type: "Variable", name: token.value };
        }

        // Handle parentheses for grouping
        if (token.type === "paren" && token.value === "(") {
            let expr = parseExpression();
            if (!peek() || peek().type !== "paren" || peek().value !== ")") {
                throw new Error("Expected ')'");
            }
            consume(); // consume ')'
            return expr;
        }

        throw new Error("Unexpected token: " + token.value);
    }

    const parseExponent = () => {
        let left = parsePrimary();
        while (peek() && peek().type === "operator" && peek().value === "^") {
            consume(); // consume '^'
            const right = parseExponent();
            left = { type: "BinaryExpression", operator: "^", left, right };
        }
        return left;
    };

    const parseTerm = () => {
        let left = parseExponent();
        while (peek() && peek().type === "operator" && (peek().value === "*" || peek().value === "/")) {
            let op = consume().value; // consume operator and get its value
            let right = parseExponent();
            left = { type: "BinaryExpression", operator: op, left, right };
        }
        return left;
    };

    const parseExpression = () => {
        let left = parseTerm();
        while (peek() && peek().type === "operator" && (peek().value === "+" || peek().value === "-")) {
            let op = consume().value; // consume operator and get its value
            let right = parseTerm();
            left = { type: "BinaryExpression", operator: op, left, right };
        }
        return left;
    };

    const ast = parseExpression();
    if (i < tokens.length) throw new Error("Unexpected token at end: " + peek().value);
    return ast;
};

const evaluate = (ast, context = {}) => {
    switch (ast.type) {
        case 'NumberLiteral':  // Updated for parsed AST
            return ast.value;

        case 'BinaryExpression':  // Binary operation
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

        case 'UnaryExpression':  // Unary operation
            const val = evaluate(ast.argument, context);
            switch (ast.operator) {
                case '+': return +val;
                case '-': return -val;
                default: throw new Error(`Unknown unary operator: ${ast.operator}`);
            }

        case 'FunctionCall':  // Function call handling
            const args = ast.args.map(arg => evaluate(arg, context));  // Adjust for `args` in `FunctionCall`

            // User-defined function evaluation
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

            // Built-in Math functions (e.g., Math.sin, Math.cos)
            if (typeof Math[ast.name] === 'function') {
                return Math[ast.name](...args);
            }

            throw new Error(`Unknown function: ${ast.name}`);

        case 'Variable':  // Variable lookup
            if (context.hasOwnProperty(ast.name)) {
                return context[ast.name];
            }
            throw new Error(`Undefined variable: ${ast.name}`);

        default:
            throw new Error(`Unknown AST node type: ${ast.type}`);
    }
};

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
     console.log("Input: ", input)
    const tokens = tokenize(input);
     console.log("Tokens:", JSON.stringify(tokens, null, 2));
    const ast = parse(tokens);
     console.log("AST:", JSON.stringify(ast, null, 2));
    const result = evaluate(ast);
     console.log("Result:", result);
     console.log("\n");
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