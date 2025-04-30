// Ghost Pickaxe ------------------------
import Settings from "../config";

const keyCode = Client.getKeyBindFromDescription("Ghost pick") || Keyboard.KEY_G;
const ghostPick = new KeyBind("Ghost pick", keyCode, "AAA Ghost pick");

// Create pickaxe with efficiency 10
const pickItem = new net.minecraft.item.ItemStack(net.minecraft.init.Items.field_151046_w); // Diamond Pickaxe
const nbt = new net.minecraft.nbt.NBTTagCompound();
const ench = new net.minecraft.nbt.NBTTagList();
const eff = new net.minecraft.nbt.NBTTagCompound();

eff.func_74777_a("id", 32);  // Efficiency
eff.func_74777_a("lvl", 10);

ench.func_74742_a(eff);
nbt.func_74782_a("ench", ench);
pickItem.func_77982_d(nbt);

ghostPick.registerKeyPress(() => {
    if (!Settings.toggleGhostPickaxe) return;
    
    const slotIndex = Player.getHeldItemIndex() + 36;
    const container = Client.getMinecraft().field_71439_g.field_71070_bA;
    const slot = container.func_75139_a(slotIndex);
    if (!slot) return ChatLib.chat("Failed to find held slot.");
    slot.func_75215_d(pickItem);
    ChatLib.chat(`Ghost pickaxe placed in slot ${slotIndex}.`);
});