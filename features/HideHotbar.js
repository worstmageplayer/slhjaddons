// Hide Hotbar --------------------------
import Settings from "../config";
let prevHeld;
let lastSwitchTime = 0;
let isXPHidden = false;

Settings.registerListener('Hide Hotbar', value => {
    if (value) {
        hideHotbar.register();
        return;
    }
    hideHotbar.unregister();
    if (isXPHidden) {
        hideExperience.unregister();
        isXPHidden = false;
    }
})

// Hide Hotbar
const hideHotbar = register('renderHotbar', (event) => {
    const currentHeld = Player.getHeldItemIndex();
    const now = Date.now();

    if (prevHeld === undefined) {
        prevHeld = currentHeld;
        lastSwitchTime = now;
        return;
    }

    if (currentHeld !== prevHeld) {
        prevHeld = currentHeld;
        lastSwitchTime = now;
        if (isXPHidden) {
            hideExperience.unregister();
            isXPHidden = false;
        }
        return;
    }

    if (now - lastSwitchTime >= 5000) {
        cancel(event);
        if (!isXPHidden) {
            hideExperience.register();
            isXPHidden = true;
        }
    }
}).unregister();

const hideExperience = register('renderExperience', (event) => {
    cancel(event);
}).unregister();

initialLoad = Settings.toggleHideHotbar;
if (initialLoad) {
    hideHotbar.register();
}