import Polyglot from "node-polyglot";
import en from "./en.json";
import de from "./de.json";
import { bestMatchingLanguage } from "./locale";

const DEFAULT_LANGUAGE = "en";

const phrases = { de, en };
const language = bestMatchingLanguage(Object.keys(phrases)) || DEFAULT_LANGUAGE;

const polyglot = new Polyglot({
  phrases: phrases[language],
  locale: language,
  allowMissing: false,
  onMissingKey: (key, _, locale) => {
    console.warn("Missing", locale, "translation for key", key);
    return `🕵️${key}`;
  },
});

export const i18n = (...args) => polyglot.t(...args);
export default i18n;
