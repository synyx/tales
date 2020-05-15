import {
  db,
  signal,
  trigger,
  connect,
  connector,
  handler,
  withInputSignals,
  triggerImmediately,
} from "flyps";
import { mount, h } from "flyps-dom-snabbdom";

import "./animation";
import "./camera";
import "./project";
import "./slide";

import { editor } from "./editor/index";
import { presenter } from "./presenter/index";
import * as router from "./router";

handler("initialize", () => ({
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
  },
}));

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
  let key = cord(ev);
  switch (key) {
    case "Enter":
      ev.preventDefault();
      ev.stopPropagation();
      trigger("slide/focus-current");
      break;
    case "ArrowLeft":
      ev.preventDefault();
      ev.stopPropagation();
      trigger(presenting ? "slide/fly-to-prev" : "slide/activate-prev");
      break;
    case "ArrowRight":
      ev.preventDefault();
      ev.stopPropagation();
      trigger(presenting ? "slide/fly-to-next" : "slide/activate-next");
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
      h("div.poster", {
        style: {
          "background-image": tale["file-path"]
            ? `url(/editor/${tale.slug}/${tale["file-path"]})`
            : "url(/images/missing-image.svg)",
        },
      }),
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
        break;
      case 27: // Escape
        clear();
        break;
    }
  };
  let onInput = e => input.reset(e.target.value);

  return h("div#home", [
    h("img.logo", { attrs: { src: "/images/tales.svg" } }),
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
