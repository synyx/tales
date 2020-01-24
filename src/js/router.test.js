import { trigger } from "flyps";

import { init, navigate } from "./router";

jest.mock("flyps");

beforeEach(() => {
  window.addEventListener.mockClear();
});

window.addEventListener = jest.fn();

describe("router", () => {
  it("handles empty location hash", () => {
    window.location.hash = "";
    init();
    expect(trigger).toHaveBeenCalledWith("page/activate", "home");
  });
  it("handles #home location hash", () => {
    window.location.hash = "#home";
    init();
    expect(trigger).toHaveBeenCalledWith("page/activate", "home");
  });
  it("handles #editor location hash", () => {
    window.location.hash = "#editor/example";
    init();
    expect(trigger).toHaveBeenCalledWith("page/activate", "editor");
    expect(trigger).toHaveBeenCalledWith("projects/activate", "example");
  });
  it("handles hash changes", () => {
    window.location.hash = "";
    init();
    expect(window.addEventListener).toHaveBeenCalledWith(
      "hashchange",
      expect.anything(),
      false,
    );
    let onHashChange = window.addEventListener.mock.calls[0][1];

    window.location.hash = "#editor/project";
    onHashChange();
    expect(trigger).toHaveBeenCalledWith("page/activate", "editor");
    expect(trigger).toHaveBeenCalledWith("projects/activate", "project");
  });
});

describe("navigate", () => {
  const { location } = window;

  beforeAll(() => {
    delete window.location;
    window.location = { href: "" };
  });

  afterAll(() => {
    window.location = location;
  });

  it("changes location href", () => {
    navigate("/example");
    expect(window.location.href).toBe("/example");
  });
});
