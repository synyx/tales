import {
  activate,
  activatePrev,
  activateNext,
  swap,
  swapPrev,
  swapNext,
  flyToPrev,
  flyToNext,
  focusCurrent,
  flyToCurrent,
  add,
  update,
  deleteCurrent,
  deactivate,
  isPointInRect,
  activateAtPosition,
} from "./slide";

describe("slide", () => {
  it("activates slide", () => {
    let db = activate({}, 42);
    expect(db.editor.activeSlide).toBe(42);
  });
  it("activates prev slide", () => {
    let db = activatePrev({
      tales: [{ slug: "foo", slides: [{}, {}, {}] }],
      activeTale: "foo",
      editor: { activeSlide: 1 },
    });
    expect(db.editor.activeSlide).toBe(0);
  });
  it("activates last slide on prev with no activated slide", () => {
    let db = activatePrev({
      tales: [{ slug: "foo", slides: [{}, {}, {}] }],
      activeTale: "foo",
      editor: {},
    });
    expect(db.editor.activeSlide).toBe(2);
  });
  it("activates last after first slide", () => {
    let db = activatePrev({
      tales: [{ slug: "foo", slides: [{}, {}, {}] }],
      activeTale: "foo",
      editor: { activeSlide: 0 },
    });
    expect(db.editor.activeSlide).toBe(2);
  });
  it("activates next slide", () => {
    let db = activateNext({
      tales: [{ slug: "foo", slides: [{}, {}, {}] }],
      activeTale: "foo",
      editor: { activeSlide: 1 },
    });
    expect(db.editor.activeSlide).toBe(2);
  });
  it("activates first slide on next with no activated slide", () => {
    let db = activateNext({
      tales: [{ slug: "foo", slides: [{}, {}, {}] }],
      activeTale: "foo",
      editor: {},
    });
    expect(db.editor.activeSlide).toBe(0);
  });
  it("activates first after last slide", () => {
    let db = activateNext({
      tales: [{ slug: "foo", slides: [{}, {}, {}] }],
      activeTale: "foo",
      editor: { activeSlide: 2 },
    });
    expect(db.editor.activeSlide).toBe(0);
  });
  it("deactivates slide", () => {
    let db = deactivate({ editor: { activeSlide: 1 } });
    expect(db.editor.activeSlide).toBeUndefined();
  });
  it("swaps slides", () => {
    let initialDb = {
      tales: [{ slug: "foo", slides: [{ id: 1 }, { id: 2 }, { id: 3 }] }],
      activeTale: "foo",
    };
    let { db, trigger } = swap(initialDb, 0, 2);
    expect(db.editor.activeSlide).toBe(2);
    expect(trigger).toEqual([
      "projects/update",
      { slug: "foo", slides: [{ id: 3 }, { id: 2 }, { id: 1 }] },
    ]);
  });
  it("handles invalid indices when swapping slides", () => {
    let initialDb = {
      tales: [{ slug: "foo", slides: [{ id: 1 }, { id: 2 }, { id: 3 }] }],
      activeTale: "foo",
    };
    expect(swap(initialDb, 2, 3)).toBeUndefined();
    expect(swap(initialDb, 3, 2)).toBeUndefined();
    expect(swap(initialDb, 0, -1)).toBeUndefined();
    expect(swap(initialDb, -1, 0)).toBeUndefined();
  });
  it("swaps current slide with prev", () => {
    let initialDb = {
      tales: [{ slug: "foo", slides: [{ id: 1 }, { id: 2 }, { id: 3 }] }],
      activeTale: "foo",
      editor: { activeSlide: 1 },
    };
    let { db, trigger } = swapPrev(initialDb);
    expect(db.editor.activeSlide).toBe(0);
    expect(trigger).toEqual([
      "projects/update",
      { slug: "foo", slides: [{ id: 2 }, { id: 1 }, { id: 3 }] },
    ]);
  });
  it("swaps current slide with next", () => {
    let initialDb = {
      tales: [{ slug: "foo", slides: [{ id: 1 }, { id: 2 }, { id: 3 }] }],
      activeTale: "foo",
      editor: { activeSlide: 1 },
    };
    let { db, trigger } = swapNext(initialDb);
    expect(db.editor.activeSlide).toBe(2);
    expect(trigger).toEqual([
      "projects/update",
      { slug: "foo", slides: [{ id: 1 }, { id: 3 }, { id: 2 }] },
    ]);
  });
  it("flys to prev slide", () => {
    let rect = { x: 10, y: 20, width: 100, height: 200 };
    let { db, trigger } = flyToPrev({
      tales: [{ slug: "foo", slides: [{ rect }, {}, {}] }],
      activeTale: "foo",
      editor: { activeSlide: 1 },
    });
    expect(db.editor.activeSlide).toBe(0);
    expect(trigger).toEqual(["camera/fly-to-rect", rect]);
  });
  it("flys to last after first slide", () => {
    let rect = { x: 10, y: 20, width: 100, height: 200 };
    let { db, trigger } = flyToPrev({
      tales: [{ slug: "foo", slides: [{}, {}, { rect }] }],
      activeTale: "foo",
      editor: { activeSlide: 0 },
    });
    expect(db.editor.activeSlide).toBe(2);
    expect(trigger).toEqual(["camera/fly-to-rect", rect]);
  });
  it("flys to next slide", () => {
    let rect = { x: 10, y: 20, width: 100, height: 200 };
    let { db, trigger } = flyToNext({
      tales: [{ slug: "foo", slides: [{}, {}, { rect }] }],
      activeTale: "foo",
      editor: { activeSlide: 1 },
    });
    expect(db.editor.activeSlide).toBe(2);
    expect(trigger).toEqual(["camera/fly-to-rect", rect]);
  });
  it("flys to first after last slide", () => {
    let rect = { x: 10, y: 20, width: 100, height: 200 };
    let { db, trigger } = flyToNext({
      tales: [{ slug: "foo", slides: [{ rect }, {}, {}] }],
      activeTale: "foo",
      editor: { activeSlide: 2 },
    });
    expect(db.editor.activeSlide).toBe(0);
    expect(trigger).toEqual(["camera/fly-to-rect", rect]);
  });
  it("focuses on current slide", () => {
    let rect = { x: 10, y: 20, width: 100, height: 200 };
    let effects = focusCurrent({
      tales: [
        {
          slug: "foo",
          slides: [{ rect }],
        },
      ],
      activeTale: "foo",
      editor: { activeSlide: 0 },
    });
    expect(effects.trigger).toEqual(["camera/fit-rect", rect]);
  });
  it("fly to current slide", () => {
    let rect = { x: 10, y: 20, width: 100, height: 200 };
    let effects = flyToCurrent({
      tales: [
        {
          slug: "foo",
          slides: [{ rect }],
        },
      ],
      activeTale: "foo",
      editor: { activeSlide: 0 },
    });
    expect(effects.trigger).toEqual(["camera/fly-to-rect", rect]);
  });
  it("adds slide", () => {
    let db = {
      tales: [
        { slug: "test-1", slides: [{ id: 1 }] },
        { slug: "test-2", slides: [] },
      ],
      activeTale: "test-1",
    };
    let effects = add(db, { id: 2 });
    expect(effects.trigger).toEqual([
      "projects/update",
      { slug: "test-1", slides: [{ id: 1 }, { id: 2 }] },
    ]);
  });
  it("updates slide", () => {
    let db = {
      tales: [
        { slug: "test-1", slides: [{ id: 1 }, { id: 2 }] },
        { slug: "test-2", slides: [] },
      ],
      activeTale: "test-1",
      editor: {
        activeSlide: 1,
      },
    };
    let effects = update(db, { id: 2, value: 42 });
    expect(effects.trigger).toEqual([
      "projects/update",
      { slug: "test-1", slides: [{ id: 1 }, { id: 2, value: 42 }] },
    ]);
  });
  it("deletes slide", () => {
    let db = {
      tales: [
        { slug: "test-1", slides: [{ id: 1 }, { id: 2 }, { id: 3 }] },
        { slug: "test-2", slides: [] },
      ],
      activeTale: "test-1",
      editor: {
        activeSlide: 1,
      },
    };
    let effects = deleteCurrent(db);
    expect(effects.trigger).toEqual([
      "projects/update",
      { slug: "test-1", slides: [{ id: 1 }, { id: 3 }] },
    ]);
  });
});
describe("isPointInRect", () => {
  it("accepts points inside the rect", () => {
    let rect = { x: 9, y: 9, width: 2, height: 2 };
    expect(isPointInRect([10, 10], rect)).toBeTruthy();
  });
  it("rejects points outside the rect", () => {
    let rect = { x: 9, y: 9, width: 2, height: 2 };
    expect(isPointInRect([8, 10], rect)).toBeFalsy();
    expect(isPointInRect([12, 10], rect)).toBeFalsy();
    expect(isPointInRect([10, 8], rect)).toBeFalsy();
    expect(isPointInRect([10, 12], rect)).toBeFalsy();
  });
});
describe("activateAtPosition", () => {
  it("deactivates the active slide if there's no slide at position", () => {
    let db = activateAtPosition(
      {
        tales: [
          {
            slug: "foo",
            slides: [{ rect: { x: 10, y: 10, width: 10, height: 10 } }],
          },
        ],
        activeTale: "foo",
        editor: { activeSlide: 0 },
      },
      [5, 5],
    );
    expect(db.editor.activeSlide).toBeUndefined();
  });
  it("activates the slide at the given position", () => {
    let db = activateAtPosition(
      {
        tales: [
          {
            slug: "foo",
            slides: [
              { rect: { x: 10, y: 10, width: 10, height: 10 } },
              { rect: { x: 20, y: 20, width: 10, height: 10 } }, // active
            ],
          },
        ],
        activeTale: "foo",
        editor: { activeSlide: 1 },
      },
      [15, 15],
    );
    expect(db.editor.activeSlide).toBe(0);
  });
  it("activates the slide below the currently active slide", () => {
    let db = activateAtPosition(
      {
        tales: [
          {
            slug: "foo",
            slides: [
              { rect: { x: 10, y: 10, width: 10, height: 10 } },
              { rect: { x: 10, y: 10, width: 10, height: 10 } }, // active
              { rect: { x: 10, y: 10, width: 10, height: 10 } },
            ],
          },
        ],
        activeTale: "foo",
        editor: { activeSlide: 1 },
      },
      [15, 15],
    );
    expect(db.editor.activeSlide).toBe(0);
  });
  it(
    "activates the top-most slide again if the bottom-most slide " +
      "is active",
    () => {
      let db = activateAtPosition(
        {
          tales: [
            {
              slug: "foo",
              slides: [
                { rect: { x: 10, y: 10, width: 10, height: 10 } }, // active
                { rect: { x: 10, y: 10, width: 10, height: 10 } },
                { rect: { x: 10, y: 10, width: 10, height: 10 } },
              ],
            },
          ],
          activeTale: "foo",
          editor: { activeSlide: 0 },
        },
        [15, 15],
      );
      expect(db.editor.activeSlide).toBe(2);
    },
  );
});
