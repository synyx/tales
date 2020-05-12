import { handler } from "flyps";

import { findTale } from "./project";

/**
 * handlers
 */

export function activate(db, index) {
  return { ...db, editor: { ...db.editor, activeSlide: index } };
}

export function activatePrev(db) {
  let tale = findTale([db.tales, db.activeTale]);
  let index =
    ((db.editor.activeSlide || 0) + tale.slides.length - 1) %
    tale.slides.length;
  return activate(db, index);
}

export function activateNext(db) {
  let tale = findTale([db.tales, db.activeTale]);
  let index =
    ((db.editor.activeSlide || 0) + tale.slides.length + 1) %
    tale.slides.length;
  return activate(db, index);
}

export function focusCurrent(db) {
  let tale = findTale([db.tales, db.activeTale]);
  let slide = tale.slides[db.editor.activeSlide];
  return {
    trigger: ["camera/fit-rect", slide.rect],
  };
}

export function add(db, slide) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = [...tale.slides, slide];
  return {
    trigger: ["projects/update", { ...tale, slides }],
  };
}

export function update(db, slide) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = [...tale.slides];
  slides[db.editor.activeSlide] = slide;
  return {
    trigger: ["projects/update", { ...tale, slides }],
  };
}

function dbHandler(eventId, handlerFn, interceptors) {
  return handler(
    eventId,
    ({ db }, ...args) => ({ db: handlerFn(db, ...args) }),
    interceptors,
  );
}

dbHandler("slide/activate", (db, _, slideIndex) => activate(db, slideIndex));
dbHandler("slide/activate-prev", db => activatePrev(db));
dbHandler("slide/activate-next", db => activateNext(db));
handler("slide/focus-current", ({ db }) => focusCurrent(db));
handler("slide/add", ({ db }, _, slide) => add(db, slide));
handler("slide/update", ({ db }, _, slide) => update(db, slide));
