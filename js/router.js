import { effector, handler, trigger } from "flyps";

export function navigate(url) {
  window.location.href = url;
}

effector("navigate", navigate);

handler("router/navigate", (causes, eventId, url) => ({
  navigate: url,
}));

let onNavigate = url => {
  let [name, ...args] = url.split("/");
  switch (name) {
    case "#editor":
      trigger("page/activate", "editor");
      trigger("projects/activate", args[0]);
      break;
    case "#presenter":
      trigger("page/activate", "presenter");
      trigger("projects/activate", args[0]);
      trigger("viewport/fullscreen", true);
      break;
    default:
      trigger("page/activate", "home");
  }
};

let onHashChange = () => onNavigate(window.location.hash);

export function init() {
  onHashChange();
  window.addEventListener("hashchange", onHashChange, false);
}
