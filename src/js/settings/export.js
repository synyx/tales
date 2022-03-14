import { h } from "flyps-dom-snabbdom";
import { connect, trigger, withInputSignals } from "flyps";
import i18n from "../i18n";

/**
 * Export of a Tale as self-contained HTML file.
 */
export const exportSettings = withInputSignals(
  () => connect("tale"),
  tale => {
    const hasPoster = tale && tale["file-path"];
    if (!hasPoster) {
      return;
    }

    return h("div", [
      h("h3", i18n("settings.export.title")),
      h("p", i18n("settings.export.description")),
      h(
        "button.button",
        { on: { click: () => trigger("export/download", tale) } },
        i18n("settings.export.export-action"),
      ),
    ]);
  },
);
