import { h } from "flyps-dom-snabbdom";
import { viewport } from "../viewport";
import { chevronLeft, chevronRight } from "../icons";
import { trigger } from "flyps";

const notFound = () => h("div", "An unwritten tale…");

export const viewer = (tale, posterUrl, activeSlide) => {
  if (!tale) {
    return notFound();
  }

  return h(
    "div#presenter",
    { on: { click: () => trigger("slide/fly-to-next") } },
    [
      viewport(
        tale.dimensions.width,
        tale.dimensions.height,
        {},
        poster(posterUrl, tale.fileType),
      ),
      controls(tale, activeSlide),
    ],
  );
};

const poster = (posterUrl, fileType) => {
  return h("object.poster", {
    attrs: {
      data: posterUrl,
      type: fileType,
    },
  });
};

const controls = (tale, activeSlide) => {
  const currentSlide = activeSlide === undefined ? 0 : activeSlide + 1;
  return h(
    "div.controls",
    {
      class: { show: !currentSlide },
      on: { click: ev => ev.stopPropagation() },
    },
    [
      h(
        "button",
        { on: { click: () => trigger("slide/fly-to-prev") } },
        chevronLeft(),
      ),
      h("div.progress", `${currentSlide} / ${tale.slides.length}`),
      h(
        "button",
        { on: { click: () => trigger("slide/fly-to-next") } },
        chevronRight(),
      ),
    ],
  );
};
