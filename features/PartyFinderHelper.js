// Party Finder Helper ------------------
import Settings from "../config";
import { WorldInfo } from "../utils/WorldInfo";

const worldinfo = new WorldInfo();

const renderMissingClasses = register("renderItemIntoGui", (item, x, y) => {
    //if (worldinfo.getArea() !== "Dungeon Hub") return;
    if (Player.getContainer()?.getName() !== "Party Finder") return;

    const name = item.getName().removeFormatting();
    if (!/^(\w+)'s Party$/.test(name)) return;
    const lore = item.getLore()
    if (lore[2].removeFormatting() !== 'Floor: Floor VII') return;

    const missing = getMissingClasses(lore);

    if (missing.length === 0) return;

    const positions = [
        [x + 1, y],      // TL
        [x + 10, y],      // TR
        [x + 1, y + 9],  // BL
        [x + 10, y + 9],  // BR
    ];

    missing.forEach((c, i) => {
        Renderer.translate(0, 0, 260);
        Renderer.drawString(c, positions[i][0], positions[i][1], true);
    });
}).unregister();

Settings.registerListener("Party Finder Helper", v => v ? renderMissingClasses.register() : renderMissingClasses.unregister());

if (Settings.togglePartyFinderHelper) {
    renderMissingClasses.register();
}

const getMissingClasses = (lore) => {
    const classes = ["A", "B", "M", "H", "T"];
    const found = new Set();

    for (const line of lore.slice(4, -1)) {
        const c = line.removeFormatting().match(/: ?\(?([ABMHT])\)?/);
        if (c) found.add(c[1]);
    }

    return classes.filter(clas => !found.has(clas));
};
