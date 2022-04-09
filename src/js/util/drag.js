import { vec3 } from "gl-matrix";
import { h } from "flyps-dom-snabbdom";
import { signal } from "flyps";

/**
 * Minimal distance required to turn a click into a drag.
 */
const minDragDistance = 6;

const eventPosition = ev => [ev.clientX, ev.clientY, 0];
const globalCursor = signal();

/**
 * Helper that registers global event listeners for `mousemove` and `mouseup`
 * events to track a user dragging the mouse. Callbacks can be specified to get
 * notified about drag start, changes and end. If no drag operation happened,
 * an optional click handler is called.
 */
export let dragging = (
  mouseDownEv,
  { onDragStart, onDragChange, onDragEnd, onClick, cursor } = {},
) => {
  let mouseDownPosition = eventPosition(mouseDownEv);
  let options = { passive: true };
  let isDragging = !onClick;

  if (isDragging) {
    globalCursor.reset(cursor);
    onDragStart && onDragStart(mouseDownEv, mouseDownPosition);
  }

  let mousemove = ev => {
    ev.stopPropagation();

    let dragPosition = eventPosition(ev);
    if (!isDragging) {
      globalCursor.reset(cursor);
      let dragDistance = vec3.length(
        vec3.sub(vec3.create(), mouseDownPosition, dragPosition),
      );
      if (dragDistance > minDragDistance) {
        isDragging = true;
        if (onDragStart) {
          onDragStart(ev, mouseDownPosition);
        }
      }
    }

    if (isDragging && onDragChange) {
      onDragChange(ev, mouseDownPosition, dragPosition);
    }
  };
  let mouseup = ev => {
    ev.stopPropagation();

    if (isDragging && onDragEnd) {
      onDragEnd(ev, mouseDownPosition, eventPosition(ev));
    } else if (!isDragging && onClick) {
      onClick(ev, mouseDownPosition);
    }
    globalCursor.reset();

    window.removeEventListener("mousemove", mousemove, options);
    window.removeEventListener("mouseup", mouseup, options);
  };
  window.addEventListener("mousemove", mousemove, options);
  window.addEventListener("mouseup", mouseup, options);
};

/**
 * Global overlay that sets the cursor to the one set in the `dragging` helper.
 */
export const dragOverlay = () => {
  const cursor = globalCursor.value();
  return (
    cursor &&
    h("div#drag-overlay", {
      style: {
        cursor,
      },
    })
  );
};
