import { trigger } from "flyps";
import { h } from "flyps-dom-snabbdom";
import i18n from "../i18n";

export let uploader = tale => {
  return h("div.poster-uploader", [
    h("h2", i18n("uploader.upload-poster.title")),
    h("h3", i18n("uploader.upload-poster.subtitle")),
    h("input", {
      attrs: {
        type: "file",
        accept: "image/*",
      },
      on: {
        change: ev => {
          determineImageDimensions(ev.target.files[0])
            .then(({ file, width, height }) => {
              trigger("projects/update", {
                ...tale,
                dimensions: { width, height },
              });
              trigger("camera/fit-rect", { x: 0, y: 0, width, height });
              trigger("projects/update-image", tale, file);
            })
            .catch(err =>
              console.error("error determining image dimensions", err),
            );
        },
      },
    }),
  ]);
};

export function determineImageDimensions(file) {
  return new Promise((resolve, reject) => {
    if (file.type === "image/svg+xml") {
      loadSvg(file)
        .then(svg => {
          let { width, height } = parseSvgDimensions(svg);
          if (width && height) {
            resolve({ file, width, height });
          } else {
            reject("undefined width or height");
          }
        })
        .catch(reject);
    } else {
      loadImage(file)
        .then(img => {
          let width = img.naturalWidth || img.width;
          let height = img.naturalHeight || img.height;
          if (width && height) {
            resolve({ file, width, height });
          } else {
            reject("undefined width or height");
          }
        })
        .catch(reject);
    }
  });
}

function loadSvg(file) {
  return new Promise((resolve, reject) => {
    let reader = new FileReader();
    reader.onload = ev => {
      let parser = new DOMParser();
      let doc = parser.parseFromString(ev.target.result, file.type);
      let svgs = doc.getElementsByTagName("svg");
      if (svgs.length > 0) {
        resolve(svgs[0]);
      } else {
        reject("failed to get svg from document: " + doc);
      }
    };
    reader.onerror = reject;
    reader.readAsText(file);
  });
}

function loadImage(file) {
  return new Promise((resolve, reject) => {
    let reader = new FileReader();
    reader.onload = ev => {
      let image = new Image();
      image.onload = () => resolve(image);
      image.onerror = () => reject("failed to load image: " + image.src);
      image.src = ev.target.result;
    };
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

function parseSvgDimensions(svg) {
  let width = parseFloat(svg.getAttribute("width"));
  let height = parseFloat(svg.getAttribute("height"));
  let viewbox = svg.getAttribute("viewBox");
  if (!isNaN(width) && !isNaN(height)) {
    return { width, height };
  }

  viewbox = viewbox.split(" ").map(parseFloat);
  width = viewbox[2] - viewbox[0];
  height = viewbox[3] - viewbox[1];
  if (!isNaN(width) && !isNaN(height)) {
    return { width, height };
  }

  return {};
}
