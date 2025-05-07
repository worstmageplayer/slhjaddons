// Custom Scoreboard --------------------
import Settings from '../config';
import { RegisterGroup } from '../utils/RegisterStuff';

let scoreboardLines = [];
let scoreboardFiltered = [];
let scoreboardText = '';

let scoreboardScale = Settings.scoreboardScale;
let scoreboardShadow = Settings.toggleScoreboardShadow;
let scoreboardPadding = Settings.scoreboardPadding;
let scoreboardOffset = Settings.scoreboardOffset;
let scoreboardColour = Settings.scoreboardColour.getRGB();

let xPos = 0, yPos = 0;
let width = 0, height = 0;
let bgxPos = 0, bgyPos = 0, bgWidth = 0, bgHeight = 0;

const hiddenLine = '§ewww.hypixel.ne🎂§et';
const S3EPacketTeams = Java.type('net.minecraft.network.play.server.S3EPacketTeams')

const scoreboard = new RegisterGroup({
    renderScoreboard: register('renderScoreboard', (event) => cancel(event)).unregister(),
    renderOverlay: register('renderOverlay', () => {
        if (!scoreboardText) return;
    
        Renderer.scale(scoreboardScale);
        Renderer.drawRect(scoreboardColour, bgxPos, bgyPos, bgWidth, bgHeight);
        Renderer.scale(scoreboardScale);
        Renderer.drawString(scoreboardText, xPos, yPos, scoreboardShadow);
    }).unregister(),
    packetReceived: register('packetReceived', () => updateScoreboardLines()).setFilteredClass(S3EPacketTeams).unregister(),
    worldLoad: register('worldLoad', () => Client.scheduleTask(10, () => updateScoreboardLines())).unregister(),
    worldUnload: register('worldUnload', () => scoreboardText = '').unregister(),
})

// Scoreboard Render update
Settings.registerListener('Scoreboard Scale ', value => scoreboardScale = value)
Settings.registerListener("Scoreboard Shadow", value => scoreboardShadow = value)
Settings.registerListener("Scoreboard Padding", value => scoreboardPadding = value)
Settings.registerListener("Scoreboard Offset", value => scoreboardOffset = value)
Settings.registerListener("Scoreboard Colour", value => scoreboardColour = value.getRGB())
Settings.registerListener('Custom Scoreboard', v => v ? scoreboard.register() : scoreboard.unregister());

if (Settings.toggleCustomScoreboard) {
    scoreboard.register();
}

function updateScoreboardLines() {
    scoreboardLines = Scoreboard.getLines();
    if (scoreboardLines.length === 0) {
        scoreboardText = '';
        return;
    }

    scoreboardFiltered = scoreboardLines.map(line => line.getName().trim()).reverse().filter(line => line !== hiddenLine);

    const title = Scoreboard.getTitle().removeFormatting();
    const header = title.toLowerCase().includes('skyblock') ? Settings.scoreboardHeader : title;
    
    scoreboardFiltered.unshift(header);
    scoreboardFiltered.push(Settings.scoreboardFooter);
    scoreboardText = scoreboardFiltered.join('\n');

    width = Math.max(...scoreboardFiltered.map(line => Renderer.getStringWidth(line)));
    height = scoreboardFiltered.length * 133 / 15;

    updateScoreboardPosition();
}

const GameSettings = Client.getMinecraft().field_71474_y;

function updateScoreboardPosition() {
    const guiScale = GameSettings.field_74335_Z
    const rendererScale = Renderer.screen.getScale() // Patcher inventory scale thing
    const screenWidth = Renderer.screen.getWidth() * rendererScale / guiScale;
    const screenHeight = Renderer.screen.getHeight() * rendererScale / guiScale;
    
    xPos = (screenWidth - scoreboardOffset) / scoreboardScale - width;
    yPos = (screenHeight / scoreboardScale - height) / 2;
    bgxPos = xPos - scoreboardPadding;
    bgyPos = yPos - scoreboardPadding;
    bgWidth = width + 2 * scoreboardPadding + scoreboardOffset;
    bgHeight = height + 2 * scoreboardPadding;
}