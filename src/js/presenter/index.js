import { h } from "flyps-dom-snabbdom";

import { viewport } from "../viewport";
import i18n from "../i18n";

const notFound = () => h("div", i18n("editor.unwritten-tale"));

export let presenter = tale => {
  if (!tale) {
    return notFound();
  }

  return h("div#presenter", [
    viewport(
      tale.dimensions.width,
      tale.dimensions.height,
      {},
      h("object.poster", {
        attrs: {
          data: `/editor/${tale.slug}/${tale["file-path"]}`,
          type: tale.fileType,
        },
      }),
    ),
  ]);
};
