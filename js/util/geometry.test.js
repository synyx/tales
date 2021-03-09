import { intersectRects, normalizeRect } from "./geometry";

describe("intersectRects", () => {
  it("calculates the intersection of overlapping rects", () => {
    let intersection = intersectRects(
      { x: 100, y: 200, width: 50, height: 100 },
      { x: 80, y: 220, width: 40, height: 30 },
    );
    expect(intersection).toEqual({ x: 100, y: 220, width: 20, height: 30 });
  });
  it("returns a falsy value for non-intersecting rects", () => {
    let intersection = intersectRects(
      { x: 100, y: 100, width: 50, height: 50 },
      { x: 49, y: 49, width: 50, height: 50 },
    );
    expect(intersection).toBeFalsy();
  });
});

describe("normalizeRect", () => {
  it("normalizes an inverted rect", () => {
    let normalized = normalizeRect({
      x: 100,
      y: 200,
      width: -50,
      height: -100,
    });
    expect(normalized).toEqual({ x: 50, y: 100, width: 50, height: 100 });
  });
  it("keeps an already normalized rect", () => {
    let normalized = normalizeRect({
      x: 100,
      y: 200,
      width: 50,
      height: 100,
    });
    expect(normalized).toEqual({ x: 100, y: 200, width: 50, height: 100 });
  });
});
