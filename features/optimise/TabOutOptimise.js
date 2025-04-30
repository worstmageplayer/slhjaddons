// Tab Out Optimise ---------------------
import Settings from "../../config";

const isActive = org.lwjgl.opengl.Display.isActive;
let wasTabbedIn = true;

let gameSettings = Client.getMinecraft().field_71474_y;
let initialRenderDistance = gameSettings.field_151451_c;
let initialFrameRate = gameSettings.field_74350_i;

let tabOutTime = 0
let appliedOptimise = false

Settings.registerListener('Optimise Tab Out', value => {
    if (value) {
        tabOutOptimise.register();
        gameClose.register();
    } else {
        tabOutOptimise.unregister();
        gameClose.unregister();
    }
});

const tabOutOptimise = register("step", () => {
    isTabbedIn = isActive();

    if (isTabbedIn) {
        if (wasTabbedIn && !appliedOptimise) return;

        gameSettings.field_151451_c = initialRenderDistance;
        gameSettings.field_74350_i = initialFrameRate;
        renderEntity.unregister();
        renderTileEntity.unregister();
        wasTabbedIn = true;
        appliedOptimise = false;
        return;
    }

    if (wasTabbedIn) {
        tabOutTime = Date.now();
        initialRenderDistance = gameSettings.field_151451_c;
        initialFrameRate = gameSettings.field_74350_i;
        wasTabbedIn = false;
        appliedOptimise = false;
        return;
    }

    if (appliedOptimise || Date.now() - tabOutTime <= 3000) return;

    gameSettings.field_151451_c = 1;
    gameSettings.field_74350_i = 5;
    renderEntity.register();
    renderTileEntity.register();
    appliedOptimise = true;
}).setFps(2).unregister();

const gameClose = register('gameUnload', () => {
    gameSettings.field_151451_c = initialRenderDistance;
    gameSettings.field_74350_i = initialFrameRate;
}).unregister();

const renderEntity = register('renderEntity', (entity, pos, pt, event) => cancel(event)).unregister();
const renderTileEntity = register('renderTileEntity', (entity, pos, pt, event) => cancel(event)).unregister();

if (Settings.toggleOptimiseTabOut) {
    tabOutOptimise.register();
    gameClose.register();
}