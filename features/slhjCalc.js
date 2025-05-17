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

const charTypes = {
    '0': 'number',
    '1': 'number',
    '2': 'number',
    '3': 'number',
    '4': 'number',
    '5': 'number',
    '6': 'number',
    '7': 'number',
    '8': 'number',
    '9': 'number',
    '.': 'number',

    '+': 'operator',
    '-': 'operator',
    '*': 'operator',
    '/': 'operator',

    '^': 'exponent',

    '(': 'parenthesis',
    ')': 'parenthesis',

    ',': 'comma',

    '=': 'equals'
};

/**
 * @param {String} str
 * @returns {{ type: string, value: string }[]}
 */
export const tokeninator9000 = (str) => {
    str = str.replace(/\s+/g, '');

    const tokens = [];
    let i = 0;
    const len = str.length;

    while (i < len) {
        let char = str[i];
        let type = charTypes[char];

        if (type === 'number') {
            let num = '';
            let dotCount = 0;

            do {
                if (str[i] === '.' && ++dotCount > 1) throw new Error(`Multiple dots in number at ${i}`);
                num += str[i++];
            } while (i < len && charTypes[str[i]] === 'number');

            if (num === '.') throw new Error(`Invalid number`);
            tokens.push({ type: 'number', value: num });

            if (i < len && suffixes.has(str[i])) {
                tokens.push({ type: 'suffix', value: str[i++] });
            }

            continue;
        }

        if (type !== undefined) {
            tokens.push({ type, value: char });
            i++;

            if (
                i < len &&
                suffixes.has(str[i]) &&
                tokens.length > 0 &&
                (
                    tokens[tokens.length - 1].type === 'number' ||
                    (tokens[tokens.length - 1].type === 'parenthesis' && tokens[tokens.length - 1].value === ')')
                )
            ) {
                tokens.push({ type: 'suffix', value: str[i++] });
            }

            continue;
        }

        let identifier = '';
        while (i < len && charTypes[str[i]] === undefined) {
            identifier += str[i++];
        }
        tokens.push({ type: 'identifier', value: identifier });
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
            const token = tokens[i];
            if (token?.type !== 'operator' || !operators.has(token.value)) break;
            const operator = tokens[i++].value;
            const right = parser();
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
            const suffix = tokens[i++].value;
            /** @type {SuffixNode} */
            node = { type: 'SuffixOperation', value: node, suffix };
        }
        return node;
    };

    /** @returns {LiteralNode | VariableNode | FunctionCallNode | ASTNode} */
    const parsePrimary = () => {
        const token = tokens[i];
        if (i >= tokenLength) throw new Error("Unexpected end of input")

        if (token.type === 'number') {
            /** @type {LiteralNode} */
            return { type: 'Literal', value: +tokens[i++].value };
        }

        if (token.type === 'parenthesis' && token.value === '(') {
            i++;
            const expr = parseExpression();
            if (tokens[i++]?.value !== ')') throw new Error("Expected ')'");
            return expr;
        }

        if (token.type === 'identifier') {
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
    // console.log(JSON.stringify(tokens, null, 2))
    const tree = parseinator(tokens);
    // console.log(JSON.stringify(tree, null, 2))
    const result = evaluateinator(tree);
    const rounded = Number(result.toFixed(6));
    return Number.isInteger(rounded) ? rounded : result.toFixed(2);
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