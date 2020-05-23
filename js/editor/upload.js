import { trigger } from "flyps";
import { h } from "flyps-dom-snabbdom";

export let uploader = tale => {
  return h("div.poster-uploader", [
    h("h2", "You haven't uploaded a poster yet."),
    h("h3", "Please do so now to start editing your tale!"),
    h("input", {
      attrs: {
        type: "file",
      },
      on: {
        change: ev =>
          trigger("projects/update-image", tale, ev.target.files[0]),
      },
    }),
  ]);
};
