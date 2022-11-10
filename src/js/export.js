import { handler } from "flyps";

handler("export/download", (_, __, tale) => downloadAll(tale));

const downloadAll = tale => {
  const cacheBust = "?t=" + new Date().valueOf();
  Promise.all([
    fetch(`/viewer.html${cacheBust}`).then(response => response.text()),
    fetch(`/js/viewer.js${cacheBust}`).then(response => response.text()),
    fetch(`/css/viewer.css${cacheBust}`).then(response => response.text()),
    fetchAsDataURL(`/editor/${tale.slug}/${tale["file-path"]}${cacheBust}`),
  ]).then(([template, script, style, posterBase64]) => {
    const doc = template
      .replace("{{title}}", () => tale.name)
      .replace("{{style}}", () => style)
      .replace("{{tale}}", () => JSON.stringify(tale))
      .replace("{{poster}}", () => posterBase64)
      .replace("{{script}}", () => script);
    downloadAsFile(`${sanitizeFilename(tale.name)}.html`, doc, "text/html");
  });
};

const fetchAsDataURL = url =>
  fetch(url)
    .then(response => response.blob())
    .then(
      blob =>
        new Promise(callback => {
          let reader = new FileReader();
          reader.onload = function () {
            callback(this.result);
          };
          reader.readAsDataURL(blob);
        }),
    );

const sanitizeFilename = name => name.replace(/[^\w]/gi, "_");

const downloadAsFile = (filename, data, mimeType) => {
  const blob = new Blob([data], { type: mimeType });

  const elem = window.document.createElement("a");
  elem.href = window.URL.createObjectURL(blob);
  elem.download = filename;
  document.body.appendChild(elem);
  elem.click();
  document.body.removeChild(elem);
};
