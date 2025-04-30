// Hide Health --------------------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";
let lastChangeTime = Date.now();

let isHealthHidden = false;

const hideHealth = new RegisterGroup({
    renderHealth: register('renderHealth', (event) => {
        const currentHealth = Player.getHP();
        const now = Date.now();
    
        if (currentHealth < 40) {
            lastChangeTime = now;
            isHealthHidden = false;
            return;
        }
    
        if (now - lastChangeTime >= 3000 && currentHealth === 40) {
            cancel(event);
            isHealthHidden = true;
        }
    }).unregister(),

    renderMountHealth: register('renderMountHealth', (event) => cancel(event)).unregister(),
    renderArmor: register('renderArmor', (event) => cancel(event)).unregister(),
    renderFood: register('renderFood', (event) => cancel(event)).unregister(),
})

Settings.registerListener('Hide Health', value => {
    if (value) {
        hideHealth.register();
        return;
    }
    hideHealth.unregister();
    isHealthHidden = false;
});

if (Settings.toggleHideHealth) {
    hideHealth.register();
};

export function isHealthCurrentlyHidden() {
    return isHealthHidden;
}
