let eventPosition = ev => [ev.clientX, ev.clientY, 0];

/**
 * Helper that registers global event listeners for `mousemove` and `mouseup`
 * events to track a user dragging the mouse. Callbacks can be specified to get
 * notified about drag changes and end.
 */
export let dragging = (ev, onDragChange, onDragEnd) => {
  let dragStart = eventPosition(ev);
  let options = { passive: true };
  let mousemove = ev => {
    ev.stopPropagation();

    if (onDragChange) {
      onDragChange(ev, dragStart, eventPosition(ev));
    }
  };
  let mouseup = ev => {
    ev.stopPropagation();

    if (onDragEnd) {
      onDragEnd(ev, dragStart, eventPosition(ev));
    }

    window.removeEventListener("mousemove", mousemove, options);
    window.removeEventListener("mouseup", mouseup, options);
  };
  window.addEventListener("mousemove", mousemove, options);
  window.addEventListener("mouseup", mouseup, options);
};
