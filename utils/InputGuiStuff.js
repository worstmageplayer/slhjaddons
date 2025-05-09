// Each element creates its own gui...
// Will change it tomorrow
// Also have to implement pogobject
export class GuiManager {
    constructor(gui) {
        this.gui = gui
        this.draws = []
        this.clicks = []
        this.releases = []
        this.drags = []

        this.gui.registerDraw((mx, my) => this.draws.forEach(fn => fn(mx, my)))
        this.gui.registerClicked((mx, my, btn) => this.clicks.forEach(fn => fn(mx, my, btn)))
        this.gui.registerMouseReleased(() => this.releases.forEach(fn => fn()))
        this.gui.registerMouseDragged((mx, my, btn) => this.drags.forEach(fn => fn(mx, my, btn)))
    }

    registerDraw(fn) {
        this.draws.push(fn)
    }

    registerClicked(fn) {
        this.clicks.push(fn)
    }

    registerMouseReleased(fn) {
        this.releases.push(fn)
    }

    registerMouseDragged(fn) {
        this.drags.push(fn)
    }
}

export class Slider {
    /**
     * Creates an instance of the Slider.
     * @param {number} x - The x-coordinate of the slider.
     * @param {number} y - The y-coordinate of the slider.
     * @param {number} [width = 100] - The width of the slider.
     * @param {number} [height = 10] - The height of the slider.
     * @param {number} [defaultValue=50] - The initial value of the slider.
     * @param {number} [min=0] - The minimum value the slider can have.
     * @param {number} [max=100] - The maximum value the slider can have.
     * @param {Object} [bgColour=Renderer.color(0, 0, 0, 170)] - The background color of the slider.
     * @param {Object} [sliderColour=Renderer.color(255, 255, 255, 170)] - The color of the filled portion of the slider.
     */
    constructor(gui, x, y, width = 100, height = 10, defaultValue = 50, min = 0, max = 100, bgColour = Renderer.color(0, 0, 0, 170), sliderColour = Renderer.color(255, 255, 255, 255)) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.min = min
        this.max = max
        this.value = defaultValue
        this.clicked = false
        this.bgColour = bgColour
        this.sliderColour = sliderColour

        gui.registerDraw(this.draw.bind(this))
        gui.registerClicked(this.click.bind(this))
        gui.registerMouseReleased(() => this.clicked = false)
        gui.registerMouseDragged(this.drag.bind(this))
    }

    /**
     * Draws the slider and its value on the screen.
     * @param {number} mx - The x-coordinate of the mouse.
     * @param {number} my - The y-coordinate of the mouse.
     */
    draw(mx, my) {
        Renderer.drawRect(this.bgColour, this.x, this.y, this.width, this.height)
        const fillWidth = (this.value / 100) * this.width
        Renderer.drawRect(this.sliderColour, this.x, this.y, fillWidth, this.height)
        Renderer.drawString(`${Math.round(this.value)}`, this.x + this.width + 5, this.y + (this.height / 2) - 4, true)
    }

    /**
     * Handles the click event on the slider.
     * @param {number} mx - The x-coordinate of the mouse when clicked.
     * @param {number} my - The y-coordinate of the mouse when clicked.
     */
    click(mx, my) {
        if (mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height) {
            this.clicked = true
            this.updateValue(mx)
        }
    }

    /**
     * Handles the mouse drag event to adjust the slider value.
     * @param {number} mx - The x-coordinate of the mouse while dragging.
     * @param {number} my - The y-coordinate of the mouse while dragging.
     */
    drag(mx, my) {
        if (this.clicked) this.updateValue(mx)
    }

    /**
     * Updates the value of the slider based on the mouse position.
     * The value is clamped between the minimum and maximum range.
     * @param {number} mouseX - The x-coordinate of the mouse during the drag or click.
     */
    updateValue(mouseX) {
        const clampedX = Math.max(this.x, Math.min(mouseX, this.x + this.width))
        this.value = Math.max(this.min, Math.min(this.max, ((clampedX - this.x) / this.width) * 100))
    }

    /**
     * Gets the current value of the slider.
     * @returns {number} The current value of the slider.
     */
    getValue() {
        return this.value
    }
}

export class ToggleButton {
    constructor(gui, x, y, width = 25, height = 10, defaultState = false, onColor = Renderer.color(0, 255, 0, 170), offColor = Renderer.color(0, 0, 0, 170), knobColor = Renderer.color(255, 255, 255, 255)) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.state = defaultState
        this.onColor = onColor
        this.offColor = offColor
        this.knobColor = knobColor
        this.clicked = false

        gui.registerDraw(this.draw.bind(this))
        gui.registerClicked(this.click.bind(this))
        gui.registerMouseReleased(() => this.clicked = false)
        gui.registerMouseDragged(this.drag.bind(this))
    }

    draw(mx, my) {
        const bgColor = this.state ? this.onColor : this.offColor
        Renderer.drawRect(bgColor, this.x, this.y, this.width, this.height)
    
        const knobRadius = this.height * 0.8
        const knobX = this.state ? this.x + this.width - knobRadius - (this.height * 0.1) : this.x + (this.height * 0.1)
        Renderer.drawRect(this.knobColor, knobX, this.y + (this.height * 0.1), knobRadius, knobRadius)
    }

    click(mx, my) {
        if (mx >= this.x && mx <= this.x + this.width && my >= this.y && my <= this.y + this.height) {
            this.clicked = true
            this.state = !this.state
        }
    }

    drag(mx, my) {
        if (!this.clicked) return
        const relativeX = mx - this.x
        const newState = relativeX >= this.width / 2
        this.state = newState
    }

    getState() {
        return this.state
    }

    setState(newState) {
        this.state = newState
    }
}