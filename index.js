import "./features/AutoFish";
import "./features/AutoPetRules";
import "./features/AutoZombieShootout";
import "./features/BlazeSwap";
import "./features/BloodWarpTimer";
import "./features/CancelEmptyTooltip";
import "./features/CancelSlotHighlight";
import "./features/Clock";
import "./features/CustomActionBar";
import "./features/CustomScoreboard";
import "./features/CustomTabList";
import "./features/DungeonCommands";
import "./features/GhostPickaxe";
import "./features/GoldenFish";
import "./features/HideHealth";
import "./features/HideHotbar";
import "./features/PartyFinderHelper";
import "./features/QuickCommands";
import "./features/RagAxeCooldown";
import "./features/ShiftClick";
import "./features/SignHelper";
import "./features/SuperBoomSwap";
import "./features/slhjCalc"

import "./features/optimise/TabOutOptimise";

import Settings from "./config";

register("command", Settings.openGUI).setName("slhjaddons").setAliases("slhj");
// test below
import { data } from "./data";
import { GuiManager, Slider, ToggleButton, MultiCheckbox, RadioButtons} from "./utils/InputGuiStuff"
import { PlayerStats } from "./utils/PlayerStats";
new PlayerStats()

const gui = new Gui()
const guiManager = new GuiManager(gui)
const items = ["Option 1", "Option 2", "Option 3"];

const s1 = new Slider(guiManager, 100, 50, 200, 10)
const s2 = new Slider(guiManager, 100, 75, 200, 10, undefined, 20, 70)
const b1 = new ToggleButton(guiManager, 100, 100)
const b2 = new ToggleButton(guiManager, 130, 100)
const b3 = new ToggleButton(guiManager, 160, 100)
const multiCheckbox = new MultiCheckbox(guiManager, 100, 125, Settings.quickCommandsList.split(",").map(c => c.trim()).filter(c => c.length), undefined, 200, 20);
const rb = new RadioButtons(guiManager, 350, 50, ['option1', 'option2', 'option3', 'option4'])

register('command', () => gui.open()).setName('guiinput')

register('command', () => {
    ChatLib.chat(data.player.dungeon.class + " " + data.player.dungeon.classLevel)
}).setName('test')