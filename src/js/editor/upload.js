import { signal, trigger } from "flyps";
import { h } from "flyps-dom-snabbdom";
import { i18n } from "../i18n";

let dropTarget = signal();

export let uploader = tale => {
  return h(
    "div.poster-uploader",
    {
      class: { dropping: dropTarget.value() },
      on: {
        dragenter: ev => {
          ev.preventDefault();
          dropTarget.reset(true);
        },
        dragover: ev => {
          ev.preventDefault();
        },
        dragleave: () => {
          dropTarget.reset(false);
        },
        drop: ev => {
          ev.preventDefault();
          dropTarget.reset(false);
          handleFileInput(tale, ev.dataTransfer.files.item(0));
        },
      },
    },
    [
      h("h2", i18n("uploader.upload-poster.title")),
      h("h3", i18n("uploader.upload-poster.subtitle")),
      h("p", i18n("uploader.upload-poster.description")),
      h("div.drop-target", { class: { active: dropTarget.value() } }, [
        h("p", i18n("uploader.drag-and-drop")),
        h("p", i18n("uploader.or")),
        h("label.button.select-image", [
          i18n("uploader.select-an-image"),
          h("input", {
            attrs: {
              type: "file",
              accept: "image/*",
            },
            on: {
              change: ev => {
                handleFileInput(tale, ev.target.files[0]);
              },
            },
          }),
        ]),
      ]),
    ],
  );
};

const handleFileInput = (tale, file) => {
  if (!file) {
    return;
  }
  determineImageDimensions(file)
    .then(({ file, width, height }) => {
      trigger("projects/update", {
        ...tale,
        dimensions: { width, height },
      });
      trigger("camera/fit-rect", { x: 0, y: 0, width, height });
      trigger("projects/update-image", tale, file);
    })
    .catch(err => console.error("error determining image dimensions", err));
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
