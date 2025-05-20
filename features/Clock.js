// Clock --------------------------------
import Settings from "../config";
import { RegisterGroup } from "../utils/RegisterStuff";

let xPos, yPos;
let now = new Date();
let currentTime = `${now.getHours()} : ${now.getMinutes().toString().padStart(2, '0')}`;
const time = new Text('', 0, 0).setShadow(true);

Settings.registerListener('Ingame Clock', v => v ? clock.register() : clock.unregister());

const clock = new RegisterGroup({
    step: register('step', () => {
        const now = new Date();
        currentTime = `${now.getHours()} : ${now.getMinutes().toString().padStart(2, '0')}`;
        updateClockPosition();
        time.setString(currentTime).setX(xPos).setY(yPos).setColor(Settings.clockColor.getRGB());
    }).setFps(5).unregister(),

    renderOverlay: register('renderOverlay', () => {
        if (!currentTime || !xPos || !yPos) return;
        Renderer.scale(Settings.clockScale);
        time.draw();
    }).unregister()
})

if (Settings.toggleClock) clock.register();

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