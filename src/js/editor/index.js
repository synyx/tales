import { signal, trigger } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { mat4, vec3 } from "gl-matrix";

import { dragging } from "../util/drag";
import { viewport } from "../viewport";

let isMoving = signal(false);
let viewportOffset = [0, 0, 0];

export function onWheel(ev, mvp) {
  let mouse = [ev.clientX, ev.clientY, 0];
  let anchor = vec3.transformMat4(
    vec3.create(),
    vec3.sub(vec3.create(), mouse, viewportOffset),
    mat4.invert(mat4.create(), mvp),
  );
  trigger(ev.deltaY > 0 ? "camera/zoom-in" : "camera/zoom-out", anchor);
}

export function onMouseDown(ev, mvp) {
  ev.preventDefault();
  isMoving.reset(true);
  dragging(
    ev,
    (ev, start, end) => {
      let newPosition = vec3.transformMat4(
        vec3.create(),
        vec3.sub(vec3.create(), start, end),
        mat4.invert(mat4.create(), mvp),
      );
      trigger("camera/move-to", newPosition);
    },
    () => isMoving.reset(false),
  );
}

export function notFound() {
  return h("div", "Unwritten tale…");
}

export function editor(tale, mvp) {
  if (!tale) {
    return notFound();
  }
  return h("div#editor", [
    h("header", [
      h("section.left", [h("span.title", tale.name)]),
      h("section.right", [h("a", { attrs: { href: "#" } }, "×")]),
    ]),
    viewport(
      mvp,
      {
        style: {
          cursor: isMoving.value() ? "grabbing" : "grab",
        },
        on: {
          wheel: ev => onWheel(ev, mvp),
          mousedown: ev => onMouseDown(ev, mvp),
        },
        hook: {
          insert: vnode => {
            let rect = vnode.elm.getBoundingClientRect();
            viewportOffset = [rect.left, rect.top, 0];
          },
        },
      },
      h("img", {
        attrs: { src: `/editor/${tale.slug}/${tale["file-path"]}` },
      }),
    ),
  ]);
}
