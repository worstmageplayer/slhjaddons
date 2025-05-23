import Settings from "../config";
import { isHealthCurrentlyHidden } from "../features/HideHealth";
import { RegisterGroup } from "../utils/RegisterStuff";

const S02PacketChat = Java.type('net.minecraft.network.play.server.S02PacketChat');

let actionBarText = "";
let prevText = "";
let animationStartTime = null;
let yPos = 0;
let startY = 0;
let targetY = 0;
let xPos = 0;
let prevHealthHidden = false;

const customActionBar = new RegisterGroup({
    tick: register("tick", () => {
        if (!actionBarText) return;

        const screenHeight = Renderer.screen.getHeight();
        const baseY = screenHeight - 72;
        const healthHidden = isHealthCurrentlyHidden();
        const newTargetY = baseY + (healthHidden ? 20 : 0);

        const needsAnimation = newTargetY !== targetY || healthHidden !== prevHealthHidden;
        if (needsAnimation) {
            prevHealthHidden = healthHidden;
            targetY = newTargetY;
            startY = yPos;
            animationStartTime = Date.now();
        }

        if (animationStartTime) {
            const t = (Date.now() - animationStartTime) / 600;
            if (t < 1) {
                yPos = startY + (targetY - startY) * easeOutExpo(t);
            } else {
                yPos = targetY;
                animationStartTime = null;
            }
        }

        if (actionBarText !== prevText) {
            prevText = actionBarText;
            xPos = (Renderer.screen.getWidth() - Renderer.getStringWidth(actionBarText)) / 2;
        }
    }),

    renderOverlay: register("renderOverlay", () => {
        if (!actionBarText) return;
        Renderer.drawString(actionBarText, xPos, yPos, true);
    }).unregister(),

    packetReceived: register("packetReceived", (packet) => {
        if (packet.func_179841_c() !== 2) return;
        const newText = packet.func_148915_c().text;
        if (newText !== actionBarText) actionBarText = newText;
    }).setFilteredClass(S02PacketChat).unregister(),

    actionBar: register("actionBar", (text, event) => cancel(event)).setCriteria("${text}").unregister(),

    worldUnload: register("worldUnload", () => {
        actionBarText = "";
        prevText = "";
        yPos = Renderer.screen.getHeight() - 72;
        xPos = 0;
        animationStartTime = null;
    }).unregister()
});

Settings.registerListener("Custom Action Bar", v => v ? customActionBar.register() : customActionBar.unregister());
if (Settings.toggleCustomActionBar) customActionBar.register();

function easeOutExpo(t) {
    return t >= 1 ? 1 : 1 - Math.pow(2, -10 * t);
}