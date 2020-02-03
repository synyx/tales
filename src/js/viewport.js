import { h } from "flyps-dom-snabbdom";

let matrix3d = ([
  m00,
  m01,
  m02,
  m03,
  m10,
  m11,
  m12,
  m13,
  m20,
  m21,
  m22,
  m23,
  m30,
  m31,
  m32,
  m33,
]) => {
  return (
    "matrix3d(" +
    m00 +
    "," +
    m01 +
    "," +
    m02 +
    "," +
    m03 +
    "," +
    m10 +
    "," +
    m11 +
    "," +
    m12 +
    "," +
    m13 +
    "," +
    m20 +
    "," +
    m21 +
    "," +
    m22 +
    "," +
    m23 +
    "," +
    m30 +
    "," +
    m31 +
    "," +
    m32 +
    "," +
    m33 +
    ")"
  );
};

function scene(width, height, mvp, children = []) {
  return h(
    "div.scene",
    {
      style: {
        "transform-origin": "0 0",
        transform: matrix3d(mvp),
        width: `${width}px`,
        height: `${height}px`,
      },
    },
    children,
  );
}

export function viewport(width, height, mvp, data = {}, children = []) {
  return h(
    "div.viewport",
    {
      ...data,
      style: {
        width: "800px",
        height: "600px",
        overflow: "hidden",
        ...data.style,
      },
    },
    scene(width, height, mvp, children),
  );
}
