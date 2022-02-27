import { slider2time, time2slider } from "./transition";

describe("slider2time", () => {
  test.each([
    [0, 5000],
    [1, 4500],
    [21, 1],
  ])("maps slider value %i to %i ms", (sliderVal, expectedMs) => {
    expect(slider2time(sliderVal)).toEqual(expectedMs);
  });
});

describe("time2slider", () => {
  test.each([
    [1, 21],
    [100, 20],
    [5000, 0],
  ])("maps %i ms to slider value %i", (ms, expectedValue) => {
    expect(time2slider(ms)).toEqual(expectedValue);
  });
  test.each([
    [49, 21],
    [50, 21],
    [51, 20],
    [149, 20],
    [8000, 0],
  ])(
    "maps non-matching duration %i to closest value %i",
    (ms, expectedValue) => {
      expect(time2slider(ms)).toEqual(expectedValue);
    },
  );
});

describe("slider2time and time2slider", () => {
  test.each([5000, 2500, 1000, 500, 100, 1])(
    "composition of slider2time and time2slider is an identity function",
    v => {
      expect(slider2time(time2slider(v))).toEqual(v);
    },
  );
  test.each([0, 1, 2, 5, 10, 20, 21])(
    "composition of time2slider and slider2time is an identity function",
    v => {
      expect(time2slider(slider2time(v))).toEqual(v);
    },
  );
});
