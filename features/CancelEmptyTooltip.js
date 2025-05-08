// Cancel Empty Tooltip -----------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

let itemName = null;

const blackList = ["Spirit Leap", "Teleport to Player"];
const guiChest = Java.type('net.minecraft.client.gui.inventory.GuiChest');

const cancelItemToolTip = new RegisterGroup ({
    itemTooltip: register('itemTooltip', (lore, item, event) => {
        itemName = item.getName();
        if (itemName !== ' ') return;
        cancel(event);
    }).unregister(),

    guiMouseClick: register('guiMouseClick', (mx, my, btn, gui, event) => 
        gui instanceof guiChest && 
        !blackList.includes(Player.getContainer().getName()) && 
        itemName === ' ' && 
        cancel(event)
    ).unregister(),

    guiClosed: register('guiClosed', () => itemName = null).unregister(),
})

Settings.registerListener('Hide Empty Tooltip', v => v ? cancelItemToolTip.register() : cancelItemToolTip.unregister());

if (Settings.toggleCancelEmptyTooltip) {
    cancelItemToolTip.register();
}