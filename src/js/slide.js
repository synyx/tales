import { handler } from "flyps";

import { findTale } from "./project";

/**
 * handlers
 */

export function activate(db, index) {
  return { ...db, editor: { ...db.editor, activeSlide: index } };
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
handler("slide/add", ({ db }, _, slide) => add(db, slide));
handler("slide/update", ({ db }, _, slide) => update(db, slide));
