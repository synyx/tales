import { dragEnter, dragOver, dragStart, drop } from "./preview-move";
import * as flyps from "flyps";

// eslint-disable-next-line no-import-assign
flyps.trigger = jest.fn();
const mockEv = {
  preventDefault: jest.fn(),
  dataTransfer: {
    getData: jest.fn(),
    setData: jest.fn(),
    setDragImage: jest.fn(),
    types: { includes: jest.fn() },
  },
  currentTarget: { getBoundingClientRect: jest.fn() },
};

beforeEach(() => {
  flyps.trigger.mockClear();
  mockEv.preventDefault.mockClear();
  mockEv.dataTransfer.getData.mockClear();
  mockEv.dataTransfer.setData.mockClear();
  mockEv.dataTransfer.setDragImage.mockClear();
  mockEv.dataTransfer.types.includes.mockClear();
});

describe("dragStart", () => {
  it("sets HTML's drag&drop properties", () => {
    dragStart(2, mockEv);
    expect(mockEv.dataTransfer.setData).toHaveBeenLastCalledWith(
      "application/x-tales-slide",
      2,
    );
    expect(mockEv.dataTransfer.setDragImage).toHaveBeenCalled();
  });
  it("deactivates any selected slide", () => {
    dragStart(3, mockEv);
    expect(flyps.trigger).toHaveBeenCalledWith("slide/deactivate");
  });
});

describe("dragEnter", () => {
  it("accepts a dragged Tales slide", () => {
    mockEv.dataTransfer.types.includes.mockReturnValue(true);
    dragEnter(1, mockEv);
    expect(mockEv.dataTransfer.types.includes).toHaveBeenCalledWith(
      "application/x-tales-slide",
    );
    expect(mockEv.preventDefault).toHaveBeenCalled();
  });
  it("rejects other dragged data types", () => {
    mockEv.dataTransfer.types.includes.mockReturnValue(false);
    dragEnter(1, mockEv);
    expect(mockEv.preventDefault).not.toHaveBeenCalled();
  });
});

describe("dragOver", () => {
  it("accepts a dragged Tales slide", () => {
    mockEv.dataTransfer.types.includes.mockReturnValue(true);
    mockEv.currentTarget.getBoundingClientRect.mockReturnValue({ top: 1000 });
    dragOver(1, 100, mockEv);
    expect(mockEv.dataTransfer.types.includes).toHaveBeenCalledWith(
      "application/x-tales-slide",
    );
    expect(mockEv.preventDefault).toHaveBeenCalled();
  });
  it("rejects other dragged data types", () => {
    mockEv.dataTransfer.types.includes.mockReturnValue(false);
    dragEnter(1, mockEv);
    expect(mockEv.preventDefault).not.toHaveBeenCalled();
  });
});

describe("drop", () => {
  it("moves the slide before the current one if dropped at the top", () => {
    mockEv.dataTransfer.getData.mockReturnValue(8);
    mockEv.currentTarget.getBoundingClientRect.mockReturnValue({ top: 1000 });
    mockEv.clientY = 1049;
    drop(5, 100, mockEv);
    expect(flyps.trigger).toHaveBeenCalledWith("slide/move", 8, 5);
  });
  it("moves the slide after the current one if dropped at the bottom", () => {
    mockEv.dataTransfer.getData.mockReturnValue(8);
    mockEv.currentTarget.getBoundingClientRect.mockReturnValue({ top: 1000 });
    mockEv.clientY = 1051;
    drop(5, 100, mockEv);
    expect(flyps.trigger).toHaveBeenCalledWith("slide/move", 8, 6);
  });
  it("doesn't move a slide that's dropped at its current pos", () => {
    mockEv.dataTransfer.getData.mockReturnValue(8);
    mockEv.currentTarget.getBoundingClientRect.mockReturnValue({ top: 1000 });
    mockEv.clientY = 1051;
    drop(8, 100, mockEv);
    expect(flyps.trigger).not.toHaveBeenCalled();
  });
});
