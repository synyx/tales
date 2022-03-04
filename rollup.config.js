import babel from "@rollup/plugin-babel";
import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import json from "@rollup/plugin-json";
import replace from "@rollup/plugin-replace";
import pkg from "./package.json";

export default [
  {
    input: "src/js/index.js",
    output: [
      {
        file: pkg.browser,
        format: "umd",
        name: "tales",
        sourcemap: true,
      },
    ],
    plugins: [
      resolve({
        browser: true,
        dedupe: ["flyps"],
      }),
      commonjs(),
      json(),
      replace({
        preventAssignment: true,
        /* workaround for missing env variable  */
        "process.env.NODE_ENV": JSON.stringify("production"),
      }),
      babel({ exclude: "node_modules/**", babelHelpers: "bundled" }),
    ],
  },
  {
    input: "app/main.js",
    output: [
      {
        file: "dist/main.js",
        format: "cjs",
        name: "tales-desktop",
        sourcemap: true,
      },
    ],
    external: ["child_process", "electron", "path"],
    plugins: [
      resolve(),
      commonjs(),
      babel({ exclude: "node_modules/**", babelHelpers: "bundled" }),
    ],
  },
];
