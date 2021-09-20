import { db, handler, connect, connector, withInputSignals } from "flyps";

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
  trigger: ["project/reset-view"],
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
  db: {
    ...db,
    activeTale: slug,
    editor: { ...db.editor, activeSlide: undefined },
  },
  trigger: ["project/reset-view"],
}));

export const resetView = db => {
  let tale = findTale([db.tales, db.activeTale]);
  if (tale && tale.dimensions) {
    let { width, height } = tale.dimensions;
    let rect = { x: 0, y: 0, width, height };
    return { trigger: ["camera/fit-rect", rect] };
  }
};

handler("project/reset-view", ({ db }) => resetView(db));
