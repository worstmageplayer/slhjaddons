// Clock --------------------------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

let xPos;
let yPos;
let now = new Date();
let currentTime = `${now.getHours()} : ${now.getMinutes().toString().padStart(2, '0')}`;

Settings.registerListener('Ingame Clock', v => v ? clock.register() : clock.unregister());

const clock = new RegisterGroup({
    step: register('step', () => {
        const now = new Date();
        currentTime = `${now.getHours()} : ${now.getMinutes().toString().padStart(2, '0')}`;
        updateClockPosition();
    }).setFps(2).unregister(),

    renderOverlay: register('renderOverlay', () => {
        if (!currentTime || !xPos || !yPos) return;
        const time = new Text(currentTime, xPos, yPos).setShadow(true).setColor(Settings.clockColor.getRGB())
        Renderer.scale(Settings.clockScale);
        time.draw();
    }).unregister()
})

if (Settings.toggleClock) {
    clock.register();
}

function updateClockPosition() {
    const screenWidth = Renderer.screen.getWidth();
    const width = Renderer.getStringWidth(currentTime);
    const clockPadding = Settings.clockPadding;
    const clockScale = Settings.clockScale;

    const positions = [
        { xPos: clockPadding , yPos: clockPadding },
        { xPos: screenWidth - width * clockScale - clockPadding, yPos: clockPadding }
    ];

    const position = positions[Settings.clockPosition] || positions[0];
    xPos = position.xPos / clockScale;
    yPos = position.yPos / clockScale
}