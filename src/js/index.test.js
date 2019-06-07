import { findTale } from "./index";

describe("findTale", () => {
  it("returns tale with matching slug", () => {
    let tales = [{ slug: "foo" }, { slug: "bar" }];
    expect(findTale([tales, "bar"])).toBe(tales[1]);
  });

  it("returns undefined when no matching tale is found", () => {
    let tales = [{ slug: "foo" }];
    expect(findTale([tales, "bar"])).toBeUndefined();
  });
});
