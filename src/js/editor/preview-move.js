import { signal, trigger } from "flyps";

/**
 * Reordering of slide previews in the sidebar using
 * HTML's native drag and drop mechanism.
 */

const DRAG_DATA_TYPE = "application/x-tales-slide";

const dragIndex = signal(null);
const dropIndex = signal(null);

export const isDraggedSlide = slideIndex => dragIndex.value() === slideIndex;

export const isDropTarget = slideIndex => dropIndex.value() === slideIndex;

export const isDragging = () => dragIndex.value() !== null;

export const dragStart = (slideIndex, ev) => {
  ev.dataTransfer.setData(DRAG_DATA_TYPE, slideIndex);
  ev.dataTransfer.effectAllowed = "move";

  ev.dataTransfer.setDragImage(
    // empty image, has to be part of the DOM
    document.getElementById("drag-image"),
    1,
    1,
  );
  trigger("slide/deactivate");
  dragIndex.reset(slideIndex);
};

export const dragEnter = (slideIndex, ev) => {
  if (!ev.dataTransfer.types.includes(DRAG_DATA_TYPE)) {
    return;
  }
  ev.preventDefault();
  dropIndex.reset(slideIndex);
};

export const dragOver = (slideIndex, previewHeight, ev) => {
  if (!ev.dataTransfer.types.includes(DRAG_DATA_TYPE)) {
    return;
  }
  ev.preventDefault();
  dropIndex.reset(targetIndex(ev, slideIndex, previewHeight));
};

export const dragLeave = (slideIndex, ev) => {
  ev.preventDefault();
  dropIndex.update(v => (v === slideIndex ? null : v));
};

export const drop = (slideIndex, previewHeight, ev) => {
  ev.preventDefault();
  let draggedSlide = ev.dataTransfer.getData(DRAG_DATA_TYPE);
  let targetSlide = targetIndex(ev, slideIndex, previewHeight);
  let moveTo = targetSlide > draggedSlide ? targetSlide - 1 : targetSlide;
  if (draggedSlide !== moveTo) {
    trigger("slide/move", draggedSlide, moveTo);
  }
};

export const dragEnd = ev => {
  ev.preventDefault();
  dragIndex.reset(null);
  dropIndex.reset(null);
};

const targetIndex = (ev, slideIndex, previewHeight) => {
  let cursorY = ev.clientY - ev.currentTarget.getBoundingClientRect().top;
  let insertAfter = cursorY > previewHeight / 2;
  return slideIndex + (insertAfter ? 1 : 0);
};
