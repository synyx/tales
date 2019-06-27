import { effector, handler, trigger } from "flyps";

effector("navigate", url => {
  window.location.href = url;
});

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
    default:
      trigger("page/activate", "home");
  }
};

export function init() {
  onNavigate(window.location.hash);
  window.addEventListener(
    "hashchange",
    () => onNavigate(window.location.hash),
    false,
  );
}
