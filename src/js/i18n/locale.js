/**
 * Selects, from the given languages, the first matching language the user
 * has set in the browser preferences.
 */
export const bestMatchingLanguage = availableLanguages => {
  for (let locale of window.navigator.languages) {
    let language = locale.split("-")[0];
    if (availableLanguages.includes(language)) {
      return language;
    }
  }
};
