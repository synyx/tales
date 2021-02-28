import { h } from "flyps-dom-snabbdom";

export const chevronLeft = (data = {}) => {
  return h("svg.chevron", { ...data, attrs: { viewBox: [0, 0, 12, 24] } }, [
    h("path", {
      attrs: {
        d: "M9 2 L1 12 L9 22",
        stroke: "currentColor",
        fill: "none",
        "stroke-width": 2,
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
      },
    }),
  ]);
};

export const chevronRight = (data = {}) => {
  return h("svg.chevron", { ...data, attrs: { viewBox: [0, 0, 12, 24] } }, [
    h("path", {
      attrs: {
        d: "M3 2 L11 12 L3 22",
        stroke: "currentColor",
        fill: "none",
        "stroke-width": 2,
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
      },
    }),
  ]);
};

export const home = (data = {}) => {
  return h("svg.home", { ...data, attrs: { viewBox: [0, 0, 12, 11] } }, [
    h("path.base", {
      attrs: {
        d:
          "M 6 2 L 2 5 L 2 9 A 1 1 0 0 0 3 10 L 5 10 L 5 7 l 2 0 l 0 3 " +
          "l 2 0 A 1 1 0 0 0 10 9 l 0 -4 Z",
        stroke: "none",
        fill: "currentColor",
      },
    }),
    h("path.roof", {
      attrs: {
        d: "M 1 5 l 5 -4 l 5 4",
        stroke: "currentColor",
        fill: "none",
        "stroke-width": 2,
        "stroke-linecap": "round",
        "stroke-linejoin": "round",
      },
    }),
  ]);
};

export const gear = (data = {}) => {
  return h("svg.gear", { ...data, attrs: { viewBox: [-12, -12, 24, 24] } }, [
    h("path", {
      attrs: {
        "fill-rule": "evenodd",
        d:
          // cog wheel
          "M 0.9,7.8 2.4,10.7 5.9,9.3 4.9,6.2 6.2,4.9 9.3,5.9 10.7,2.4 " +
          "7.8,0.9 7.8,-0.9 10.7,-2.4 9.3,-5.9 6.2,-4.9 4.9,-6.2 5.9,-9.3 " +
          "2.4,-10.7 0.9,-7.8 -0.9,-7.8 -2.4,-10.7 -5.9,-9.3 -4.9,-6.2 " +
          "-6.2,-4.9 -9.3,-5.9 -10.7,-2.4 -7.8,-0.9 -7.8,0.9 -10.7,2.4 " +
          "-9.3,5.9 -6.2,4.9 -4.9,6.2 -5.9,9.3 -2.4,10.7 -0.9,7.8 z" +
          // inner circle (cut out)
          "M -3 0 A 1 1 0 0 0 3 0 A 1 1 0 0 0 -3 0 Z",
        stroke: "none",
        fill: "currentColor",
      },
    }),
  ]);
};
