import {
  db,
  connect,
  signal,
  trigger,
  withInputSignals,
  connector,
} from "flyps";
import { h } from "flyps-dom-snabbdom";
import { mat4, vec3 } from "gl-matrix";

import { chevronRight, gear, home } from "../icons";
import { dragging } from "../util/drag";
import { viewport } from "../viewport";
import { preview } from "./preview";
import { uploader } from "./upload";
import { slideBounds } from "./slide-bounds";
import { settings } from "../settings";

/**
 * The speed in which the editor zoom level changes when the user zooms in/out.
 */
const ZOOM_SPEED = 0.5;

let isMoving = signal(false);
let drawRect = signal(null);

function preventNextClickEvent() {
  window.addEventListener("click", ev => ev.stopPropagation(), {
    capture: true,
    once: true,
  });
}

function normalizeRect(rect) {
  let p1 = [rect.x, rect.y, 0];
  let p2 = [p1[0] + rect.width, p1[1] + rect.height, 0];
  let [x1, y1, _z1] = p1;
  let [x2, y2, _z2] = p2;
  return {
    x: Math.min(x1, x2),
    y: Math.min(y1, y2),
    width: Math.abs(x2 - x1),
    height: Math.abs(y2 - y1),
  };
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
    onDragStart: () => isMoving.reset(true),
    onDragChange: (_, start, end) => {
      let delta = vec3.sub(vec3.create(), projectFn(start), projectFn(end));
      let newPosition = vec3.add(vec3.create(), cameraPosition, delta);
      trigger("camera/move-to", newPosition);
    },
    onDragEnd: (_, start, end) => {
      isMoving.reset(false);
      if (!vec3.exactEquals(start, end)) {
        preventNextClickEvent();
      }
    },
    onClick: () => trigger("slide/deactivate"),
  });
}

/**
 * connectors
 */

connector(
  "editor/active-slide",
  withInputSignals(
    () => db,
    db => db.editor.activeSlide,
  ),
);

/**
 * views
 */

export function notFound() {
  return h("div", "Unwritten tale…");
}

function poster(url) {
  return h("img.poster", {
    attrs: { src: url },
  });
}

function layer(data, children) {
  return h("svg.layer", data, children);
}

let navigator = (tale, transformMatrix, cameraPosition, activeSlide) => {
  let elm,
    projectFn = vec =>
      vec3.transformMat4(
        vec3.create(),
        vec,
        mat4.invert(mat4.create(), transformMatrix),
      ),
    scale = mat4.getScaling(vec3.create(), transformMatrix)[0];

  let onWindowResize = () => {
    let { left, top, width, height } = elm.getBoundingClientRect();
    trigger("viewport/set-rect", [left, top, width, height]);
  };

  let startCreate = ev => {
    if (ev.ctrlKey || ev.metaKey) {
      dragging(ev, {
        onDragChange: onCreate,
        onDragEnd: onCreateEnd,
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
    drawRect.reset(rect);
  };
  let onMoveEnd = (ev, slide, start, end) => {
    let delta = vec3.sub(vec3.create(), projectFn(end), projectFn(start));
    let rect = moveRect(slide.rect, delta);
    drawRect.reset(null);
    trigger("slide/update", { ...slide, rect });
  };
  let onResize = (ev, slide, position, start, end) => {
    let delta = vec3.sub(vec3.create(), projectFn(end), projectFn(start));
    let rect = resizeRect(slide.rect, position, delta);
    drawRect.reset(rect);
  };
  let onResizeEnd = (ev, slide, position, start, end) => {
    let delta = vec3.sub(vec3.create(), projectFn(end), projectFn(start));
    let rect = resizeRect(slide.rect, position, delta);
    drawRect.reset(null);
    trigger("slide/update", { ...slide, rect });
  };

  return viewport(
    tale.dimensions.width,
    tale.dimensions.height,
    {
      style: {
        cursor: isMoving.value() ? "grabbing" : "grab",
      },
      on: {
        wheel: ev => onWheel(ev, projectFn),
        mousedown: ev => onMouseDown(ev, cameraPosition, projectFn),
      },
      hook: {
        insert: vnode => {
          elm = vnode.elm;
          window.addEventListener("resize", onWindowResize);
          onWindowResize();
        },
        remove: () => {
          elm = null;
          window.removeEventListener("resize", onWindowResize);
        },
      },
    },
    [
      poster(`/editor/${tale.slug}/${tale["file-path"]}`),
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
              onResize: (ev, position, start, end) =>
                onResize(ev, slide, position, start, end),
              onResizeEnd: (ev, position, start, end) =>
                onResizeEnd(ev, slide, position, start, end),
            }),
          ),
          ...(drawRect.value() ? [slideBounds(drawRect.value(), scale)] : []),
        ],
      ),
    ],
  );
};

export let editor = withInputSignals(
  () => [
    connect("matrix/transform"),
    connect("camera/position"),
    connect("editor/active-slide"),
  ],
  ([transformMatrix, cameraPosition, activeSlide], tale) => {
    if (!tale) {
      return notFound();
    }

    return h("div#editor", [
      h("div.sidebar", [
        h("header.sidebar-header", [
          h("h2.sidebar-title", tale.name),
          h(
            "a.button.sidebar-tell",
            {
              attrs: {
                href: `#presenter/${tale.slug}/`,
                title: "Start the presentation",
              },
            },
            ["Tell", h("span.icon.--right", chevronRight())],
          ),
        ]),
        preview(tale),
        h("footer.sidebar-footer", [
          h(
            "a.icon.sidebar-icon",
            { attrs: { href: "#", title: "Go back to the home page" } },
            home(),
          ),
          h(
            "button.icon.sidebar-icon",
            {
              attrs: { title: "Show Tales settings" },
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
    ]);
  },
);
