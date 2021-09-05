import babel from "rollup-plugin-babel";
import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import pkg from "./package.json";

export default [
  {
    input: "js/index.js",
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
        dedupe: ["flyps"],
      }),
      commonjs(),
      babel({ exclude: "node_modules/**" }),
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
    plugins: [resolve(), commonjs(), babel({ exclude: "node_modules/**" })],
  },
];
