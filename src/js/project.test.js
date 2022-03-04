import { findTale, resetView } from "./project";

describe("findTale", () => {
  it("returns tale with matching slug", () => {
    let tales = [{ slug: "foo" }, { slug: "bar" }];
    expect(findTale([tales, "bar"])).toBe(tales[1]);
  });

  it("returns undefined when no matching tale is found", () => {
    let tales = [{ slug: "foo" }];
    expect(findTale([tales, "bar"])).toBeUndefined();
  });
});

describe("resetView", () => {
  it("resets the camera to show the complete active poster", () => {
    let effects = resetView({
      tales: [
        { slug: "foo", dimensions: { x: 0, y: 0, width: 200, height: 200 } },
      ],
      activeTale: "foo",
    });
    expect(effects.trigger).toEqual([
      "camera/fit-rect",
      { x: 0, y: 0, width: 200, height: 200 },
    ]);
  });
  it("gracefully handles non-existing tales", () => {
    let effects = resetView({
      tales: [],
      activeTale: "foo",
    });
    expect(effects).toBeFalsy();
  });
});
