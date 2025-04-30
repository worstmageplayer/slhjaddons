const S38PacketPlayerListItem = Java.type('net.minecraft.network.play.server.S38PacketPlayerListItem');
const regex = /^(?:Area|Dungeon): ([\w ]+)$/;

export const onHypixel = () => /hypixel\.(?:io|net)|ilovecatgirls\.xyz/.test(Server.getIP());

export class WorldInfo {
    constructor() {
        this.area = null;

        this.packetReceived = register('packetReceived', (packet, event) => {
            const entries = packet.func_179767_a();
            const action = packet.func_179768_b();
            if (
                action !== S38PacketPlayerListItem.Action.ADD_PLAYER &&
                action !== S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME
            ) return;

            entries.forEach(addPlayerData => {
                const displayName = addPlayerData.func_179961_d();
                if (!displayName) return;
                const unformattedText = displayName.func_150260_c();
                const match = unformattedText.match(regex);
                if (!match) return;

                const newArea = match[1];
                if (newArea === this.area) return;
                this.area = newArea;
            });
        }).setFilteredClass(S38PacketPlayerListItem);

        this.resetWorldUnload = register('worldUnload', () => this.resetInfo());
    }

    inSkyblock() {
        const title = Scoreboard.getTitle();
        return title.removeFormatting().toLowerCase().includes('skyblock');
    }

    inDungeon() {
        return this.inSkyblock() && this.area === 'Catacombs';
    }

    getArea() {
        return this.area;
    }

    resetInfo() {
        this.area = null;
    }
}