// Golden Fish --------------------------
import Settings from "../config";

// Golden Fish
const goldenFishAlert = register("Chat", (message, event) => {
    Renderer.drawString(
        '&6Golden Fish&r', 
        Renderer.screen.getWidth()/2 - Renderer.getStringWidth('&6Golden Fish&r')/2, 
        Renderer.screen.getHeight()/2, 
        true
    );
}).setCriteria('You spot a Golden Fish surface from beneath the lava!').unregister();

Settings.registerListener('Golden Fish Alert', v => v ? goldenFishAlert.register() : goldenFishAlert.unregister());

if (Settings.toggleGFAlert) {
    goldenFishAlert.register();
}

//&r&9You spot a &r&6Golden Fish &r&9surface from beneath the lava!&r