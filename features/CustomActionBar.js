// Custom Action Bar --------------------
import Settings from "../config";
import { isHealthCurrentlyHidden } from "../features/HideHealth";
import { RegisterGroup } from "../utils/RegisterStuff";

const S02PacketChat = Java.type('net.minecraft.network.play.server.S02PacketChat');

let actionBarText = null;
let currentY = null;

const customActionBar = new RegisterGroup({
    renderOverlay: register("renderOverlay", () => {
        if (!actionBarText) return;
        const x = (Renderer.screen.getWidth() - Renderer.getStringWidth(actionBarText)) / 2;
        const yPos = Renderer.screen.getHeight() - 72 + (isHealthCurrentlyHidden() ? 20 : 0);
        const y = currentY ? currentY : yPos
        const yMove = move(y, yPos, 1.5)
        currentY = Math.abs(yMove - yPos) < 0.1 ? yPos : yMove
        Renderer.drawStringWithShadow(actionBarText, x, y);
    }).unregister(),

    packetReceived: register('packetReceived', (packet, event) => {
        if (packet.func_179841_c() !== 2) return;
        actionBarText = packet.func_148915_c().text;
    }).setFilteredClass(S02PacketChat).unregister(),

    actionBar: register('actionBar', (text, event) => {
        cancel(event)
    }).setCriteria('${text}').unregister(),

    worldUnload: register('worldUnload', () => {
        actionBarText = null;
    }).unregister()
})

function move(start, end, amount) {
    return start + (end - start) * amount;
}

Settings.registerListener('Custom Action Bar', v => v ? customActionBar.register() : customActionBar.unregister());

if (Settings.toggleCustomActionBar) {
    customActionBar.register();
}
