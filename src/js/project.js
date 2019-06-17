import { handler, connector, signal, trigger } from "flyps";

const tales = signal([]);

handler("projects/get-all", () => {
  return {
    xhr: {
      url: "/api/tales/",
      responseType: "json",
      onSuccess: data => tales.reset(data),
    },
  };
});

handler("projects/add", (causes, eventId, project) => {
  return {
    xhr: {
      url: "/api/tales/",
      method: "POST",
      data: JSON.stringify(project),
      headers: {
        "Content-Type": "application/json",
      },
      responseType: "json",
      onSuccess: data => trigger("projects/add-success", data),
      onError: data => console.error(data),
    },
  };
});

handler("projects/add-success", (causes, eventId, project) => {
  tales.update(tales => [
    ...tales.filter(tale => tale.slug != project.slug),
    project,
  ]);

  return {
    navigate: `/editor/${project.slug}/`,
  };
});

connector("tales", () => tales.value());
