/**
 * Calculates the intersection between two rectangles, each defined by
 * {x, y, width, height}.
 *
 * Returns the intersecting rectangle or false if the given
 * rects do not intersect.
 */
export const intersectRects = (a, b) => {
  let rect = {
    x: Math.max(a.x, b.x),
    y: Math.max(a.y, b.y),
    width: Math.min(a.x + a.width, b.x + b.width) - Math.max(a.x, b.x),
    height: Math.min(a.y + a.height, b.y + b.height) - Math.max(a.y, b.y),
  };
  return rect.width > 0 && rect.height > 0 && rect;
};

/**
 * Adds a padding to the given rectangle, effectively
 * shrinking (positive padding) or growing (negative padding) it.
 *
 * The padding is relative to the shorter side of the rect and will be the
 * same absolute amount in both directions.
 */
export const padRect = (rect, padding) => {
  const paddingSize = Math.min(rect.width, rect.height) * padding;
  return {
    x: rect.x + paddingSize,
    y: rect.y + paddingSize,
    width: rect.width - paddingSize * 2,
    height: rect.height - paddingSize * 2,
  };
};

export const normalizeRect = rect => {
  let x1 = rect.x,
    y1 = rect.y,
    x2 = x1 + rect.width,
    y2 = y1 + rect.height;
  return {
    x: Math.min(x1, x2),
    y: Math.min(y1, y2),
    width: Math.abs(x2 - x1),
    height: Math.abs(y2 - y1),
  };
};
