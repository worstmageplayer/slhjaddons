import Settings from "../config";

let commandsList = Settings.quickCommandsList.split(",").map(c => c.trim()).filter(c => c.length);
const pi = Math.PI;
const keyCode = Client.getKeyBindFromDescription("Quick commands") || Keyboard.KEY_Q;
let screenWidth = Renderer.screen.getWidth();
let screenHeight = Renderer.screen.getHeight();
let innerRadius = Settings.innerRadius;
let outerRadius = Settings.outerRadius;
let sectionOffset = Settings.sectionOffset;
const shapes = [];

let guiColour = Settings.quickCommandsColour.getRGB();
let guiHoverColour = Settings.quickCommandsHoverColour.getRGB();
const guiQuickCommands = new Gui();

let hoveredSection = -1;
let deadZone = true;
let innerPoints = [];
let angleOffset = - pi / 2;
let showCommandString = Settings.toggleShowCommandString

// Update Settings
Settings.registerListener('Show Commands', value => showCommandString = value);
Settings.registerListener('Quick Commands Colour', value => guiColour = value.getRGB());
Settings.registerListener('Quick Commands Hover Colour', value => guiHoverColour = value.getRGB());
Settings.registerListener('Outer Radius', value => outerRadius = value);
Settings.registerListener('Inner Radius', value => innerRadius = value);
Settings.registerListener('Section Offset', value => sectionOffset = value);
Settings.registerListener('Commands List', value => commandsList = 
    value.split(",").map(c => c.trim()).filter(c => c.length)
)

// Open Quick Commands Gui
const quickCommandsKeyBind = new KeyBind("Quick commands", keyCode, "AAA Quick commands")

quickCommandsKeyBind.registerKeyPress(() => {
    if (!Settings.toggleQuickCommands) return;

    screenWidth = Renderer.screen.getWidth();
    screenHeight = Renderer.screen.getHeight();

    generateShapesVertex(screenWidth, screenHeight, innerRadius, outerRadius);
    guiQuickCommands.open();
    quickRelease.register();
});

// Handles the release of keybind
const quickRelease = register('tick', () => {
    const isDown = Keyboard.isKeyDown(quickCommandsKeyBind.getKeyCode());
    if (isDown) return;
    !deadZone && quickCommands(hoveredSection);
    guiQuickCommands.close();
    quickRelease.unregister();
    deadZone = true;
    hoveredSection = -1;
}).unregister();

// Quick Commands Gui
guiQuickCommands.registerDraw(() => {
    const mouseX = Client.getMouseX(), mouseY = Client.getMouseY();
    const dx = mouseX - screenWidth / 2, dy = mouseY - screenHeight / 2;
    const shapesLength = shapes.length;

    deadZone = pointInPolygon(mouseX, mouseY, innerPoints);
    hoveredSection = !deadZone ? getMouseSection(dx, dy) : -1;

    for (let shapeSection = 0; shapeSection < shapesLength; shapeSection++) {
        let colour = (shapeSection === hoveredSection) ? guiHoverColour : guiColour;

        let shape = new Shape(colour);
        for (var [x, y] of shapes[shapeSection]) {
            shape.addVertex(x, y);
        }
        shape.draw();
    }

    if (deadZone || !showCommandString) return;
    const commandName = `/${commandsList[hoveredSection]}`;
    Renderer.drawString(commandName, Client.getMouseX() + 2, Client.getMouseY() - 7, true);

    // Draw deadzone
    //let dz = new Shape(Renderer.color(255, 0, 0, 80));
    //innerPoints.forEach(([x, y]) => dz.addVertex(x, y));
    //dz.draw();
});

function quickCommands(mouseSection) {
    const command = commandsList[mouseSection];
    if (!command) return ChatLib.chat('Command not found for section ' + mouseSection);
    ChatLib.chat('Running Command: /' + command);
    ChatLib.command(command);
}

function generateShapesVertex(screenWidth, screenHeight, innerRadius, outerRadius) {
    shapes.length = 0;
    innerPoints.length = 0;

    const centerX = screenWidth / 2, centerY = screenHeight / 2;
    const numSections = Math.max(commandsList.length, 3);
    const sectionAngle = (pi * 2 / numSections)
    const deadRadius = +innerRadius + sectionOffset * 10 / 9;

    for (let i = 0; i < numSections; i++) {
        let angle1 = sectionAngle * i + angleOffset;
        let angle2 = sectionAngle * (i + 1) + angleOffset;
        let avgAngle = (angle1 + angle2) / 2;
        let offsetX = Math.cos(avgAngle) * sectionOffset;
        let offsetY = Math.sin(avgAngle) * sectionOffset;
        let sectionCenterX = centerX + offsetX, sectionCenterY= centerY + offsetY;

        let cos1 = Math.cos(angle1), sin1 = Math.sin(angle1);
        let cos2 = Math.cos(angle2), sin2 = Math.sin(angle2);

        shapes.push([
            [sectionCenterX + cos1 * innerRadius, sectionCenterY+ sin1 * innerRadius],
            [sectionCenterX + cos2 * innerRadius, sectionCenterY+ sin2 * innerRadius],
            [sectionCenterX + cos2 * outerRadius, sectionCenterY+ sin2 * outerRadius],
            [sectionCenterX + cos1 * outerRadius, sectionCenterY+ sin1 * outerRadius]
        ]);

        innerPoints.push([
            centerX + Math.cos(angle1) * deadRadius,
            centerY + Math.sin(angle1) * deadRadius
        ]);
    }
}

function getMouseSection(dx, dy) {
    let angle = Math.atan2(dy, dx) + (pi / 2);
    if (angle < 0) angle += 2 * pi;

    const numSections = Math.max(commandsList.length, 3);
    const sectionAngle = 2 * pi / numSections;
    return Math.floor(angle / sectionAngle);
}

function pointInPolygon(mouseX, mouseY, polygonPoints) {
    let isInside = false;
    const pointCount = polygonPoints.length;

    for (let current = 0, previous = pointCount - 1; current < pointCount; previous = current++) {
        const [currentX, currentY] = polygonPoints[current];
        const [previousX, previousY] = polygonPoints[previous];

        const currentAbove = currentY > mouseY;
        const previousAbove = previousY > mouseY;

        if (currentAbove === previousAbove) continue;

        const verticalChange = previousY - currentY;
        if (verticalChange === 0) continue;

        const horizontalChange = previousX - currentX;
        const slope = horizontalChange / verticalChange;
        const xAtMouseY = currentX + (mouseY - currentY) * slope;

        if (mouseX < xAtMouseY) {
            isInside = !isInside;
        }
    }

    return isInside;
}