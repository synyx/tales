import { bestMatchingLanguage } from "./locale";

describe("bestMatchingLocale", () => {
  let windowSpy;
  beforeEach(() => {
    windowSpy = jest.spyOn(window, "window", "get");
  });
  afterEach(() => windowSpy.mockRestore());
  const setMockLanguages = languages =>
    windowSpy.mockImplementation(() => ({ navigator: { languages } }));

  it("selects the first matching locale", () => {
    setMockLanguages(["fr", "de", "en"]);
    const locale = bestMatchingLanguage(["it", "de", "en"]);
    expect(locale).toBe("de");
  });
  it("uses the language part of locales", () => {
    setMockLanguages(["fr-CA", "ru-RU", "en-AU"]);
    const locale = bestMatchingLanguage(["it", "ru", "en"]);
    expect(locale).toBe("ru");
  });
  it("returns a falsy value if no locale matches", () => {
    setMockLanguages(["fr", "de", "en"]);
    const locale = bestMatchingLanguage(["it"]);
    expect(locale).toBeFalsy();
  });
});
