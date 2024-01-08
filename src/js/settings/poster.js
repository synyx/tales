import { h } from "flyps-dom-snabbdom";
import {
  connect,
  connector,
  db,
  handler,
  trigger,
  withInputSignals,
} from "flyps";
import { i18n } from "../i18n";

/**
 * Settings for the poster in the editor.
 */
export const posterDimSetting = withInputSignals(
  () => [connect("settings/poster-dim")],
  ([dim]) => {
    return h("div", [
      h("h3", i18n("settings.dim-poster.title")),
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
          i18n("settings.dim-poster.description"),
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
