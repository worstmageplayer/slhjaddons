// Cancel Slot Highlight ----------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";
import { drawHollowRect } from "../utils/RendererStuff";

Settings.registerListener('Hide Slot Highlight', v => v ? cancelSlotHighlight.register() : cancelSlotHighlight.unregister());

const cancelSlotHighlight = new RegisterGroup({
    renderSlotHighlight: register('renderSlotHighlight', (mx, my, slot, container, event) => cancel(event)).unregister(),
    preItemRender: register('preItemRender', (mx, my, slot, container) => {
        const itemName = slot?.func_75211_c()?.func_82833_r();
        if (itemName === ' ') return;
    
        const slotX = slot.field_75223_e;
        const slotY = slot.field_75221_f;
        drawHollowRect(Renderer.color(255, 255, 255, 170), slotX, slotY, 16, 16, 1.2);
    }).unregister()
})

if (Settings.toggleCancelSlotHighlight) cancelSlotHighlight.register();