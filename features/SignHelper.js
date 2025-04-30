import Settings from "../config";
import { inHypixel } from "../utils/WorldInfo";
const GuiEditSign = Java.type("net.minecraft.client.gui.inventory.GuiEditSign");
const tileSign = GuiEditSign.class.getDeclaredField("field_146848_f");
tileSign.setAccessible(true);

Settings.registerListener('Sign Helper', value => {
    if (value) {
        guiRender.register();
    } else {
        guiRender.unregister();
    }
})

const guiRender = register('guiRender', (mx, my, gui) => {
    if (!inHypixel()) return
    if (!(gui instanceof GuiEditSign)) return

    const currentTileSign = tileSign.get(Client.currentGui.get());
    if (!currentTileSign?.field_145915_a?.[0]) return;

    const number = currentTileSign.field_145915_a[0].func_150254_d().removeFormatting();
    const value = addCommas(number);
    if (!value) return;

    Renderer.drawString(value, (Renderer.screen.getWidth() - Renderer.getStringWidth(value)) / 2, 55, true);
}).unregister();

if (Settings.toggleSignHelper) {
    guiRender.register();
}

function addCommas(input) {
    const num = Number(input);
    if (isNaN(num)) return '0';

    const [intPart, decPart] = num.toString().split(".");
    const formatted = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, ",");

    return decPart ? `${formatted}.${decPart}` : formatted;
}

