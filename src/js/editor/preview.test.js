import { previewRect } from "./preview";

describe("previewRect", () => {
  it("calcutates rect for landscape slides", () => {
    let poster = [800, 400];
    let target = [100, 50];
    let rect = { x: 400, y: 300, width: 200, height: 100 };
    let { x, y, width, height } = previewRect(rect, poster, target);

    expect(x).toBe(-200);
    expect(y).toBe(-150);
    expect(width).toBe(400);
    expect(height).toBe(200);
  });
  it("calculates rect for portrait slides", () => {
    let poster = [800, 400];
    let target = [100, 50];
    let rect = { x: 400, y: 300, width: 100, height: 200 };
    let { x, y, width, height } = previewRect(rect, poster, target);

    expect(x).toBe(-62.5);
    expect(y).toBe(-75);
    expect(width).toBe(200);
    expect(height).toBe(100);
  });
});
