// Party Finder Helper ------------------
import Settings from "../config";

let partyList = []
let mostWanted = "None"
let leastWanted = "None"

const pfHelper = register('guiMouseClick', (mx, my, btn, gui, event) => {
    Client.scheduleTask(10, () => {
        const container = Player.getContainer();
        if (!container || container.getName() !== "Party Finder") return; // Checks in Party Finder
        
        const itemList = container.getItems();
        partyList = getPartyList(itemList, gui); // Array of objects {"slot":11,"missing":["H"],"position":[[135,79]]}

        const classCounts = { A: 0, B: 0, M: 0, H: 0, T: 0 };

        partyList.forEach(p => {
            p.missing.forEach(cls => classCounts[cls]++);
        }); 

        const values = Object.values(classCounts);
        const max = Math.max(...values);
        const min = Math.min(...values);

        const maxKeys = Object.keys(classCounts).filter(k => classCounts[k] === max && max > 0);
        const minKeys = Object.keys(classCounts).filter(k => classCounts[k] === min);

        mostWanted = maxKeys.length ? maxKeys.join(", ") : "None";
        leastWanted = minKeys.length ? minKeys.join(", ") : "None";
        guiRender.register();
        guiClosed.register();
    });
}).unregister()

const guiRender = register('guiRender', (mx, my, gui) => {
    if (partyList.length < 1) return;
    partyList.forEach(p => {
        for (let i = 0; i < p.missing.length; i++) {
            Renderer.translate(0, 0, 260);
            Renderer.drawString(p.missing[i], p.position[i][0], p.position[i][1], true)
        }
    });

    Renderer.drawString(`Most Wanted: ${mostWanted}\nLeast Wanted: ${leastWanted}`, 10, 10, true);
}).unregister()

const guiClosed = register('guiClosed', () => {
    partyList = []
    mostWanted = "None"
    leastWanted = "None"
    guiRender.unregister();
    guiClosed.unregister();
}).unregister();

Settings.registerListener("Party Finder Helper", v => v ? pfHelper.register() : pfHelper.unregister());

if (Settings.togglePartyFinderHelper) {
    pfHelper.register();
}

const getPartyList = (itemList, gui) => {
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