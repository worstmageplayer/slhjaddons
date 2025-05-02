// Party Finder Helper ------------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

let partyList = []
let mostWanted = "None"
let leastWanted = "None"

const pfHelper = new RegisterGroup({
    guiMouseClick: register('guiMouseClick', (mx, my, btn, gui, event) => {
        Client.scheduleTask(10, () => {
            const container = Player.getContainer();
            if (!container || container.getName() !== "Party Finder") return; // Checks in Party Finder
            
            const itemList = container.getItems();
            partyList = getPartyList(itemList, gui); // Array of objects {"slot":11,"missing":["H"],"position":[[135,79]]}
    
            const classCounts = { A: 0, B: 0, M: 0, H: 0, T: 0 };
    
            for (const p of partyList) { // Counts the missing classes
                for (const cls of p.missing) {
                    if (cls in classCounts) classCounts[cls]++;
                }
            }
    
            const entries = Object.entries(classCounts);
            const max = Math.max(...entries.map(([_, v]) => v)); // Gets the largest number
            const min = Math.min(...entries.map(([_, v]) => v)); // Gets the lowest number
    
            mostWanted = entries.filter(([_, v]) => v === max && v > 0).map(([k]) => k).join(", ") || "None";
            leastWanted = entries.filter(([_, v]) => v === min && v > 0).map(([k]) => k).join(", ") || "None";
        });
    }).unregister(),

    guiRender: register('guiRender', (mx, my, gui) => {
        const container = Player.getContainer();
        if (!container || container.getName() !== "Party Finder") return; // Checks if in Party Finder
        if (partyList.length < 1) return;
        partyList.forEach(p => {
            for (let i = 0; i < p.missing.length; i++) {
                Renderer.translate(0, 0, 260);
                Renderer.drawString(p.missing[i], p.position[i][0], p.position[i][1], true)
            }
        });
    
        Renderer.drawString(`Most Wanted: ${mostWanted}\nLeast Wanted: ${leastWanted}`, 10, 10, true);
    }).unregister(),
})

Settings.registerListener("Party Finder Helper", v => v ? pfHelper.register() : pfHelper.unregister());

if (Settings.togglePartyFinderHelper) {
    pfHelper.register();
}

function getPartyList(itemList, gui) {
    let partyList = [];
    for (let i = 0; i < itemList.length; i++) {
        let item = itemList[i];
        let name = item?.getName();
        if (!name || !name.endsWith("'s Party")) continue;

        let lore = item.getLore();
        if (!lore[2].removeFormatting().endsWith("Floor VII")) continue;

        let missing = getMissingClasses(lore);
        if (missing.length === 0) continue;

        let slot = gui.field_147002_h?.func_75139_a(i);
        if (!slot) continue;

        let x = slot.field_75223_e + (gui?.getGuiLeft() ?? 0);
        let y = slot.field_75221_f + (gui?.getGuiTop() ?? 0);

        let positions = [
            [x + 1, y],
            [x + 10, y],
            [x + 1, y + 9],
            [x + 10, y + 9],
        ].slice(0, missing.length)  ;

        partyList.push({
            slot: i,
            missing: missing,
            position: positions,
        });
    }
    return partyList;
}


const getMissingClasses = (lore) => {
    const required = ["A", "B", "M", "H", "T"];
    const found = new Set();

    for (const line of lore.slice(4, -1)) {
        const match = line.removeFormatting().match(/: ?\(?([ABMHT])\)?/);
        if (match) found.add(match[1]);
    }
    return required.filter(cls => !found.has(cls));
};