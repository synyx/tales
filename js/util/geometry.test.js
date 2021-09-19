import { intersectRects, normalizeRect, padRect } from "./geometry";

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

describe("padRect", () => {
  it("shrinks the rect for positive paddings", () => {
    const padded = padRect({ x: 50, y: 60, width: 100, height: 100 }, 0.1);
    expect(padded).toEqual({ x: 60, y: 70, width: 80, height: 80 });
  });
  it("grows the rect for negative paddings", () => {
    const padded = padRect({ x: 50, y: 60, width: 100, height: 100 }, -0.1);
    expect(padded).toEqual({ x: 40, y: 50, width: 120, height: 120 });
  });
  it("uses the shorter side as base value", () => {
    const portraitRect = { x: 50, y: 50, width: 10, height: 50 };
    expect(padRect(portraitRect, 0.1)).toEqual({
      x: 51,
      y: 51,
      width: 8,
      height: 48,
    });
    const landscapeRect = { x: 50, y: 50, width: 50, height: 10 };
    expect(padRect(landscapeRect, 0.1)).toEqual({
      x: 51,
      y: 51,
      width: 48,
      height: 8,
    });
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
