// AutoFish -----------------------------
import Settings from "../config";
import {rightClick} from "../utils/RightClickPacket"
const EntityArmorStandClass = Java.type('net.minecraft.entity.item.EntityArmorStand').class;
const fishingTimerRegex = /^\d\.\d$/

let shouldClick = false;

Settings.registerListener('Auto Fish', v => v ? autoFish.register() : autoFish.unregister());

const autoFish = register('step', () => {
    if (Client.isInGui()) return;
    const heldItem = Player.getHeldItem();
    if (!heldItem || heldItem.getRegistryName() !== "minecraft:fishing_rod") return;

    World.getAllEntitiesOfType(EntityArmorStandClass).forEach((armorStand) => {
        const name = armorStand.getName().removeFormatting();

        if (name.match(fishingTimerRegex) && !shouldClick) {
            shouldClick = true;
            return;
        } else if (name === '!!!' && shouldClick) {
            rightClick()
            Client.scheduleTask(Settings.rodDelay, () => rightClick())
            shouldClick = false;
            return;
        }
    });
}).setFps(5).unregister();

if (Settings.toggleAutoFish) {
    autoFish.register();
}