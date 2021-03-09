import { connect, trigger, withInputSignals } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { intersectRects } from "../util/geometry";

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

export function previewItem([tw, th], tale, slide, index, active) {
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
      class: {
        active: active,
      },
      hook: {
        postpatch: vnode => {
          if (active) {
            vnode.elm.scrollIntoView();
          }
        },
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

export const preview = withInputSignals(
  () => [connect("editor/active-slide"), connect("camera/rect")],
  ([activeSlide, cameraRect], tale) => {
    let tw = 200,
      th = 150;

    let onInsert = index => () => {
      let croppedRect = intersectRects(cameraRect, {
        x: 0,
        y: 0,
        ...tale.dimensions,
      });
      if (!croppedRect) {
        return;
      }
      trigger(
        "slide/insert",
        {
          rect: croppedRect,
        },
        index,
      );
    };

    let items = [gap(onInsert(0))];
    (tale.slides || []).forEach((slide, index) => {
      items.push(
        previewItem([tw, th], tale, slide, index, index === activeSlide),
        gap(onInsert(index + 1)),
      );
    });
    return h("ol.previews", items);
  },
);

const gap = onInsert => {
  return h("li.slide-gap", [
    h("div.insert", {
      on: {
        click: onInsert,
      },
      attrs: { title: "Insert new slide" },
    }),
  ]);
};
