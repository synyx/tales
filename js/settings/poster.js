import { h } from "flyps-dom-snabbdom";
import {
  connect,
  connector,
  db,
  handler,
  trigger,
  withInputSignals,
} from "flyps";

/**
 * Settings for the poster in the editor.
 */
export const posterDimSetting = withInputSignals(
  () => [connect("settings/poster-dim")],
  ([dim]) => {
    return h("div", [
      h("h3", "Dim poster"),
      h(
        "div.settings-options",

        h("label.checkable", [
          h("input", {
            attrs: {
              type: "checkbox",
              name: "theme",
              checked: dim,
            },
            on: {
              change: () => trigger("settings/poster-dim-changed", !dim),
            },
          }),
          "Dim the poster in the editor to increase contrast",
        ]),
      ),
    ]);
  },
);

connector(
  "settings/poster-dim",
  withInputSignals(
    () => db,
    db => db.posterDim,
  ),
);

handler("settings/poster-dim-changed", ({ db }, _, dim) => {
  return {
    db: { ...db, posterDim: dim },
    trigger: ["settings/changed"],
  };
});
