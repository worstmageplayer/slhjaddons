import { WorldInfo } from "./WorldInfo";
import { data } from "../data";

const S02PacketChat = Java.type('net.minecraft.network.play.server.S02PacketChat');
const S38PacketPlayerListItem = Java.type('net.minecraft.network.play.server.S38PacketPlayerListItem')
const healthRegex = /([\d,]+)\/([\d,]+)❤/;
const manaRegex = /([\d,]+)\/([\d,]+)✎/;
const overflowRegex = /([\d,]+)ʬ/;
const defenseRegex = /([\d,]+)❈ Defense/;
const worldInfo = new WorldInfo

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

        this.packetReceived();
        this.worldUnload();
        this.updateDungeonClass();
    }

    parseNumber(str) {
        return parseInt(str.replace(/,/g, ""), 10) || 0;
    }

    packetReceived() {
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

    updateDungeonClass() {
        register('packetReceived', () => {
            if (worldInfo.getArea() !== 'Dungeon Hub') return;
            
            const tblst = TabList.getNames();
            const idx = tblst.findIndex(line => line.includes('Dungeons:'));
            if (idx === -1 || idx + 2 >= tblst.length) return;

            const levelLine = tblst[idx + 1].removeFormatting().trim();
            const classLine = tblst[idx + 2].removeFormatting().trim();

            const classMatch = classLine.match(/\b(Mage|Healer|Berserk|Archer|Tank)\b/);
            const levelMatch = levelLine.match(/Catacombs\s+(\d+)/);

            if (!classMatch || !levelMatch) return;

            if (classMatch[0] !== data.player.dungeon.class || parseInt(levelMatch[1]) !== data.player.dungeon.level) {
                data.player.dungeon.class = classMatch[0];
                data.player.dungeon.level = parseInt(levelMatch[1]);
                data.save();
            }
        }).setFilteredClass(S38PacketPlayerListItem);
    }

    worldUnload() {
        register('worldUnload', () => {
            Object.keys(this.stats).forEach(key => this.stats[key] = null);
        })
    }

    getStats() {
        return this.stats;
    }
}