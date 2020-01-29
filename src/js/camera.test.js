import {
  getPosition,
  getScale,
  getCameraMatrix,
  getModelViewMatrix,
  getProjectionMatrix,
  getMVPMatrix,
  moveBy,
  moveTo,
  zoomIn,
  zoomOut,
} from "./camera";
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

describe("camera", () => {
  it("gets position", () => {
    let position = getPosition({ camera: { position: [10, 20, 0] } });
    expect(vec3.exactEquals(position, [10, 20, 0])).toBe(true);
  });
  it("gets scale", () => {
    let scale = getScale({ camera: { scale: 4 } });
    expect(scale).toBe(4);
  });
  it("gets camera matrix", () => {
    let camera = getCameraMatrix([1, 2, 3], 4);
    expect(camera).toEqualMat4(
      // prettier-ignore
      [
        4, 0, 0, 0,
        0, 4, 0, 0,
        0, 0, 1, 0,
        1, 2, 3, 1,
      ],
    );
  });
  it("gets model-view matrix", () => {
    let mv = getModelViewMatrix(getCameraMatrix([1, 2, 3], 4));
    expect(mv).toEqualMat4(
      // prettier-ignore
      [
         0.25,  0   ,  0, 0,
         0   ,  0.25,  0, 0,
         0   ,  0   ,  1, 0,
        -0.25, -0.5 , -3, 1,
      ],
    );
  });
  it("gets projection matrix", () => {
    let proj = getProjectionMatrix();
    expect(proj).toEqualMat4(
      // prettier-ignore
      [
        1, 0,  0  , 0,
        0, 1,  0  , 0,
        0, 0, -0.5, 0,
        0, 0, -3  , 1,
      ],
    );
  });
  it("gets model-view-projection matrix", () => {
    let mv = getModelViewMatrix(getCameraMatrix([1, 2, 3], 4));
    let proj = getProjectionMatrix();
    let mvp = getMVPMatrix(mv, proj);
    expect(mvp).toEqualMat4(
      // prettier-ignore
      [
         0.25,  0   ,  0  , 0,
         0   ,  0.25,  0  , 0,
         0   ,  0   , -0.5, 0,
        -0.25, -0.5 , -1.5, 1,
      ],
    );
  });
  it("zooms in", () => {
    let db = zoomIn({ camera: { position: [0, 0, 0], scale: 1 } });
    expect(db.camera.scale).toBe(2);
  });
  it("zooms in with anchor", () => {
    let db = zoomIn({ camera: { position: [10, 20, 0], scale: 1 } }, [
      40,
      80,
      0,
    ]);
    expect(db.camera.position).toEqualVec3([-20, -40, 0]);
    expect(db.camera.scale).toBe(2);
  });
  it("zooms in with anchor at current position", () => {
    let db = zoomIn({ camera: { position: [10, 20, 0], scale: 1 } }, [
      10,
      20,
      0,
    ]);
    expect(db.camera.position).toEqualVec3([10, 20, 0]);
    expect(db.camera.scale).toBe(2);
  });
  it("zooms out", () => {
    let db = zoomOut({ camera: { position: [0, 0, 0], scale: 1 } });
    expect(db.camera.scale).toBe(0.5);
  });
  it("zooms out with anchor", () => {
    let db = zoomOut({ camera: { position: [10, 20, 0], scale: 1 } }, [
      40,
      80,
      0,
    ]);
    expect(db.camera.position).toEqualVec3([25, 50, 0]);
    expect(db.camera.scale).toBe(0.5);
  });
  it("zooms out with anchor at current position", () => {
    let db = zoomOut({ camera: { position: [10, 20, 0], scale: 1 } }, [
      10,
      20,
      0,
    ]);
    expect(db.camera.position).toEqualVec3([10, 20, 0]);
    expect(db.camera.scale).toBe(0.5);
  });
  it("moves to", () => {
    let db = moveTo({ camera: { position: [0, 0, 0] } }, [10, 20, 0]);
    expect(db.camera.position).toEqualVec3([10, 20, 0]);
  });
  it("moves by", () => {
    let db = moveBy({ camera: { position: [1, 2, 0] } }, [9, 18, 0]);
    expect(db.camera.position).toEqualVec3([10, 20, 0]);
  });
});
