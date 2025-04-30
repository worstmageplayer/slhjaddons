// Party Finder Helper ------------------
import Settings from "../config";
import { WorldInfo } from "../utils/WorldInfo";

const worldinfo = new WorldInfo();

const renderMissingClasses = register("renderItemIntoGui", (item, x, y) => {
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
}).unregister();

Settings.registerListener("Party Finder Helper", value =>
    value ? renderMissingClasses.register() : renderMissingClasses.unregister()
);

if (Settings.togglePartyFinderHelper) {
    renderMissingClasses.register();
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
