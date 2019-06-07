import { signal, connect, connector, withInputSignals } from "flyps";
import { mount, h } from "flyps-dom-snabbdom";

import { xhr } from "./xhr";

const TALES_API = "http://localhost:3449/api/tales";
let tales = signal([]);
connector("tales", () => tales.value());

let page = signal("home");
connector("page", () => page.value());

let slug = signal("slug");
connector("slug", () => slug.value());

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

let home = withInputSignals(
  () => connect("tales"),
  tales => {
    return h(
      "ul",
      tales.map(tale =>
        h("li", [
          h("a", { attrs: { href: `#editor/${tale.slug}` } }, tale.name),
        ]),
      ),
    );
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
  xhr({
    url: TALES_API,
    responseType: "json",
    onSuccess: data => tales.reset(data),
  });

  window.addEventListener("hashchange", router, false);

  mount(document.querySelector("#app"), () =>
    app({
      home: home,
      editor: editor,
    }),
  );
}
