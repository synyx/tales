import { dragging } from "../util/drag";
import { h } from "flyps-dom-snabbdom";

let slideMarkers = slideRect => [
  {
    position: "top-left",
    cursor: "nw-resize",
    bounds: {
      x: slideRect.x,
      y: slideRect.y,
    },
  },
  {
    position: "top",
    cursor: "n-resize",
    bounds: {
      x: slideRect.x + slideRect.width / 2,
      y: slideRect.y,
    },
  },
  {
    position: "top-right",
    cursor: "ne-resize",
    bounds: {
      x: slideRect.x + slideRect.width,
      y: slideRect.y,
    },
  },
  {
    position: "right",
    cursor: "e-resize",
    bounds: {
      x: slideRect.x + slideRect.width,
      y: slideRect.y + slideRect.height / 2,
    },
  },
  {
    position: "bottom-right",
    cursor: "se-resize",
    bounds: {
      x: slideRect.x + slideRect.width,
      y: slideRect.y + slideRect.height,
    },
  },
  {
    position: "bottom",
    cursor: "s-resize",
    bounds: {
      x: slideRect.x + slideRect.width / 2,
      y: slideRect.y + slideRect.height,
    },
  },
  {
    position: "bottom-left",
    cursor: "sw-resize",
    bounds: {
      x: slideRect.x,
      y: slideRect.y + slideRect.height,
    },
  },
  {
    position: "left",
    cursor: "w-resize",
    bounds: {
      x: slideRect.x,
      y: slideRect.y + slideRect.height / 2,
    },
  },
];

export const slideBounds = (rect, scale, index, options = {}) => {
  let { active, preview, onMove, onMoveEnd, onResize, onResizeEnd, onClick } =
    options;
  let { x, y, width, height } = rect;
  let strokeWidth = 2 / scale;
  let markerSize = 5 / scale;
  let markerClickSize = 15 / scale;
  let markerStrokeWidth = 1.5 / scale;

  let startMove = ev => {
    dragging(ev, {
      onDragChange: onMove,
      onDragEnd: onMoveEnd,
      onClick: onClick,
    });
    ev.stopPropagation();
  };
  let startResize = (ev, position, cursor) => {
    dragging(ev, {
      onDragChange: (ev, ...args) => onResize(ev, position, cursor, ...args),
      onDragEnd: (ev, ...args) => onResizeEnd(ev, position, ...args),
    });
    ev.stopPropagation();
  };
  let showMarkers = active && !preview;
  let markers = showMarkers
    ? [
        h("rect", {
          attrs: {
            x: x,
            y: y,
            width: width,
            height: height,
            "fill-opacity": 0,
            cursor: "move",
          },
          on: {
            mousedown: ev => startMove(ev),
          },
        }),
        ...slideMarkers(rect).map(({ position, cursor, bounds }) =>
          h("g", [
            h("circle.slide-bound-click", {
              id: position,
              attrs: {
                cursor,
                cx: bounds.x,
                cy: bounds.y,
                r: markerClickSize,
              },
              on: {
                mousedown: ev => startResize(ev, position, cursor),
              },
            }),
            h("circle.slide-bound-show", {
              attrs: {
                cursor,
                cx: bounds.x,
                cy: bounds.y,
                r: markerSize,
                "stroke-width": markerStrokeWidth,
              },
            }),
          ]),
        ),
      ]
    : [];

  return h(
    "g.slide-bounds",
    {
      class: { active, preview },
    },
    [
      h("rect.frame", {
        attrs: {
          x: x,
          y: y,
          width: width,
          height: height,
          "stroke-width": strokeWidth,
        },
      }),
      ...markers,
    ],
  );
};
