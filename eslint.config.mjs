import jest from "eslint-plugin-jest";
import prettier from "eslint-plugin-prettier";
import globals from "globals";
import path from "node:path";
import { fileURLToPath } from "node:url";
import js from "@eslint/js";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
    baseDirectory: __dirname,
    recommendedConfig: js.configs.recommended,
    allConfig: js.configs.all
});

export default [...compat.extends(
    "eslint:recommended",
    "plugin:jest/recommended",
    "plugin:prettier/recommended",
), {
    plugins: {
        jest,
        prettier,
    },

    languageOptions: {
        globals: {
            ...globals.browser,
            ...globals.jest,
            process: true,
        },

        ecmaVersion: 2018,
        sourceType: "module",
    },

    rules: {
        "max-len": ["error", 80],

        "no-console": ["error", {
            allow: ["warn", "error"],
        }],

        "no-unused-vars": ["error", {
            argsIgnorePattern: "^_",
            varsIgnorePattern: "^_",
        }],
    },
}];