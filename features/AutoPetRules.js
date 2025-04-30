// Auto Pet Rules Title -----------------
import Settings from "../config";
import { data } from "../data"
import { GuiHandler } from "../utils/GuiHandler";

let pet;
let rendering = false;

const moveGui = new GuiHandler(data.moveAutoPetRuleDisplay);

Settings.moveAutoPetRuleDisplay.registerDraw(() => {
    Renderer.drawString('Editing Gui', Renderer.screen.getWidth()/2 - Renderer.getStringWidth('Editing Gui')/2, 10, true)
    Renderer.scale(data.moveAutoPetRuleDisplay.scale)
    Renderer.drawString('§6PET NAME', data.moveAutoPetRuleDisplay.x, data.moveAutoPetRuleDisplay.y, true);
})

Settings.moveAutoPetRuleDisplay.registerOpened(() => {
    moveGui.register();
    petRuleRender.unregister();
});

Settings.moveAutoPetRuleDisplay.registerClosed(() => {
    moveGui.unregister();
    data.save();
    if (rendering) petRuleRender.register();
});

Settings.registerListener('Display Auto Pet Rules', value => {
    if (value) {
        petRule.register();
        return;
    }
    petRule.unregister();
})

const petRule = register('chat', (event) => {
    const chat = ChatLib.getChatMessage(event, true);
    const regex = /\[Lvl (\d+)](.*?)!/;
    const match = regex.exec(chat);
    if (Settings.toggleCancelAutoPetRuleMessage) cancel(event);
    if (!match) return; 
    pet = {
        level: match[1],
        name: match[2].trim()
    };
    fadeAlpha = 255;
    fadeStartTime = Date.now();
    petRuleRender.register();
    
}).setCriteria('Autopet equipped your ${*} VIEW RULE').setParameter('contains').unregister();

const petRuleRender = register('renderOverlay', () => {
    if (!pet || !pet.name) return;
    rendering = true;

    const elapsed = Date.now() - fadeStartTime;
    const duration = 5000;
    const t = Math.min(1, elapsed / duration);
    fadeAlpha = 255 * (1 - fadeOut(t));

    if (fadeAlpha <= 0) {
        petRuleRender.unregister();
        rendering = false;
        return;
    }
    
    text = new Text(pet.name, data.moveAutoPetRuleDisplay.x, data.moveAutoPetRuleDisplay.y).setShadow(true).setColor(Renderer.color(255, 255, 255, fadeAlpha))
    Renderer.scale(data.moveAutoPetRuleDisplay.scale);
    text.draw();
}).unregister(); 

function fadeOut(t) {
    if (t < 0.7) return 0;
    return Math.pow((t - 0.7) / 0.3, 5);
}

initialLoad = Settings.toggleAutoPetRuleDisplay
if (initialLoad) {
    petRule.register();
}
