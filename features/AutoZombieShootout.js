// Auto Zombie Shootout -----------------
// Not my code
import Settings from "../config";
import {rightClick} from "../utils/RightClickPacket"

const EntityZombie = Java.type('net.minecraft.entity.monster.EntityZombie').class

let lampcoords = [
    [-96, 76, 31],
    [-99, 77, 32],
    [-102, 75, 32],
    [-106, 77, 31],
    [-109, 75, 30],
    [-112, 76, 28],
    [-115, 77, 25],
    [-117, 76, 22],
    [-118, 76, 19],
    [-119, 75, 15],
    [-119, 77, 12],
    [-118, 76, 9]
];

let lastClick = 0

Settings.registerListener("Auto Zombie Shootout", value => {
    if (value) {
        autoZombieShootout.register();
        return;
    }
    autoZombieShootout.unregister();
})

const autoZombieShootout = register("tick", () => {
    if (Date.now() - lastClick < 200) return;
    const playerHolding = Player.getHeldItem()
    if (!playerHolding?.getName()?.removeFormatting()?.includes("Dart")) return;
    const Target = getTarget()
    if (!Target) return;
    const currentTarget = Target.shift()
    if (!currentTarget) return;
    const [yaw, pitch] = calcYawPitch(currentTarget)
    snapTo(yaw, pitch)
    Client.scheduleTask(0, () => {
        rightClick()
    })

}).unregister();

initialLoad = Settings.autoZombieShootout
if (initialLoad) {
    autoZombieShootout.register();
}

function getTarget() {
    const Zombies = World.getAllEntitiesOfType(EntityZombie);
    if (!Zombies) return;

    let itemLists = {
        Diamond: [],
        Golden: [],
        Iron: [],
        Leather: []
    };

    Zombies.forEach(Zombie => {
        let currentZombie = new EntityLivingBase(Zombie.getEntity());
        let chestplate = currentZombie.getItemInSlot(3);
        if (!chestplate) return;
        let chestplatename = chestplate.getName();
        let targetcoord = {x: Zombie.getX() + Zombie.getMotionX()*8, y: Zombie.getY() + Zombie.getEyeHeight(), z: Zombie.getZ()+ Zombie.getMotionZ()*8};
        if (Player.asPlayerMP().distanceTo(Zombie.getX(), Zombie.getY(), Zombie.getZ()) > 40) return;
        for (let key in itemLists) {
            if (chestplatename.includes(key)) {
                itemLists[key].push(targetcoord);
                break;
            }
        }
    });

    let lamplist = [];

    lampcoords.forEach(coord => {
        let blockID = World.getBlockAt(...coord)?.type?.getID();
        if (blockID !== 124) return;
        lamplist.push({x: coord[0] + 0.5, y: coord[1] + 0.6, z: coord[2] + 0.5});
    });

    let targetlist = [
        ...itemLists.Diamond,
        ...lamplist,
        ...itemLists.Golden,
        ...itemLists.Iron,
        ...itemLists.Leather
    ];

    return targetlist;
}

function getEyePos() {
    return {
        x: Player.getX(),
        y: Player.getY() + Player.getPlayer().func_70047_e(),
        z: Player.getZ()
    };
}

function snapTo(yaw, pitch) {
    const player = Player.getPlayer(); 

    player.field_70177_z = yaw
    player.field_70125_A = pitch;
}

function calcYawPitch(blcPos, plrPos) {
    if (!plrPos) plrPos = getEyePos();
    let d = {
        x: blcPos.x - plrPos.x,
        y: blcPos.y - plrPos.y,
        z: blcPos.z - plrPos.z
    };
    let yaw = 0;
    let pitch = 0;
    if (d.x != 0) {
        if (d.x < 0) { yaw = 1.5 * Math.PI; } else { yaw = 0.5 * Math.PI; }
        yaw = yaw - Math.atan(d.z / d.x);
    } else if (d.z < 0) { yaw = Math.PI; }
    d.xz = Math.sqrt(Math.pow(d.x, 2) + Math.pow(d.z, 2));
    pitch = -Math.atan(d.y / d.xz);
    yaw = -yaw * 180 / Math.PI;
    pitch = pitch * 180 / Math.PI;
    if (pitch < -90 || pitch > 90 || isNaN(yaw) || isNaN(pitch)) return;

    return [yaw, pitch]
   
}