// Superboom Swap -----------------------
//config
import Settings from "../config";
import { WorldInfo } from '../utils/WorldInfo'

const worldInfo = new WorldInfo
const boomboomBlocks = ["minecraft:stonebrick", "minecraft:stone_slab", "minecraft:double_stone_slab"];

Settings.registerListener("Superboom Auto Swap", value => {
    if (value) {
        superboomSwap.register();
        return;
    }
    superboomSwap.unregister();
})


const superboomSwap = register('Clicked', (x, y, button, state) => {
    if (button === 1 || !state) return;

    // Check in dungeons
    if (!worldInfo.inDungeon()) return;

    // Test check
    const block = Player.lookingAt()
    const blockName = block?.type?.getRegistryName();
    if (!boomboomBlocks.includes(blockName)) return;
    if (blockName === 'minecraft:stonebrick' && block.getMetadata() !== 2) return;

    // Checks hotbar for superboom
    const hotbar = Player.getContainer()?.getItems().slice(36, 44);
    const slottoswap = hotbar.findIndex(slot => 
        slot.getNBT()?.toObject()?.tag?.ExtraAttributes?.id === 'SUPERBOOM_TNT'
    );

    if (slottoswap !== -1) Player.setHeldItemIndex(slottoswap);
}).unregister();

if (Settings.toggleSuperboomSwap) {
    superboomSwap.register();
}