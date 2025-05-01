// Cancel Slot Highlight ----------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

let slotHighlightColour = Renderer.color(255, 255, 255, 170)
let renderCustomSlotHighlight = Settings.toggleCustomSlotHighlight;

Settings.registerListener('Custom Slot Highlight', value => {
    renderCustomSlotHighlight = value;
})
Settings.registerListener('Hide Slot Highlight', v => v ? cancelSlotHighlight.register() : cancelSlotHighlight.unregister());

const cancelSlotHighlight = new RegisterGroup({
    renderSlotHighlight: register('renderSlotHighlight', (mx, my, slot, container, event) => cancel(event)).unregister(),
    preItemRender: register('preItemRender', (mx, my, slot, container) => {
        if (!renderCustomSlotHighlight) return;
        const itemName = slot?.func_75211_c()?.func_82833_r();
        if (itemName === ' ') return;
    
        const slotX = slot.field_75223_e;
        const slotY = slot.field_75221_f;
        drawHollowRect(slotHighlightColour, slotX, slotY, 16, 16, 1.2);
    }).unregister()
})

if (Settings.toggleCancelSlotHighlight) {
    cancelSlotHighlight.register();
}

function drawHollowRect(color, x, y, width, height, thickness = 1) {
    Renderer.drawRect(color, x, y, width, thickness);
    Renderer.drawRect(color, x, y + height - thickness, width, thickness);
    Renderer.drawRect(color, x, y + thickness, thickness, height - 2 * thickness);
    Renderer.drawRect(color, x + width - thickness, y + thickness, thickness, height - 2 * thickness);
}
