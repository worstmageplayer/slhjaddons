import Settings from "../config";
import { calculator } from "./chatCalc";

const GuiEditSign = Java.type("net.minecraft.client.gui.inventory.GuiEditSign");
const tileSign = GuiEditSign.class.getDeclaredField("field_146848_f");
tileSign.setAccessible(true);
let lines = null;
let result = null;

const guiOpened = register('guiOpened', () => {
    Client.scheduleTask(10, () => {
        const gui = Client.currentGui.get()

        if (!(gui instanceof GuiEditSign)) return;
        const tile = tileSign.get(Client.currentGui.get());
        lines = tile?.field_145915_a;
        if (!lines?.[0] || lines[1].func_150254_d().removeFormatting() !== "^^^^^^^^^^^^^^^") return;

        guiRender.register();
        guiClosed.register();
    })
}).unregister()

const guiRender = register('guiRender', (mx, my, gui) => {
    const raw = lines[0].func_150254_d().removeFormatting().trim();
    if (!raw) return;
    result = calculator(raw)
    const display = result == raw ? addCommas(result.toString()) : `${raw} = ${addCommas(result.toString())}`

    Renderer.drawString(display, (Renderer.screen.getWidth() - Renderer.getStringWidth(display)) / 2, 55, true);
}).unregister()

const guiClosed = register('guiClosed', (gui) => {
    if (!isNaN(result)) lines[0] = new net.minecraft.util.IChatComponent.Serializer().func_150699_a(`"${result}"`);

    lines = null;
    result = null;
    guiRender.unregister();
    guiClosed.unregister();
}).unregister()

Settings.registerListener("Sign Helper", v => v ? guiOpened.register() : guiOpened.unregister());
if (Settings.toggleSignHelper) guiOpened.register();

function addCommas(input) {
    const num = Number(input);
    if (isNaN(num)) return "0";
    const [intPart, decPart] = num.toString().split(".");
    return intPart.replace(/\B(?=(\d{3})+(?!\d))/g, ",") + (decPart ? `.${decPart}` : "");
}