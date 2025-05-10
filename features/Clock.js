// Clock --------------------------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

let screenWidth = Renderer.screen.getWidth();
let clockColour = Settings.clockColor;

let clockPadding = Settings.clockPadding;
let clockScale = Settings.clockScale;
let clockPosition = Settings.clockPosition;

let xPos;
let yPos;
let now = new Date();
let currentTime = `${now.getHours()} : ${now.getMinutes().toString().padStart(2, '0')}`;
let width = Renderer.getStringWidth(currentTime);

updateClockPosition();

// Update clock info
Settings.registerListener('Clock Position', value => {
    clockPosition = value;
    updateClockPosition()
})
Settings.registerListener('Clock Scale', value => clockScale = value)
Settings.registerListener('Clock Padding', value => clockPadding = value)
Settings.registerListener('Clock Colour', value => clockColour = value)

Settings.registerListener('Ingame Clock', v => v ? clock.register() : clock.unregister());

const clock = new RegisterGroup({
    step: register('step', () => {
        const now = new Date();
        currentTime = `${now.getHours()} : ${now.getMinutes().toString().padStart(2, '0')}`;
        updateClockPosition();
    }).setFps(2).unregister(),

    renderOverlay: register('renderOverlay', () => {
        if (!currentTime || !xPos || !yPos) return;
        const time = new Text(currentTime, xPos, yPos).setShadow(true).setColor(clockColour.getRGB())
        Renderer.scale(clockScale);
        time.draw();
    }).unregister()
})

if (Settings.toggleClock) {
    clock.register();
}

function updateClockPosition() {
    screenWidth = Renderer.screen.getWidth();
    width = Renderer.getStringWidth(currentTime);

    const positions = [
        { xPos: clockPadding / clockScale, yPos: clockPadding / clockScale },
        { xPos: (screenWidth - width * clockScale - clockPadding) / clockScale, yPos: clockPadding / clockScale }
    ];

    const position = positions[clockPosition] || positions[0];
    xPos = position.xPos;
    yPos = position.yPos;
}