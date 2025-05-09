// Each element creates its own guiManager...
// Will change it tomorrow
// Also have to implement pogobject

/**
 * Manages GUI event registration and dispatching for multiple components.
 * Use this to avoid conflicts when multiple GUI elements need to respond to the same events.
 */
export class GuiManager {
    constructor(gui) {
        this.gui = gui
        this.draws = []
        this.clicks = []
        this.releases = []
        this.drags = []
        this.keys = []

        this.gui.registerDraw((mx, my) => this.draws.forEach(fn => fn(mx, my)))
        this.gui.registerClicked((mx, my, btn) => this.clicks.forEach(fn => fn(mx, my, btn)))
        this.gui.registerMouseReleased(() => this.releases.forEach(fn => fn()))
        this.gui.registerMouseDragged((mx, my, btn) => this.drags.forEach(fn => fn(mx, my, btn)))
        this.gui.registerKeyTyped((char, keyCode) => this.keys.forEach(fn => fn(char, keyCode)))
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

    registerKeyTyped(fn) {
        this.keys.push(fn)
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
    constructor(guiManager, x, y, width = 100, height = 10, defaultValue = 50, min = 0, max = 100, bgColour = Renderer.color(0, 0, 0, 170), sliderColour = Renderer.color(255, 255, 255, 255)) {
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

        guiManager.registerDraw(this.draw.bind(this))
        guiManager.registerClicked(this.click.bind(this))
        guiManager.registerMouseReleased(() => this.clicked = false)
        guiManager.registerMouseDragged(this.drag.bind(this))
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
    constructor(guiManager, x, y, width = 25, height = 10, defaultState = false, onColor = Renderer.color(0, 255, 0, 170), offColor = Renderer.color(0, 0, 0, 170), knobColor = Renderer.color(255, 255, 255, 255)) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.state = defaultState
        this.onColor = onColor
        this.offColor = offColor
        this.knobColor = knobColor
        this.clicked = false

        guiManager.registerDraw(this.draw.bind(this))
        guiManager.registerClicked(this.click.bind(this))
        guiManager.registerMouseReleased(() => this.clicked = false)
        guiManager.registerMouseDragged(this.drag.bind(this))
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
        this.state = (mx - this.x) >= this.width / 2
    }

    getState() {
        return this.state
    }

    setState(newState) {
        this.state = newState
    }
}

export class MultiCheckbox {
    constructor(guiManager, x, y, items = [], states = [], width = 100, height = 20, onColor = Renderer.color(255, 255, 255, 170), offColor = Renderer.color(0, 0, 0, 170)) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.items = items
        this.states = states.length ? states : new Array(items.length).fill(false)
        this.onColor = onColor
        this.offColor = offColor

        guiManager.registerDraw(this.draw.bind(this))
        guiManager.registerClicked(this.click.bind(this))
    }

    draw(mx, my) {
        for (let i = 0; i < this.items.length; i++) {
            let boxColor = this.states[i] ? this.onColor : this.offColor
            let boxX = this.x
            let boxY = this.y + (i * (this.height + 5))

            Renderer.drawRect(boxColor, boxX, boxY, this.width, this.height)

            if (this.states[i]) {
                let padding = 4
                Renderer.drawLine(boxX + padding, boxY + padding, boxX + this.width - padding, boxY + this.height - padding, 2, Renderer.color(255, 255, 255))
                Renderer.drawLine(boxX + this.width - padding, boxY + padding, boxX + padding, boxY + this.height - padding, 2, Renderer.color(255, 255, 255))
            }

            Renderer.drawString(this.items[i], boxX + 5, boxY + (this.height / 2) - 4, true)
        }
    }

    click(mx, my) {
        for (let i = 0; i < this.items.length; i++) {
            let boxX = this.x
            let boxY = this.y + (i * (this.height + 5))

            if (mx >= boxX && mx <= boxX + this.width && my >= boxY && my <= boxY + this.height) {
                this.states[i] = !this.states[i]
            }
        }
    }

    getStates() {
        return this.states
    }

    setStates(newStates) {
        if (newStates.length === this.items.length) {
            this.states = newStates
        }
    }
}