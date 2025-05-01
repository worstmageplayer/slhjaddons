import Settings from "../config";

const containerList = ['Trades', 'Your Equipment and Stats']
const guiContainer = Java.type('net.minecraft.client.gui.inventory.GuiContainer')

const shiftClick = register('guiMouseClick', (mx, my, btn, gui, event) => {
    if (!(gui instanceof guiContainer)) return;
    const container = Player.getContainer()
    const guiName = container?.getName();
    if (!containerList.includes(guiName)) return;
    const slotIndex = Client.currentGui.getSlotUnderMouse()?.getIndex();
    if (!slotIndex || slotIndex < 54) return;
    cancel(event)
    container.click(slotIndex, true, 'LEFT');
}).unregister();

Settings.registerListener('Shift Click', v => v ? shiftClick.register() : shiftClick.unregister());

if (Settings.toggleShiftClick) {
    shiftClick.register();
};