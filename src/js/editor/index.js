import { connect, signal, trigger, withInputSignals } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { mat4, vec3 } from "gl-matrix";

import { chevronRight, gear, home } from "../icons";
import { dragging, dragOverlay } from "../util/drag";
import { viewport } from "../viewport";
import { preview } from "./preview";
import { uploader } from "./upload";
import { slideBounds } from "./slide-bounds";
import { settings } from "../settings";
import { normalizeRect } from "../util/geometry";
import i18n from "../i18n";

/**
 * The speed in which the editor zoom level changes when the user zooms in/out.
 */
const ZOOM_SPEED = 0.5;

let viewportCursor = signal();
let drawRect = signal(null);

function preventNextClickEvent() {
  window.addEventListener("click", ev => ev.stopPropagation(), {
    capture: true,
    once: true,
  });
}

function moveRect(rect, delta) {
  let p1 = [rect.x, rect.y, 0];
  let p2 = vec3.add(vec3.create(), p1, delta);
  return { ...rect, x: p2[0], y: p2[1] };
}

function resizeRect(rect, position, [dx, dy, _dz]) {
  let p1 = [rect.x, rect.y, 0];
  let p2 = [p1[0] + rect.width, p1[1] + rect.height, 0];
  switch (position) {
    case "top-left":
      p1[0] += dx;
      p1[1] += dy;
      break;
    case "top":
      p1[1] += dy;
      break;
    case "top-right":
      p1[1] += dy;
      p2[0] += dx;
      break;
    case "right":
      p2[0] += dx;
      break;
    case "bottom-right":
      p2[0] += dx;
      p2[1] += dy;
      break;
    case "bottom":
      p2[1] += dy;
      break;
    case "bottom-left":
      p1[0] += dx;
      p2[1] += dy;
      break;
    case "left":
      p1[0] += dx;
      break;
  }
  return normalizeRect({
    x: p1[0],
    y: p1[1],
    width: p2[0] - p1[0],
    height: p2[1] - p1[1],
  });
}

export function onWheel(ev, projectFn) {
  let anchor = projectFn(vec3.fromValues(ev.clientX, ev.clientY, 0));
  trigger(
    ev.deltaY < 0 ? "camera/zoom-in" : "camera/zoom-out",
    anchor,
    ZOOM_SPEED,
  );
}

export function onMouseDown(ev, cameraPosition, projectFn) {
  ev.preventDefault();
  ev.stopPropagation();
  dragging(ev, {
    onDragStart: () => viewportCursor.reset("move"),
    onDragChange: (_, start, end) => {
      let delta = vec3.sub(vec3.create(), projectFn(start), projectFn(end));
      let newPosition = vec3.add(vec3.create(), cameraPosition, delta);
      trigger("camera/move-to", newPosition);
    },
    onDragEnd: (_, start, end) => {
      viewportCursor.reset();
      if (!vec3.exactEquals(start, end)) {
        preventNextClickEvent();
      }
    },
    onClick: (ev, position) =>
      trigger("slide/activate-at-position", projectFn(position)),
    cursor: "move",
  });
}

/**
 * views
 */

export function notFound() {
  return h("div", i18n("editor.unwritten-tale"));
}

function poster(url, type) {
  return h("object.poster", {
    attrs: { data: url, type },
  });
}

const layer = withInputSignals(
  () => [connect("settings/poster-dim")],
  ([dim], data, children) => {
    return h("svg.layer", { ...data, class: { ...data.class, dim } }, children);
  },
);

let navigator = (tale, transformMatrix, cameraPosition, activeSlide) => {
  let projectFn = vec =>
      vec3.transformMat4(
        vec3.create(),
        vec,
        mat4.invert(mat4.create(), transformMatrix),
      ),
    scale = mat4.getScaling(vec3.create(), transformMatrix)[0];

  let startCreate = ev => {
    if (ev.ctrlKey || ev.metaKey) {
      dragging(ev, {
        onDragChange: onCreate,
        onDragEnd: onCreateEnd,
        cursor: "crosshair",
      });
      ev.stopPropagation();
      preventNextClickEvent();
    }
  };

  let onCreate = (ev, start, end) => {
    let p1 = projectFn(start);
    let p2 = projectFn(end);
    let rect = normalizeRect({
      x: p1[0],
      y: p1[1],
      width: p2[0] - p1[0],
      height: p2[1] - p1[1],
    });
    drawRect.reset(rect);
  };
  let onCreateEnd = (ev, start, end) => {
    let p1 = projectFn(start);
    let p2 = projectFn(end);
    let rect = normalizeRect({
      x: p1[0],
      y: p1[1],
      width: p2[0] - p1[0],
      height: p2[1] - p1[1],
    });
    drawRect.reset(null);
    trigger("slide/add", { rect });
  };
  let onMove = (ev, slide, start, end) => {
    let delta = vec3.sub(vec3.create(), projectFn(end), projectFn(start));
    let rect = moveRect(slide.rect, delta);
    viewportCursor.reset("move");
    drawRect.reset(rect);
  };
  let onMoveEnd = (ev, slide, start, end) => {
    let delta = vec3.sub(vec3.create(), projectFn(end), projectFn(start));
    let rect = moveRect(slide.rect, delta);
    drawRect.reset(null);
    viewportCursor.reset();
    trigger("slide/update", { ...slide, rect });
  };
  let onResize = (ev, slide, position, cursor, start, end) => {
    let delta = vec3.sub(vec3.create(), projectFn(end), projectFn(start));
    let rect = resizeRect(slide.rect, position, delta);
    viewportCursor.reset(cursor);
    drawRect.reset(rect);
  };
  let onResizeEnd = (ev, slide, position, start, end) => {
    let delta = vec3.sub(vec3.create(), projectFn(end), projectFn(start));
    let rect = resizeRect(slide.rect, position, delta);
    drawRect.reset(null);
    viewportCursor.reset();
    trigger("slide/update", { ...slide, rect });
  };

  return viewport(
    tale.dimensions.width,
    tale.dimensions.height,
    {
      style: {
        cursor: viewportCursor.value() || "grab",
      },
      on: {
        wheel: ev => onWheel(ev, projectFn),
        mousedown: ev => onMouseDown(ev, cameraPosition, projectFn),
      },
    },
    [
      poster(`/editor/${tale.slug}/${tale["file-path"]}`, tale.fileType),
      layer(
        {
          on: {
            mousedown: startCreate,
          },
        },
        [
          ...(tale.slides || []).map((slide, index) =>
            slideBounds(slide.rect, scale, index, {
              active: index === activeSlide,
              onMove: (ev, start, end) => onMove(ev, slide, start, end),
              onMoveEnd: (ev, start, end) => onMoveEnd(ev, slide, start, end),
              onResize: (ev, position, cursor, start, end) =>
                onResize(ev, slide, position, cursor, start, end),
              onResizeEnd: (ev, position, start, end) =>
                onResizeEnd(ev, slide, position, start, end),
              onClick: (ev, position) =>
                trigger("slide/activate-at-position", projectFn(position)),
            }),
          ),
          ...(drawRect.value()
            ? [
                slideBounds(drawRect.value(), scale, null, {
                  active: true,
                  ghost: true,
                }),
              ]
            : []),
        ],
      ),
    ],
  );
};

export let editor = withInputSignals(
  () => [
    connect("matrix/client-transform"),
    connect("camera/position"),
    connect("slide/active"),
  ],
  ([transformMatrix, cameraPosition, activeSlide], tale) => {
    if (!tale) {
      return notFound();
    }
    const hasPoster = tale["file-path"];

    return h("div#editor", [
      h("div.sidebar", [
        h("header.sidebar-header", [
          h("h2.sidebar-title", tale.name),
          hasPoster &&
            h(
              "a.button.sidebar-tell",
              {
                attrs: {
                  href: `#presenter/${tale.slug}/`,
                  title: i18n("editor.tell.hint"),
                },
              },
              [i18n("editor.tell"), h("span.icon.--right", chevronRight())],
            ),
        ]),
        hasPoster ? preview(tale) : h("div.previews-empty"),
        h("footer.sidebar-footer", [
          h(
            "a.icon-button.sidebar-icon",
            { attrs: { href: "#", title: i18n("editor.back-to-home") } },
            home(),
          ),
          h(
            "button.icon-button.sidebar-icon",
            {
              attrs: { title: i18n("editor.show-settings") },
              on: { click: () => trigger("settings/show") },
            },
            gear(),
          ),
        ]),
      ]),
      tale["file-path"]
        ? navigator(tale, transformMatrix, cameraPosition, activeSlide)
        : uploader(tale),
      settings(),
      dragOverlay(),
    ]);
  },
);
