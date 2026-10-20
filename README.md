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

## HUD editor

Open **Edit HUD** in settings or run `/slhj hud`. Only enabled HUD features appear.
Click overlapping elements repeatedly to cycle selection; drag to move, scroll to scale,
and use Tab/Shift+Tab to select the next/previous enabled element.

The selection strip at the top identifies the selected element. **Edit** opens two
independent nine-point grids for the screen anchor and element anchor, plus X/Y offsets
and scale. Changing anchors preserves the visible position. **Align anchors** clears
offsets subject to keeping the element on-screen; **Reset selected** restores its default
placement and scale. Changes save when leaving the editor.

Gameplay and editing use the same HUD objects and renderer. Enabled alerts without live
data show example content only in the editor. Existing saved positions and legacy Clock
scale/position settings migrate automatically. Appearance settings remain
in feature settings; positioning and scaling are controlled in the HUD editor.

Run the placement, migration, content, and interaction regression suite with
`./gradlew hudTest`; it also runs as part of `./gradlew build`.
