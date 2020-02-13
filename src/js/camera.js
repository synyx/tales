import { connector, connect, handler, withInputSignals } from "flyps";
import { mat4, vec3 } from "gl-matrix";

/**
 * connectors
 */

export function getPosition(db) {
  return db.camera.position;
}

export function getScale(db) {
  return db.camera.scale;
}

export function getCameraMatrix(position, scale) {
  let m = mat4.create();
  mat4.translate(m, m, position);
  mat4.scale(m, m, [scale, scale, 1]);
  return m;
}

export function getModelViewMatrix(cameraMatrix) {
  return mat4.invert(mat4.create(), cameraMatrix);
}

export function getProjectionMatrix(aspect) {
  return mat4.ortho(mat4.create(), -aspect, aspect, -1, 1, -1, 1);
}

export function getMVPMatrix(modelViewMatrix, projectionMatrix) {
  return mat4.multiply(mat4.create(), projectionMatrix, modelViewMatrix);
}

export function getViewportRect(db) {
  return db.viewport.rect;
}

export function getViewportMatrix([_x, _y, w, h]) {
  let w2 = w / 2.0;
  let h2 = h / 2.0;
  let m = mat4.create();
  mat4.translate(m, m, [w2, h2, 0]);
  mat4.scale(m, m, [w2, h2, 1]);
  return m;
}

connector(
  "camera/position",
  withInputSignals(
    () => connect("db"),
    db => getPosition(db),
  ),
);

connector(
  "camera/scale",
  withInputSignals(
    () => connect("db"),
    db => getScale(db),
  ),
);

connector(
  "viewport/rect",
  withInputSignals(
    () => connect("db"),
    db => getViewportRect(db),
  ),
);

connector(
  "viewport/aspect",
  withInputSignals(
    () => connect("viewport/rect"),
    ([_x, _y, w, h]) => w / h,
  ),
);

connector(
  "matrix/camera",
  withInputSignals(
    () => [connect("camera/position"), connect("camera/scale")],
    ([position, scale]) => getCameraMatrix(position, scale),
  ),
);

connector(
  "matrix/modelview",
  withInputSignals(
    () => connect("matrix/camera"),
    camera => getModelViewMatrix(camera),
  ),
);

connector(
  "matrix/projection",
  withInputSignals(
    () => connect("viewport/aspect"),
    aspect => getProjectionMatrix(aspect),
  ),
);

connector(
  "matrix/mvp",
  withInputSignals(
    () => [connect("matrix/modelview"), connect("matrix/projection")],
    ([modelview, projection]) => getMVPMatrix(modelview, projection),
  ),
);

connector(
  "matrix/viewport",
  withInputSignals(
    () => connect("db"),
    db => getViewportMatrix(getViewportRect(db)),
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

export function zoomIn(db, anchor) {
  return setScale(db, db.camera.scale * 2, anchor);
}

export function zoomOut(db, anchor) {
  return setScale(db, db.camera.scale / 2, anchor);
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

export function setRect(db, [vx, vy, vw, vh]) {
  return { ...db, viewport: { ...db.viewport, rect: [vx, vy, vw, vh] } };
}

function dbHandler(eventId, handlerFn, interceptors) {
  return handler(
    eventId,
    ({ db }, ...args) => ({ db: handlerFn(db, ...args) }),
    interceptors,
  );
}

dbHandler("camera/zoom-in", (db, id, anchor) => zoomIn(db, anchor));
dbHandler("camera/zoom-out", (db, id, anchor) => zoomOut(db, anchor));
dbHandler("camera/move-to", (db, id, pos) => moveTo(db, pos));
dbHandler("camera/move-by", (db, id, delta) => moveBy(db, delta));
dbHandler("camera/fit-rect", (db, id, rect) =>
  fitRect(db, rect, db.viewport.rect),
);
dbHandler("viewport/set-rect", (db, id, rect) => setRect(db, rect));
