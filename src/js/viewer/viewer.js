import { h } from "flyps-dom-snabbdom";

import { viewport } from "../viewport";
import i18n from "../i18n";

const notFound = () => h("div", i18n("editor.unwritten-tale"));

export const viewer = (tale, imgData) => {
  if (!tale) {
    return notFound();
  }

  return h("div#presenter", [
    viewport(
      tale.dimensions.width,
      tale.dimensions.height,
      {},
      poster(imgData, tale.fileType),
    ),
  ]);
};

const poster = (imgData, fileType) => {
  return h("object.poster", {
    attrs: {
      data: imgData,
      type: fileType,
    },
  });
};
