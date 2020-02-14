import { connect, signal, trigger, withInputSignals } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { mat4, vec3 } from "gl-matrix";

import { dragging } from "../util/drag";
import { viewport } from "../viewport";
import { preview } from "./preview";
import { chevronLeft } from "../icons";

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

function rect(rect, scale) {
  let { x, y, width, height } = rect;
  let strokeWidth = 2.5 / scale;
  let selected = false;
  let color = selected ? "#ff0000" : "#07272b";
  return h(
    "g",
    {},
    h(
      "rect",
      {
        attrs: {
          x: x,
          y: y,
          width: width,
          height: height,
          stroke: color,
          "stroke-width": strokeWidth,
          fill: color,
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

    let onResize = () => {
      let { left, top, width, height } = elm.getBoundingClientRect();
      trigger("viewport/set-rect", [left, top, width, height]);
    };

    return h("div#editor", [
      h("header", [
        h("a.icon", { attrs: { href: "#" } }, chevronLeft()),
        h("h2", tale.name),
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
          layer((tale.slides || []).map(slide => rect(slide.rect, scale))),
        ],
      ),
      h("footer", preview(tale)),
    ]);
  },
);
