import babel from "rollup-plugin-babel";
import resolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
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
      }
    ],
    plugins: [
        resolve({
          dedupe: ["flyps"]
        }),
        commonjs(),
        babel({ exclude: "node_modules/**" })
    ],
  },
];
