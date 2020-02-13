import { mat4 } from "gl-matrix";

import { viewport } from "./viewport";

describe("viewport", () => {
  it("renders viewport with a default style", () => {
    let vp = viewport(0, 0, mat4.create());
    expect(vp.sel).toBe("div.viewport");
    expect(vp.data.style.width).toBe("800px");
    expect(vp.data.style.height).toBe("600px");
    expect(vp.data.style.overflow).toBe("hidden");
    expect(vp.children.length).toBe(1);
  });
  it("renders viewport with additional data", () => {
    let vp = viewport(0, 0, mat4.create(), { foo: "bar" });
    expect(vp.sel).toBe("div.viewport");
    expect(vp.data.foo).toBe("bar");
    expect(vp.children.length).toBe(1);
  });
  it("renders viewport with scene", () => {
    let scene = viewport(800, 600, mat4.create()).children[0];
    expect(scene.sel).toBe("div.scene");
    expect(scene.children.length).toBe(1);
  });
  it("renders viewport with scene and viewport transformation matrix", () => {
    let scene = viewport(800, 600, mat4.create()).children[0];
    expect(scene.sel).toBe("div.scene");
    expect(scene.data.style["transform-origin"]).toBe("0 0");
    expect(scene.data.style["transform"]).toBe(
      "matrix3d(400,0,0,0,0,300,0,0,0,0,1,0,400,300,0,1)",
    );
  });
  it("renders world with given size", () => {
    let scene = viewport(800, 600, mat4.create()).children[0];
    let world = scene.children[0];
    expect(world.sel).toBe("div.world");
    expect(world.data.style["width"]).toBe("800px");
    expect(world.data.style["height"]).toBe("600px");
  });
  it("renders world with given transform matrix", () => {
    let scene = viewport(800, 600, mat4.create()).children[0];
    let world = scene.children[0];
    expect(world.sel).toBe("div.world");
    expect(world.data.style["transform-origin"]).toBe("0 0");
    expect(world.data.style["transform"]).toBe(
      "matrix3d(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1)",
    );
  });
  it("renders world with given children", () => {
    let scene = viewport(800, 600, mat4.create(), {}, ["foo", "bar"])
      .children[0];
    let world = scene.children[0];
    expect(world.children.length).toBe(2);
    expect(world.children[0].text).toBe("foo");
    expect(world.children[1].text).toBe("bar");
  });
});
