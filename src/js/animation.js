import { db, effector } from "flyps";

function queue(nextTick = window.requestAnimationFrame) {
  /** Holds the registered animations. */
  let registry = new Map();
  let scheduled = false;

  return {
    enqueue(animationId, fn) {
      if (registry.has(animationId)) {
        console.warn("overwriting running animation for", animationId);
      }
      registry.set(animationId, fn);
      this.schedule();
    },
    cancel(animationId) {
      registry.delete(animationId);
    },
    flush(t) {
      scheduled = false;

      registry.forEach((fn, animationId) => {
        if (!fn(t)) {
          this.cancel(animationId);
        }
      });

      if (registry.size > 0) {
        this.schedule();
      }
    },
    schedule() {
      if (!scheduled) {
        scheduled = true;
        nextTick(this.flush.bind(this));
      }
    },
    tickFn(fn) {
      nextTick = fn;
    },
  };
}

export const animationQueue = queue();

export function animate(animationId, animationFn) {
  animationQueue.enqueue(animationId, animationFn);
}

export function easingAnimator(fn, duration) {
  let start;
  return t => {
    if (!start) start = t;
    let progress = (t - start) / duration;
    fn(easeInOutBezier(progress));
    return progress < 1;
  };
}

function easeInOutBezier(x) {
  return x * x * (3 - 2 * x);
}

export function dbAnimator(fn, duration) {
  return easingAnimator(
    progress => db.reset(fn(db.value(), progress)),
    duration,
  );
}

effector("animation", ([animationId, animationFn]) =>
  animate(animationId, animationFn),
);
