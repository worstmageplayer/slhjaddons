// Hide Health --------------------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

let lastHealthVisibleTime = Date.now();
let isHealthHidden = false;

const HIDE_HEALTH_DELAY_MS = 3000;
const LOW_HEALTH_THRESHOLD = 40;

function shouldHideHealth(currentHealth, now) {
    const timeSinceLastChange = now - lastHealthVisibleTime;
    return currentHealth === LOW_HEALTH_THRESHOLD && timeSinceLastChange >= HIDE_HEALTH_DELAY_MS;
}

const hideHealth = new RegisterGroup({
    renderHealth: register('renderHealth', (event) => {
        const currentHealth = Player.getHP();
        const now = Date.now();

        if (currentHealth < LOW_HEALTH_THRESHOLD) {
            lastHealthVisibleTime = now;
            isHealthHidden = false;
            return;
        }

        if (shouldHideHealth(currentHealth, now)) {
            cancel(event);
            isHealthHidden = true;
        }
    }).unregister(),

    renderMountHealth: register('renderMountHealth', (event) => cancel(event)).unregister(),
    renderArmor: register('renderArmor', (event) => cancel(event)).unregister(),
    renderFood: register('renderFood', (event) => cancel(event)).unregister(),
});

Settings.registerListener('Hide Health', (enabled) => {
    if (enabled) {
        hideHealth.register();
    } else {
        hideHealth.unregister();
        isHealthHidden = false;
    }
});

if (Settings.toggleHideHealth) hideHealth.register();

export function isHealthCurrentlyHidden() {
    return isHealthHidden;
}