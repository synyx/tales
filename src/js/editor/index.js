import { connect, withInputSignals } from "flyps";
import { h } from "flyps-dom-snabbdom";

export const notFound = () => h("div", "Unwritten tale…");

export const editor = withInputSignals(
  () => connect("tale"),
  tale => {
    if (!tale) {
      return notFound();
    }
    return h("div", [
      h("a", { attrs: { href: "#" } }, "Back"),
      h("h1", tale.name),
      h("img", { attrs: { src: `/editor/${tale.slug}/${tale["file-path"]}` } }),
    ]);
  },
);
