import Settings from "../config";

let prevHeld = null;
let lastSwitchTime = 0;
let isHidden = false;

const hideHotbar = register('renderHotbar', (event) => {
    const currentHeld = Player.getHeldItemIndex();
    const now = Date.now();

    if (prevHeld === null || currentHeld !== prevHeld) {
        prevHeld = currentHeld;
        lastSwitchTime = now;
    
        if (isHidden) {
            hideExperience.unregister();
            isHidden = false;
        }
        return;
    }
    
    if (now - lastSwitchTime < 5000) return;
    
    cancel(event);
    
    if (!isHidden) {
        hideExperience.register();
        isHidden = true;
    }
    

}).unregister();

const hideExperience = register('renderExperience', (event) => {
    cancel(event);
}).unregister();

Settings.registerListener('Hide Hotbar', value => {
    if (value) {
        hideHotbar.register();
    } else {
        hideHotbar.unregister();
        hideExperience.unregister();
        isHidden = false;
        prevHeld = null;
    }
});

if (Settings.toggleHideHotbar) {
    hideHotbar.register();
}