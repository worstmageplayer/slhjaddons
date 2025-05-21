// Party Finder Helper ------------------
import Settings from "../config";
import { data } from "../data";
import { drawHollowRect } from "../utils/RendererStuff";
import { RegisterGroup } from "../utils/RegisterStuff";

let partyList = null
let mostWanted = null
let leastWanted = null

const pfHelper = register('guiMouseClick', () => {
    Client.scheduleTask(10, () => {
        const container = Player.getContainer();
        if (!container || container.getName() !== "Party Finder") return; // Checks in Party Finder
        
        const gui = Client.currentGui.get()
        const itemList = container.getItems();
        partyList = getPartyList(itemList, gui); // Array of objects {"slot":11,"missing":["H"],"position":[[135,79]]}

        const classCounts = { A: 0, B: 0, M: 0, H: 0, T: 0 };

        partyList.forEach(party => party.missing.forEach(cls => classCounts[cls]++));

        const max = Math.max(...Object.values(classCounts));
        const min = Math.min(...Object.values(classCounts));
        const maxKeys = Object.keys(classCounts).filter(k => classCounts[k] === max && max > 0);
        const minKeys = Object.keys(classCounts).filter(k => classCounts[k] === min);

        mostWanted = maxKeys.length > 0 ? maxKeys.join(", ") : "None";
        leastWanted = minKeys.length > 0 ? minKeys.join(", ") : "None";
        
        pfStuff.register();
    });
}).unregister()

const pfStuff = new RegisterGroup({
    guiRender: register('guiRender', () => {
        if (!partyList) return;

        partyList.forEach(party => {
            const partyMissingLength = party.missing.length;

            for (let i = 0; i < partyMissingLength; i++) {
                let cls = party.missing[i];
                let [x, y] = party.position[i];
                Renderer.translate(0, 0, 260);
                Renderer.drawString(cls, x, y, true)

                if (cls === data.player.dungeon.class[0]) {
                    let [x, y] = party.slotPos
                    Renderer.translate(0, 0, 1);
                    drawHollowRect(Renderer.color(0, 170, 0), x, y, 16, 16, 1)
                }   
            }
        });

        Renderer.drawString(`Most Wanted: ${mostWanted}\nLeast Wanted: ${leastWanted}`, 10, 10, true)
    }).unregister(),

    guiClosed: register('guiClosed', () => {
        partyList = null
        mostWanted = null
        leastWanted = null
        pfStuff.unregister();
    }).unregister(),
})

Settings.registerListener("Party Finder Helper", v => v ? pfHelper.register() : pfHelper.unregister());

if (Settings.togglePartyFinderHelper) {
    pfHelper.register();
}

const getPartyList = (itemList, gui) => {
    const partyList = [];
    const guiLeft = gui.getGuiLeft() ?? 0;
    const guiTop = gui.getGuiTop() ?? 0;

    itemList.forEach((item, i) => {
        const name = item?.getName();
        if (!name || !name.endsWith("'s Party")) return;

        const lore = item.getLore();
        if (!lore[2].removeFormatting().endsWith("Floor VII")) return;

        const missing = getMissingClasses(lore);
        if (missing.length === 0) return;

        const slot = gui.field_147002_h?.func_75139_a(i);
        if (!slot) return;

        const x = slot.field_75223_e + guiLeft;
        const y = slot.field_75221_f + guiTop;
        const positions = [
            [x + 1, y],
            [x + 10, y],
            [x + 1, y + 9],
            [x + 10, y + 9],
        ].slice(0, missing.length);

        partyList.push({ 
            slot: i, 
            missing, 
            position: positions, 
            slotPos: [x, y] 
        });
    });

    return partyList;
}

const getMissingClasses = (lore) => {
    const required = new Set(["A", "B", "M", "H", "T"]);
    const found = new Set();

    lore.slice(4, -1).forEach(line => {
        const match = line.removeFormatting().match(/: ?\(?([ABMHT])\)?/);
        if (match) found.add(match[1]);
    });

    return [...required].filter(cls => !found.has(cls));
}