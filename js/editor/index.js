import { connect, signal, trigger, withInputSignals, connector } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { mat4, vec3 } from "gl-matrix";

import { chevronLeft, chevronRight } from "../icons";
import { dragging } from "../util/drag";
import { viewport } from "../viewport";
import { preview } from "./preview";

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
  let delta = Math.abs(ev.deltaY / 3);
  trigger(ev.deltaY > 0 ? "camera/zoom-in" : "camera/zoom-out", anchor, delta);
}

export function onMouseDown(ev, cameraPosition, projectFn) {
  isMoving.reset(true);
  ev.preventDefault();
  ev.stopPropagation();
  dragging(
    ev,
    (ev, start, end) => {
      let delta = vec3.sub(vec3.create(), projectFn(start), projectFn(end));
      let newPosition = vec3.add(vec3.create(), cameraPosition, delta);
      trigger("camera/move-to", newPosition);
    },
    (ev, start, end) => {
      isMoving.reset(false);
      if (!vec3.exactEquals(start, end)) {
        preventNextClickEvent();
      }
    },
  );
}

/**
 * connectors
 */

connector(
  "editor/active-slide",
  withInputSignals(
    () => connect("db"),
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
  return h("img", {
    attrs: { src: url },
  });
}

function layer(data, children) {
  return h("svg.layer", data, children);
}

// prettier-ignore
let slideMarkers = [
  ["top-left",     { x: 0, y: 0, cursor: "nw-resize" }],
  ["top",          { x: 1, y: 0, cursor: "n-resize" }],
  ["top-right",    { x: 2, y: 0, cursor: "ne-resize" }],
  ["right",        { x: 2, y: 1, cursor: "e-resize" }],
  ["bottom-right", { x: 2, y: 2, cursor: "se-resize" }],
  ["bottom",       { x: 1, y: 2, cursor: "s-resize" }],
  ["bottom-left",  { x: 0, y: 2, cursor: "sw-resize" }],
  ["left",         { x: 0, y: 1, cursor: "w-resize" }],
];

function slideBounds(rect, scale, index, options = {}) {
  let { active, onMove, onMoveEnd, onResize, onResizeEnd } = options;
  let { x, y, width, height } = rect;
  let markerWidth = width / 3;
  let markerHeight = height / 3;
  let strokeWidth = 2.5 / scale;

  let startMove = ev => {
    dragging(ev, onMove, onMoveEnd);
    ev.stopPropagation();
  };
  let startResize = (ev, position) => {
    dragging(
      ev,
      (ev, ...args) => onResize(ev, position, ...args),
      (ev, ...args) => onResizeEnd(ev, position, ...args),
    );
    ev.stopPropagation();
  };

  let markers = active
    ? [
        h("rect", {
          attrs: {
            x: x + markerWidth,
            y: y + markerHeight,
            width: markerWidth,
            height: markerHeight,
            "fill-opacity": 0.0,
            cursor: "move",
          },
          on: {
            mousedown: ev => startMove(ev),
          },
        }),
        ...slideMarkers.map(([position, options]) =>
          h("rect", {
            id: position,
            attrs: {
              x: x + options.x * markerWidth,
              y: y + options.y * markerHeight,
              width: markerWidth,
              height: markerHeight,
              "fill-opacity": 0.0,
              cursor: options.cursor,
            },
            on: {
              mousedown: ev => startResize(ev, position),
            },
          }),
        ),
      ]
    : [];

  return h(
    "g.slide-bounds",
    {
      on: { click: () => trigger("slide/activate", index) },
      class: { active: active },
    },
    [
      h("rect.frame", {
        attrs: {
          x: x,
          y: y,
          width: width,
          height: height,
          "stroke-width": strokeWidth,
          "fill-opacity": 0.2,
        },
      }),
      ...markers,
    ],
  );
}

let navigator = (tale, mvpMatrix, viewportMatrix, cameraPosition, activeSlide) => {
  let elm,
    projectFn = vec =>
      vec3.transformMat4(
        vec3.create(),
        vec,
        mat4.invert(
          mat4.create(),
          mat4.mul(mat4.create(), viewportMatrix, mvpMatrix),
        ),
      ),
    scale = Math.max(
      ...vec3.div(
        vec3.create(),
        [1, 1, 1],
        vec3.sub(vec3.create(), projectFn([1, 1, 1]), projectFn([0, 0, 0])),
      ),
    );

  let onWindowResize = () => {
    let { left, top, width, height } = elm.getBoundingClientRect();
    trigger("viewport/set-rect", [left, top, width, height]);
  };

  let startCreate = ev => {
    if (ev.ctrlKey || ev.metaKey) {
      dragging(ev, onCreate, onCreateEnd);
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
    viewportMatrix,
    mvpMatrix,
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
    connect("matrix/mvp"),
    connect("matrix/viewport"),
    connect("camera/position"),
    connect("editor/active-slide"),
  ],
  ([mvpMatrix, viewportMatrix, cameraPosition, activeSlide], tale) => {
    if (!tale) {
      return notFound();
    }

    return h("div#editor", [
      h("header", [
        h("section.left", [
          h("a.icon", { attrs: { href: "#" } }, chevronLeft()),
          h("h2", tale.name),
        ]),
        h("section.right", [
          h("h2", "Tell"),
          h(
            "a.icon",
            { attrs: { href: `#presenter/${tale.slug}/` } },
            chevronRight(),
          ),
        ]),
      ]),
      navigator(tale, mvpMatrix, viewportMatrix, cameraPosition, activeSlide),
      h("footer", preview(tale)),
    ]);
  },
);
