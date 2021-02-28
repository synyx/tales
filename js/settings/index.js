import {
  causing,
  connect,
  connector,
  db,
  effector,
  handler,
  trigger,
  withInputSignals,
} from "flyps";
import { h } from "flyps-dom-snabbdom";
import "../util/effects";
import { themeSetting } from "./theme";
import { posterDimSetting } from "./poster";

const DEFAULT_SETTINGS = {
  theme: "light",
  posterDim: true,
};

/**
 * The store and key where Tales settings are persisted.
 */
const STORE = window.localStorage;
const SETTINGS_KEY = "tales/settings";

export let settings = withInputSignals(
  () => [connect("settings/visible")],
  ([visible]) => {
    if (!visible) {
      return h("div#settings");
    }
    return h(
      "div#settings.visible",
      { on: { click: () => trigger("settings/hide") } },
      [
        h("div", { on: { click: e => e.stopPropagation() } }, [
          h("header", h("h2", "Settings")),
          h("div.settings-body", [themeSetting(), posterDimSetting()]),
          h("footer", [
            h(
              "button.button.settings-done",
              { on: { click: () => trigger("settings/hide") } },
              "Done",
            ),
          ]),
        ]),
      ],
    );
  },
);

connector(
  "settings/visible",
  withInputSignals(
    () => db,
    db => db.settingsVisible,
  ),
);

handler("settings/show", ({ db }) => {
  return { db: { ...db, settingsVisible: true } };
});

handler("settings/hide", ({ db }) => {
  return { db: { ...db, settingsVisible: false } };
});

handler("settings/changed", ({ db }) => {
  return {
    settings: {
      theme: db.theme,
      posterDim: db.posterDim,
    },
  };
});

/**
 * Stores the given settings to the local storage.
 */
effector("settings", settings => {
  STORE.setItem(SETTINGS_KEY, JSON.stringify(settings));
});

/**
 * Retrieves settings from the local storage or uses the defaults.
 */
causing("settings", () => {
  let storedSettings = STORE.getItem(SETTINGS_KEY);
  if (storedSettings) {
    return { ...DEFAULT_SETTINGS, ...JSON.parse(storedSettings) };
  }
  return DEFAULT_SETTINGS;
});
