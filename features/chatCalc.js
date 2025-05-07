// Credits to chatGPT
import Settings from "../config";   

let devMode = Settings.devCalc;
Settings.registerListener('Calc BeDugging', v => devMode = v)
const dev = {
    step: 1,
    logStep: (msg, detail = "") => devMode && console.log(`Step ${dev.step++}: ${msg}${detail ? ' → ' + detail : ''}`),
    log: (msg) => devMode && console.log(msg),
    indent: (msg) => devMode && console.log(`  ${msg}`)
};

const definedFunctions = new Map();
const tolerance = 1e-7;

const functionMap = {
    'sin': 'Math.sin', 'cos': 'Math.cos', 'tan': 'Math.tan',
    'asin': 'Math.asin', 'acos': 'Math.acos', 'atan': 'Math.atan',
    'sinh': 'Math.sinh', 'cosh': 'Math.cosh', 'tanh': 'Math.tanh',
    'asinh': 'Math.asinh', 'acosh': 'Math.acosh', 'atanh': 'Math.atanh',
    'abs': 'Math.abs', 'pow': 'Math.pow', 'floor': 'Math.floor',
    'ceil': 'Math.ceil', 'round': 'Math.round', 'exp': 'Math.exp',
    'max': 'Math.max', 'min': 'Math.min', 'sign': 'Math.sign',
    'hypot': 'Math.hypot', 'random': 'Math.random',
    'log': 'Math.log10', 'ln': 'Math.log', 'sqrt': 'Math.sqrt',
    'pi': 'Math.PI', 'e': 'Math.E'
};

register('Command', (...args) => {
    let expression = args.join('');
    ChatLib.chat(defineFunction(expression));
}).setName('define').setAliases('let');

register('Command', (...args) => {
    let expression = args.join('').toLowerCase();
    if (!expression || expression.toLowerCase() === 'help') {
        ChatLib.chat([
            "How to use /calc:",
            "- /calc expression (e.g. /calc sin(pi/2) + 3^2)"
        ].join("\n"));
    }
    ChatLib.chat(calculator(expression));
    dev.log('---------------------------');
}).setName('calc');

function defineFunction(input) {
    dev.log(`Defining function with input: ${input}`);
    if (input.length === 0 || input.toLowerCase() === 'help') {
        return [
            'How to use /define:',
            '- /define f(x) = expression',
            '- /define show - shows all defined functions',
            '- /define clear - clears all defined functions'
        ].join('\n');
    }

    const inputText = input.toLowerCase();

    if (inputText === 'show') {
        if (definedFunctions.size === 0) return "No functions have been defined yet.";
        return "Defined functions:\n" + [...definedFunctions.entries()].map(([name, func]) => `${name}(${func.variables.join(', ')}) = ${func.expression}`).join('\n');
    }

    if (inputText === 'clear' || inputText === 'reset') {
        definedFunctions.clear();
        return "All functions have been cleared.";
    }

    const match = input.match(/^([a-zA-Z_][a-zA-Z0-9_]*)\(([^()=]+)\)=([^=]+)$/);
    if (!match) {
        return `Invalid function format.\n/define help for help.`;
    }

    const [, name, rawVariables, rawExpression] = match;

    const reservedNames = ['e', 'pi'];
    if (reservedNames.includes(name)) {
        return `"${name}" is not allowed as a function name.`;
    }

    const variableArray = rawVariables.split(',').map(v => v.trim()).filter(v => v);
    if (variableArray.length === 0) return "You must define at least one variable.";

    const seen = new Set();
    for (const v of variableArray) {
        if (reservedNames.includes(v)) return `"${v}" is not allowed as a variable name.`;
        if (seen.has(v)) return `Duplicate variable name: ${v}`;
        seen.add(v);
    }

    let parsedExpression = rawExpression;
    const mappedVariables = [];

    variableArray.forEach((variable, index) => {
        const replacement = `x${index}`;
        const re = new RegExp(`\\b${variable}\\b`, 'g');
        parsedExpression = parsedExpression.replace(re, replacement);
        mappedVariables.push(replacement);
    });

    const funcObject = {
        name,
        variables: mappedVariables,
        expression: parsedExpression
    };

    definedFunctions.set(name, funcObject);
    dev.log(`Function defined: ${name}(${rawVariables})=${rawExpression}`);
    return `Defined function: ${input}`;
}

export function calculator(input) {
    dev.log('===Calculations===');
    dev.step = 1;
    dev.logStep("Starting calculation", input);

    dev.logStep("Tokenizing expression", input);
    const tokens = splitMathExpression(input);

    dev.logStep("Expanding defined functions");
    const expandedTokens = expandDefinedFunctions(tokens);
    if (typeof expandedTokens === 'string') return expandedTokens;

    dev.logStep("Replacing function names");
    const expandedSuffix = expandSuffixes(expandedTokens);

    dev.logStep("Replacing function names");
    const replacedArray = replaceFunctionsInArray(expandedSuffix);

    const joinedExpression = replacedArray.join('');
    dev.logStep("Final JS expression", joinedExpression);

    try {
        const result = eval(joinedExpression);
        dev.logStep("Evaluation result", result);
        return formatResult(result);
    } catch (e) {
        dev.logStep("Error", e.message);
        return "Invalid expression.";
    }
}

function expandSuffixes(tokens) {
    const multipliers = { k: '1000', m: '1000000', b: '1000000000' };
    const result = [];

    for (let token of tokens) {
        let match = token.match(/^(\d*\.?\d+)([kmb])$/i);
        if (match) {
            let [, number, suffix] = match;
            result.push(number, '*', multipliers[suffix.toLowerCase()]);
        } else if (multipliers[token.toLowerCase()]) {
            result.push('*', multipliers[token.toLowerCase()]);
        } else {
            result.push(token);
        }
    }
    dev.logStep("Replacing Suffix: " + result)
    return result;
}

function formatResult(result) {
    if (Math.abs(result) < tolerance) return '0';
    if (typeof result === 'number' && !Number.isInteger(result)) {
        return parseFloat(result.toFixed(10)).toString();
    }
    return result.toString();
}

function expandDefinedFunctions(tokens) {
    dev.log('===Expand Functions===');
    dev.logStep("Expanding functions for tokens:", JSON.stringify(tokens));

    for (let i = 0; i < tokens.length; i++) {
        const func = definedFunctions.get(tokens[i]);
        if (func && tokens[i + 1] === '(') {
            dev.indent(`Expanding function ${func.name}`);

            let j = i + 2;
            let depth = 1;
            let args = [];
            let currentArg = [];

            while (j < tokens.length && depth > 0) {
                const token = tokens[j];
                if (token === '(') depth++;
                else if (token === ')') depth--;

                if ((token === ',' && depth === 1) || depth === 0) {
                    args.push(currentArg);
                    currentArg = [];
                } else {
                    currentArg.push(token);
                }

                j++;
            }

            if (args.length !== func.variables.length) {
                dev.logStep(`Invalid number of arguments for ${func.name}, expected ${func.variables.length}, got ${args.length}`);
                return `Invalid number of arguments for ${func.name}`
            };

            let substituted = func.expression;
            args.forEach((argTokens, idx) => {
                const expanded = expandDefinedFunctions(argTokens);
                const re = new RegExp(`\\bx${idx}\\b`, 'g');
                substituted = substituted.replace(re, `(${Array.isArray(expanded) ? expanded.join('') : expanded})`);
            });

            const subTokens = splitMathExpression(substituted);
            tokens.splice(i, j - i, ...subTokens);
            return expandDefinedFunctions(tokens);
        }
    }

    dev.indent(`Expanded tokens: ${JSON.stringify(tokens)}`);
    return tokens;
}

function splitMathExpression(expression) {
    dev.log('===Split Math Expression===');
    dev.logStep("Splitting math expression:", expression);
    const regex = /([+\-*/()^,])|([^+\-*/()^,]+)/g;
    let result = [];
    let match;

    while ((match = regex.exec(expression)) !== null) {
        if (match[1]) result.push(match[1]);
        else if (match[2]) result.push(match[2].trim());
    }

    dev.logStep("Tokenized expression:", JSON.stringify(result));
    return result;
}

function replaceFunctionsInArray(array) {
    dev.log('===Replace Functions===');
    dev.logStep("Replacing functions in array:", JSON.stringify(array));
    const result = array.map(item => item === '^' ? '**' : functionMap[item] || item);
    dev.logStep("Replaced array:", JSON.stringify(result));
    return result;
}