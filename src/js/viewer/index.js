import {
  connect,
  handler,
  trigger,
  triggerImmediately,
  withInputSignals,
} from "flyps";

import { mount } from "flyps-dom-snabbdom";
import "../animation";
import "../camera";
import "../keyboard";
import "../project";
import "../slide";
import { viewer } from "./viewer";

window.addEventListener("keydown", ev => trigger("key-pressed", ev));

handler("initialize", (_, __, tale) => ({
  db: {
    activeTale: tale.slug,
    activePage: "presenter",
    editor: {},
    tales: [{ ...tale }],
    camera: {
      position: [0, 0, 0],
      scale: 1,
    },
    viewport: {
      rect: [0, 0, 800, 600],
    },
  },
}));

export const init = (tale, imgData) => {
  triggerImmediately("initialize", tale);

  trigger("project/reset-view");

  fetch(imgData)
    .then(res => res.blob())
    .then(blob => {
      const objectURL = window.URL.createObjectURL(blob);

      mount(
        document.querySelector("#app"),
        withInputSignals(
          () => [connect("tale"), connect("slide/active")],
          ([tale, activeSlide]) => viewer(tale, objectURL, activeSlide),
        ),
      );
    });
};
