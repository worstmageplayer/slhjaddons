const S02PacketChat = Java.type('net.minecraft.network.play.server.S02PacketChat');
const healthRegex = /([\d,]+)\/([\d,]+)❤/;
const manaRegex = /([\d,]+)\/([\d,]+)✎/;
const overflowRegex = /([\d,]+)ʬ/;
const defenseRegex = /([\d,]+)❈ Defense/;

export class PlayerStats {
    constructor() {
        this.stats = {
            currentHealth: null,
            maxHealth: null,
            currentMana: null,
            maxMana: null,
            currentDefense: null,
            overflowMana: null,
            effectiveHP: null,
            speed: null,
        };

        this.registerPacketHandler();
        this.registerWorldUnload(); 
    }

    parseNumber(str) {
        return parseInt(str.replace(/,/g, ""), 10) || 0;
    }

    registerPacketHandler() {
        register('packetReceived', (packet, event) => {
            if (packet.func_179841_c() !== 2) return;

            const msg = packet.func_148915_c().text.removeFormatting();

            const healthMatch = healthRegex.exec(msg);
            if (healthMatch) {
                this.stats.currentHealth = this.parseNumber(healthMatch[1]);
                this.stats.maxHealth = this.parseNumber(healthMatch[2]);
            }

            const manaMatch = manaRegex.exec(msg);
            if (manaMatch) {
                this.stats.currentMana = this.parseNumber(manaMatch[1]);
                this.stats.maxMana = this.parseNumber(manaMatch[2]);
            }

            const overflowMatch = overflowRegex.exec(msg);
            this.stats.overflowMana = overflowMatch ? this.parseNumber(overflowMatch[1]) : 0;

            const defenseMatch = defenseRegex.exec(msg);
            if (defenseMatch) {
                this.stats.currentDefense = this.parseNumber(defenseMatch[1]);
            }   

            this.stats.effectiveHP = (this.stats.currentHealth * (1 + this.stats.currentDefense / 100)) | 0;
            this.stats.speed = (Player.getPlayer()?.field_71075_bZ?.func_75094_b() * 1000) | 0;
        }).setFilteredClass(S02PacketChat);
    }

    registerWorldUnload() {
        register('worldUnload', () => {
            Object.keys(this.stats).forEach(key => this.stats[key] = null);
        })
    }

    getStats() {
        return this.stats;
    }
}
