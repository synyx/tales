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

describe("onWheel", () => {
  it("zooms out on negative delta", () => {
    onWheel({ deltaY: -1 });
    expect(flyps.trigger).toHaveBeenCalledWith("camera/zoom-out");
  });
  it("zooms in on positive delta", () => {
    onWheel({ deltaY: 1 });
    expect(flyps.trigger).toHaveBeenCalledWith("camera/zoom-in");
  });
});

describe("onMouseDown", () => {
  it("starts dragging", () => {
    onMouseDown(fakeEvent({ clientX: 10, clientY: 20 }), mat4.create());

    listeners.mousemove(fakeEvent({ clientX: 0, clientY: 0 }));
    listeners.mouseup(fakeEvent());

    expect(flyps.trigger).toHaveBeenCalledWith(
      "camera/move-to",
      vec3.fromValues(10, 20, 0),
    );
  });
});
