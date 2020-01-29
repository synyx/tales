import * as flyps from "flyps";
import { mat4, vec3 } from "gl-matrix";

import { onWheel, onMouseDown } from "./index";

flyps.trigger = jest.fn();

let listeners = {};
window.addEventListener = jest.fn((ev, cb) => (listeners[ev] = cb));
window.removeEventListener = jest.fn((ev, cb) => {
  if (listeners[ev] == cb) {
    delete listeners[ev];
  }
});

beforeEach(() => {
  listeners = {};
  window.addEventListener.mockClear();
  window.removeEventListener.mockClear();
  flyps.trigger.mockClear();
});

let fakeEvent = (ev = {}) => ({
  preventDefault: jest.fn(),
  stopPropagation: jest.fn(),
  ...ev,
});

let matrix = (offset, scale) => {
  let mat = mat4.create();
  mat4.translate(mat, mat, vec3.fromValues(offset, offset, 0));
  mat4.scale(mat, mat, vec3.fromValues(scale, scale, 1));
  return mat;
};

describe("onWheel", () => {
  it("zooms out on negative delta", () => {
    onWheel({ deltaY: -1, clientX: 0, clientY: 0 }, mat4.create());
    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/zoom-out",
      vec3.fromValues(0, 0, 0),
    );
  });
  it("zooms out on negative delta with anchor", () => {
    let scale = 2,
      offset = 10,
      mat = matrix(offset, scale);

    onWheel({ deltaY: -1, clientX: 40, clientY: 80 }, mat);
    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/zoom-out",
      vec3.fromValues(15, 35, 0),
    );
  });
  it("zooms in on positive delta", () => {
    onWheel({ deltaY: 1, clientX: 0, clientY: 0 }, mat4.create());
    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/zoom-in",
      vec3.fromValues(0, 0, 0),
    );
  });
  it("zooms in on positive delta with anchor", () => {
    let scale = 2,
      offset = 10,
      mat = matrix(offset, scale);

    onWheel({ deltaY: 1, clientX: 40, clientY: 80 }, mat);
    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/zoom-in",
      vec3.fromValues(15, 35, 0),
    );
  });
});

describe("onMouseDown", () => {
  it("starts dragging", () => {
    let scale = 2,
      offset = 10,
      mat = matrix(offset, scale);

    onMouseDown(fakeEvent({ clientX: 10, clientY: 20 }), mat);

    listeners.mousemove(fakeEvent({ clientX: 40, clientY: 80 }));
    listeners.mouseup(fakeEvent());

    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/move-to",
      vec3.fromValues(
        -30 / scale - offset / scale,
        -60 / scale - offset / scale,
        0,
      ),
    );
  });
});
