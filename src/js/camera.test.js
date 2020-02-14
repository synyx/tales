import {
  fitRect,
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

describe("camera", () => {
  it("gets position", () => {
    let position = getPosition({ camera: { position: [10, 20, 0] } });
    expect(position).toEqualVec3([10, 20, 0]);
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
    let proj = getProjectionMatrix(1);
    expect(proj).toEqualMat4(
      // prettier-ignore
      [
        1, 0,  0, 0,
        0, 1,  0, 0,
        0, 0, -1, 0,
        0, 0,  0, 1,
      ],
    );
  });
  it("gets model-view-projection matrix", () => {
    let mv = getModelViewMatrix(getCameraMatrix([1, 2, 3], 4));
    let proj = getProjectionMatrix(1);
    let mvp = getMVPMatrix(mv, proj);
    expect(mvp).toEqualMat4(
      // prettier-ignore
      [
         0.25,  0   ,  0, 0,
         0   ,  0.25,  0, 0,
         0   ,  0   , -1, 0,
        -0.25, -0.5 ,  3, 1,
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
  it("zooms in with factor", () => {
    let db = zoomIn(
      { camera: { position: [0, 0, 0], scale: 1 } },
      [0, 0, 0],
      0.5,
    );
    expect(db.camera.scale).toBe(1.5);
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
  it("zooms out with factor", () => {
    let db = zoomOut(
      { camera: { position: [0, 0, 0], scale: 1 } },
      [0, 0, 0],
      0.25,
    );
    expect(db.camera.scale).toBe(0.8);
  });
  it("moves to", () => {
    let db = moveTo({ camera: { position: [0, 0, 0] } }, [10, 20, 0]);
    expect(db.camera.position).toEqualVec3([10, 20, 0]);
  });
  it("moves by", () => {
    let db = moveBy({ camera: { position: [1, 2, 0] } }, [9, 18, 0]);
    expect(db.camera.position).toEqualVec3([10, 20, 0]);
  });
  it("fits landscape rect", () => {
    let db = fitRect({ camera: {} }, { width: 200, height: 100, x: 0, y: 0 }, [
      0,
      0,
      400,
      200,
    ]);
    expect(db.camera.position).toEqualVec3([100, 50, 0]);
    expect(db.camera.scale).toBe(50);
  });
  it("fits portait rect", () => {
    let db = fitRect(
      { camera: {} },
      { width: 100, height: 200, x: 25, y: 50 },
      [0, 0, 400, 200],
    );
    expect(db.camera.position).toEqualVec3([75, 150, 0]);
    expect(db.camera.scale).toBe(100);
  });
});
