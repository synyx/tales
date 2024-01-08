import {
  db,
  signal,
  trigger,
  connect,
  connector,
  handler,
  withInputSignals,
  triggerImmediately,
  injectCause,
} from "flyps";
import { mount, h } from "flyps-dom-snabbdom";

import "./animation";
import "./camera";
import "./export";
import "./keyboard";
import "./project";
import "./slide";

import { editor } from "./editor/index";
import { presenter } from "./presenter/index";
import * as router from "./router";
import { i18n, dateFormatter } from "./i18n/index";
import { arrowRight } from "./icons";

handler(
  "initialize",
  ({ settings }) => ({
    db: {
      tales: [],
      activePage: "home",
      camera: {
        position: [0, 0, 0],
        scale: 1,
      },
      viewport: {
        rect: [0, 0, 800, 600],
      },
      editor: {},
      theme: settings.theme,
      posterDim: settings.posterDim,
    },
    trigger: ["settings/theme-changed", settings.theme],
  }),
  [injectCause("settings")],
);

handler("page/activate", ({ db }, eventId, page) => ({
  db: { ...db, activePage: page },
}));

connector(
  "page",
  withInputSignals(
    () => db,
    db => db.activePage,
  ),
);

window.addEventListener("keydown", ev => trigger("key-pressed", ev));

let input = signal("");
let clear = () => input.reset("");
let save = () => {
  let name = input.value().trim();
  if (name) {
    trigger("projects/add", { name });
  }
  clear();
};

let taleItem = tale => {
  return h(
    "li",
    h("a", { attrs: { href: `#editor/${tale.slug}/` } }, [
      h(
        "div.poster",
        h("img", {
          attrs: {
            decoding: "async",
            importance: "low",
            loading: "lazy",
            src: tale["file-path"]
              ? `/editor/${tale.slug}/${tale["file-path"]}`
              : "/images/missing-image.svg",
          },
        }),
      ),
      h("div.title", tale.name),
    ]),
  );
};

let taleList = withInputSignals(
  () => connect("tales"),
  tales => h("ul.tale-list", tales.map(taleItem)),
);

connector("app/version", () => process.env.APP_VERSION || "dev");
connector("app/build-date", () =>
  dateFormatter.format(new Date(process.env.BUILD_DATE)),
);

let footer = () => {
  const version = connect("app/version").value();
  const buildDate = connect("app/build-date").value();
  return h("footer", i18n("home.build-info", { version, buildDate }));
};

let home = () => {
  let onKeydown = e => {
    switch (e.which) {
      case 13: // Enter
        save();
        e.stopPropagation();
        break;
      case 27: // Escape
        clear();
        e.stopPropagation();
        break;
    }
  };
  let onInput = e => input.reset(e.target.value);
  let validName = !!input.value().trim();

  return h("div#home", [
    h("object.logo", {
      attrs: { type: "image/svg+xml", data: "/images/tales.svg" },
    }),
    h("div.tale-name", [
      h("input", {
        attrs: {
          type: "text",
          placeholder: i18n("home.tale-name-prompt"),
          autofocus: true,
          value: input.value(),
        },
        hook: {
          update: (o, n) => (n.elm.value = input.value()),
        },
        on: {
          keydown: onKeydown,
          input: onInput,
        },
      }),
      h(
        "button.icon-button",
        { attrs: { disabled: !validName }, on: { click: save } },
        arrowRight(),
      ),
    ]),
    taleList(),
    footer(),
  ]);
};

let app = withInputSignals(
  () => connect("page"),
  (page, pages) => h("div#app", pages[page]()),
);

export function init() {
  triggerImmediately("initialize");

  router.init();

  trigger("projects/get-all");

  mount(
    document.querySelector("#app"),
    withInputSignals(
      () => connect("tale"),
      tale =>
        app({
          home: home,
          editor: () => editor(tale),
          presenter: () => presenter(tale),
        }),
    ),
  );
}
