import { mat4 } from "gl-matrix";

import { viewport } from "./viewport";

describe("viewport", () => {
  it("renders viewport with a default style", () => {
    let vp = viewport(0, 0, mat4.create(), mat4.create());
    expect(vp.sel).toBe("div.viewport");
    expect(vp.data.style.width).toBe("100%");
    expect(vp.data.style.height).toBe("100%");
    expect(vp.data.style.overflow).toBe("hidden");
    expect(vp.children.length).toBe(1);
  });
  it("renders viewport with additional data", () => {
    let vp = viewport(0, 0, mat4.create(), mat4.create(), { foo: "bar" });
    expect(vp.sel).toBe("div.viewport");
    expect(vp.data.foo).toBe("bar");
    expect(vp.children.length).toBe(1);
  });
  it("renders viewport with scene", () => {
    let scene = viewport(0, 0, mat4.create(), mat4.create()).children[0];
    expect(scene.sel).toBe("div.scene");
    expect(scene.children.length).toBe(1);
  });
  it("renders viewport with scene and viewport transformation matrix", () => {
    let m = mat4.fromValues(
       0,  1,  2,  3,
      10, 11, 12, 13,
      20, 21, 22, 23,
      30, 31, 32, 33,
    );
    let scene = viewport(0, 0, m, mat4.create()).children[0];
    expect(scene.sel).toBe("div.scene");
    expect(scene.data.style["transform-origin"]).toBe("0 0");
    expect(scene.data.style["transform"]).toBe(
      "matrix3d(0,1,2,3,10,11,12,13,20,21,22,23,30,31,32,33)",
    );
  });
  it("renders world with given size", () => {
    let scene = viewport(1000, 500, mat4.create(), mat4.create()).children[0];
    let world = scene.children[0];
    expect(world.sel).toBe("div.world");
    expect(world.data.style["width"]).toBe("1000px");
    expect(world.data.style["height"]).toBe("500px");
  });
  it("renders world with given transform matrix", () => {
    // prettier-ignore
    let m = mat4.fromValues(
       0,  1,  2,  3,
      10, 11, 12, 13,
      20, 21, 22, 23,
      30, 31, 32, 33,
    );
    let scene = viewport(0, 0, mat4.create(), m).children[0];
    let world = scene.children[0];
    expect(world.sel).toBe("div.world");
    expect(world.data.style["transform-origin"]).toBe("0 0");
    expect(world.data.style["transform"]).toBe(
      "matrix3d(0,1,2,3,10,11,12,13,20,21,22,23,30,31,32,33)",
    );
  });
  it("renders world with given children", () => {
    let scene = viewport(0, 0, mat4.create(), mat4.create(), {}, ["foo", "bar"])
      .children[0];
    let world = scene.children[0];
    expect(world.children.length).toBe(2);
    expect(world.children[0].text).toBe("foo");
    expect(world.children[1].text).toBe("bar");
  });
});
