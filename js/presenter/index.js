import { trigger } from "flyps";
import { h } from "flyps-dom-snabbdom";

import { viewport } from "../viewport";
import i18n from "../i18n";

/**
 * views
 */

export function notFound() {
  return h("div", i18n("editor.unwritten-tale"));
}

export let presenter = tale => {
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
      h("object", {
        attrs: {
          data: `/editor/${tale.slug}/${tale["file-path"]}`,
          type: tale.fileType,
        },
      }),
    ),
  ]);
};
