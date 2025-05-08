// Party Finder Helper ------------------
import Settings from "../config";

let partyList = []
let mostWanted = "None"
let leastWanted = "None"
let playerClass = ''

const pfHelper = register('guiMouseClick', () => {
    Client.scheduleTask(10, () => {
        const container = Player.getContainer();
        if (!container || container.getName() !== "Party Finder") return; // Checks in Party Finder

        const tblst = TabList.getNames();
        for (let i = 0; i < tblst.length; i++) {
            if (tblst[i].includes('Dungeons:')) {
                const classLine = tblst[i+2].removeFormatting().trim();
                playerClass = classLine.match(/^[A-Za-z]/)[0];
            }
        }
        
        const gui = Client.currentGui.get()
        const itemList = container.getItems();
        partyList = getPartyList(itemList, gui); // Array of objects {"slot":11,"missing":["H"],"position":[[135,79]]}

        const classCounts = { A: 0, B: 0, M: 0, H: 0, T: 0 };

        partyList.forEach(p => p.missing.forEach(cls => classCounts[cls]++));

        const max = Math.max(...Object.values(classCounts));
        const min = Math.min(...Object.values(classCounts));
        const maxKeys = Object.keys(classCounts).filter(k => classCounts[k] === max && max > 0);
        const minKeys = Object.keys(classCounts).filter(k => classCounts[k] === min);

        mostWanted = maxKeys.length ? maxKeys.join(", ") : "None";
        leastWanted = minKeys.length ? minKeys.join(", ") : "None";
        
        guiRender.register();
        guiClosed.register();
    });
}).unregister()

const guiRender = register('guiRender', () => {
    if (partyList.length < 1) return;
    partyList.forEach(p => {
        for (let i = 0; i < p.missing.length; i++) {
            let cls = p.missing[i];
            let [x, y] = p.position[i];
            Renderer.translate(0, 0, 260);
            Renderer.drawString(cls, x, y, true)

            if (Settings.toggleHighlightPF && cls === playerClass) {
                let [x, y] = p.slotPos
                drawHollowRect(Renderer.color(0, 170, 0), x, y, 16, 16, 1)
            }   
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
    const partyList = [];
    const guiLeft = gui?.getGuiLeft() ?? 0;
    const guiTop = gui?.getGuiTop() ?? 0;

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

function drawHollowRect(color, x, y, width, height, thickness = 1) {
    Renderer.translate(0, 0, 1);
    Renderer.drawRect(color, x, y, width, thickness);
    Renderer.translate(0, 0, 1);
    Renderer.drawRect(color, x, y + height - thickness, width, thickness);
    Renderer.translate(0, 0, 1);
    Renderer.drawRect(color, x, y + thickness, thickness, height - 2 * thickness);
    Renderer.translate(0, 0, 1);
    Renderer.drawRect(color, x + width - thickness, y + thickness, thickness, height - 2 * thickness);
}