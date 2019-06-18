import {
  effector,
  signal,
  trigger,
  connect,
  connector,
  withInputSignals,
} from "flyps";
import { mount, h } from "flyps-dom-snabbdom";

import "./project";
import "./xhr";

let page = signal("home");
connector("page", () => page.value());

let slug = signal("slug");
connector("slug", () => slug.value());

effector("navigate", url => {
  window.location.href = url;
});

export function findTale([tales, slug]) {
  return tales.find(tale => tale.slug === slug);
}

connector(
  "tale",
  withInputSignals(() => [connect("tales"), connect("slug")], findTale),
);

let router = () => {
  let [name, ...args] = location.hash.split("/");
  switch (name) {
    case "#editor":
      page.reset("editor");
      slug.reset(args[0]);
      break;
    default:
      page.reset("home");
  }
};

let input = signal("");
let clear = () => input.reset("");
let save = () => {
  if (input.value()) {
    trigger("projects/add", { name: input.value() });
  }
  clear();
};

let home = withInputSignals(
  () => connect("tales"),
  tales => {
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
          keydown: e => {
            switch (e.which) {
              case 13: // Enter
                save();
                break;
              case 27: // Escape
                clear();
                break;
            }
          },
          input: e => input.reset(e.target.value),
        },
      }),
      h(
        "ul.tales-list",
        tales.map(tale =>
          h(
            "li",
            h("a", { attrs: { href: `/editor/${tale.slug}/` } }, [
              h("div.poster", {
                style: {
                  "background-image": tale["file-path"]
                    ? `url(/editor/${tale.slug}/${tale["file-path"]})`
                    : "url(/images/missing-image.svg)",
                },
              }),
              h("div.title", tale.name),
            ]),
          ),
        ),
      ),
    ]);
  },
);

let editor = withInputSignals(
  () => connect("tale"),
  tale => {
    if (!tale) {
      return h("div", "Unwritten tale…");
    }
    return h("div", [
      h("a", { attrs: { href: "#" } }, "Back"),
      h("h1", tale.name),
      h("img", { attrs: { src: tale["file-path"] } }),
    ]);
  },
);

let app = withInputSignals(
  () => connect("page"),
  (page, pages) => h("div#app", pages[page]()),
);

export function init() {
  router();
  window.addEventListener("hashchange", router, false);

  trigger("projects/get-all");

  mount(document.querySelector("#app"), () =>
    app({
      home: home,
      editor: editor,
    }),
  );
}
