import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

const floorName = ["ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN"];

let timerStart = 0;
let addedToQueue = false;

const dungeonCommands = new RegisterGroup({
    command: register('command', (arg) => {
        if (!arg) {
            ChatLib.command('warp dungeons');
            return;
        }
    
        let match = arg.toLowerCase().match(/^(m|f)(\d)$/);
        if (!match) return;
    
        const [daWholeTing, prefix, num] = match;
        const floorNum = parseInt(num);
    
        if ((prefix === 'm' || prefix === 'f') && floorNum >= 1 && floorNum <= 7) {
            const floorWord = floorName[floorNum - 1];
            const instanceType = prefix === 'm' ? "MASTER_CATACOMBS" : "CATACOMBS";
    
            let timeStart = Date.now() - timerStart;
            let timeLeft = Math.max(0, 30000 - timeStart);
    
            if (timeLeft > 0) {
                if (addedToQueue) {
                    ChatLib.chat(`Rejoining in ${(timeLeft / 1000).toFixed(2)}s`)
                    return;
                };
                addedToQueue = true;
                ChatLib.command(`pc Waiting for cooldown, rejoining in ${(timeLeft / 1000).toFixed(2)}s.`);
            }
    
            setTimeout(() => {
                ChatLib.command(`joininstance ${instanceType}_FLOOR_${floorWord}`);
                enterUndersized();
                addedToQueue = false;
            }, timeLeft + 100);
    
            return;
        }
    }).setName('d').unregister(),

    chat: register('chat', (rank, name, dungeontype, number) => {
        if (name === Player.getName()) {
            timerStart = Date.now();
        }   
    }).setCriteria('${*}[${rank}] ${name} entered ${dungeontype} Catacombs, Floor ${number}${*}').unregister(),
})

Settings.registerListener("Dungeon Commands", value => {
    if (value) {
        dungeonCommands.register();
        return;
    }
    dungeonCommands.unregister();
});

initialLoad = Settings.toggleDcmd
if (initialLoad) {
    dungeonCommands.register();
}

function enterUndersized() {
    if (!Settings.toggleUndersizedEntry) return;
    Client.scheduleTask(10, () => {
        Player.getContainer()?.click(13, false, 'LEFT');
    });
};