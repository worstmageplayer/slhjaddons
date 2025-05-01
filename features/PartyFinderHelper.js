// Party Finder Helper ------------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";
const pfHelper = new RegisterGroup({
    renderItemIntoGui: register("renderItemIntoGui", (item, x, y) => {
        const container = Player.getContainer();
        if (!container || container.getName() !== "Party Finder") return;
    
        const name = item.getName().removeFormatting();
        if (!name.endsWith("'s Party")) return;
    
        const lore = item.getLore();
        if (!lore[2].removeFormatting().endsWith("Floor VII")) return;
    
        const missing = getMissingClasses(lore);
        if (!missing.length) return;
    
        const positions = [
            [x + 1, y],       // TL
            [x + 10, y],      // TR
            [x + 1, y + 9],   // BL
            [x + 10, y + 9],  // BR
        ];
    
        for (let i = 0; i < missing.length && i < 4; i++) {
            Renderer.translate(0, 0, 260);
            Renderer.drawString(missing[i], positions[i][0], positions[i][1], true);
        }
    }).unregister(),

    guiRender: register('guiRender', () => {
        const container = Player.getContainer();
        if (!container || container.getName() !== "Party Finder") return;
    
        const classCounts = { A: 0, B: 0, M: 0, H: 0, T: 0 };
        const itemList = container.getItems();
    
        for (const item of itemList) {
            const name = item?.getName()?.removeFormatting();
            if (!name || !name.endsWith("'s Party")) continue;
    
            const lore = item.getLore();
            const missing = getMissingClasses(lore); // e.g., ["H", "T"]
            for (const cls of missing) {
                if (cls in classCounts) classCounts[cls]++;
            }
        }
    
        const entries = Object.entries(classCounts);
        const maxCount = Math.max(...entries.map(e => e[1]));
        const minCount = Math.min(...entries.map(e => e[1]));
    
        const mostWanted = entries
            .filter(([_, count]) => count === maxCount)
            .map(([cls]) => cls)
            .sort()
            .join(", ") || "None";
    
        const leastWanted = entries
            .filter(([_, count]) => count === minCount)
            .map(([cls]) => cls)
            .sort()
            .join(", ") || "None";
        Renderer.drawStringWithShadow(`Most Wanted: ${mostWanted}\nLeast Wanted: ${leastWanted}`, 10, 10);
    }).unregister()
});

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
