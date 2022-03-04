import { determineImageDimensions } from "./upload";

/* global global */

global.Image = class {
  constructor() {
    this._src = undefined;
    Object.defineProperty(this, "width", {
      get: () => global.Image.width,
    });
    Object.defineProperty(this, "height", {
      get: () => global.Image.height,
    });
    Object.defineProperty(this, "src", {
      get: () => this._src,
      set: src => {
        this._src = src;
        setTimeout(this.onload, 0);
      },
    });
  }
};

const pngFile = imageFromBase64(
  "foo.png",
  "image/png",
  "iVBORw0KGgoAAAANSUhEUgAAAGQAAABQCAQAAABFABhkAAAAWUlEQVR42u3P" +
    "AQ0AAAgDoL9/aK3hHDSgmbxQERERERERERERERERERERERERERERERERERER" +
    "ERERERERERERERERERERERERERERERERERERERERERERkUsWHIBQAXlFihYA" +
    "AAAASUVORK5CYII=",
);

let svgFile1 = svg(
  "foo.svg",
  '<?xml version="1.0" ?><svg width="100mm" height="80mm"></svg>',
);
let svgFile2 = svg(
  "foo.svg",
  '<?xml version="1.0" ?><svg viewBox="20 10 120 100"></svg>',
);
let svgFile3 = svg(
  "foo.svg",
  '<?xml version="1.0" ?><svg viewBox="foo bar"></svg>',
);
let xmlFile = svg("foo.xml", '<?xml version="1.0" ?>');

beforeEach(() => {
  global.Image.width = undefined;
  global.Image.height = undefined;
});

describe("determineImageDimensions", () => {
  it("returns width and height from image", () => {
    global.Image.width = 100;
    global.Image.height = 80;
    expect.assertions(3);
    return determineImageDimensions(pngFile).then(({ file, width, height }) => {
      expect(file).toBe(pngFile);
      expect(width).toBe(100);
      expect(height).toBe(80);
    });
  });
  it("rejects on unknown width or height", () => {
    expect.assertions(1);
    return determineImageDimensions(pngFile).catch(reason =>
      // eslint-disable-next-line jest/no-conditional-expect
      expect(reason).toBe("undefined width or height"),
    );
  });
  it("returns width and height from svg width and height", () => {
    expect.assertions(3);
    return determineImageDimensions(svgFile1).then(
      ({ file, width, height }) => {
        expect(file).toBe(svgFile1);
        expect(width).toBe(100);
        expect(height).toBe(80);
      },
    );
  });
  it("returns width and height from svg viewbox", () => {
    expect.assertions(3);
    return determineImageDimensions(svgFile2).then(
      ({ file, width, height }) => {
        expect(file).toBe(svgFile2);
        expect(width).toBe(100);
        expect(height).toBe(90);
      },
    );
  });
  it("rejects on invalid svg viewbox", () => {
    expect.assertions(1);
    return determineImageDimensions(svgFile3).catch(reason =>
      // eslint-disable-next-line jest/no-conditional-expect
      expect(reason).toBe("undefined width or height"),
    );
  });
  it("rejects on invalid svg", () => {
    expect.assertions(1);
    return determineImageDimensions(xmlFile).catch(reason =>
      // eslint-disable-next-line jest/no-conditional-expect
      expect(reason).toContain("failed to get svg from document:"),
    );
  });
});

function imageFromBase64(name, contentType, encodedImage) {
  const bstr = atob(encodedImage);
  let n = bstr.length;
  const u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new File([u8arr], name, { type: contentType });
}

function svg(name, value) {
  return new File([value], name, { type: "image/svg+xml" });
}
