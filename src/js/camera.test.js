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
    expect(
      // prettier-ignore
      mat4.exactEquals(camera, [
        4, 0, 0, 0,
        0, 4, 0, 0,
        0, 0, 1, 0,
        1, 2, 3, 1,
      ]),
    ).toBe(true);
  });
  it("gets model-view matrix", () => {
    let mv = getModelViewMatrix(getCameraMatrix([1, 2, 3], 4));
    expect(
      // prettier-ignore
      mat4.exactEquals(mv, [
         0.25,  0   ,  0, 0,
         0   ,  0.25,  0, 0,
         0   ,  0   ,  1, 0,
        -0.25, -0.5 , -3, 1,
      ]),
    ).toBe(true);
  });
  it("gets projection matrix", () => {
    let proj = getProjectionMatrix();
    expect(
      // prettier-ignore
      mat4.exactEquals(proj, [
        1, 0,  0  , 0,
        0, 1,  0  , 0,
        0, 0, -0.5, 0,
        0, 0, -3  , 1,
      ]),
    ).toBe(true);
  });
  it("gets model-view-projection matrix", () => {
    let mv = getModelViewMatrix(getCameraMatrix([1, 2, 3], 4));
    let proj = getProjectionMatrix();
    let mvp = getMVPMatrix(mv, proj);
    expect(
      // prettier-ignore
      mat4.exactEquals(mvp, [
         0.25,  0   ,  0  , 0,
         0   ,  0.25,  0  , 0,
         0   ,  0   , -0.5, 0,
        -0.25, -0.5 , -1.5, 1,
      ]),
    ).toBe(true);
  });
  it("zooms in", () => {
    let db = zoomIn({ camera: { scale: 1 } });
    expect(db.camera.scale).toBe(2);
  });
  it("zooms out", () => {
    let db = zoomOut({ camera: { scale: 1 } });
    expect(db.camera.scale).toBe(0.5);
  });
  it("moves to", () => {
    let db = moveTo({ camera: { position: [0, 0, 0] } }, [10, 20, 0]);
    expect(vec3.exactEquals(db.camera.position, [10, 20, 0])).toBe(true);
  });
  it("moves by", () => {
    let db = moveBy({ camera: { position: [1, 2, 0] } }, [9, 18, 0]);
    expect(vec3.exactEquals(db.camera.position, [10, 20, 0])).toBe(true);
  });
});
