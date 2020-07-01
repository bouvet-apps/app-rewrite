import {model} from "./model";
import {selectTool} from "./toolbar";
import {modalOpen} from "./modals";

export let defineShortcuts = function () {


    document.onkeyup = function (event) {

        // TODO: Check if any edit-stuff is up, then ignore keypress

        const key = event.key; // "a", "1", "Shift", etc.

        if (key === 'R') {
            if (!modalOpen(model.modals.all)) {
                $(model.buttons.rule.create).trigger("click");
            }
        }

        if (key === 'Escape') {
            $(model.modals.overlay).trigger("click");
        }

    };


};






