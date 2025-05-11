// Custom Action Bar --------------------
import Settings from "../config";
import { isHealthCurrentlyHidden } from "../features/HideHealth";
import { RegisterGroup } from "../utils/RegisterStuff";

const S02PacketChat = Java.type('net.minecraft.network.play.server.S02PacketChat');

let actionBarText = null;
let animStartTime = 0;
let startY = 0;
let targetY = 0;
let currentY = Renderer.screen.getHeight() - 72;

const customActionBar = new RegisterGroup({
    renderOverlay: register("renderOverlay", () => {
        if (!actionBarText) return;

        const baseY = Renderer.screen.getHeight() - 72;
        const dynamicOffset = isHealthCurrentlyHidden() ? 20 : 0;
        const newTargetY = baseY + dynamicOffset;

        if (newTargetY !== targetY) {
            startY = currentY;
            targetY = newTargetY;
            animStartTime = Date.now();
        }

        const t = Math.min(1, (Date.now() - animStartTime) / 600);

        currentY = startY + (targetY - startY) * easeOutExpo(t);

        const x = (Renderer.screen.getWidth() - Renderer.getStringWidth(actionBarText)) / 2;
        Renderer.drawStringWithShadow(actionBarText, x, currentY);
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
});

Settings.registerListener('Custom Action Bar', v => v ? customActionBar.register() : customActionBar.unregister());

if (Settings.toggleCustomActionBar) {
    customActionBar.register();
}

function easeOutExpo(t) {
    return t === 1 ? 1 : 1 - Math.pow(2, -10 * t);
}