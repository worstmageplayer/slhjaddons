import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

const floorName = ["ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN"];

let timerStart = 0;
let addedToQueue = false;

const dungeonCommands = new RegisterGroup({
    command: register('command', (arg) => {
        if (!arg) return ChatLib.command('warp dungeons');
    
        const match = arg.toLowerCase().match(/^(m|f)(\d)$/);
        if (!match) return;
    
        const [_, prefix, num] = match;
        const floorNum = parseInt(num);
        if (!['m', 'f'].includes(prefix) || floorNum < 1 || floorNum > 7) return;
    
        const floorWord = floorName[floorNum - 1];
        const instanceType = prefix === 'm' ? "MASTER_CATACOMBS" : "CATACOMBS";
    
        const timeStart = Date.now() - timerStart;
        const timeLeft = Math.max(0, 30000 - timeStart);
    
        if (timeLeft > 0) {
            if (addedToQueue) return ChatLib.chat(`Rejoining in ${(timeLeft / 1000).toFixed(2)}s`);
            addedToQueue = true;
            ChatLib.command(`pc Waiting for cooldown, rejoining in ${(timeLeft / 1000).toFixed(2)}s.`);
        }
    
        setTimeout(() => {
            ChatLib.command(`joininstance ${instanceType}_FLOOR_${floorWord}`);
            enterUndersized();
            addedToQueue = false;
        }, timeLeft + 100);
        
    }).setName('d').unregister(),

    chat: register('chat', (rank, name, dungeontype, number) => {
        if (name === Player.getName()) timerStart = Date.now();
    }).setCriteria('${*}[${rank}] ${name} entered ${dungeontype} Catacombs, Floor ${number}${*}').unregister(),
})

Settings.registerListener('Dungeon Commands', v => v ? dungeonCommands.register() : dungeonCommands.unregister());

if (Settings.toggleDcmd) dungeonCommands.register();

function enterUndersized() {
    if (!Settings.toggleUndersizedEntry) return;
    Client.scheduleTask(10, () => {
        Player.getContainer()?.click(13, false, 'LEFT');
    });
};