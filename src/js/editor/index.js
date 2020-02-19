import { connect, signal, trigger, withInputSignals } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { mat4, vec3 } from "gl-matrix";

import { dragging } from "../util/drag";
import { viewport } from "../viewport";
import { preview } from "./preview";

let isMoving = signal(false);

export function onWheel(ev, projectFn) {
  let anchor = projectFn(vec3.fromValues(ev.clientX, ev.clientY, 0));
  trigger(ev.deltaY > 0 ? "camera/zoom-in" : "camera/zoom-out", anchor);
  ev.preventDefault();
}

export function onMouseDown(ev, cameraPosition, projectFn) {
  isMoving.reset(true);
  dragging(
    ev,
    (ev, start, end) => {
      let delta = vec3.sub(vec3.create(), projectFn(start), projectFn(end));
      let newPosition = vec3.add(vec3.create(), cameraPosition, delta);
      trigger("camera/move-to", newPosition);
      ev.preventDefault();
    },
    () => isMoving.reset(false),
  );
  ev.preventDefault();
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
  ([mvpMatrix, viewportMatrix, cameraPosition], tale) => {
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

    let elm;

    let onResize = () => {
      let { left, top, width, height } = elm.getBoundingClientRect();
      trigger("viewport/set-rect", [left, top, width, height]);
    };

    return h("div#editor", [
      h("header", [
        h("h2", tale.name),
        h("a.icon", { attrs: { href: "#" } }, "×"),
      ]),
      viewport(
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
              window.addEventListener("resize", onResize);
              onResize();
            },
            remove: () => {
              elm = null;
              window.removeEventListener("resize", onResize);
              onResize();
            },
          },
        },
        [
          poster(`/editor/${tale.slug}/${tale["file-path"]}`),
          layer((tale.slides || []).map(slide => rect(slide.rect))),
        ],
      ),
      h("footer", preview(tale)),
    ]);
  },
);
