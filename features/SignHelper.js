import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";
import { calculator } from "./chatCalc";

const GuiEditSign = Java.type("net.minecraft.client.gui.inventory.GuiEditSign");
const tileSign = GuiEditSign.class.getDeclaredField("field_146848_f");
tileSign.setAccessible(true);
let lines = result = display = null;

const guiOpened = register('guiOpened', () => {
    Client.scheduleTask(0, () => {
        const gui = Client.currentGui.get()

        if (!(gui instanceof GuiEditSign)) return;
        const tile = tileSign.get(gui);
        lines = tile?.field_145915_a;
        if (!lines?.[0] || lines[1].func_150254_d().removeFormatting() !== "^^^^^^^^^^^^^^^") return;

        signHelper.register();
    })
}).unregister()

const signHelper = new RegisterGroup({
    guiKey: register('guiKey', () => {
        Client.scheduleTask(0, () => {
            const raw = lines[0].func_150254_d().removeFormatting().trim();
            if (!raw) {
                display = null;
                return;
            }
            result = calculator(raw)
            display = result == raw ? addCommas(result.toString()) : `${raw} = ${addCommas(result.toString())}`
        })
    }).unregister(),

    guiRender: register('guiRender', () => {
        if (!display) return;
        Renderer.drawString(display, (Renderer.screen.getWidth() - Renderer.getStringWidth(display)) / 2, 55, true);
    }).unregister(),

    guiClosed: register('guiClosed', () => {
        if (!isNaN(result)) lines[0] = new net.minecraft.util.IChatComponent.Serializer().func_150699_a(`"${result}"`);
    
        lines = result = display = null;
        signHelper.unregister();
    }).unregister()
})

Settings.registerListener("Sign Helper", v => v ? guiOpened.register() : guiOpened.unregister());
if (Settings.toggleSignHelper) guiOpened.register();

function addCommas(input) {
    const num = Number(input);
    if (isNaN(num)) return "0";
    const [intPart, decPart] = num.toString().split(".");
    return intPart.replace(/\B(?=(\d{3})+(?!\d))/g, ",") + (decPart ? `.${decPart}` : "");
}