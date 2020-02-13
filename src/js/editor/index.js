import { connect, signal, trigger, withInputSignals } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { mat4, vec3 } from "gl-matrix";

import { dragging } from "../util/drag";
import { viewport } from "../viewport";
import { preview } from "./preview";

let isMoving = signal(false);
let viewportOffset = [0, 0, 0];

export function onWheel(ev, projectFn) {
  let mouse = vec3.sub(
    vec3.create(),
    [ev.clientX, ev.clientY, 0],
    viewportOffset,
  );
  let anchor = projectFn(mouse);

  trigger(ev.deltaY > 0 ? "camera/zoom-in" : "camera/zoom-out", anchor);
}

export function onMouseDown(ev, originalPosition, projectFn) {
  ev.preventDefault();

  isMoving.reset(true);

  dragging(
    ev,
    (ev, start, end) => {
      let delta = vec3.sub(vec3.create(), projectFn(start), projectFn(end));
      let newPosition = vec3.add(vec3.create(), originalPosition, delta);

      trigger("camera/move-to", newPosition);
    },
    () => isMoving.reset(false),
  );
}

export function notFound() {
  return h("div", "Unwritten tale…");
}

function poster(url) {
  return h("img", {
    attrs: { src: url },
  });
}

function layer(children) {
  return h("svg.layer", {}, children);
}

function rect(rect) {
  return h(
    "g",
    {},
    h(
      "rect",
      {
        attrs: {
          x: rect.x,
          y: rect.y,
          width: rect.width,
          height: rect.height,
          stroke: "red",
          "stroke-width": 2.5,
          fill: "red",
          "fill-opacity": 0.2,
        },
      },
      [],
    ),
  );
}

export let editor = withInputSignals(
  () => [
    connect("matrix/mvp"),
    connect("matrix/viewport"),
    connect("camera/position"),
  ],
  ([mvpMatrix, viewportMatrix, originalPosition], tale) => {
    if (!tale) {
      return notFound();
    }

    let projectFn = vec =>
      vec3.transformMat4(
        vec3.create(),
        vec,
        mat4.invert(
          mat4.create(),
          mat4.mul(mat4.create(), viewportMatrix, mvpMatrix),
        ),
      );

    return h("div#editor", [
      h("header", [
        h("section.left", [h("span.title", tale.name)]),
        h("section.right", [h("a", { attrs: { href: "#" } }, "×")]),
      ]),
      viewport(
        tale.dimensions.width,
        tale.dimensions.height,
        mvpMatrix,
        {
          style: {
            cursor: isMoving.value() ? "grabbing" : "grab",
          },
          on: {
            wheel: ev => onWheel(ev, projectFn),
            mousedown: ev => onMouseDown(ev, originalPosition, projectFn),
          },
          hook: {
            insert: vnode => {
              let rect = vnode.elm.getBoundingClientRect();
              viewportOffset = [rect.left, rect.top, 0];
            },
          },
        },
        [
          poster(`/editor/${tale.slug}/${tale["file-path"]}`),
          layer((tale.slides || []).map(slide => rect(slide.rect))),
        ],
      ),
      h("footer", [h("section.left", preview(tale)), h("section.right")]),
    ]);
  },
);
