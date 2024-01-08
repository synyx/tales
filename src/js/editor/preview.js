import { connect, trigger, withInputSignals } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { intersectRects, padRect } from "../util/geometry";
import * as previewMove from "./preview-move";
import { i18n } from "../i18n";

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

function previewItem([tw, th], tale, slide, index, active) {
  let imgSrc = `/editor/${tale.slug}/${tale["file-path"]}`;
  let { width, height, x, y } = previewRect(
    slide.rect,
    [tale.dimensions.width, tale.dimensions.height],
    [tw, th],
  );
  return h(
    "li.preview-container",
    {
      class: {
        dragging: previewMove.isDragging(),
      },
      on: {
        dragstart: ev => previewMove.dragStart(index, ev),
        dragenter: ev => previewMove.dragEnter(index, ev),
        dragover: ev => previewMove.dragOver(index, th, ev),
        dragleave: ev => previewMove.dragLeave(index, ev),
        drop: ev => previewMove.drop(index, th, ev),
        dragend: ev => previewMove.dragEnd(ev),
      },
    },
    [
      h(
        "div.preview",
        {
          style: {
            width: `${tw}px`,
            height: `${th}px`,
          },
          class: {
            active: active,
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
              draggable: true,
            },
            style: {
              width: `${width}px`,
              height: `${height}px`,
              left: `${x}px`,
              top: `${y}px`,
            },
          }),
          h("span.index", (index + 1).toString()),
          previewMove.isDraggedSlide(index)
            ? h("div.overlay", i18n("editor.move-slide"))
            : null,
        ],
      ),
    ],
  );
}

export const preview = withInputSignals(
  () => [connect("slide/active"), connect("camera/rect")],
  ([activeSlide, cameraRect], tale) => {
    let tw = 200,
      th = 112; // aspect ratio 16:9

    let onInsert = index => {
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
          rect: padRect(croppedRect, 0.1),
        },
        index,
      );
    };

    let items = [gap(onInsert, 0)];
    (tale.slides || []).forEach((slide, index) => {
      items.push(
        previewItem([tw, th], tale, slide, index, index === activeSlide),
        gap(onInsert, index + 1),
      );
    });

    if (!tale.slides || tale.slides.length === 0) {
      items.push(
        h("li.insert-slide-help", i18n("editor.insert-new-slide-help")),
      );
    }

    return h("ol.previews", items);
  },
);

const gap = (onInsert, index) => {
  return h(
    "li.slide-gap",
    {
      class: {
        dragging: previewMove.isDragging(),
      },
    },
    [
      h("div.insert", {
        on: {
          click: () => onInsert(index),
        },
        attrs: { title: i18n("editor.insert-new-slide") },
      }),
      previewMove.isDropTarget(index) ? h("div.move-indicator") : null,
    ],
  );
};
