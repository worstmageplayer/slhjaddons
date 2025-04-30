import Settings from "../config";

const containerList = ['Trades', 'Your Equipment and Stats']

Settings.registerListener('Shift Click', value => {
    if (value) {
        shiftClick.register();
    } else {
        shiftClick.unregister();
    }
})

const shiftClick = register('guiMouseClick', (mx, my, btn, gui, event) => {
    const container = Player.getContainer()
    const guiName = container?.getName();
    if (!containerList.includes(guiName)) return;
    const slotIndex = Client.currentGui.getSlotUnderMouse()?.getIndex();
    if (!slotIndex || slotIndex < 54) return;
    cancel(event)
    container.click(slotIndex, true, 'LEFT');
}).unregister();

if (Settings.toggleShiftClick) {
    shiftClick.register();
};
