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
import { posterDimSetting } from "./poster";
import { themeSetting } from "./theme";
import { transitionSettings } from "./transition";
import i18n from "../i18n";
import { exportSettings } from "./export";

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
          h("div.settings-body", [
            h("div", [
              h("h2", i18n("settings.tale")),
              transitionSettings(),
              exportSettings(),
            ]),
            h("div.vr"),
            h("div", [
              h("h2", i18n("settings.global")),
              themeSetting(),
              posterDimSetting(),
            ]),
          ]),
          h("footer", [
            h(
              "button.button.settings-done",
              { on: { click: () => trigger("settings/hide") } },
              i18n("settings.done"),
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
