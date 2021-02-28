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
 * The application UI theme (light/dark).
 */
export const themeSetting = withInputSignals(
  () => [connect("settings/theme")],
  ([currentTheme]) => {
    return h("div", [
      h("label", "Theme"),
      h(
        "div.settings-options",
        ["light", "dark"].map(theme =>
          h("label.checkable", [
            h("input", {
              attrs: {
                type: "radio",
                name: "theme",
                checked: theme === currentTheme,
              },
              on: {
                change: () => trigger("settings/theme-changed", theme),
              },
            }),
            theme,
          ]),
        ),
      ),
    ]);
  },
);

connector(
  "settings/theme",
  withInputSignals(
    () => db,
    db => db.theme,
  ),
);

handler("settings/theme-changed", ({ db }, _, theme) => {
  return {
    db: { ...db, theme },
    attrs: {
      html: {
        theme: theme,
      },
    },
    trigger: ["settings/changed"],
  };
});
