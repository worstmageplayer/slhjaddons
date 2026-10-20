# slhjaddons
Minecraft Fabric mod for Hypixel Skyblock.

Targets Minecraft **26.3** with **Java 25**, Fabric Loader **0.19.5+**, and Fabric API for 26.3.
Set `JAVA_HOME` to a JDK 25 installation before building.

The 26.3 port follows the [Minecraft changelog](https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3),
[Fabric migration notes](https://www.fabricmc.net/2026/09/15/263.html), and the game's sources generated with `./gradlew genSources`.
Run source generation separately from `./gradlew build`.

## BUILD
```bash
git clone https://github.com/worstmageplayer/slhjaddons.git
cd slhjaddons
```
   - **Linux/macOS:**
```bash
     ./gradlew build
```
   - **Windows:**
```bash
     gradlew.bat build
```

## Settings

Run `/slhj` to open settings. All toggleable features and their settings appear in one scrolling
list on the right. Category buttons on the left jump to sections in that list;
each feature has a separate ON/OFF button. Disabled features remain visible and
configurable. Search checks all categories, feature descriptions, and setting labels.
Choosing a category clears search and jumps to its section in the full list.

Number settings support sliders and direct entry. Click a color swatch to adjust
RGB and opacity using standard Minecraft sliders; hex colors use `#AARRGGBB`.
Invalid input keeps the last valid value. Changes apply immediately and save when
leaving the screen. Search and scroll state survive visits to the
HUD editor and color picker. Reset settings restores only that feature's options.
In the color picker, **Done** keeps changes; **Cancel** and Escape restore the color
from when the picker opened, including after using **Default**.
Saved numeric values outside their supported range are clamped; malformed or
non-finite values use the setting's default.

`/slhj` and calculator commands are always available. `/slhj list` identifies them
as such, and `/slhj toggle` cannot disable them. Calculator evaluation is unchanged.

Quick Commands defaults to translucent black (`#80000000`) with translucent white
on hover (`#80FFFFFF`), both about 50% opacity. Existing saved colors are preserved;
the color picker's Default button restores the new default for that color.
Its key starts unbound on new configurations; existing saved keybindings are
preserved exactly. Assign keys in **Options > Controls > Key Binds > slhjaddons**.
Enter comma-separated local or server commands, for example `/warp hub, /slhj`;
the leading slash is optional. An empty command list does nothing. Changing an
inner/outer radius adjusts the other when needed to maintain a one-unit gap.
Opening another screen, disabling the feature, or disconnecting cancels the wheel
without executing its selection.

## Feature behavior

- **Dungeon Commands:** `/d` warps to dungeons; `/d f1` through `/d f7` and `/d m1`
  through `/d m7` queue a floor. The feature must be enabled. Only the first pending
  request is retained, and disabling or disconnecting cancels it. The cooldown
  starts when a join command is sent. Auto-Enter Undersized has been removed.
- **Auto Click:** The rate is 0–20 CPS; 0 generates no clicks. Disabling the entity
  requirement still attacks a targeted entity and permits swinging without one.
- **Auto Fish:** Opening a screen, disabling, disconnecting, or no longer holding
  a fishing rod cancels a pending recast. Resume fishing manually after cancellation.
- **Sign Helper:** Each supported sign has fresh input state. Successful results
  are submitted in full; the server handles length limits. Failed calculations
  leave the original input unchanged.
- **Ragnarock Cooldown Timer:** The optional ready sound is a note-block pling at
  normal pitch, with adjustable volume defaulting to 50%. Minecraft's master and
  UI sound settings also apply.

## HUD editor

Open **Edit HUD** in settings or run `/slhj hud`. Only enabled HUD features appear.
Click overlapping elements repeatedly to cycle selection; drag to move and scroll to scale.
With the edit panel closed, Tab/Shift+Tab select the next/previous enabled element.
With it open, Tab/Shift+Tab navigate controls and scroll focused controls into view.
The **<** and **>** buttons select the previous/next element in either mode.

The selection strip at the top identifies the selected element. **Edit** opens two
independent nine-point grids for the screen anchor and element anchor, plus X/Y offsets
and scale. Changing anchors preserves the visible position. **Align anchors** clears
offsets subject to keeping the element on-screen; **Reset selected** restores its default
placement and scale. Number fields apply on Enter or focus loss, clamp to the valid
placement, and display the applied value. Invalid drafts retain the last valid
placement. Changes save when leaving the editor.

Gameplay and editing use the same HUD objects and renderer. Enabled alerts without live
data show example content only in the editor. Existing saved positions and legacy Clock
scale/position settings migrate automatically. Appearance settings remain
in feature settings; positioning and scaling are controlled in the HUD editor.
Party Finder Helper also uses the HUD editor for position and scale. Its two-line
live overlay appears only over the Party Finder screen; the editor shows example
content when that screen is not open.

Run the HUD, settings, configuration, command queue, input pacing, fishing, sign,
and cooldown regression suites with
`./gradlew hudTest`; it also runs as part of `./gradlew build`.
