export class GuiHandler {
    constructor(guiConfig) {
        this.guiConfig = guiConfig;
        this.clickHandler = this.clicked();
        this.dragHandler = this.dragged();
        this.sizeHandler = this.scrolled();
    }

    clicked() {
        return register('clicked', (mx, my, btn, btnstate) => {
            if (!btnstate) return;
            const scale = this.guiConfig.scale;
            this.guiConfig.x = mx / scale;
            this.guiConfig.y = my / scale;
        }).unregister();
    }

    dragged() {
        return register('dragged', (dx, dy, mx, my, btn) => {
            const scale = this.guiConfig.scale;
            const rawX = mx / scale;
            const rawY = my / scale;
            const distanceSq = dx * dx + dy * dy;
            const gridSize = 5 / scale;

            if (Client.isShiftDown() && distanceSq <= 0.5) {
                this.guiConfig.x = Math.round(rawX / gridSize) * gridSize;
                this.guiConfig.y = Math.round(rawY / gridSize) * gridSize;
            } else {
                this.guiConfig.x = rawX;
                this.guiConfig.y = rawY;
            }
        }).unregister();
    }

    scrolled() {
        return register('scrolled', (mx, my, dir) => {
            if (dir == 1) this.guiConfig.scale += 0.05;
            else this.guiConfig.scale -= 0.05;
            this.guiConfig.x = mx / this.guiConfig.scale;
            this.guiConfig.y = my / this.guiConfig.scale;
        }).unregister();
    }

    register() {
        this.clickHandler.register();
        this.dragHandler.register();
        this.sizeHandler.register();
    }

    unregister() {
        this.clickHandler.unregister();
        this.dragHandler.unregister();
        this.sizeHandler.unregister();
    }
}

// example
// const moveGui = new GuiHandler(data.moveRagnarockCooldownGui);