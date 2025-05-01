// Party Finder Helper ------------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

const pfHelper = register('guiRender', (mx, my, gui) => {
    const container = Player.getContainer();
    if (!container || container.getName() !== "Party Finder") return; // Checks if in Party Finder
    
    const classCounts = { A: 0, B: 0, M: 0, H: 0, T: 0 };
    const itemList = container.getItems(); // Gets lists of items in gui
    
    for (let i = 0; i < itemList.length; i++) {
        const item = itemList[i] // Gets item
        const name = item?.getName()?.removeFormatting();
        if (!name || !name.endsWith("'s Party")) continue; // Checks item

        const lore = item.getLore();
        if (!lore[2].removeFormatting().endsWith("Floor VII")) continue; // Checks for f7/m7

        const missing = getMissingClasses(lore); // Gets the missing classes e.g. ["H", "T"]
        if (!missing.length) continue;
            
        const slot = gui.field_147002_h?.func_75139_a(i) // Gets the slot of the item
        const x = slot.field_75223_e + gui?.getGuiLeft() ?? 0; // Gets the position of the slot
        const y = slot.field_75221_f + gui?.getGuiTop() ?? 0;

        const positions = [   // Position of the classes to be rendered in the slot
            [x + 1, y],       // TL
            [x + 10, y],      // TR
            [x + 1, y + 9],   // BL
            [x + 10, y + 9],  // BR
        ];

        for (let i = 0; i < missing.length && i < 4; i++) { // Renders the missing classes in the slot
            Renderer.translate(0, 0, 260);
            Renderer.drawString(missing[i], positions[i][0], positions[i][1], true);
        }

        for (const cls of missing) { // Increments count of how many times each class is needed
            if (cls in classCounts) classCounts[cls]++;
        }
    }
    
    const entries = Object.entries(classCounts);
    const maxCount = Math.max(...entries.map(e => e[1])); // Gets the highest count
    const minCount = Math.min(...entries.map(e => e[1])); // Gets the lowest count
    
    const mostWanted = entries // Gets the classes with the most count
        .filter(([_, count]) => count === maxCount)
        .map(([cls]) => cls)
        .join(", ") || "None";
    
    const leastWanted = entries // Gets the classes with the least count
        .filter(([_, count]) => count === minCount)
        .map(([cls]) => cls)
        .join(", ") || "None";

    // Renders the most and least wanted class
    Renderer.drawStringWithShadow(`Most Wanted: ${mostWanted}\nLeast Wanted: ${leastWanted}`, 10, 10);
}).unregister()


Settings.registerListener("Party Finder Helper", v => v ? pfHelper.register() : pfHelper.unregister());

if (Settings.togglePartyFinderHelper) {
    pfHelper.register();
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
