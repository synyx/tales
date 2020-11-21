import { vec3 } from "gl-matrix";

/**
 * Minimal distance required to turn a click into a drag.
 */
const minDragDistance = 6;

let eventPosition = ev => [ev.clientX, ev.clientY, 0];

/**
 * Helper that registers global event listeners for `mousemove` and `mouseup`
 * events to track a user dragging the mouse. Callbacks can be specified to get
 * notified about drag start, changes and end. If no drag operation happened,
 * an optional click handler is called.
 */
export let dragging = (
  ev,
  { onDragStart, onDragChange, onDragEnd, onClick } = {},
) => {
  let dragStart = eventPosition(ev);
  let options = { passive: true };
  let isDragging = !onClick;

  if (isDragging && onDragStart) {
    onDragStart(ev, dragStart);
  }

  let mousemove = ev => {
    ev.stopPropagation();

    let dragPosition = eventPosition(ev);
    if (!isDragging) {
      let dragDistance = vec3.length(
        vec3.sub(vec3.create(), dragStart, dragPosition),
      );
      if (dragDistance > minDragDistance) {
        isDragging = true;
        if (onDragStart) {
          onDragStart(ev, dragStart);
        }
      }
    }

    if (isDragging && onDragChange) {
      onDragChange(ev, dragStart, dragPosition);
    }
  };
  let mouseup = ev => {
    ev.stopPropagation();

    if (isDragging && onDragEnd) {
      onDragEnd(ev, dragStart, eventPosition(ev));
    } else if (!isDragging && onClick) {
      onClick(ev);
    }

    window.removeEventListener("mousemove", mousemove, options);
    window.removeEventListener("mouseup", mouseup, options);
  };
  window.addEventListener("mousemove", mousemove, options);
  window.addEventListener("mouseup", mouseup, options);
};
