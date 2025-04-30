// Right Click Packet -------------------
const C08PacketPlayerBlockPlacement = Java.type("net.minecraft.network.play.client.C08PacketPlayerBlockPlacement");
const MCBlockPos = Java.type("net.minecraft.util.BlockPos");

export function rightClickPacket() {
    const packet = new C08PacketPlayerBlockPlacement(
        new MCBlockPos(-1, -1, -1),
        255,
        Player.getHeldItem().getItemStack(),
        0, 0, 0
    );
    Client.sendPacket(packet);
}

export function rightClick() {
    const rightClickMethod = Client.getMinecraft().getClass().getDeclaredMethod("func_147121_ag", null)
    rightClickMethod.setAccessible(true);
    rightClickMethod.invoke(Client.getMinecraft(), null);
} 