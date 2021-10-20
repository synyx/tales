import {
  db,
  connector,
  connect,
  effector,
  handler,
  withInputSignals,
  trigger,
} from "flyps";
import { h } from "flyps-dom-snabbdom";
import { mat4 } from "gl-matrix";

let matrix3d = mat => `matrix3d(${mat.join(",")})`;

/**
 * effectors
 */

effector("fullscreen", fullscreen => {
  if (fullscreen) {
    document.documentElement.requestFullscreen({ navigationUI: "hide" });
  } else {
    document.exitFullscreen();
  }
});

/**
 * connectors
 */

export function getViewportRect(db) {
  return db.viewport.rect;
}

export function getViewportAspect([_x, _y, w, h]) {
  return w / h;
}

export function getViewportMatrix([x, y, w, h]) {
  let w2 = w / 2.0;
  let h2 = h / 2.0;
  let m = mat4.create();
  mat4.translate(m, m, [x + w2, y + h2, 0]);
  mat4.scale(m, m, [w2, h2, 1]);
  return m;
}

connector(
  "viewport/rect",
  withInputSignals(
    () => db,
    db => getViewportRect(db),
  ),
);

connector(
  "viewport/aspect",
  withInputSignals(
    () => connect("viewport/rect"),
    rect => getViewportAspect(rect),
  ),
);

/**
 * handlers
 */

export function setViewportRect(db, [vx, vy, vw, vh]) {
  return { ...db, viewport: { ...db.viewport, rect: [vx, vy, vw, vh] } };
}

function dbHandler(eventId, handlerFn, interceptors) {
  return handler(
    eventId,
    ({ db }, ...args) => ({ db: handlerFn(db, ...args) }),
    interceptors,
  );
}

dbHandler("viewport/set-rect", (db, id, rect) => setViewportRect(db, rect));

handler("viewport/fullscreen", (_causes, _id, fullscreen) => ({
  fullscreen: fullscreen,
}));

/**
 * views
 */

export const windowResizeListener = {
  add(elm) {
    this.listener = () => {
      let { left, top, width, height } = elm.getBoundingClientRect();
      trigger("viewport/set-rect", [left, top, width, height]);
    };
    window.addEventListener("resize", this.listener);
    this.listener();
  },
  remove() {
    window.removeEventListener("resize", this.listener);
    this.listener = null;
  },
};

export function viewport(worldWidth, worldHeight, data = {}, children = []) {
  let disconnect;
  return h(
    "div.viewport",
    {
      ...data,
      style: {
        width: "100%",
        height: "100%",
        overflow: "hidden",
        ...data.style,
      },
      hook: {
        insert: vnode => {
          windowResizeListener.add(vnode.elm);
        },
        destroy: () => {
          windowResizeListener.remove();
        },
        ...data.hook,
      },
    },
    h(
      "div.scene",
      {
        style: {
          "transform-origin": "0 0",
        },
        hook: {
          insert: vnode => {
            disconnect = connect("matrix/viewport-transform").connect(m => {
              vnode.elm.style.transform = matrix3d(m.value());
            });
          },
          remove: () => disconnect && disconnect(),
        },
      },
      h(
        "div.world",
        {
          style: {
            width: `${worldWidth}px`,
            height: `${worldHeight}px`,
          },
        },
        children,
      ),
    ),
  );
}
