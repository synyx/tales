import { h } from "flyps-dom-snabbdom";
import { connect, handler, signal, trigger, withInputSignals } from "flyps";
import { animator } from "../animation";
import { vec3 } from "gl-matrix";
import { DEFAULT_SLIDE_TRANSITION_DURATION } from "../config";
import i18n from "../i18n";

/**
 * Possible values (in ms) for transition duration setting.
 */
const AVAILABLE_DURATIONS = [
  5000, 4500, 4000, 3500, 3000, 2500, 2000, 1800, 1600, 1400, 1200, 1000, 900,
  800, 700, 600, 500, 400, 300, 200, 100, 1,
];

const inputDuration = signal();

/**
 * Tale-specific settings concerning the transitions between two slides.
 */
export const transitionSettings = withInputSignals(
  () => connect("tale"),
  tale => {
    const duration =
      inputDuration.value() ||
      tale.settings.transitionDuration ||
      DEFAULT_SLIDE_TRANSITION_DURATION;

    return h("div", [
      h("h3", [
        i18n("settings.transition.title"),
        h("span.small", ` (${formatDuration(duration)})`),
      ]),
      h("div", [
        h("label.range", [
          h("span", i18n("settings.transition.slower")),
          h("span", i18n("settings.transition.faster")),
          h("input", {
            attrs: {
              type: "range",
              max: AVAILABLE_DURATIONS.length - 1,
              value: time2slider(duration),
            },
            on: {
              input: ev => {
                const newDuration = slider2time(ev.target.value);
                inputDuration.reset(newDuration);
              },
              change: ev => {
                const newDuration = slider2time(ev.target.value);
                trigger("settings/transition-duration-changed", newDuration);
                trigger("projects/update", {
                  ...tale,
                  settings: {
                    ...tale.settings,
                    transitionDuration: newDuration,
                  },
                });
              },
            },
          }),
        ]),
        transitionPreview(),
      ]),
    ]);
  },
);

const formatDuration = ms =>
  ms <= 1
    ? i18n("settings.transition.instantly")
    : i18n("settings.transition.current-value", {
        duration: (Math.round(ms / 100) / 10).toFixed(1),
      });

const previewTransform = signal([0, 0, 0]);

export const transitionPreview = () => {
  const imgWidth = 150;
  const imgHeight = 50;
  const patternWidth = 100;
  const patternHeight = imgHeight;
  const slideWidth = 60;
  const slideHeight = 36;

  const [x, y, _z] = previewTransform.value();

  return h(
    "svg.transition-preview",
    { attrs: { viewBox: [0, 0, imgWidth, imgHeight] } },
    [
      h(
        "pattern#slides",
        {
          attrs: {
            x: imgWidth / 2 - patternWidth / 2,
            y: 0,
            width: patternWidth,
            height: patternHeight,
            patternUnits: "userSpaceOnUse",
            patternTransform: `translate(${-x} ${-y})`,
          },
        },
        [
          h("rect", {
            attrs: {
              x: patternWidth / 2 - slideWidth / 2,
              y: patternHeight / 2 - slideHeight / 2,
              width: slideWidth,
              height: slideHeight,
            },
          }),
          h(
            "text",
            { attrs: { x: patternWidth / 2, y: patternHeight / 2 } },
            i18n("settings.transition.preview"),
          ),
        ],
      ),
      h("rect", {
        attrs: {
          x: 0,
          y: 0,
          width: "100%",
          height: "100%",
          fill: "url(#slides)",
        },
      }),
    ],
  );
};

/**
 * Converts a slider value to milliseconds duration.
 */
export const slider2time = value => AVAILABLE_DURATIONS[value];

/**
 * Converts a millisecond duration to the best-fitting slider value.
 */
export const time2slider = ms => {
  const d = AVAILABLE_DURATIONS.map(v => Math.abs(v - ms));
  return d.indexOf(Math.min(...d));
};

handler("settings/transition-duration-changed", (_, __, value) => ({
  animation: ["transition-preview", animator(transitionAnimation, value)],
}));

const transitionAnimation = progress => {
  const pos = vec3.lerp(
    vec3.create(),
    [0, 0, 0],
    [100, 0, 0],
    Math.min(1.0, progress),
  );
  previewTransform.reset(pos);
};
