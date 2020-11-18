import * as flyps from "flyps";
import { mat4, vec3 } from "gl-matrix";

import { onWheel, onMouseDown } from "./index";

/* eslint no-import-assign: off */

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

let projectFn = (offset, scale) => vec =>
  vec3.transformMat4(
    vec3.create(),
    vec,
    mat4.invert(mat4.create(), matrix(offset, scale)),
  );

describe("onWheel", () => {
  it("zooms out on positive delta", () => {
    onWheel(fakeEvent({ deltaY: 1, clientX: 0, clientY: 0 }), vec => vec);
    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/zoom-out",
      vec3.fromValues(0, 0, 0),
      expect.any(Number),
    );
  });
  it("zooms out on positive delta with anchor", () => {
    let scale = 2,
      offset = 10;

    onWheel(
      fakeEvent({ deltaY: 3, clientX: 40, clientY: 80 }),
      projectFn(offset, scale),
    );
    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/zoom-out",
      vec3.fromValues(15, 35, 0),
      expect.any(Number),
    );
  });
  it("zooms in on negative delta", () => {
    onWheel(fakeEvent({ deltaY: -1, clientX: 0, clientY: 0 }), vec => vec);
    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/zoom-in",
      vec3.fromValues(0, 0, 0),
      expect.any(Number),
    );
  });
  it("zooms in on negative delta with anchor", () => {
    let scale = 2,
      offset = 10;

    onWheel(
      fakeEvent({ deltaY: -3, clientX: 40, clientY: 80 }),
      projectFn(offset, scale),
    );
    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/zoom-in",
      vec3.fromValues(15, 35, 0),
      expect.any(Number),
    );
  });
});

describe("onMouseDown", () => {
  it("drags the camera", () => {
    let scale = 1,
      offset = 10;

    onMouseDown(
      fakeEvent({ clientX: 10, clientY: 20 }),
      [-1, -2, 0],
      projectFn(offset, scale),
    );

    listeners.mousemove(fakeEvent({ clientX: 40, clientY: 80 }));
    listeners.mouseup(fakeEvent());

    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/move-to",
      vec3.fromValues(10 - 40 - 1, 20 - 80 - 2, 0),
    );
  });
});
