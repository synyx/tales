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
import "./project";
import "./slide";

import { editor } from "./editor/index";
import { presenter } from "./presenter/index";
import * as router from "./router";

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

function cord(ev) {
  return [
    ev.altKey ? "Alt" : null,
    ev.ctrlKey ? "Ctrl" : null,
    ev.shiftKey ? "Shift" : null,
    ev.metaKey ? "Meta" : null,
    ev.key || ev.keyCode,
  ]
    .filter(ev => !!ev)
    .join("+");
}

handler("key-pressed", ({ db }, eventId, ev) => {
  let presenting = db.activePage === "presenter";
  let editing = db.activePage === "editor";
  let key = cord(ev);
  switch (key) {
    case "Enter":
      ev.preventDefault();
      ev.stopPropagation();
      trigger("slide/focus-current");
      break;
    case "Delete":
      ev.preventDefault();
      ev.stopPropagation();
      if (!presenting) {
        trigger("slide/delete-current");
      }
      break;
    case "Shift+PageUp":
    case "Shift+ArrowLeft":
      ev.preventDefault();
      ev.stopPropagation();
      if (!presenting) {
        trigger("slide/swap-prev");
      }
      break;
    case "Shift+PageDown":
    case "Shift+ArrowRight":
      ev.preventDefault();
      ev.stopPropagation();
      if (!presenting) {
        trigger("slide/swap-next");
      }
      break;
    case "PageUp":
    case "ArrowLeft":
      ev.preventDefault();
      ev.stopPropagation();
      trigger(presenting ? "slide/fly-to-prev" : "slide/activate-prev");
      break;
    case "PageDown":
    case "ArrowRight":
      ev.preventDefault();
      ev.stopPropagation();
      trigger(presenting ? "slide/fly-to-next" : "slide/activate-next");
      break;
    case "Escape":
      if (presenting) {
        ev.preventDefault();
        ev.stopPropagation();
        trigger("router/navigate", `#editor/${db.activeTale}`);
        return;
      }
      if (editing) {
        ev.preventDefault();
        ev.stopPropagation();
        trigger("router/navigate", "#");
        return;
      }
      break;
  }
});

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
  if (input.value()) {
    trigger("projects/add", { name: input.value() });
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

  return h("div#home", [
    h("object.logo", {
      attrs: { type: "image/svg+xml", data: "/images/tales.svg" },
    }),
    h("input", {
      attrs: {
        type: "text",
        placeholder: "Enter the name of your tale",
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
    taleList(),
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
