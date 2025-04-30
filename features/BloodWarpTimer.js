// Blood Warp Timer ---------------------
import Settings from "../config";
import { WorldInfo } from "../utils/WorldInfo";
import { RegisterGroup } from "../utils/RegisterStuff";

const worldInfo = new WorldInfo

// Enter blood at 10s
let bloodOpen = null;
const bloodWarpTimer = new RegisterGroup({
    chat: register('chat', () => {
        if (!worldInfo.inDungeon()) return;
        bloodOpen = Date.now();
    }).setCriteria('The BLOOD DOOR has been opened!').setParameter('contains').unregister(),

    renderOverlay: register('renderOverlay', () => {
        if (!bloodOpen) return;
        const time = ((Date.now() - bloodOpen) / 1000).toFixed(1);
        let colorCode = time < 5 ? 'a' : time < 7.5 ? 'e' : 'c';
        
        if (time > 11) {
            bloodOpen = null;
            return;
        }
        Renderer.drawString(
            `§${colorCode}${time}`, 
            (Renderer.screen.getWidth() - Renderer.getStringWidth(time)) / 2, 
            Renderer.screen.getHeight() / 2 - 10, 
            true
        );
    }).unregister(),

    worldUnload: register('worldUnload', () => bloodOpen = null).unregister()
})

Settings.registerListener("Blood Time", value => {
    if (value) {
        bloodWarpTimer.register();
    } else {
        bloodWarpTimer.unregister();
    }
})

initialLoad = Settings.toggleBloodTime
if (initialLoad) {
    bloodWarpTimer.register();
}
