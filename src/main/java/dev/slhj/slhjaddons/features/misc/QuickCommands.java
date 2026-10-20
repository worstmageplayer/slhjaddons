package dev.slhj.slhjaddons.features.misc;

import com.mojang.blaze3d.platform.InputConstants;
import dev.slhj.slhjaddons.core.Feature;
import dev.slhj.slhjaddons.core.Setting;
import dev.slhj.slhjaddons.util.client.ChatUtils;
import dev.slhj.slhjaddons.util.client.ClientUtils;
import dev.slhj.slhjaddons.util.client.InputUtils;
import dev.slhj.slhjaddons.util.client.KeyBindingUtils;
import dev.slhj.slhjaddons.util.render.RenderUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class QuickCommands extends Feature {
    private KeyMapping key;
    private final CommandWheelSession session = new CommandWheelSession();
    private int hoveredSection = -1;
    private boolean inDeadZone = true;
    private final List<String> commands = new ArrayList<>();
    private final Setting.ToggleSetting showCommandOnHover;

    private final Setting.TextSetting commandsSetting;
    private final Setting.SliderSetting innerRadius;
    private final Setting.SliderSetting outerRadius;
    private final Setting.SliderSetting sectionOffset;
    private final Setting.ColorSetting guiColor;
    private final Setting.ColorSetting guiHoverColor;

    public QuickCommands() {
        setLabel("Quick Commands");
        setDescription("Hold the Quick Commands key to choose a command from a radial menu. Assign the key in Options > Controls > Key Binds.");
        setCategory(Category.MISC);
        commandsSetting = text("quick_commands.commands", "Commands", "",
                "Comma-separated commands, e.g. /warp hub, /slhj. Leading / is optional.");
        showCommandOnHover = toggle("quick_commands.show_string", "Show Command On Hover", true);
        innerRadius = intSlider("quick_commands.inner_radius", "Inner Radius (GUI px)", 0, 254, 30);
        outerRadius = intSlider("quick_commands.outer_radius", "Outer Radius (GUI px)", 1, 255, 90);
        sectionOffset = intSlider("quick_commands.section_offset", "Section Offset (GUI px)", 0, 255, 10);
        guiColor = color("quick_commands.color", "Color", 0x80000000);
        guiHoverColor = color("quick_commands.hover_color", "Hover Color", 0x80FFFFFF);
        if (outerRadius.value().get() <= innerRadius.value().get())
            outerRadius.value().set(innerRadius.value().get() + 1);
        innerRadius.value().onChange(value -> {
            if (outerRadius.value().get() <= value) outerRadius.value().set(value + 1);
        });
        outerRadius.value().onChange(value -> {
            if (innerRadius.value().get() >= value) innerRadius.value().set(value - 1);
        });
        commandsSetting.value().onChange(value -> reloadCommands());
    }

    public static final String ID = "quick_commands";
    @Override public String id() { return ID; }

    @Override
    public void init() {
        reloadCommands();

        key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.slhjaddons.quick_commands",
                InputConstants.Type.KEYBOARD,
                InputConstants.UNKNOWN.getValue(),
                KeyBindingUtils.getMainCategory()));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> tick());
        ScreenEvents.BEFORE_INIT.register((mc, screen, w, h) -> cancelWheel(false));
        ClientPlayConnectionEvents.DISCONNECT.register((h, mc) -> cancelWheel(false));

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("slhjaddons", "quick_commands"),
                (graphics, delta) -> render(graphics));
    }

    private void reloadCommands() {
        commands.clear();
        for (String command : commandsSetting.value().get().split(",")) {
            String normalized = ChatUtils.normalizeCommand(command);
            if (!normalized.isEmpty()) commands.add(normalized);
        }
    }

    private void tick() {
        boolean available = isEnabled() && !ClientUtils.inGui() && ClientUtils.player() != null
                && !commands.isEmpty();
        switch (session.update(key.isDown(), available)) {
            case OPEN -> {
                inDeadZone = true;
                hoveredSection = -1;
                ClientUtils.mc().mouseHandler.releaseMouse();
                InputUtils.centerCursor();
            }
            case RELEASE -> {
                String command = !inDeadZone && hoveredSection >= 0 && hoveredSection < commands.size()
                        ? commands.get(hoveredSection) : null;

                cancelWheel(true);
                if (command != null) executeCommand(command);
            }
            case CANCEL -> cancelWheel(true);
            case NONE -> { }
        }
    }

    private void cancelWheel(boolean restoreMouse) {
        session.cancel();
        inDeadZone = true;
        hoveredSection = -1;
        if (restoreMouse && !ClientUtils.inGui() && ClientUtils.player() != null)
            ClientUtils.mc().mouseHandler.grabMouse();
    }

    @Override protected void onDisable() {
        if (session.isOpen()) cancelWheel(true);
    }

    private void render(GuiGraphicsExtractor g) {
        if (!isEnabled() || !session.isOpen() || ClientUtils.inGui()) return;

        int screenWidth = g.guiWidth();
        int screenHeight = g.guiHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        Minecraft mc = ClientUtils.mc();

        double mouseX = mc.mouseHandler.getScaledXPos(mc.getWindow());
        double mouseY = mc.mouseHandler.getScaledYPos(mc.getWindow());

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;

        int innerRad = innerRadius.value().get().intValue();
        int outerRad = outerRadius.value().get().intValue();
        int sectionOff = sectionOffset.value().get().intValue();

        List<int[]> innerPoints = new ArrayList<>();
        populateDeadZonePolygon(
                centerX, centerY,
                innerRad,
                sectionOff,
                innerPoints);

        inDeadZone = pointInPolygon(mouseX, mouseY, innerPoints);
        if (!inDeadZone) {
            hoveredSection = getMouseSection(dx, dy, commands.size());
        } else {
            hoveredSection = -1;
        }

        int numSections = Math.max(commands.size(), 3);
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

            int color = (i == hoveredSection) ? guiHoverColor.value().get() : guiColor.value().get();

            drawFilledQuad(g, p1, p2, p3, p4, color);
        }

        if (showCommandOnHover.value().get() && !inDeadZone && hoveredSection >= 0 && hoveredSection < commands.size()) {
            String commandName = "/" + commands.get(hoveredSection);
            RenderUtils.text(g, commandName, (int) mouseX + 2, (int) mouseY - 7, 0xFFFFFFFF, true);
        }
    }

    private void populateDeadZonePolygon(int centerX, int centerY, int innerRadius, int sectionOffset, List<int[]> innerPoints) {
        int numSections = Math.max(commands.size(), 3);
        double pi = Math.PI;
        double sectionAngle = (pi * 2) / numSections;
        double angleOffset = -pi / 2;
        double deadRadius = innerRadius + sectionOffset * 10 / 9.0;

        for (int i = 0; i < numSections; i++) {
            double angle1 = sectionAngle * i + angleOffset;
            int x = (int) (centerX + Math.cos(angle1) * deadRadius);
            int y = (int) (centerY + Math.sin(angle1) * deadRadius);
            innerPoints.add(new int[]{x, y});
        }
    }

    private void drawFilledQuad(GuiGraphicsExtractor g, int[] p1, int[] p2, int[] p3, int[] p4, int color) {
        drawTriangle(g, p1[0], p1[1], p2[0], p2[1], p3[0], p3[1], color);
        drawTriangle(g, p1[0], p1[1], p3[0], p3[1], p4[0], p4[1], color);
    }

    private void drawTriangle(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
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
                    RenderUtils.fill(g, minX, y, maxX - minX, 1, color);
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
            ChatUtils.chat("&cCommand not found");
            return;
        }

        ChatUtils.chat("&7Running Command: /" + command);

        ChatUtils.runCommand(command);
    }
}
