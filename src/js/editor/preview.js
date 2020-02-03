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
    "div.preview-item",
    {
      style: {
        position: "relative",
        display: "inline-block",
        width: `${tw}px`,
        height: `${th}px`,
        overflow: "hidden",
        cursor: "pointer",
        "border-width": "3px",
        "border-style": "solid",
        "border-color": "transparent",
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
          position: "absolute",
          width: `${width}px`,
          height: `${height}px`,
          left: `${x}px`,
          top: `${y}px`,
        },
      }),
      h(
        "div",
        {
          style: {
            position: "absolute",
            "background-color": "#000a",
            color: "#fff",
            "line-height": "1em",
            "font-size": "0.8em",
            "font-style": "monospace",
            left: "0px",
            bottom: "0px",
            padding: "4px 4px 3px 3px",
          },
        },
        (index + 1).toString(),
      ),
    ],
  );
}

export function preview(tale) {
  let tw = 100, th = 75;
  return h(
    "div.preview",
    tale.slides.map((slide, index) =>
      previewItem([tw, th], tale, slide, index),
    ),
  );
}
