import { 
    @Vigilant, 
    @ButtonProperty, 
    @TextProperty, 
    @SwitchProperty, 
    @SelectorProperty, 
    @ColorProperty, 
    @SelectorProperty, 
    @NumberProperty, 
    @ParagraphProperty, 
    Color
} from "../Vigilance";

@Vigilant("slhjaddons", "§lslhj§raddons", {
	getCategoryComparator: () => (a, b) => {
		const categories = ["Skyblock", "Dungeons", "Fishing", "Slayers", "Events", "Misc"];
		return categories.indexOf(a.name) - categories.indexOf(b.name);
	}
})

class Settings {
    moveRagnarockCooldownGui = new Gui()
    moveAutoPetRuleDisplay = new Gui()

    @SwitchProperty({
        name: 'Custom Action Bar',
        description: 'Not so custom action bar.',
        category: 'Skyblock',
        subcategory: 'Action Bar'
    })
    toggleCustomActionBar = false;

    @SwitchProperty({
        name: 'Shift Click',
        description: 'Converts clicks into shift clicks in certain inventories.',
        category: 'Skyblock',
        subcategory: 'Shift Click'
    })
    toggleShiftClick = false;

    @SwitchProperty({
		name: 'Activate slhjTech',
		category: 'Skyblock',
        subcategory: 'slhjTech'
	})
	slhjTechActivate = false;

    @ButtonProperty({
		name: 'slhj Technology',
        description: 'Access to advanced technology developed by slhj.',
		category: 'Skyblock',
        subcategory: 'slhjTech',
        placeholder: 'click'
	})
	slhjTech() {
        Client.currentGui.close()
        World.playSound('mob.enderdragon.end', 1, 1)
        Client.showTitle('slhjTECH ACTIVATED!', '', 10, 100, 0);
        Client.showTitle('slhjTECH ACTIVATED!', '', 10, 100, 0);
        this.slhjTechActivate = false;
        setTimeout(() => Client.getMinecraft().func_71400_g(), 3000)
    };

    @SwitchProperty({
		name: 'Display Ragnarock Cooldown',
		description: 'Displays ragnarock cooldown.',
		category: 'Skyblock',
        subcategory: 'Cooldowns'
	})
	toggleRagCooldown = false;

    @TextProperty({
        name: 'Cooldown End Message',
        description: 'Sends a message when cooldown ends.',
		category: 'Skyblock',
        subcategory: 'Cooldowns',
        placeholder: '§r§5§lRagnarock §aReady!'
    })
    ragCooldownEndMessage = '§r§5§lRagnarock §aReady!';

    @SwitchProperty({
		name: 'Cooldown End Sound',
		description: 'Plays a sound when cooldown ends.',
		category: 'Skyblock',
        subcategory: 'Cooldowns'
	})
	toggleRagCooldownSound = false;

    @ButtonProperty({
		name: 'Move Ragnarock Cooldown',
		category: 'Skyblock',
        subcategory: 'Cooldowns',
        placeholder: "Move"
	})
	MoveRagGui() {
        this.moveRagnarockCooldownGui.open()
    };

    @SwitchProperty({
		name: 'Display Auto Pet Rules',
		description: 'Displays pet on screen when auto pet rules is triggered.',
		category: 'Skyblock',
        subcategory: 'Pets'
	})
	toggleAutoPetRuleDisplay = false;

    @ButtonProperty({
		name: 'Move Display Auto Pet Rules',
		category: 'Skyblock',
        subcategory: 'Pets',
        placeholder: "Move"
	})
	MoveAPRGui() {
        this.moveAutoPetRuleDisplay.open()
    };

    @SwitchProperty({
		name: 'Cancel Auto Pet Rule Message',
		description: 'Cancels the auto pet rule message from sending.',
		category: 'Skyblock',
        subcategory: 'Pets'
	})
	toggleCancelAutoPetRuleMessage = false;

    @SwitchProperty({
		name: 'Blood Time',
		description: 'Display time after blood opens. Enter blood at 10s for slightly faster watcher move.',
		category: 'Dungeons',
        subcategory: 'Blood'
	})
	toggleBloodTime = false;

	@SwitchProperty({
		name: 'Dungeon Commands',
		description: '//d',
		category: 'Dungeons',
        subcategory: 'Commands'
	})
	toggleDcmd = false;

    @SwitchProperty({
		name: 'Enter Undersized',
		category: 'Dungeons',
        subcategory: 'Commands'
	})
	toggleUndersizedEntry = false;

    @SwitchProperty({
		name: 'Superboom Auto Swap',
		description: 'Swaps to superboom when clicking on stone slab or cracked stone bricks.',
		category: 'Dungeons',
        subcategory: 'Not Legit'
	})
	toggleSuperboomSwap = false;

    @SwitchProperty({
		name: 'Auto Fish',
		description: 'Automatically reels and casts rod.',
		category: 'Fishing',
        subcategory: 'Auto'
	})
	toggleAutoFish = false;

    @TextProperty({
        name: 'Recast Rod Delay',
        description: 'Adjust time between cast in ticks.',
        category: 'Fishing',
        subcategory: 'Auto',
        placeholder: "20"
    })
    rodDelay = "20";

    @SwitchProperty({
		name: 'Golden Fish Alert',
		description: 'Alert when golden fish spawns.',
		category: 'Fishing',
        subcategory: 'Golden Fish'
	})
	toggleGFAlert = false;

	@SwitchProperty({
		name: 'Blaze Auto Swap',
        description: 'Auto swap blaze daggers and attunement.',
		category: 'Slayers',
        subcategory: 'Blaze'
	})
	toggleBlaze = false;

    @TextProperty({
        name: 'Attunement Swap Cooldown',
        description: 'Adjust depending on ping.',
        category: 'Slayers',
        subcategory: 'Blaze',
        placeholder: "500"
    })
    cooldown = "500";

    @SwitchProperty({
		name: 'Auto Zombie Shootout',
		description: 'Toggles auto zombie shootout. (not mine)',
		category: 'Events',
        subcategory: 'Carnival'
	})
	toggleAutoZombieShootout = false;

	@SwitchProperty({
		name: "Ingame Clock",
        description: "Ingame clock.",
		category: "Misc",
        subcategory: 'Clock'
	})
	toggleClock = true;

    @SelectorProperty({
        name: 'Clock Position',
        category: 'Misc',
        subcategory: 'Clock',
        options: ['Left', 'Right']
    })
    clockPosition = 1;

    @TextProperty({
        name: 'Clock Scale',
        category: 'Misc',
        subcategory: 'Clock',
        placeholder: '1'
    })
    clockScale = '1';

    @TextProperty({
        name: 'Clock Padding',
        category: 'Misc',
        subcategory: 'Clock',
        placeholder: '5'
    })
    clockPadding = '5';

    @ColorProperty({
        name: 'Clock Colour',
        category: 'Misc',
        subcategory: "Clock"
    })
    clockColor = Color.WHITE;

    @SwitchProperty({
        name: 'Custom Player List',
        description: 'Not so custom player list.',
        category: 'Misc',
        subcategory: 'Player List'
    })
    toggleCustomPlayerList = false;

    @SwitchProperty({
        name: 'Custom Scoreboard',
        description: 'Renders a custom scoreboard.',
        category: 'Misc',
        subcategory: 'Scoreboard'
    })
    toggleCustomScoreboard = false;

    @TextProperty({
        name: 'Scoreboard Scale ',
        category: 'Misc',
        subcategory: 'Scoreboard',
        placeholder: '1'
    })
    scoreboardScale = '1';

    @SwitchProperty({
        name: 'Scoreboard Shadow',
        category: 'Misc',
        subcategory: 'Scoreboard'
    })
    toggleScoreboardShadow = false;

    @TextProperty({
        name: 'Scoreboard Padding',
        category: 'Misc',
        subcategory: 'Scoreboard',
        placeholder: '2'
    })
    scoreboardPadding = '2';

    @TextProperty({
        name: 'Scoreboard Offset',
        category: 'Misc',
        subcategory: 'Scoreboard',
        placeholder: '1'
    })
    scoreboardOffset = '1';

    @ColorProperty({
        name: 'Scoreboard Colour',
        category: 'Misc',
        subcategory: "Scoreboard"
    })
    scoreboardColour = new Color(0, 0, 0, 0.3333);

    @TextProperty({
        name: 'Scoreboard Header',
        category: 'Misc',
        subcategory: 'Scoreboard',
        placeholder: '§e§lSKYBLOCK'
    })
    scoreboardHeader = '§e§lSKYBLOCK';

    @TextProperty({
        name: 'Scoreboard Footer',
        category: 'Misc',
        subcategory: 'Scoreboard',
        placeholder: '§lslhj§raddons§r'
    })
    scoreboardFooter = '§lslhj§raddons§r';

    @SwitchProperty({
        name: 'Calc BeDugging',
        category: 'Misc',
        subcategory: 'Calc (short for calculator)'
    })
    devCalc = false;

    @SwitchProperty({
        name: 'Quick Commands',
        description: 'Description.',
        category: 'Misc',
        subcategory: 'Quick Commands'
    })
    toggleQuickCommands = false;

    @ParagraphProperty({
        name: 'Commands List',
        category: 'Misc',
        subcategory: 'Quick Commands',
        placeholder: 'sax, storage, trades, gfs ENDER_PEARL 16, gfs INFLATABLE_JERRY 10, gfs SUPERBOOM_TNT 10'
    })
    quickCommandsList = 'sax, storage, trades, gfs ENDER_PEARL 16, gfs INFLATABLE_JERRY 10, gfs SUPERBOOM_TNT 10';

    @SwitchProperty({
        name: 'Show Commands',
        description: 'Displays the command on hover.',
        category: 'Misc',
        subcategory: 'Quick Commands'
    })
    toggleShowCommandString = false;

    @TextProperty({
        name: 'Section Offset',
        category: 'Misc',
        subcategory: 'Quick Commands',
        placeholder: '5'
    })
    sectionOffset = '5';

    @TextProperty({
        name: 'Outer Radius',
        category: 'Misc',
        subcategory: 'Quick Commands',
        placeholder: '140'
    })
    outerRadius = '140';

    @TextProperty({
        name: 'Inner Radius',
        category: 'Misc',
        subcategory: 'Quick Commands',
        placeholder: '30'
    })
    innerRadius = '30';

    @ColorProperty({
        name: 'Quick Commands Colour',
        category: 'Misc',
        subcategory: "Quick Commands"
    })
    quickCommandsColour = new Color(0, 0, 0, 0.6666);

    @ColorProperty({
        name: 'Quick Commands Hover Colour',
        category: 'Misc',
        subcategory: "Quick Commands"
    })
    quickCommandsHoverColour = new Color(1, 1, 1, 0.6666);

    @SwitchProperty({
        name: 'Hide Hotbar',
        description: 'Automatically hides hotbar.',
        category: 'Misc',
        subcategory: 'Hide GUI'
    })
    toggleHideHotbar = false;

    @SwitchProperty({
        name: 'Hide Health',
        description: 'Automatically hides health.',
        category: 'Misc',
        subcategory: 'Hide GUI'
    })
    toggleHideHealth = false;

    @SwitchProperty({
        name: 'Hide Slot Highlight',
        description: 'Stops slot highlight from rendering.',
        category: 'Misc',
        subcategory: 'Hide GUI'
    })
    toggleCancelSlotHighlight = false;

    @SwitchProperty({
        name: 'Custom Slot Highlight',
        description: 'Renders a custom slot highlight.',
        category: 'Misc',
        subcategory: 'Hide GUI'
    })
    toggleCustomSlotHighlight = false;

    @SwitchProperty({
        name: 'Hide Empty Tooltip',
        description: 'Hides empty tooltip.',
        category: 'Misc',
        subcategory: 'Hide GUI'
    })
    toggleCancelEmptyTooltip = false;

    @SwitchProperty({
        name: 'Optimise Tab Out',
        description: 'Reduces Fps and Render Distance and other stuff when tabbed out.',
        category: 'Misc',
        subcategory: 'Optimisations'
    })
    toggleOptimiseTabOut = false;

	constructor() {
		this.initialize(this);
        this.addDependency('slhj Technology', 'Activate slhjTech')

        this.addDependency('Move Display Auto Pet Rules', 'Display Auto Pet Rules')
        this.addDependency('Cancel Auto Pet Rule Message', 'Display Auto Pet Rules')

        this.addDependency('Move Ragnarock Cooldown', 'Display Ragnarock Cooldown')
        this.addDependency('Cooldown End Message', 'Display Ragnarock Cooldown')
        this.addDependency('Cooldown End Sound', 'Display Ragnarock Cooldown')

        this.addDependency('Enter Undersized', 'Dungeon Commands')

        this.addDependency('Recast Rod Delay', 'Auto Fish')

        this.addDependency('Attunement Swap Cooldown', 'Blaze Auto Swap')

        this.addDependency('Clock Position', 'Ingame Clock')
        this.addDependency('Clock Scale', 'Ingame Clock')
        this.addDependency('Clock Padding', 'Ingame Clock')
        this.addDependency('Clock Colour', 'Ingame Clock')

        this.addDependency('Scoreboard Scale ', 'Custom Scoreboard')
        this.addDependency('Scoreboard Shadow', 'Custom Scoreboard')
        this.addDependency('Scoreboard Padding', 'Custom Scoreboard')
        this.addDependency('Scoreboard Offset', 'Custom Scoreboard')
        this.addDependency('Scoreboard Colour', 'Custom Scoreboard')
        this.addDependency('Scoreboard Header', 'Custom Scoreboard')
        this.addDependency('Scoreboard Footer', 'Custom Scoreboard')

        this.addDependency('Custom Slot Highlight', 'Hide Slot Highlight')

        this.addDependency('Commands List', 'Quick Commands')
        this.addDependency('Show Commands', 'Quick Commands')
        this.addDependency('Section Offset', 'Quick Commands')
        this.addDependency('Outer Radius', 'Quick Commands')
        this.addDependency('Inner Radius', 'Quick Commands')
        this.addDependency('Quick Commands Colour', 'Quick Commands')
        this.addDependency('Quick Commands Hover Colour', 'Quick Commands')
	}
}

export default new Settings();
