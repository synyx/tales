import {
  db,
  handler,
  connect,
  connector,
  trigger,
  withInputSignals,
} from "flyps";

export function findTale([tales, slug]) {
  return tales.find(tale => tale.slug === slug);
}

connector(
  "tales",
  withInputSignals(
    () => db,
    db => db.tales || [],
  ),
);

connector(
  "tale-slug",
  withInputSignals(
    () => db,
    db => db.activeTale,
  ),
);

connector(
  "tale",
  withInputSignals(() => [connect("tales"), connect("tale-slug")], findTale),
);

// triggers an event whenever the tale slug changes
connect("tale-slug").connect(() => trigger("project/loaded"));

handler("projects/get-all", () => ({
  xhr: {
    url: "/api/tales/",
    responseType: "json",
    onSuccess: ["projects/get-all-success"],
    onError: ["projects/request-error"],
  },
}));

handler("projects/get-all-success", ({ db }, eventId, projects) => ({
  db: { ...db, tales: projects },
  trigger: ["project/loaded"],
}));

handler("projects/add", (causes, eventId, project) => ({
  xhr: {
    url: "/api/tales/",
    method: "POST",
    data: JSON.stringify(project),
    headers: {
      "Content-Type": "application/json",
    },
    responseType: "json",
    onSuccess: ["projects/request-success"],
    onError: ["projects/request-error"],
  },
}));

handler("projects/update", (causes, eventId, project) => ({
  xhr: {
    url: "/api/tales/" + project.slug,
    method: "PUT",
    data: JSON.stringify(project),
    headers: {
      "Content-Type": "application/json",
    },
    responseType: "json",
    onSuccess: ["projects/request-success"],
    onError: ["projects/request-error"],
  },
}));

handler("projects/update-image", (causes, eventId, project, file) => ({
  xhr: {
    url: "/api/tales/" + project.slug + "/image",
    method: "PUT",
    data: file,
    headers: {
      "Content-Type": file.type,
    },
    responseType: "json",
    onSuccess: ["projects/request-success"],
    onError: ["projects/request-error"],
  },
}));

handler("projects/request-success", ({ db }, eventId, project) => ({
  db: {
    ...db,
    tales: [...db.tales.filter(tale => tale.slug !== project.slug), project],
  },
  navigate: `#editor/${project.slug}/`,
}));

handler("projects/request-error", response => {
  console.error(response);
});

handler("projects/activate", ({ db }, eventId, slug) => ({
  db: { ...db, activeTale: slug },
}));

handler("project/loaded", ({ db }) => {
  let effects = {
    db: { ...db, editor: { ...db.editor, activeSlide: undefined } },
  };
  let tale = findTale([db.tales, db.activeTale]);
  if (tale && tale.dimensions) {
    let { width, height } = tale.dimensions;
    let rect = { x: 0, y: 0, width: width, height: height };
    effects.trigger = ["camera/fit-rect", rect];
  }
  return effects;
});
