import { handler, trigger } from "flyps";

function cord(ev) {
  return [
    ev.altKey ? "Alt" : null,
    ev.ctrlKey ? "Ctrl" : null,
    ev.shiftKey ? "Shift" : null,
    ev.metaKey ? "Meta" : null,
    ev.key || ev.keyCode,
  ]
    .filter(ev => !!ev)
    .join("+");
}

handler("key-pressed", ({ db }, eventId, ev) => {
  let presenting = db.activePage === "presenter";
  let editing = db.activePage === "editor";
  let key = cord(ev);
  switch (key) {
    case "Enter":
      ev.preventDefault();
      ev.stopPropagation();
      trigger("slide/focus-current");
      break;
    case "Delete":
      ev.preventDefault();
      ev.stopPropagation();
      if (!presenting) {
        trigger("slide/delete-current");
      }
      break;
    case "Shift+PageUp":
    case "Shift+ArrowLeft":
      ev.preventDefault();
      ev.stopPropagation();
      if (!presenting) {
        trigger("slide/swap-prev");
      }
      break;
    case "Shift+PageDown":
    case "Shift+ArrowRight":
      ev.preventDefault();
      ev.stopPropagation();
      if (!presenting) {
        trigger("slide/swap-next");
      }
      break;
    case "PageUp":
    case "ArrowLeft":
      ev.preventDefault();
      ev.stopPropagation();
      trigger(presenting ? "slide/fly-to-prev" : "slide/activate-prev");
      break;
    case "PageDown":
    case "ArrowRight":
      ev.preventDefault();
      ev.stopPropagation();
      trigger(presenting ? "slide/fly-to-next" : "slide/activate-next");
      break;
    case "Escape":
      if (presenting) {
        ev.preventDefault();
        ev.stopPropagation();
        trigger("router/navigate", `#editor/${db.activeTale}`);
        return;
      }
      if (editing) {
        ev.preventDefault();
        ev.stopPropagation();
        trigger("router/navigate", "#");
        return;
      }
      break;
  }
});
