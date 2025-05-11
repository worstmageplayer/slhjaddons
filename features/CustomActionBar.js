// Custom Action Bar --------------------
import Settings from "../config";
import { isHealthCurrentlyHidden } from "../features/HideHealth";
import { RegisterGroup } from "../utils/RegisterStuff";

const S02PacketChat = Java.type('net.minecraft.network.play.server.S02PacketChat');

let actionBarText;
let animStartTime;
let startY;
let targetY;
let previousHealthHidden = false;
let currentY = Renderer.screen.getHeight() - 72;

const customActionBar = new RegisterGroup({
    renderOverlay: register("renderOverlay", () => {
        if (!actionBarText) return;

        const baseY = Renderer.screen.getHeight() - 72;
        const healthHidden = isHealthCurrentlyHidden();
        const newTargetY = baseY + (healthHidden ? 20 : 0);

        if (newTargetY !== targetY || healthHidden !== previousHealthHidden) {
            previousHealthHidden = healthHidden;
            startY = currentY;
            targetY = newTargetY;
            animStartTime = Date.now();
        }

        if (animStartTime) {
            const t = Math.min(1, (Date.now() - animStartTime) / 600);
            currentY = t < 1
                ? startY + (targetY - startY) * easeOutExpo(t)
                : (animStartTime = null, targetY);
        }

        const x = (Renderer.screen.getWidth() - Renderer.getStringWidth(actionBarText)) / 2;
        Renderer.drawStringWithShadow(actionBarText, x, currentY);
    }).unregister(),

    packetReceived: register('packetReceived', (packet, event) => {
        if (packet.func_179841_c() !== 2) return;
        actionBarText = packet.func_148915_c().text;
    }).setFilteredClass(S02PacketChat).unregister(),

    actionBar: register('actionBar', (text, event) => {
        cancel(event);
    }).setCriteria('${text}').unregister(),

    worldUnload: register('worldUnload', () => {
        actionBarText = null;
        currentY = Renderer.screen.getHeight() - 72;
    }).unregister()
});

Settings.registerListener('Custom Action Bar', v => v ? customActionBar.register() : customActionBar.unregister());

if (Settings.toggleCustomActionBar) {
    customActionBar.register();
}

function easeOutExpo(t) {
    return t === 1 ? 1 : 1 - Math.pow(2, -10 * t);
}
