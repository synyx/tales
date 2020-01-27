import { mat4 } from "gl-matrix";

import { viewport } from "./viewport";

describe("viewport", () => {
  it("renders viewport with a default style", () => {
    let vp = viewport(mat4.create());
    expect(vp.sel).toBe("div#viewport");
    expect(vp.data.style.width).toBe("800px");
    expect(vp.data.style.height).toBe("600px");
    expect(vp.data.style.overflow).toBe("hidden");
    expect(vp.children.length).toBe(1);
  });
  it("renders viewport with additional data", () => {
    let vp = viewport(mat4.create(), { foo: "bar" });
    expect(vp.sel).toBe("div#viewport");
    expect(vp.data.foo).toBe("bar");
    expect(vp.children.length).toBe(1);
  });
  it("renders scene inside viewport with given transform matrix", () => {
    let scene = viewport(mat4.create()).children[0];
    expect(scene.sel).toBe("div#scene");
    expect(scene.data.style["transform-origin"]).toBe("0 0");
    expect(scene.data.style["transform"]).toBe(
      "matrix3d(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1)",
    );
  });
  it("renders scene inside viewport with given children", () => {
    let scene = viewport(mat4.create(), {}, ["foo", "bar"]).children[0];
    expect(scene.children.length).toBe(2);
    expect(scene.children[0].text).toBe("foo");
    expect(scene.children[1].text).toBe("bar");
  });
});
