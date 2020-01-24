let eventPosition = ev => [ev.clientX, ev.clientY, 0];

/**
 * Helper that registers global event listeners for `mousemove` and `mouseup`
 * events to track a user dragging the mouse. Callbacks can be specified to get
 * notified about drag changes and end.
 */
export let dragging = (ev, onDragChange, onDragEnd) => {
  let dragStart = eventPosition(ev);
  let mousemove = ev => {
    ev.preventDefault();
    ev.stopPropagation();

    if (onDragChange) {
      onDragChange(ev, dragStart, eventPosition(ev));
    }
  };
  let mouseup = ev => {
    ev.preventDefault();
    ev.stopPropagation();

    if (onDragEnd) {
      onDragEnd(ev, dragStart, eventPosition(ev));
    }

    window.removeEventListener("mousemove", mousemove);
    window.removeEventListener("mouseup", mouseup);
  };
  window.addEventListener("mousemove", mousemove);
  window.addEventListener("mouseup", mouseup);
};
