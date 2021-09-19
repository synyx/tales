import { h } from "flyps-dom-snabbdom";
import {
  connect,
  connector,
  db,
  handler,
  trigger,
  withInputSignals,
} from "flyps";
import i18n from "../i18n";

/**
 * The application UI theme (light/dark).
 */
export const themeSetting = withInputSignals(
  () => [connect("settings/theme")],
  ([currentTheme]) => {
    return h("div", [
      h("h3", i18n("settings.theme.title")),
      h(
        "div.settings-options",
        [
          { theme: "light", label: i18n("settings.theme.light") },
          { theme: "dark", label: i18n("settings.theme.dark") },
        ].map(({ theme, label }) =>
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
            label,
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
