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

import "./camera";
import "./project";

import { editor } from "./editor/index";
import * as router from "./router";

handler("initialize", () => ({
  db: {
    tales: [],
    activePage: "home",
    camera: {
      position: [0, 0, 0],
      scale: 1,
    },
  },
}));

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
        }),
    ),
  );
}
