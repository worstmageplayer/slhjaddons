export function drawHollowRect(color, x, y, width, height, thickness = 1) {
    const shape = new Shape(color);
    shape.setDrawMode(7); // GL_QUADS

    // Top
    shape.addVertex(x, y);
    shape.addVertex(x + width, y);
    shape.addVertex(x + width, y + thickness);
    shape.addVertex(x, y + thickness);

    // Bottom
    shape.addVertex(x, y + height - thickness);
    shape.addVertex(x + width, y + height - thickness);
    shape.addVertex(x + width, y + height);
    shape.addVertex(x, y + height);

    // Left
    shape.addVertex(x, y + thickness);
    shape.addVertex(x + thickness, y + thickness);
    shape.addVertex(x + thickness, y + height - thickness);
    shape.addVertex(x, y + height - thickness);

    // Right
    shape.addVertex(x + width - thickness, y + thickness);
    shape.addVertex(x + width, y + thickness);
    shape.addVertex(x + width, y + height - thickness);
    shape.addVertex(x + width - thickness, y + height - thickness);

    shape.draw();
}