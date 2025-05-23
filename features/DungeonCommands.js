import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

const COOLDOWN_MS = 30000;

const Floor = {
  1: "ONE",
  2: "TWO",
  3: "THREE",
  4: "FOUR",
  5: "FIVE",
  6: "SIX",
  7: "SEVEN"
};

const InstanceType = {
  m: "MASTER_CATACOMBS",
  f: "CATACOMBS"
};

let cooldownEnd = 0;
let addedToQueue = false;

const dungeonCommands = new RegisterGroup({
  command: register("command", (arg) => {
    if (!arg) return ChatLib.command("warp dungeons");

    const match = /^([mf])([1-7])$/i.exec(arg);
    if (!match) return;

    const [_, prefix, num] = match;
    const floorNum = parseInt(num);
    const floorWord = Floor[floorNum];
    const instanceType = InstanceType[prefix.toLowerCase()];
    if (!floorWord || !instanceType) return;

    const now = Date.now();
    const timeLeft = Math.max(0, cooldownEnd - now);

    if (timeLeft > 0) {
      if (addedToQueue) return ChatLib.chat(`Rejoining in ${(timeLeft / 1000).toFixed(2)}s`);
      addedToQueue = true;
      ChatLib.command(`pc Waiting for cooldown, rejoining in ${(timeLeft / 1000).toFixed(2)}s.`);
    }

    setTimeout(() => {
      ChatLib.command(`joininstance ${instanceType}_FLOOR_${floorWord}`);
      enterUndersized();
    }, timeLeft + 100);
  }).setName("d").unregister(),

  chat: register("chat", (rank, name, dungeontype, number) => {
    if (name === Player.getName()) cooldownEnd = Date.now() + COOLDOWN_MS;
  }).setCriteria("${*}[${rank}] ${name} entered ${dungeontype} Catacombs, Floor ${number}${*}").unregister(),
});

Settings.registerListener("Dungeon Commands", v => v ? dungeonCommands.register() : dungeonCommands.unregister());
if (Settings.toggleDcmd) dungeonCommands.register();

function enterUndersized() {
  if (!Settings.toggleUndersizedEntry) {
    addedToQueue = false;
    return;
  }

  Client.scheduleTask(10, () => {
    Player.getContainer()?.click(13, false, "LEFT");
    addedToQueue = false;
  });
}