import { activate, add, update } from "./slide";

describe("slide", () => {
  it("activates slide", () => {
    let db = activate({}, 42);
    expect(db.editor.activeSlide).toBe(42);
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
});
