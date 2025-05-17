const functions = [
    { name: 'cube', params: ['x'], body: 'x^3' },
    { name: 'sqrt', params: ['x'], body: 'x^0.5' },
    { name: 'power', params: ['x', 'y'], body: 'x^y' },
    { name: 'f', params: ['x', 'y'], body: 'x^2-y+3' },
];

const variables = {
    one: 1, two: 2, three: 3, four: 4, five: 5,
    six: 6, seven: 7, eight: 8, nine: 9, ten: 10,
    pi: Math.PI, e: Math.E,
};

const suffixes = new Map([
    ['k', 1_000],
    ['m', 1_000_000],
    ['b', 1_000_000_000],
    ['t', 1_000_000_000_000],
]);

const isFunction = name => functions.some(fn => fn.name === name);
const getFunction = name => functions.find(fn => fn.name === name);
const isVariable = name => Object.hasOwn(variables, name);
const getVariableValue = name => variables[name];

const isNumber = char => (char >= '0' && char <= '9') || char === '.';
const isAlpha = char => (char >= 'a' && char <= 'z') || (char >= 'A' && char <= 'Z') || char === '_';
const isSuffix = char => char === 'k' || char === 'm' || char === 'b' || char === 't';
const isSymbol = (char) => '+-*/^=,()'.includes(char);
const isOperator = char => '+-*/'.includes(char);
const isParenthesis = char => char === '(' || char === ')';

/**
 * @param {String} str
 * @returns {{ type: string, value: string }[]}
 */
export const tokeninator9000 = (str) => {
    str = str.replace(/\s+/g, '');
    const tokens = [];
    let i = 0;

    while (i < str.length) {
        let char = str[i];

        if (isNumber(char)) {
            let num = '';
            let dotCount = 0;

            while (i < str.length && isNumber(str[i])) {
                if (str[i] === '.') {
                    if (++dotCount > 1) throw new Error(`Multiple dots in number at ${i}`);
                }
                num += str[i++];
            }

            if (num === '.') throw new Error(`Invalid number`);
            tokens.push({ type: 'number', value: num });

            if (i < str.length && isSuffix(str[i])) {
                tokens.push({ type: 'suffix', value: str[i++] });
            }

            continue;
        }

        if (isSymbol(char)) {
            tokens.push({
                type: isOperator(char) ? 'operator' :
                      isParenthesis(char) ? 'parenthesis' :
                      char === '^' ? 'exponent' :
                      char === ',' ? 'comma' : 'equals',
                value: char
            });
            i++;

            const nextIsSuffix = i < str.length && isSuffix(str[i]);
            const lastToken = tokens[tokens.length - 1];
            const suffixAllowed = lastToken && (
                lastToken.type === 'number' ||
                (lastToken.type === 'parenthesis' && lastToken.value === ')')
            );

            if (nextIsSuffix && suffixAllowed) {
                tokens.push({ type: 'suffix', value: str[i++] });
            }

            continue;
        }

        if (isAlpha(char)) {
            let identifier = '';
            while (i < str.length && isAlpha(str[i])) {
                identifier += str[i++];
            }
            tokens.push({ type: 'identifier', value: identifier });
            continue;
        }

        throw new Error(`Unknown character: '${char}' at position ${i}`);
    }

    return tokens;
};

const ADD_SUB = new Set(['+', '-']);
const MUL_DIV = new Set(['*', '/']);
const UNARY_OPERATORS = new Set(['+', '-']);

/**
 * @param {{ type: string, value: string }[]} tokens
 * @returns {ASTNode}
 */
export const parseinator = (tokens) => {
    const tokenLength = tokens.length
    let i = 0;
    /**
     * @param {() => ASTNode} parser 
     * @param {Set<string>} operators
     * @returns {BinaryOperationNode}
     */
    const parseBinary = (parser, operators) => {
        let node = parser();
        while (i < tokenLength) {
            let token = tokens[i];
            if (token.type !== 'operator' || !operators.has(token.value)) break;
            let operator = tokens[i++].value;
            let right = parser();
            node = { type: 'BinaryOperation', left: node, operator, right };
        }
        return node;
    };

    // Unary > MUL_DIV > ADD_SUB
    /** @returns {ASTNode} */
    const parseExpression = () => parseBinary(parseTerm, ADD_SUB);

    /** @returns {ASTNode} */
    const parseTerm = () => parseBinary(parseUnary, MUL_DIV);

    /** @returns {ASTNode} */
    const parseUnary = () => {
        const token = tokens[i];
        if (token?.type === 'operator' && UNARY_OPERATORS.has(token.value)) {
            const operator = tokens[i++].value;
            if (i >= tokenLength) throw new Error(`Expected argument after unary operator '${operator}'`);
            const argument = parseUnary();
            /** @type {UnaryOperationNode} */
            return { type: 'UnaryOperation', operator, argument };
        }
        return parseExponent();
    };

    /** @returns {ASTNode} */
    const parseExponent = () => {
        let node = parseSuffix(parsePrimary());
        const token = tokens[i];
        if (token?.type === 'exponent') {
            const operator = tokens[i++].value;
            const right = parseUnary();
            /** @type {BinaryOperationNode} */
            node = { type: 'BinaryOperation', left: node, operator, right };
        }
        return node;
    };

    /** @param {ASTNode} node */
    const parseSuffix = (node) => {
        while (i < tokenLength && tokens[i]?.type === 'suffix') {
            let suffix = tokens[i++].value;
            /** @type {SuffixNode} */
            node = { type: 'SuffixOperation', value: node, suffix };
        }
        return node;
    };

    /** @returns {LiteralNode | VariableNode | FunctionCallNode | ASTNode} */
    const parsePrimary = () => {
        if (i >= tokenLength) throw new Error("Unexpected end of input");
        
        const token = tokens[i];

        switch (token.type) {
            case 'number':
                /** @type {LiteralNode} */
                return { type: 'Literal', value: +tokens[i++].value };

            case 'parenthesis':
                if (token.value === '(') {
                    i++;
                    const expr = parseExpression();
                    if (tokens[i++]?.value !== ')') throw new Error("Expected ')'");
                    return expr;
                }
                break;

            case 'identifier':
                return parseVariableOrFunction();
        }

        throw new Error(`Unexpected token: ${token.type} (${token.value})`);
    };

    /** @returns {VariableNode | FunctionCallNode} */
    const parseVariableOrFunction = () => {
        const name = tokens[i++].value;
        const next = tokens[i];

        if (isFunction(name) && next?.type === 'parenthesis' && next.value === '(') {
            i++;
            /** @type {ASTNode[]} */
            const args = [];
            while (i < tokenLength && tokens[i].value !== ')') {
                args.push(parseExpression());
                if (tokens[i]?.type === 'comma') i++;
            }
            if (tokens[i++]?.value !== ')') throw new Error("Expected ')'");
            /** @type {FunctionCallNode} */
            return { type: 'FunctionCall', name, args };
        }

        if (isVariable(name)) {
            /** @type {VariableNode} */
            return { type: 'Variable', name };
        }

        throw new Error(`Unknown identifier: '${name}'`);
    };

    const result = parseExpression();
    if (i < tokenLength) {
        const leftover = tokens.slice(i).map(t => `${t.type}(${t.value})`).join(', ');
        throw new Error(`Unexpected token(s): '${leftover}'`);
    }

    return result;
};

/**
 * @param {ASTNode} node
 * @returns {number}
 */
export const evaluateinator = (node) => {
    switch (node.type) {
        case 'Literal':
            return node.value;

        case 'UnaryOperation': {
            const val = evaluateinator(node.argument);
            return node.operator === '-' ? -val : val;
        }

        case 'SuffixOperation': {
            const value = evaluateinator(node.value);
            return value * suffixes.get(node.suffix);
        }

        case 'BinaryOperation': {
            let left = evaluateinator(node.left);
            let right = evaluateinator(node.right);
            switch (node.operator) {
                case '+': return left + right;
                case '-': return left - right;
                case '*': return left * right;
                case '/':
                    if (right === 0) throw new Error('Division by zero');
                    return left / right;
                case '^': 
                    if (right < 0 || right % 1 !== 0) return left ** right;
                    else {
                        let result = 1;
                        while (right > 0) {
                            if (right % 2 === 1) result *= left;
                            left *= left;
                            right = (right / 2) | 0;
                        }
                        return result;
                    }
                default: throw new Error(`Unknown binary operator: ${node.operator}`);
            }
        }

        case 'FunctionCall': {
            const fn = getFunction(node.name);
            if (!fn) throw new Error(`Function '${node.name}' not found`);
            
            const expected = fn.params.length;
            const args = node.args;
            
            if (expected !== args.length)
                throw new Error(`Function '${fn.name}' expects ${expected} args`);

            const argValues = new Array(expected);
            for (let j = 0; j < expected; j++) {
                argValues[j] = evaluateinator(args[j]);
            }

            let tokens = tokeninator9000(fn.body);

            tokens = tokens.map(token => {
                if (token.type === 'identifier') {
                    const index = fn.params.indexOf(token.value);
                    if (index !== -1) {
                        return { type: 'number', value: String(argValues[index]) };
                    }
                }
                return token;
            });

            return evaluateinator(parseinator(tokens));
        }

        case 'Variable': {
            if (isVariable(node.name)) return getVariableValue(node.name);
            throw new Error(`Unknown variable: ${node.name}`);
        }

        default:
            throw new Error(`Unknown AST node type: ${node.type}`);
    }
};

/**
 * @param {string} input
 * @returns {number}
 */
export const calculator = (input) => {
    const tokens = tokeninator9000(input);
    const tree = parseinator(tokens);
    const result = evaluateinator(tree);
    const RANGE = 1e-7;
    if (Math.abs(result - Math.round(result)) < RANGE) {
        return Math.round(result);
    } else {
        return +result.toFixed(2);
    }
};

/**
 * @typedef {Object} LiteralNode
 * @property {'Literal'} type
 * @property {number} value
 *
 * @typedef {Object} UnaryOperationNode
 * @property {'UnaryOperation'} type
 * @property {string} operator
 * @property {ASTNode} argument
 *
 * @typedef {Object} BinaryOperationNode
 * @property {'BinaryOperation'} type
 * @property {string} operator
 * @property {ASTNode} left
 * @property {ASTNode} right
 *
 * @typedef {Object} FunctionCallNode
 * @property {'FunctionCall'} type
 * @property {string} name
 * @property {ASTNode[]} args
 *
 * @typedef {Object} VariableNode
 * @property {'Variable'} type
 * @property {string} name
 * 
 * @typedef {Object} SuffixNode
 * @property {'SuffixOperation'} type
 * @property {ASTNode} value
 * @property {'k' | 'm' | 'b' | 't'} suffix
 *
 * @typedef { LiteralNode | UnaryOperationNode | BinaryOperationNode | FunctionCallNode | VariableNode | SuffixNode } ASTNode
 */

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