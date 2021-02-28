import { effector } from "flyps";

/**
 * Effect which applies the attributes to selected elements.
 * For example
 *   {
 *     body: { example: 123 }
 *   }
 * sets the attribute "example" of the body element to "123".
 */
effector("attrs", modifiers => {
  Object.entries(modifiers).forEach(([selector, attrs]) => {
    const node = document.querySelector(selector);
    Object.entries(attrs).forEach(([attr, value]) => {
      node.setAttribute(attr, value);
    });
  });
});
