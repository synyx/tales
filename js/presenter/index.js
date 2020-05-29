import { trigger } from "flyps";
import { h } from "flyps-dom-snabbdom";

import { viewport } from "../viewport";

/**
 * views
 */

export function notFound() {
  return h("div", "Unwritten tale…");
}

export let presenter = withInputSignals(
  () => connect("matrix/transform"),
  (transformMatrix, tale) => {
    if (!tale) {
      return notFound();
    }

    let elm;

    let onWindowResize = () => {
      let { left, top, width, height } = elm.getBoundingClientRect();
      trigger("viewport/set-rect", [left, top, width, height]);
    };

    return h("div#presenter", [
      viewport(
        tale.dimensions.width,
        tale.dimensions.height,
        transformMatrix,
        {
          hook: {
            insert: vnode => {
              elm = vnode.elm;
              window.addEventListener("resize", onWindowResize);
              onWindowResize();
            },
            remove: () => {
              elm = null;
              window.removeEventListener("resize", onWindowResize);
            },
          },
        },
        h("img", {
          attrs: {
            src: `/editor/${tale.slug}/${tale["file-path"]}`,
          },
        }),
      ),
    ]);
  },
);
