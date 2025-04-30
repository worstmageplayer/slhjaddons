import Settings from "../config";
import { inHypixel } from "../utils/WorldInfo";

const GuiEditSign = Java.type("net.minecraft.client.gui.inventory.GuiEditSign");
const tileSign = GuiEditSign.class.getDeclaredField("field_146848_f");
tileSign.setAccessible(true);

const guiRender = register('guiRender', (mx, my, gui) => {
    if (!inHypixel() || !(gui instanceof GuiEditSign)) return;

    const tile = tileSign.get(Client.currentGui.get());
    const lines = tile?.field_145915_a;
    if (!lines?.[0] || lines[2].func_150254_d().removeFormatting() !== "Enter amount") return;

    const raw = lines[0].func_150254_d().removeFormatting();
    const formatted = addCommas(raw);
    Renderer.drawString(formatted, (Renderer.screen.getWidth() - Renderer.getStringWidth(formatted)) / 2, 55, true);
}).unregister();

Settings.registerListener("Sign Helper", v => v ? guiRender.register() : guiRender.unregister());

if (Settings.toggleSignHelper) guiRender.register();

function addCommas(input) {
    const num = Number(input);
    if (isNaN(num)) return "0";

    const [intPart, decPart] = num.toString().split(".");
    return intPart.replace(/\B(?=(\d{3})+(?!\d))/g, ",") + (decPart ? `.${decPart}` : "");
}