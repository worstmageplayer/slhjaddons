// Custom Scoreboard --------------------
import Settings from '../config';
import { RegisterGroup } from '../utils/RegisterStuff';
import { WorldInfo } from '../utils/WorldInfo';

let scoreboardLines = [];
let scoreboardFiltered = [];
let scoreboardText = '';
let xPos = 0, yPos = 0;
let width = 0, height = 0;
let bgxPos = 0, bgyPos = 0, bgWidth = 0, bgHeight = 0;
let scoreboardScale = Settings.scoreboardScale;
let scoreboardShadow = Settings.toggleScoreboardShadow;
let scoreboardPadding = Settings.scoreboardPadding;
let scoreboardOffset = Settings.scoreboardOffset;
let scoreboardColour = Settings.scoreboardColour;
let scoreboardHeader = Settings.scoreboardHeader;
let scoreboardFooter = Settings.scoreboardFooter;
let bgColour = scoreboardColour.getRGB();
const worldInfo = new WorldInfo

const hiddenLine = '§ewww.hypixel.ne🎂§et';
const scoreboard = new RegisterGroup({
    renderScoreboard: register('renderScoreboard', (event) => cancel(event)).unregister(),
    renderOverlay: register('renderOverlay', () => {
        if (scoreboardLines.length === 0) return;
    
        Renderer.scale(scoreboardScale);
        Renderer.drawRect(bgColour, bgxPos, bgyPos, bgWidth, bgHeight);
        Renderer.scale(scoreboardScale);
        Renderer.drawString(scoreboardText, xPos, yPos, scoreboardShadow);
    }).unregister(),
    step: register('step', () => updateScoreboardLines()).setFps(10).unregister(),
    worldLoad: register('worldLoad', () => Client.scheduleTask(1,() => updateScoreboardLines())).unregister(),
})

// Scoreboard Render update
Settings.registerListener('Scoreboard Scale ', value => scoreboardScale = value)
Settings.registerListener("Scoreboard Shadow", value => scoreboardShadow = value)
Settings.registerListener("Scoreboard Padding", value => scoreboardPadding = value)
Settings.registerListener("Scoreboard Offset", value => scoreboardOffset = value)
Settings.registerListener("Scoreboard Colour", value => {
    scoreboardColour = value;
    bgColour = scoreboardColour.getRGB();
})
Settings.registerListener("Scoreboard Header", value => scoreboardHeader = value)
Settings.registerListener("Scoreboard Footer", value => scoreboardFooter = value)

Settings.registerListener("Custom Scoreboard", value => {
    if (value) {
        scoreboard.register();
        return;
    }
    scoreboard.unregister();
})

if (Settings.toggleCustomScoreboard) {
    scoreboard.register();
}

function updateScoreboardLines() {
    scoreboardLines = Scoreboard.getLines()

    if (scoreboardLines.length === 0) return;

    scoreboardFiltered = scoreboardLines.map(line => line.getName().trim()).reverse().filter(line => line !== hiddenLine);

    const scoreboardFooter = Settings.scoreboardFooter;
    const scoreboardHeader = Settings.scoreboardHeader;
    scoreboardFiltered.unshift(Scoreboard.getTitle().removeFormatting().toLowerCase().includes('skyblock') ? scoreboardHeader : Scoreboard.getTitle());
    scoreboardFiltered.push(scoreboardFooter);
    scoreboardText = scoreboardFiltered.join('\n');

    width = Math.max(...scoreboardFiltered.map(line => Renderer.getStringWidth(line)));
    height = scoreboardFiltered.length * 133/15;
    updateScoreboardPosition();
}

function updateScoreboardPosition() {
    screenWidth = Renderer.screen.getWidth();
    screenHeight = Renderer.screen.getHeight();

    xPos = (screenWidth - scoreboardOffset) / scoreboardScale - width;
    yPos = (screenHeight / scoreboardScale - height) / 2;
    bgxPos = xPos - scoreboardPadding;
    bgyPos = yPos - scoreboardPadding;
    bgWidth = width + 2 * scoreboardPadding + scoreboardOffset;
    bgHeight = height + 2 * scoreboardPadding;
}
