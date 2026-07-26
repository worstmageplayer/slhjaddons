package dev.slhj.slhjaddons.features;

import com.mojang.blaze3d.platform.InputConstants;
import dev.slhj.slhjaddons.SlhjAddons;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.ClientUtils;
import dev.slhj.slhjaddons.util.KeyBindingUtils;
import dev.slhj.slhjaddons.util.McUtils;
import dev.slhj.slhjaddons.util.RenderUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class QuickCommands extends Feature {

    private KeyMapping key;
    private boolean wasDown = false;
    private boolean isOpen = false;
    private int hoveredSection = -1;
    private boolean inDeadZone = true;
    private final List<String> commandsList = new ArrayList<>();
    private boolean showCommandString = true;

    private final Setting.TextSetting commandsString;
    private final Setting.SliderSetting innerRadius;
    private final Setting.SliderSetting outerRadius;
    private final Setting.SliderSetting sectionOffset;
    private final Setting.HexSetting guiColor;
    private final Setting.HexSetting guiHoverColor;

    public QuickCommands() {
        setLabel("Quick Commands");
        category(Category.MISC);
        commandsString = text("quick_commands.commands", "Commands", "");
        innerRadius = intSlider("quick_commands.inner_radius", "Inner Radius", 0, 255, 30);
        outerRadius = intSlider("quick_commands.outer_radius", "Outer Radius", 0, 255, 90);
        sectionOffset = intSlider("quick_commands.section_offset", "Section Offset", 0, 255, 10);
        guiColor = hex("quick_commands.color", "Color", 0xFF8B8B8B);
        guiHoverColor = hex("quick_commands.hover_color", "Hover Color", 0xFFFFFF00);
    }

    public static final String id = "quick_commands";
    @Override public String id() { return id; }

    @Override
    public void init() {
        // Load config
        loadConfig();

        key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.slhjaddons.quick_commands",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Q,
                KeyBindingUtils.getMainCategory()));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> keyDown());

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "quick_commands"),
                (graphics, delta) -> render(graphics));
    }

    private void loadConfig() {
        var cfg = SlhjAddons.config();

        String commandsStr = commandsString.value().get();
        commandsList.clear();
        if (!commandsStr.isEmpty()) {
            String[] commands = commandsStr.split(",");
            for (String cmd : commands) {
                String trimmed = cmd.trim();
                if (!trimmed.isEmpty()) {
                    commandsList.add(trimmed);
                }
            }
        }

        showCommandString = "true".equals(cfg.getString("quick_commands.show_string", "true"));
    }

    private void keyDown() {
        if (!isEnabled() || ClientUtils.inGui()) return;
        boolean down = key.isDown();
        if (down && !wasDown) {
            // McUtils.chat("opened menu");
            isOpen = true;
            inDeadZone = true;
            hoveredSection = -1;

            McUtils.MC.mouseHandler.releaseMouse();
            McUtils.scheduleTask(McUtils::centerCursor);
        }
        wasDown = down;

        if (isOpen) {
            if (!down) {
                // McUtils.chat("closed menu");
                if (!inDeadZone && hoveredSection >= 0 && hoveredSection < commandsList.size()) {
                    executeCommand(commandsList.get(hoveredSection));
                }
                isOpen = false;
                inDeadZone = true;
                hoveredSection = -1;
                McUtils.MC.mouseHandler.grabMouse();
            }
        }
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || !isOpen) return;

        loadConfig();

        int screenWidth = g.guiWidth();
        int screenHeight = g.guiHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Get mouse position
        double guiScale = McUtils.MC.getWindow().getGuiScale();
        double mouseX = McUtils.MC.mouseHandler.xpos() / guiScale;
        double mouseY = McUtils.MC.mouseHandler.ypos() / guiScale;

        // Calculate relative position from center
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;

        int innerRad = innerRadius.value().get().intValue();
        int outerRad = outerRadius.value().get().intValue();
        int sectionOff = sectionOffset.value().get().intValue();

        // Update dead zone and hovered section
        List<int[]> innerPoints = new ArrayList<>();
        generateRadialMenuShapes(
                centerX, centerY,
                innerRad,
                outerRad,
                sectionOff,
                innerPoints);

        inDeadZone = pointInPolygon(mouseX, mouseY, innerPoints);
        if (!inDeadZone) {
            hoveredSection = getMouseSection(dx, dy, commandsList.size());
        } else {
            hoveredSection = -1;
        }

        // Draw radial menu sections
        int numSections = Math.max(commandsList.size(), 3);
        double pi = Math.PI;
        double sectionAngle = (pi * 2) / numSections;
        double angleOffset = -pi / 2;

        for (int i = 0; i < numSections; i++) {
            double angle1 = sectionAngle * i + angleOffset;
            double angle2 = sectionAngle * (i + 1) + angleOffset;
            double avgAngle = (angle1 + angle2) / 2;

            double offsetX = Math.cos(avgAngle) * sectionOff;
            double offsetY = Math.sin(avgAngle) * sectionOff;

            int sectionCenterX = centerX + (int) offsetX;
            int sectionCenterY = centerY + (int) offsetY;

            double cos1 = Math.cos(angle1);
            double sin1 = Math.sin(angle1);
            double cos2 = Math.cos(angle2);
            double sin2 = Math.sin(angle2);

            int[] p1 = {(int) (sectionCenterX + cos1 * innerRad), (int) (sectionCenterY + sin1 * innerRad)};
            int[] p2 = {(int) (sectionCenterX + cos2 * innerRad), (int) (sectionCenterY + sin2 * innerRad)};
            int[] p3 = {(int) (sectionCenterX + cos2 * outerRad), (int) (sectionCenterY + sin2 * outerRad)};
            int[] p4 = {(int) (sectionCenterX + cos1 * outerRad), (int) (sectionCenterY + sin1 * outerRad)};

            int colour = (i == hoveredSection) ? guiHoverColor.value().get() : guiColor.value().get();

            // Draw filled quad
            drawFilledQuad(g, p1, p2, p3, p4, colour);
        }

        // Draw command name on hover
        if (showCommandString && !inDeadZone && hoveredSection >= 0 && hoveredSection < commandsList.size()) {
            String commandName = "/" + commandsList.get(hoveredSection);
            RenderUtils.text(g, commandName, (int) mouseX + 2, (int) mouseY - 7, 0xFFFFFFFF, true);
        }
    }

    private void generateRadialMenuShapes(int centerX, int centerY, int innerRadius, int outerRadius, int sectionOffset, List<int[]> innerPoints) {
        int numSections = Math.max(commandsList.size(), 3);
        double pi = Math.PI;
        double sectionAngle = (pi * 2) / numSections;
        double angleOffset = -pi / 2;
        double deadRadius = innerRadius + sectionOffset * 10 / 9.0;

        for (int i = 0; i < numSections; i++) {
            double angle1 = sectionAngle * i + angleOffset;
            double angle2 = sectionAngle * (i + 1) + angleOffset;

            int x = (int) (centerX + Math.cos(angle1) * deadRadius);
            int y = (int) (centerY + Math.sin(angle1) * deadRadius);
            innerPoints.add(new int[]{x, y});
        }
    }

    private void drawFilledQuad(GuiGraphicsExtractor g, int[] p1, int[] p2, int[] p3, int[] p4, int colour) {
        // Draw quad as two triangles
        drawTriangle(g, p1[0], p1[1], p2[0], p2[1], p3[0], p3[1], colour);
        drawTriangle(g, p1[0], p1[1], p3[0], p3[1], p4[0], p4[1], colour);
    }

    private void drawTriangle(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int x3, int y3, int colour) {
        int minY = Math.min(Math.min(y1, y2), y3);
        int maxY = Math.max(Math.max(y1, y2), y3);

        for (int y = minY; y <= maxY; y++) {
            int[] xPoints = new int[3];
            int xCount = 0;

            xCount += lineIntersectY(x1, y1, x2, y2, y, xPoints, xCount);
            xCount += lineIntersectY(x2, y2, x3, y3, y, xPoints, xCount);
            xCount += lineIntersectY(x3, y3, x1, y1, y, xPoints, xCount);

            if (xCount >= 2) {
                int minX = Math.min(Math.min(xPoints[0], xPoints[1]), xCount > 2 ? xPoints[2] : Integer.MAX_VALUE);
                int maxX = Math.max(Math.max(xPoints[0], xPoints[1]), xCount > 2 ? xPoints[2] : Integer.MIN_VALUE);
                if (minX < maxX) {
                    RenderUtils.fill(g, minX, y, maxX - minX, 1, colour);
                }
            }
        }
    }

    private int lineIntersectY(int x1, int y1, int x2, int y2, int y, int[] xPoints, int index) {
        if ((y1 <= y && y < y2) || (y2 <= y && y < y1)) {
            double t = (double) (y - y1) / (y2 - y1);
            int x = (int) (x1 + t * (x2 - x1));
            xPoints[index] = x;
            return 1;
        }
        return 0;
    }

    private int getMouseSection(double dx, double dy, int numSections) {
        if (numSections == 0) return -1;

        double angle = Math.atan2(dy, dx) + (Math.PI / 2);
        if (angle < 0) angle += 2 * Math.PI;

        double sectionAngle = 2 * Math.PI / Math.max(numSections, 3);
        return (int) (angle / sectionAngle) % Math.max(numSections, 3);
    }

    private boolean pointInPolygon(double mx, double my, List<int[]> polygon) {
        if (polygon.isEmpty()) return false;

        boolean inside = false;
        int n = polygon.size();

        for (int i = 0, j = n - 1; i < n; j = i++) {
            int[] current = polygon.get(i);
            int[] previous = polygon.get(j);

            if ((current[1] > my) == (previous[1] > my)) continue;

            double dy = previous[1] - current[1];
            if (dy == 0) continue;

            double dx = previous[0] - current[0];
            double slope = dx / dy;
            double xIntersect = current[0] + (my - current[1]) * slope;

            if (mx < xIntersect) inside = !inside;
        }

        return inside;
    }

    private void executeCommand(String command) {
        if (command == null || command.isEmpty()) {
            McUtils.chat("&cCommand not found");
            return;
        }

        McUtils.chat("&7Running Command: /" + command);

        var player = ClientUtils.player();
        if (player != null) {
            player.connection.sendChat("/" + command);
        }
    }
}