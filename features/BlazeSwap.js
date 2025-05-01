// Blaze --------------------------------
import Settings from "../config";
import {rightClickPacket} from "../utils/RightClickPacket"

const EntityArmorStand = Java.type('net.minecraft.entity.item.EntityArmorStand').class;
const checkValues = ['Inferno Demon', 'Slay the boss!'];

const BlazeDaggers = new Set(['HEARTFIRE_DAGGER', 'HEARTMAW_DAGGER']);
const AttunementData = {
    ASHEN:   { number: 0, dagger: 'HEARTFIRE_DAGGER' },
    AURIC:   { number: 1, dagger: 'HEARTFIRE_DAGGER' },
    SPIRIT:  { number: 2, dagger: 'HEARTMAW_DAGGER' },
    CRYSTAL: { number: 3, dagger: 'HEARTMAW_DAGGER' }
};
const AttunementKeys = Object.keys(AttunementData);
const AttunementValues = Object.values(AttunementData);
const INVENTORY_OFFSET = 36;
const ATTUNEMENT_REGEX = new RegExp(AttunementKeys.join('|'));

let lastExecutionTime = 0;

Settings.registerListener('Blaze Auto Swap', v => v ? autoBlaze.register() : autoBlaze.unregister());

// Check if Blaze Slayer quest is active
function isBlazeSlayerActive() {
    return checkValues.every(check =>
        Scoreboard.getLines().some(line => line.getName().includes(check))
    );
}

const autoBlaze = register('AttackEntity', () => {
    if (!isBlazeSlayerActive()) return;
    
    const cooldownTime = Settings.cooldown;
    const daggerInfoArray = getDaggersInfo();
    const heldDaggerInfo = getHeldDaggerInfo();
    const blazeAttunementNumber = getBlazeAttunementNo();

    toSwapOrNotToSwap(blazeAttunementNumber, heldDaggerInfo, daggerInfoArray, cooldownTime);
}).unregister();

if (Settings.toggleBlaze) {
    autoBlaze.register();
}

// Get dagger info from inventory
function getDaggersInfo() {
    return Player.getContainer()?.getItems()?.map((item, slot) => {
        const itemAttributes = item?.getNBT()?.toObject()?.tag?.ExtraAttributes
        const itemid = itemAttributes.id
        const attunement = itemAttributes.td_attune_mode
        
        if (!BlazeDaggers.has(itemid)) return null;

        return { itemid, itemslot: slot, attunement };
    }).filter(Boolean);
}

// Get held dagger info
function getHeldDaggerInfo() {
    const heldItem = Player.getHeldItem();
    if (!heldItem) return null;

    const itemAttributes = heldItem?.getNBT()?.toObject()?.tag?.ExtraAttributes
    const itemid = itemAttributes.id
    const attunement = itemAttributes.td_attune_mode

    if (!BlazeDaggers.has(itemid)) return null;

    return {
        itemid,
        itemslot: Player.getHeldItemIndex(),
        attunement
    };
}

// Get boss attunement number
function getBlazeAttunementNo() {
    const blazeTitle = World.getAllEntitiesOfType(EntityArmorStand).find(stand => {
        const name = stand?.getName()?.removeFormatting();
        const isMatching = name && ATTUNEMENT_REGEX.test(name);
        const isCloseEnough = stand.distanceTo(Player.lookingAt()) <= 3;
        return isMatching && isCloseEnough;
    });

    if (!blazeTitle) return null;

    const blazeName = blazeTitle.getName()?.removeFormatting();
    const attunementKey = AttunementKeys.find(key => blazeName.includes(key));

    return attunementKey ? AttunementData[attunementKey].number : null;
}

// Swap dagger if needed
function toSwapOrNotToSwap(blazeAttunementNumber, heldDaggerInfo, daggerInfoArray, cooldownTime) {
    if (blazeAttunementNumber == null) return;

    const correctDagger = AttunementValues.find(a => a.number === blazeAttunementNumber)?.dagger;
    if (!correctDagger) return;

    const needsSwap = !heldDaggerInfo || heldDaggerInfo.attunement !== blazeAttunementNumber;
    if (!needsSwap) return;

    const daggerSwap = daggerInfoArray.find(({ itemid }) => itemid === correctDagger);
    if (!daggerSwap) return;

    Player.setHeldItemIndex(daggerSwap.itemslot - INVENTORY_OFFSET);

    if (daggerSwap.attunement !== blazeAttunementNumber) rightClickCooldown(cooldownTime);
}

// Right-click with cooldown
function rightClickCooldown(cooldownTime) {
    const now = Date.now();
    if (now - lastExecutionTime < cooldownTime) return;

    Client.scheduleTask(0, () => rightClickPacket())

    lastExecutionTime = now;
}