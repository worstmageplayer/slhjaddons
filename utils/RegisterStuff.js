export class RegisterGroup {
    constructor(createRegisters) {
        this.createRegisters = createRegisters;
        this.registers = {};
    }

    setup() {
        this.registers = this.createRegisters;
    }

    register() {
        if (Object.keys(this.registers).length === 0) this.setup();
        Object.values(this.registers).forEach(it => it.register());
    }

    unregister() {
        if (Object.keys(this.registers).length === 0) this.setup();
        Object.values(this.registers).forEach(it => it.unregister());
    }
}
