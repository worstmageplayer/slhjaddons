/**
 * @param {Color} color - The color of the rectangle.
 * @param {number} x - The X coordinate of the top-left corner of the rectangle.
 * @param {number} y - The Y coordinate of the top-left corner of the rectangle.
 * @param {number} width - The total width of the rectangle.
 * @param {number} height - The total height of the rectangle.
 * @param {number} [thickness=1] - The thickness of the border. Defaults to 1.
 */
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