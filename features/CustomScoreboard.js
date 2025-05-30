// Custom Scoreboard --------------------
import Settings from '../config';
import { RegisterGroup } from '../utils/RegisterStuff';

let scoreboardText = null;

let xPos = 0, yPos = 0;
let width = 0, height = 0;
let bgxPos = 0, bgyPos = 0, bgWidth = 0, bgHeight = 0;

const hiddenLine = '§ewww.hypixel.ne🎂§et';
const S3EPacketTeams = Java.type('net.minecraft.network.play.server.S3EPacketTeams')

const scoreboard = new RegisterGroup({
    renderScoreboard: register('renderScoreboard', (event) => cancel(event)).unregister(),
    renderOverlay: register('renderOverlay', () => {
        if (!scoreboardText) return;
        Renderer.retainTransforms(true);
        Renderer.scale(Settings.scoreboardScale);
        Renderer.drawRect(Settings.scoreboardColour.getRGB(), bgxPos, bgyPos, bgWidth, bgHeight);
        Renderer.drawString(scoreboardText, xPos, yPos, Settings.toggleScoreboardShadow);
        Renderer.retainTransforms(false);
    }).unregister(),
    packetReceived: register('packetReceived', () => updateScoreboardLines()).setFilteredClass(S3EPacketTeams).unregister(),
    worldLoad: register('worldLoad', () => Client.scheduleTask(10, () => updateScoreboardLines())).unregister(),
    worldUnload: register('worldUnload', () => scoreboardText = '').unregister(),
})

Settings.registerListener('Custom Scoreboard', v => v ? scoreboard.register() : scoreboard.unregister());

if (Settings.toggleCustomScoreboard) scoreboard.register();

function updateScoreboardLines() {
    const scoreboardLines = Scoreboard.getLines();
    if (scoreboardLines.length === 0) return scoreboardText = null;

    const scoreboardFiltered = scoreboardLines.map(line => line.getName().trim()).reverse().filter(line => line !== hiddenLine);

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
    
    xPos = (screenWidth - Settings.scoreboardOffset) / Settings.scoreboardScale - width;
    yPos = (screenHeight / Settings.scoreboardScale - height) / 2;

    const pad = Settings.scoreboardPadding;
    bgxPos = xPos - pad;
    bgyPos = yPos - pad;
    bgWidth = width + 2 * pad + Settings.scoreboardOffset;
    bgHeight = height + 2 * pad;
}