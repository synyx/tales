import { db, connector, connect, handler, withInputSignals } from "flyps";
import { mat4, vec3 } from "gl-matrix";

import { dbAnimator } from "./animation";
import { getViewportMatrix } from "./viewport";

/**
 * connectors
 */

export function getPosition(db) {
  return db.camera.position;
}

export function getScale(db) {
  return db.camera.scale || 1;
}

export function getCameraMatrix(position, scale) {
  let m = mat4.create();
  mat4.translate(m, m, position);
  mat4.scale(m, m, [scale, scale, 1]);
  return m;
}

export function getModelViewMatrix(position, scale) {
  let m = getCameraMatrix(position, scale);
  return mat4.invert(m, m);
}

export function getProjectionMatrix(aspect) {
  return mat4.ortho(mat4.create(), -aspect, aspect, -1, 1, -1, 1);
}

export function getMVPMatrix(position, scale, aspect) {
  let m = getProjectionMatrix(aspect);
  return mat4.multiply(m, m, getModelViewMatrix(position, scale));
}

export function getTransformMatrix(position, scale, viewport) {
  let m = getViewportMatrix(viewport);
  return mat4.multiply(
    m,
    m,
    getMVPMatrix(position, scale, viewport[2] / viewport[3]),
  );
}

export function getCameraRect(transform, viewport) {
  let projectFn = vec =>
    vec3.transformMat4(
      vec3.create(),
      vec,
      mat4.invert(mat4.create(), transform),
    );
  let [x1, y1, _z1] = projectFn([0, 0, 0]);
  let [x2, y2, _z2] = projectFn([viewport[2], viewport[3], 0]);
  return {
    x: x1,
    y: y1,
    width: x2 - x1,
    height: y2 - y1,
  };
}

connector(
  "camera/position",
  withInputSignals(
    () => db,
    db => getPosition(db),
  ),
);

connector(
  "camera/scale",
  withInputSignals(
    () => db,
    db => getScale(db),
  ),
);

/**
 * Transform matrix relative to the viewport.
 */
connector(
  "matrix/viewport-transform",
  withInputSignals(
    () => [
      connect("camera/position"),
      connect("camera/scale"),
      connect("viewport/rect"),
    ],
    ([position, scale, viewport]) =>
      getTransformMatrix(position, scale, [0, 0, viewport[2], viewport[3]]),
  ),
);

/**
 * Transform matrix relative to the client (e.g. the browser).
 */
connector(
  "matrix/client-transform",
  withInputSignals(
    () => [
      connect("camera/position"),
      connect("camera/scale"),
      connect("viewport/rect"),
    ],
    ([position, scale, viewport]) =>
      getTransformMatrix(position, scale, viewport),
  ),
);

connector(
  "camera/rect",
  withInputSignals(
    () => [connect("matrix/viewport-transform"), connect("viewport/rect")],
    ([transform, viewport]) => getCameraRect(transform, viewport),
  ),
);

/**
 * handlers
 */

function setScale(db, scale, anchor) {
  let position = db.camera.position;

  if (anchor) {
    let scaledDelta = vec3.scale(
      vec3.create(),
      vec3.sub(vec3.create(), db.camera.position, anchor),
      scale / db.camera.scale,
    );
    position = vec3.add(vec3.create(), anchor, scaledDelta);
  }

  return {
    ...db,
    camera: {
      ...db.camera,
      scale,
      position,
    },
  };
}

export function zoomIn(db, anchor, factor = 1) {
  return setScale(db, db.camera.scale / (1 + factor), anchor);
}

export function zoomOut(db, anchor, factor = 1) {
  return setScale(db, db.camera.scale * (1 + factor), anchor);
}

export function moveTo(db, position) {
  return { ...db, camera: { ...db.camera, position: position } };
}

export function moveBy(db, delta) {
  let position = vec3.add(vec3.create(), getPosition(db), delta);
  return moveTo(db, position);
}

export function fitRect(db, rect, [_vx, _vy, vw, vh]) {
  let position = [rect.x + rect.width / 2, rect.y + rect.height / 2, 0];
  let scale =
    Math.max(
      ...vec3.transformMat4(
        vec3.create(),
        [rect.width, rect.height, 0],
        getProjectionMatrix(vw / vh),
      ),
    ) / 2;

  return { ...db, camera: { ...db.camera, position, scale } };
}

export function cameraAnimator(source, target) {
  let [x1, y1, _z1] = source.position;
  let [x2, y2, _z2] = target.position;
  let s1 = source.scale;
  let s2 = target.scale;
  return (db, progress) => {
    let [x, y, s] =
      progress >= 1.0
        ? [x2, y2, s2]
        : vec3.lerp(vec3.create(), [x1, y1, s1], [x2, y2, s2], progress);
    return {
      ...db,
      camera: {
        ...db.camera,
        position: [x, y, 0],
        scale: s,
      },
    };
  };
}

export function flyToRect(db, rect, viewport, duration) {
  let target = fitRect(db, rect, viewport);
  let fn = cameraAnimator(db.camera, target.camera);
  return {
    animation: ["camera", dbAnimator(fn, duration)],
  };
}

function dbHandler(eventId, handlerFn, interceptors) {
  return handler(
    eventId,
    ({ db }, ...args) => ({ db: handlerFn(db, ...args) }),
    interceptors,
  );
}

dbHandler("camera/zoom-in", (db, id, anchor, factor) =>
  zoomIn(db, anchor, factor),
);
dbHandler("camera/zoom-out", (db, id, anchor, factor) =>
  zoomOut(db, anchor, factor),
);
dbHandler("camera/move-to", (db, id, pos) => moveTo(db, pos));
dbHandler("camera/move-by", (db, id, delta) => moveBy(db, delta));
dbHandler("camera/fit-rect", (db, id, rect) =>
  fitRect(db, rect, db.viewport.rect),
);
handler("camera/fly-to-rect", ({ db }, id, rect, duration) =>
  flyToRect(db, rect, db.viewport.rect, duration),
);
