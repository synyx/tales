import { h } from "flyps-dom-snabbdom";

let matrix3d = mat => `matrix3d(${mat.join(",")})`;

export function viewport(
  worldWidth,
  worldHeight,
  viewportMatrix,
  mvpMatrix,
  data = {},
  children = [],
) {
  return h(
    "div.viewport",
    {
      ...data,
      style: {
        width: "100%",
        height: "100%",
        overflow: "hidden",
        ...data.style,
      },
    },
    h(
      "div.scene",
      {
        style: {
          "transform-origin": "0 0",
          transform: matrix3d(viewportMatrix),
        },
      },
      h(
        "div.world",
        {
          style: {
            "transform-origin": "0 0",
            transform: matrix3d(mvpMatrix),
            width: `${worldWidth}px`,
            height: `${worldHeight}px`,
          },
        },
        children,
      ),
    ),
  );
}
