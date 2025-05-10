import "./features/AutoFish";
import "./features/AutoPetRules";
import "./features/AutoZombieShootout";
import "./features/BlazeSwap";
import "./features/BloodWarpTimer";
import "./features/CancelEmptyTooltip";
import "./features/CancelSlotHighlight";
import "./features/chatCalc";
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

import "./features/optimise/TabOutOptimise";

import Settings from "./config";

register("command", Settings.openGUI).setName("slhjaddons").setAliases("slhj");
// test below
import { GuiManager, Slider, ToggleButton, MultiCheckbox} from "./utils/InputGuiStuff"

const gui = new Gui()
const guiManager = new GuiManager(gui)
const items = ["Option 1", "Option 2", "Option 3"];

const s = new Slider(guiManager, 100, 50, 200, 10, undefined, 20, 70)
const b1 = new ToggleButton(guiManager, 100, 75)
const b2 = new ToggleButton(guiManager, 130, 75)
const b3 = new ToggleButton(guiManager, 160, 75)
const multiCheckbox = new MultiCheckbox(guiManager, 100, 100, Settings.quickCommandsList.split(",").map(c => c.trim()).filter(c => c.length), undefined, 200, 20);

register('command', () => gui.open()).setName('guiinput')