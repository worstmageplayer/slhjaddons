import Settings from "../config";
import { data } from "../data"
import { GuiHandler } from "../utils/GuiHandler";
import { getCooldownMultiplier } from "../utils/MageCooldownMultiplier";

const baseCooldown = 20000;
let ragnarockCooldown = baseCooldown;
let timeOfCast = 0;
let startCast = false;
let inCooldown = false;

const moveGui = new GuiHandler(data.moveRagnarockCooldownGui);

Settings.moveRagnarockCooldownGui.registerDraw(() => {
    Renderer.drawString('Editing Gui', Renderer.screen.getWidth()/2 - Renderer.getStringWidth('Editing Gui')/2, 10, true)
    Renderer.scale(data.moveRagnarockCooldownGui.scale)
    Renderer.drawString('20.0', data.moveRagnarockCooldownGui.x, data.moveRagnarockCooldownGui.y, true);
})

Settings.moveRagnarockCooldownGui.registerOpened(() => {
    moveGui.register()
    handlers.renderCooldown.unregister();
});

Settings.moveRagnarockCooldownGui.registerClosed(() => {
    moveGui.unregister()
    data.save();
    if (startCast) {handlers.renderCooldown.register()}
});

const handlers = {
    ragCast: register("actionBar", (number) => {
        handlers.ragCancel.register();
        if (startCast) return;
        startCooldown();
    }).setCriteria('CASTING ${*} ${number}s').setParameter('contains').unregister(),

    ragCancel: register("chat", () => {
        timeOfCast = Date.now();
    }).setCriteria("Ragnarock was cancelled due to taking damage!").setParameter("contains").unregister(),

    worldLoad: register("worldLoad", () => {
        resetCooldown();
    }).unregister(),

    renderCooldown: register("renderOverlay", () => {
        const timeLeft = ((ragnarockCooldown - Date.now() + timeOfCast) / 1000).toFixed(1);
        Renderer.scale(data.moveRagnarockCooldownGui.scale)
        Renderer.drawString(timeLeft, data.moveRagnarockCooldownGui.x, data.moveRagnarockCooldownGui.y, true);
    }).unregister()
};

Settings.registerListener("Display Ragnarock Cooldown", value => {
    if (value) {
        handlers.ragCast.register();
        handlers.worldLoad.register();
    } else {
        handlers.ragCast.unregister();
        handlers.worldLoad.unregister();
    }
});

if (Settings.toggleRagCooldown) {
    handlers.ragCast.register();
    handlers.worldLoad.register();
}

function startCooldown() {
    timeOfCast = Date.now();
    startCast = true;
    inCooldown = true;
    ragnarockCooldown = baseCooldown * getCooldownMultiplier();
    handlers.renderCooldown.register();

    setTimeout(() => {
        endCooldown();
    }, ragnarockCooldown);
}

function endCooldown() {
    handlers.renderCooldown.unregister();
    handlers.ragCancel.unregister();
    startCast = false;
    if (!inCooldown) return;
    if (Settings.toggleRagCooldownSound) {World.playSound('random.successful_hit', 1, 1)}
    ChatLib.chat(Settings.ragCooldownEndMessage);
    inCooldown = false;
}

function resetCooldown() {
    ragnarockCooldown = baseCooldown;
    handlers.renderCooldown.unregister();
    handlers.ragCancel.unregister();
    startCast = false;
    inCooldown = false;
}