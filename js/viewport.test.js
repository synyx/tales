import {
  getViewportAspect,
  getViewportRect,
  getViewportMatrix,
  setViewportRect,
  viewport,
} from "./viewport";

// prettier-ignore
let uniformMatrix = () => ([
  1, 0, 0, 0,
  0, 1, 0, 0,
  0, 0, 1, 0,
  0, 0, 0, 1,
]);

describe("viewport", () => {
  it("gets rect", () => {
    let rect = getViewportRect({ viewport: { rect: [0, 1, 2, 3] } });
    expect(rect).toEqual([0, 1, 2, 3]);
  });
  it("gets aspect", () => {
    let aspect = getViewportAspect([0, 1, 2, 3]);
    expect(aspect).toBe(2 / 3);
  });
  it("gets viewport matrix", () => {
    let viewport = getViewportMatrix([1, 2, 800, 600]);
    expect(viewport).toEqualMat4(
      // prettier-ignore
      [
        400,   0, 0, 0,
          0, 300, 0, 0,
          0,   0, 1, 0,
        400, 300, 0, 1,
      ],
    );
  });
  it("sets rect", () => {
    let db = setViewportRect({ viewport: { rect: [0, 0, 0, 0] } }, [
      10,
      20,
      30,
      40,
    ]);
    expect(db.viewport.rect).toEqual([10, 20, 30, 40]);
  });
  it("renders viewport with a default style", () => {
    let vp = viewport(0, 0, uniformMatrix());
    expect(vp.sel).toBe("div.viewport");
    expect(vp.data.style.width).toBe("100%");
    expect(vp.data.style.height).toBe("100%");
    expect(vp.data.style.overflow).toBe("hidden");
    expect(vp.children.length).toBe(1);
  });
  it("renders viewport with additional data", () => {
    let vp = viewport(0, 0, uniformMatrix(), { foo: "bar" });
    expect(vp.sel).toBe("div.viewport");
    expect(vp.data.foo).toBe("bar");
    expect(vp.children.length).toBe(1);
  });
  it("renders viewport with scene", () => {
    let scene = viewport(0, 0, uniformMatrix()).children[0];
    expect(scene.sel).toBe("div.scene");
    expect(scene.children.length).toBe(1);
  });
  it("renders viewport with scene and viewport transformation matrix", () => {
    // prettier-ignore
    let m = [
       0,  1,  2,  3,
      10, 11, 12, 13,
      20, 21, 22, 23,
      30, 31, 32, 33,
    ];
    let scene = viewport(0, 0, m).children[0];
    expect(scene.sel).toBe("div.scene");
    expect(scene.data.style["transform-origin"]).toBe("0 0");
    expect(scene.data.style["transform"]).toBe(
      "matrix3d(0,1,2,3,10,11,12,13,20,21,22,23,30,31,32,33)",
    );
  });
  it("renders world with given size", () => {
    let scene = viewport(1000, 500, uniformMatrix()).children[0];
    let world = scene.children[0];
    expect(world.sel).toBe("div.world");
    expect(world.data.style["width"]).toBe("1000px");
    expect(world.data.style["height"]).toBe("500px");
  });
  it("renders world with given children", () => {
    let scene = viewport(0, 0, uniformMatrix(), {}, ["foo", "bar"]).children[0];
    let world = scene.children[0];
    expect(world.children.length).toBe(2);
    expect(world.children[0].text).toBe("foo");
    expect(world.children[1].text).toBe("bar");
  });
});
