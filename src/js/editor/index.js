import { signal, trigger } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { mat4, vec3 } from "gl-matrix";

import { dragging } from "../util/drag";
import { viewport } from "../viewport";

let isMoving = signal(false);

export function onWheel(ev) {
  trigger(ev.deltaY > 0 ? "camera/zoom-in" : "camera/zoom-out");
}

export function onMouseDown(ev, mvp) {
  let originalPosition = mat4.getTranslation(
    vec3.create(),
    mat4.invert(mat4.create(), mvp),
  );
  ev.preventDefault();
  isMoving.reset(true);
  dragging(
    ev,
    (ev, start, end) => {
      let delta = vec3.div(
        vec3.create(),
        vec3.sub(vec3.create(), end, start),
        mat4.getScaling(vec3.create(), mvp),
      );
      let newPosition = vec3.sub(vec3.create(), originalPosition, delta);
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
  return h("div", [
    h("a", { attrs: { href: "#" } }, "Back"),
    h("h1", tale.name),
    viewport(
      mvp,
      {
        style: {
          cursor: isMoving.value() ? "grabbing" : "grab",
        },
        on: {
          wheel: ev => onWheel(ev),
          mousedown: ev => onMouseDown(ev, mvp),
        },
      },
      h("img", {
        attrs: { src: `/editor/${tale.slug}/${tale["file-path"]}` },
      }),
    ),
  ]);
}
