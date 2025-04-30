import Settings from "../config"

const playersPerColumn = 20;
const columnWidth = 150;
const lineHeight = 10;
const maxColumns = 4;
const maxPlayers = 80;
//toggleCustomPlayerList
/*
const S38PacketPlayerListItem = Java.type('net.minecraft.network.play.server.S38PacketPlayerListItem')

register('packetReceived', (packet, event) => {

}).setFilteredClass(S38PacketPlayerListItem)
*/
Settings.registerListener("Custom Player List", value => {
    if (value) {
        playerList.register();
        return;
    }
    playerList.unregister();
})

const playerList = register('renderPlayerList', (event) => {
    cancel(event);
    const players = TabList.getNames().slice(0, maxPlayers);
    const columns = Math.min(maxColumns, Math.ceil(players.length / playersPerColumn));
    const rows = Math.min(playersPerColumn, players.length);
    const startX = (Renderer.screen.getWidth() - columns * columnWidth) / 2;
    const startY = 10;

    Renderer.drawRect(
        Renderer.color(0, 0, 0, 120),
        startX - 5,
        startY - 5,
        columns * columnWidth + 10,
        rows * lineHeight + 10
    );

    players.forEach((name, index) => {
        const col = Math.floor(index / playersPerColumn);
        const row = index % playersPerColumn;

        const x = startX + col * columnWidth;
        const y = startY + row * lineHeight;

        Renderer.drawString(name, x, y, true);
    });
}).unregister();

initialLoad = Settings.toggleCustomPlayerList
if (initialLoad) {
    playerList.register();
}
