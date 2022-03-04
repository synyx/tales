import { db, effect } from "flyps";

import {
  animationQueue,
  animate,
  dbAnimator,
  easingAnimator,
} from "./animation";

/* global global */

let ticker = (function () {
  let fns = [];
  return {
    dispatch(fn) {
      fns.push(fn);
    },
    advance() {
      let fn = fns.pop();
      if (fn) fn();
    },
    size() {
      return fns.length;
    },
  };
})();

beforeEach(() => {
  global.console.warn = jest.fn();
  animationQueue.tickFn(ticker.dispatch);
});

describe("animate", () => {
  it("calls animationFn until it returns false", () => {
    let frames = 0;
    animate("foo", () => ++frames < 2);
    expect(frames).toBe(0);
    ticker.advance();
    expect(frames).toBe(1);
    ticker.advance();
    expect(frames).toBe(2);
    ticker.advance();
    expect(frames).toBe(2);
    ticker.advance();
    expect(frames).toBe(2);
  });
  it("logs a warning when overwriting an already running animation", () => {
    animate("foo", () => {});
    animate("foo", () => {});
    expect(global.console.warn).toHaveBeenCalledWith(
      "overwriting running animation for",
      "foo",
    );
  });
});

describe("easingAnimator", () => {
  it("returns true until duration of time has elapsed", () => {
    let fn = easingAnimator(() => {}, 1000);
    expect(fn(1000)).toBeTruthy();
    expect(fn(1500)).toBeTruthy();
    expect(fn(1999)).toBeTruthy();
    expect(fn(2000)).toBeFalsy();
  });
  it("passes current progress to animationFn", () => {
    let lastProgress = -1;
    let fn = easingAnimator(p => (lastProgress = p), 1000);
    fn(1000);
    expect(lastProgress).toBe(0);
    fn(1500);
    expect(lastProgress).toBe(0.5);
    fn(2000);
    expect(lastProgress).toBe(1);
  });
});

describe("dbAnimator", () => {
  it("passes and resets the db value", () => {
    let fn = dbAnimator((db, p) => ({ history: [...db.history, p] }), 1000);
    db.reset({ history: [] });
    expect(db.value().history).toEqual([]);
    fn(1000);
    expect(db.value().history).toEqual([0]);
    fn(1500);
    expect(db.value().history).toEqual([0, 0.5]);
    fn(2000);
    expect(db.value().history).toEqual([0, 0.5, 1]);
  });
});

describe("animation effect", () => {
  it("calls animationFn until it returns false", () => {
    let frames = 0;
    effect("animation", ["foo", () => ++frames < 2]);
    expect(frames).toBe(0);
    ticker.advance();
    expect(frames).toBe(1);
    ticker.advance();
    expect(frames).toBe(2);
    ticker.advance();
    expect(frames).toBe(2);
    ticker.advance();
    expect(frames).toBe(2);
  });
});
