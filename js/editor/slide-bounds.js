import { dragging } from "../util/drag";
import { h } from "flyps-dom-snabbdom";
import { trigger } from "flyps";

let slideMarkers = (slideRect, markerWidth, markerHeight) => [
  {
    position: "top-left",
    cursor: "nw-resize",
    bounds: {
      x: slideRect.x - markerWidth,
      y: slideRect.y - markerHeight,
      width: 2 * markerWidth,
      height: 2 * markerHeight,
    },
  },
  {
    position: "top",
    cursor: "n-resize",
    bounds: {
      x: slideRect.x + markerWidth,
      y: slideRect.y - markerHeight,
      width: slideRect.width - 2 * markerWidth,
      height: 2 * markerHeight,
    },
  },
  {
    position: "top-right",
    cursor: "ne-resize",
    bounds: {
      x: slideRect.x + slideRect.width - markerWidth,
      y: slideRect.y - markerHeight,
      width: 2 * markerWidth,
      height: 2 * markerHeight,
    },
  },
  {
    position: "right",
    cursor: "e-resize",
    bounds: {
      x: slideRect.x + slideRect.width - markerWidth,
      y: slideRect.y + markerHeight,
      width: 2 * markerWidth,
      height: slideRect.height - 2 * markerHeight,
    },
  },
  {
    position: "bottom-right",
    cursor: "se-resize",
    bounds: {
      x: slideRect.x + slideRect.width - markerWidth,
      y: slideRect.y + slideRect.height - markerHeight,
      width: 2 * markerWidth,
      height: 2 * markerHeight,
    },
  },
  {
    position: "bottom",
    cursor: "s-resize",
    bounds: {
      x: slideRect.x + markerWidth,
      y: slideRect.y + slideRect.height - markerHeight,
      width: slideRect.width - 2 * markerWidth,
      height: 2 * markerHeight,
    },
  },
  {
    position: "bottom-left",
    cursor: "sw-resize",
    bounds: {
      x: slideRect.x - markerWidth,
      y: slideRect.y + slideRect.height - markerHeight,
      width: 2 * markerWidth,
      height: 2 * markerHeight,
    },
  },
  {
    position: "left",
    cursor: "w-resize",
    bounds: {
      x: slideRect.x - markerWidth,
      y: slideRect.y + markerHeight,
      width: 2 * markerWidth,
      height: slideRect.height - 2 * markerHeight,
    },
  },
];

export const slideBounds = (rect, scale, index, options = {}) => {
  let { active, onMove, onMoveEnd, onResize, onResizeEnd } = options;
  let { x, y, width, height } = rect;
  let markerWidth = Math.min(10 / scale, width / 3);
  let markerHeight = Math.min(10 / scale, height / 3);
  let strokeWidth = 2 / scale;

  let startMove = ev => {
    dragging(ev, { onDragChange: onMove, onDragEnd: onMoveEnd });
    ev.stopPropagation();
  };
  let startResize = (ev, position) => {
    dragging(ev, {
      onDragChange: (ev, ...args) => onResize(ev, position, ...args),
      onDragEnd: (ev, ...args) => onResizeEnd(ev, position, ...args),
    });
    ev.stopPropagation();
  };

  let markers = active
    ? [
        h("rect", {
          attrs: {
            x: x + markerWidth,
            y: y + markerHeight,
            width: width - 2 * markerWidth,
            height: height - 2 * markerHeight,
            "fill-opacity": 0,
            cursor: "move",
          },
          on: {
            mousedown: ev => startMove(ev),
          },
        }),
        ...slideMarkers(rect, markerWidth, markerHeight).map(
          ({ position, cursor, bounds }) =>
            h("rect", {
              id: position,
              attrs: {
                cursor,
                ...bounds,
                "fill-opacity": 0,
              },
              on: {
                mousedown: ev => startResize(ev, position),
              },
            }),
        ),
      ]
    : [];

  return h(
    "g.slide-bounds",
    {
      on: { click: () => trigger("slide/activate", index) },
      class: { active: active },
    },
    [
      h("rect.frame", {
        attrs: {
          x: x,
          y: y,
          width: width,
          height: height,
          "stroke-width": strokeWidth,
          "fill-opacity": 0.2,
        },
      }),
      ...markers,
    ],
  );
};
