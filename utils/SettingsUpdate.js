import Settings from '../config';

const SettingsGui = Java.type('gg.essential.vigilance.gui.SettingsGui');

let vigilanceGuiClosed = register('guiClosed', (event) => {
    if (!(event instanceof SettingsGui)) return;
    // add update here

    vigilanceGuiClosed.unregister();
}).unregister();
