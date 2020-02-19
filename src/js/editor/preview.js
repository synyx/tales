import { trigger } from "flyps";
import { h } from "flyps-dom-snabbdom";

export function previewRect(
  rect,
  [posterWidth, posterHeight],
  [previewWidth, previewHeight],
) {
  let s =
    previewWidth / previewHeight > rect.width / rect.height
      ? previewHeight / rect.height
      : previewWidth / rect.width;

  return {
    x: -s * rect.x + (previewWidth - s * rect.width) / 2,
    y: -s * rect.y + (previewHeight - s * rect.height) / 2,
    width: s * posterWidth,
    height: s * posterHeight,
  };
}

export function previewItem([tw, th], tale, slide, index) {
  let imgSrc = `/editor/${tale.slug}/${tale["file-path"]}`;
  let { width, height, x, y } = previewRect(
    slide.rect,
    [tale.dimensions.width, tale.dimensions.height],
    [tw, th],
  );
  return h(
    "li.preview",
    {
      style: {
        width: `${tw}px`,
        height: `${th}px`,
      },
      on: {
        click: () => trigger("slide/activate", index),
        dblclick: () => trigger("camera/fit-rect", slide.rect),
      },
    },
    [
      h("img", {
        attrs: {
          src: imgSrc,
          decoding: "async",
          importance: "low",
          loading: "lazy",
        },
        style: {
          width: `${width}px`,
          height: `${height}px`,
          left: `${x}px`,
          top: `${y}px`,
        },
      }),
      h("span.index", (index + 1).toString()),
    ],
  );
}

export function preview(tale) {
  let tw = 100,
    th = 75;
  return h(
    "ol.previews",
    tale.slides.map((slide, index) =>
      previewItem([tw, th], tale, slide, index),
    ),
  );
}
