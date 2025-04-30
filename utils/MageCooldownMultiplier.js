import { WorldInfo } from './WorldInfo'

const worldInfo = new WorldInfo

export function getCooldownMultiplier() {
    const tabList = TabList.getNames();
    if (!worldInfo.inDungeon()) return 1;

    const mages = tabList
        .map(line => line.removeFormatting().match(/^\[(\d+)] (\w+) \((\w+) (\w+)\)$/))
        .filter(Boolean)
        .map(([_, level, name, playerClass, classLevel]) => ({
            level: `[${level}]`,    
            name,
            class: playerClass,
            classLevel
        }))
        .filter(p => p.class === "Mage");
    const you = mages.find(p => p.name === Player.getName());

    if (!you) return 1;
    const level = decodeNumeral(you.classLevel);
    const reduction = Math.floor(level / 2) / 100;
    return 1 - 0.25 - reduction - (duplicateMage() ? 0 : 0.25);
}

function duplicateMage() {
    const scoreboard = Scoreboard.getLines();

    const otherMages = scoreboard
        .map(line => line.getName().removeFormatting().match(/^\[(\w)] (\w+)/))
        .filter(Boolean)
        .map(([_, playerClass, name]) => ({
            class: playerClass,
            name
        }))
        .filter(p => p.class.includes('M'));
    return otherMages.length === 0  ? false : true;
}

const map = { I: 1, V: 5, X: 10, L: 50, C: 100, D: 500, M: 1000 };

function decodeNumeral(numeral) {
    numeral = numeral.removeFormatting().trim().toUpperCase();
    if (!numeral.match(/^[IVXLCDM]+$/)) return null;
    let total = 0, prev = 0;
    for (let i = numeral.length - 1; i >= 0; i--) {
        let val = map[numeral[i]];
        total += val < prev ? -val : val;   
        prev = val;
    }
    return total;
}
