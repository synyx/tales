import { dragging } from "./drag";

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
});

let fakeEvent = (ev = {}) => ({
  stopPropagation: jest.fn(),
  ...ev,
});

describe("dragging", () => {
  it("adds event listeners", () => {
    dragging({ clientX: 10, clientY: 20 });

    expect(window.addEventListener).toHaveBeenCalledWith(
      "mousemove",
      expect.anything(),
      { passive: true },
    );
    expect(window.addEventListener).toHaveBeenCalledWith(
      "mouseup",
      expect.anything(),
      { passive: true },
    );
  });
  it("removes added event listeners", () => {
    dragging({ clientX: 10, clientY: 20 });

    let mousemoveCb = window.addEventListener.mock.calls.find(
      ([name, _cb]) => name === "mousemove",
    )[1];
    let mouseupCb = window.addEventListener.mock.calls.find(
      ([name, _cb]) => name === "mouseup",
    )[1];

    listeners.mousemove(fakeEvent());
    listeners.mouseup(fakeEvent());

    expect(window.removeEventListener).toHaveBeenCalledWith(
      "mousemove",
      mousemoveCb,
      { passive: true },
    );
    expect(window.removeEventListener).toHaveBeenCalledWith(
      "mouseup",
      mouseupCb,
      { passive: true },
    );
  });
  it("notifies about start, change and end of dragging", () => {
    let dragStart = jest.fn();
    let dragChange = jest.fn();
    let dragEnd = jest.fn();

    let event0 = { clientX: 10, clientY: 20 };
    dragging(event0, {
      onDragStart: dragStart,
      onDragChange: dragChange,
      onDragEnd: dragEnd,
    });
    expect(dragStart).toHaveBeenCalledWith(event0, [10, 20, 0]);

    let event1 = fakeEvent({ clientX: 30, clientY: 40 });
    listeners.mousemove(event1);
    expect(event1.stopPropagation).toHaveBeenCalled();
    expect(dragChange).toHaveBeenCalledWith(event1, [10, 20, 0], [30, 40, 0]);

    let event2 = fakeEvent({ clientX: 50, clientY: 60 });
    listeners.mouseup(event2);
    expect(event2.stopPropagation).toHaveBeenCalled();
    expect(dragEnd).toHaveBeenCalledWith(event2, [10, 20, 0], [50, 60, 0]);
  });
  it("ignores missing callbacks", () => {
    expect(() => {
      dragging({ clientX: 10, clientY: 20 });

      listeners.mousemove(fakeEvent({}));
      listeners.mouseup(fakeEvent({}));
    }).not.toThrow();
  });
  it("only notifies about drag after minimal drag distance is reached", () => {
    let dragStart = jest.fn();
    let dragChange = jest.fn();
    let dragEnd = jest.fn();
    let click = jest.fn();

    dragging(
      { clientX: 10, clientY: 20 },
      {
        onDragStart: dragStart,
        onDragChange: dragChange,
        onDragEnd: dragEnd,
        onClick: click,
      },
    );

    let event1 = fakeEvent({ clientX: 15, clientY: 20 });
    listeners.mousemove(event1);
    expect(dragStart).not.toHaveBeenCalled();
    expect(dragChange).not.toHaveBeenCalled();

    let event2 = fakeEvent({ clientX: 18, clientY: 20 });
    listeners.mousemove(event2);
    expect(dragStart).toHaveBeenCalledWith(event2, [10, 20, 0]);
    expect(dragChange).toHaveBeenCalledWith(event2, [10, 20, 0], [18, 20, 0]);
  });
  it("only notifies about a click if no drag happened", () => {
    let dragStart = jest.fn();
    let dragChange = jest.fn();
    let dragEnd = jest.fn();
    let click = jest.fn();

    dragging(
      { clientX: 10, clientY: 20 },
      {
        onDragStart: dragStart,
        onDragChange: dragChange,
        onDragEnd: dragEnd,
        onClick: click,
      },
    );

    let event1 = fakeEvent({ clientX: 15, clientY: 20 });
    listeners.mousemove(event1);

    let event2 = fakeEvent({ clientX: 15, clientY: 20 });
    listeners.mouseup(event2);

    expect(dragStart).not.toHaveBeenCalled();
    expect(dragChange).not.toHaveBeenCalled();
    expect(dragEnd).not.toHaveBeenCalled();

    expect(click).toHaveBeenCalledWith(event2, [10, 20, 0]);
  });
});
