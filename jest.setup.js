import { mat4, vec3 } from "gl-matrix";

expect.extend({
  toEqualMat4(received, expected) {
    const pass = mat4.exactEquals(received, expected);
    if (pass) {
      return {
        message: () =>
          `expected [${received}] not to be equal to [${expected}]`,
        pass: true,
      };
    } else {
      return {
        message: () => `expected [${received}] to be equal to [${expected}]`,
        pass: false,
      };
    }
  },
  toEqualVec3(received, expected) {
    const pass = vec3.exactEquals(received, expected);
    if (pass) {
      return {
        message: () =>
          `expected [${received}] not to be equal to [${expected}]`,
        pass: true,
      };
    } else {
      return {
        message: () => `expected [${received}] to be equal to [${expected}]`,
        pass: false,
      };
    }
  },
});
