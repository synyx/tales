import { handler } from "flyps";

import { findTale } from "./project";

function prevSlide(slides, index) {
  index = Number.isInteger(index) ? index : 0;
  return (index + slides.length - 1) % slides.length;
}

function nextSlide(slides, index) {
  index = Number.isInteger(index) ? index : slides.length - 1;
  return (index + slides.length + 1) % slides.length;
}

/**
 * handlers
 */

export function activate(db, index) {
  return { ...db, editor: { ...db.editor, activeSlide: index } };
}

export function activatePrev(db) {
  let tale = findTale([db.tales, db.activeTale]);
  let index = prevSlide(tale.slides, db.editor.activeSlide);
  return activate(db, index);
}

export function activateNext(db) {
  let tale = findTale([db.tales, db.activeTale]);
  let index = nextSlide(tale.slides, db.editor.activeSlide);
  return activate(db, index);
}

export function isPointInRect(point, rect) {
  return (
    point[0] >= rect.x &&
    point[0] <= rect.x + rect.width &&
    point[1] >= rect.y &&
    point[1] <= rect.y + rect.height
  );
}

/**
 * Actives the slide at the given position.
 *
 * If there are multiple slides at that position, the selection will loop
 * through all of them one by one, starting with the top-most slide.
 */
export function activateAtPosition(db, position) {
  let tale = findTale([db.tales, db.activeTale]);
  let activeSlideIndex = db.editor.activeSlide || 0;
  let i = activeSlideIndex;
  do {
    i = i > 0 ? i - 1 : tale.slides.length - 1;
    if (isPointInRect(position, tale.slides[i].rect)) {
      return activate(db, i);
    }
  } while (i !== activeSlideIndex);

  return deactivate(db);
}

export function deactivate(db) {
  return { ...db, editor: { ...db.editor, activeSlide: undefined } };
}

function arraySwap(array, idx1, idx2) {
  let a = [...array];
  [a[idx1], a[idx2]] = [a[idx2], a[idx1]];
  return a;
}

export function swap(db, idx1, idx2) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = tale.slides || [];
  let count = slides.length - 1;
  if (0 <= idx1 && idx1 <= count && 0 <= idx2 && idx2 <= count) {
    return {
      db: { ...db, editor: { ...db.editor, activeSlide: idx2 } },
      trigger: [
        "projects/update",
        { ...tale, slides: arraySwap(tale.slides, idx1, idx2) },
      ],
    };
  }
}

export function swapPrev(db) {
  let idx = db.editor.activeSlide;
  return swap(db, idx, idx - 1);
}

export function swapNext(db) {
  let idx = db.editor.activeSlide;
  return swap(db, idx, idx + 1);
}

export function flyToPrev(db) {
  let newDb = activatePrev(db);
  return {
    ...flyToCurrent(newDb),
    db: newDb,
  };
}

export function flyToNext(db) {
  let newDb = activateNext(db);
  return {
    ...flyToCurrent(newDb),
    db: newDb,
  };
}

export function focusCurrent(db) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = tale.slides || [];
  let slide = slides[db.editor.activeSlide];
  return {
    trigger: ["camera/fit-rect", slide.rect],
  };
}

export function flyToCurrent(db) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = tale.slides || [];
  let slide = slides[db.editor.activeSlide];
  return {
    trigger: ["camera/fly-to-rect", slide.rect],
  };
}

export function add(db, slide) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = [...(tale.slides || []), slide];
  let index = slides.length - 1;
  return {
    db: activate(db, index),
    trigger: ["projects/update", { ...tale, slides }],
  };
}

export function insert(db, slide, index) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = [...(tale.slides || [])];
  slides.splice(index, 0, slide);
  return {
    db: activate(db, index),
    trigger: ["projects/update", { ...tale, slides }],
  };
}

export function update(db, slide) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = [...(tale.slides || [])];
  slides[db.editor.activeSlide] = slide;
  return {
    trigger: ["projects/update", { ...tale, slides }],
  };
}

export function deleteCurrent(db) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = [...(tale.slides || [])];
  slides.splice(db.editor.activeSlide, 1);
  return {
    db: { ...db, editor: { ...db.editor, activeSlide: undefined } },
    trigger: ["projects/update", { ...tale, slides }],
  };
}

export function move(db, slide, index) {
  let tale = findTale([db.tales, db.activeTale]);
  let slides = [...(tale.slides || [])];
  slides.splice(index, 0, ...slides.splice(slide, 1));
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
dbHandler("slide/activate-at-position", (db, _, position) =>
  activateAtPosition(db, position),
);
dbHandler("slide/deactivate", db => deactivate(db));
handler("slide/swap-prev", ({ db }) => swapPrev(db));
handler("slide/swap-next", ({ db }) => swapNext(db));
handler("slide/fly-to-prev", ({ db }) => flyToPrev(db));
handler("slide/fly-to-next", ({ db }) => flyToNext(db));
handler("slide/focus-current", ({ db }) => focusCurrent(db));
handler("slide/add", ({ db }, _, slide) => add(db, slide));
handler("slide/insert", ({ db }, _, slide, index) => insert(db, slide, index));
handler("slide/update", ({ db }, _, slide) => update(db, slide));
handler("slide/delete-current", ({ db }) => deleteCurrent(db));
handler("slide/move", ({ db }, _, slide, index) => move(db, slide, index));
