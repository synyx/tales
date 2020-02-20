import { h } from "flyps-dom-snabbdom";

export const chevronLeft = (data = {}) => {
  return h("svg.chevron", { ...data, attrs: { viewBox: [0, 0, 12, 24] } }, [
    h("path", {
      attrs: {
        d: "M9 2 L1 12 L9 22",
        fill: "none",
        "stroke-width": 2,
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
      },
    }),
  ]);
};
