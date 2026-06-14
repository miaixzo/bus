/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble.opencv.seg;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * Represents a geometric segment containing a series of 2D points. Extends ArrayList to provide direct point
 * manipulation while supporting hierarchical structures through child segments.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Segment extends ArrayList<Point2D> {

    /**
     * The children value.
     */
    protected final List<Segment> children = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public Segment() {
        super();
    }

    /**
     * Creates a new instance.
     *
     * @param point2DList the point 2 d list.
     */
    public Segment(Collection<? extends Point2D> point2DList) {
        this(point2DList, false);
    }

    /**
     * Creates a new instance.
     *
     * @param point2DList the point 2 d list.
     * @param forceClose  the force close.
     */
    public Segment(Collection<? extends Point2D> point2DList, boolean forceClose) {
        super();
        setPoints(point2DList, forceClose);
    }

    /**
     * Creates a new instance.
     *
     * @param points the points.
     */
    public Segment(float[] points) {
        this(points, null, false, null);
    }

    /**
     * Creates a new instance.
     *
     * @param points     the points.
     * @param inverse    the inverse.
     * @param forceClose the force close.
     * @param dim        the dim.
     */
    public Segment(float[] points, AffineTransform inverse, boolean forceClose, Dimension dim) {
        setPoints(points, inverse, forceClose, dim);
    }

    /**
     * Creates a new instance.
     *
     * @param pts the pts.
     */
    public Segment(double[] pts) {
        this(pts, null, false, null);
    }

    /**
     * Creates a new instance.
     *
     * @param pts        the pts.
     * @param inverse    the inverse.
     * @param forceClose the force close.
     * @param dim        the dim.
     */
    public Segment(double[] pts, AffineTransform inverse, boolean forceClose, Dimension dim) {
        setPoints(pts, inverse, forceClose, dim);
    }

    /**
     * Sets the points.
     *
     * @param point2DList the point 2 d list.
     * @param forceClose  the force close.
     */
    public void setPoints(Collection<? extends Point2D> point2DList, boolean forceClose) {
        clear();
        if (point2DList == null || point2DList.isEmpty()) {
            return;
        }
        addAll(point2DList);
        if (forceClose && isOpenSegment()) {
            Point2D firstPoint = get(0);
            add(new Point2D.Double(firstPoint.getX(), firstPoint.getY()));
        }
    }

    /**
     * Sets the points.
     *
     * @param points     the points.
     * @param forceClose the force close.
     * @param dim        the dim.
     */
    public void setPoints(float[] points, boolean forceClose, Dimension dim) {
        setPoints(points, null, forceClose, dim);
    }

    /**
     * Sets the points.
     *
     * @param points     the points.
     * @param inverse    the inverse.
     * @param forceClose the force close.
     * @param dim        the dim.
     */
    public void setPoints(float[] points, AffineTransform inverse, boolean forceClose, Dimension dim) {
        Objects.requireNonNull(points, "Points array cannot be null");
        setPoints(convertFloatToDouble(points), inverse, forceClose, dim);
    }

    /**
     * Sets the points.
     *
     * @param points     the points.
     * @param forceClose the force close.
     * @param dim        the dim.
     */
    public void setPoints(double[] points, boolean forceClose, Dimension dim) {
        setPoints(points, null, forceClose, dim);
    }

    /**
     * Sets the points.
     *
     * @param points     the points.
     * @param inverse    the inverse.
     * @param forceClose the force close.
     * @param dim        the dim.
     */
    public void setPoints(double[] points, AffineTransform inverse, boolean forceClose, Dimension dim) {
        Objects.requireNonNull(points, "Points array cannot be null");
        if (points.length < 4) { // Need at least 2 points (4 coordinates)
            clear();
            return;
        }

        double[] transformedPoints = applyTransform(points, inverse);
        clear();
        addPointsFromArray(transformedPoints, forceClose, dim);
    }

    /**
     * Applies the transform.
     *
     * @param points    the points.
     * @param transform the transform.
     * @return the operation result.
     */
    private double[] applyTransform(double[] points, AffineTransform transform) {
        if (transform == null) {
            return points;
        }
        double[] transformedPoints = new double[points.length];
        transform.transform(points, 0, transformedPoints, 0, points.length / 2);
        return transformedPoints;
    }

    /**
     * Adds the points from array.
     *
     * @param pts        the pts.
     * @param forceClose the force close.
     * @param dim        the dim.
     */
    private void addPointsFromArray(double[] pts, boolean forceClose, Dimension dim) {
        int pointCount = pts.length / 2;
        ensureCapacity(pointCount + (forceClose ? 1 : 0));

        boolean shouldScale = isValidDimension(dim);

        for (int i = 0; i < pointCount; i++) {
            double x = shouldScale ? pts[i * 2] * dim.width : pts[i * 2];
            double y = shouldScale ? pts[i * 2 + 1] * dim.height : pts[i * 2 + 1];
            add(new Point2D.Double(x, y));
        }

        if (forceClose && isOpenSegment()) {
            Point2D first = get(0);
            add(new Point2D.Double(first.getX(), first.getY()));
        }
    }

    /**
     * Checks whether the valid dimension condition is true.
     *
     * @param dim the dim.
     * @return true if the valid dimension condition is true; otherwise false.
     */
    private boolean isValidDimension(Dimension dim) {
        return dim != null && dim.width > 0 && dim.height > 0;
    }

    /**
     * Checks whether the open segment condition is true.
     *
     * @return true if the open segment condition is true; otherwise false.
     */
    private boolean isOpenSegment() {
        return size() >= 2 && !get(0).equals(get(size() - 1));
    }

    /**
     * Returns the children.
     *
     * @return the children.
     */
    public List<Segment> getChildren() {
        return List.copyOf(children);
    }

    /**
     * Adds the child.
     *
     * @param child the child.
     */
    public void addChild(Segment child) {
        if (child != null && child != this) {
            children.add(child);
        }
    }

    /**
     * Executes the equals operation.
     *
     * @param o the o.
     * @return true if the equals condition is true; otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Segment segment))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(children, segment.children);
    }

    /**
     * Checks whether the hash code condition is true.
     *
     * @return true if the hash code condition is true; otherwise false.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), children);
    }

    /**
     * Converts the float to double.
     *
     * @param floatArray the float array.
     * @return the operation result.
     */
    public static double[] convertFloatToDouble(float[] floatArray) {
        if (floatArray == null) {
            return null;
        }
        var doubleArray = new double[floatArray.length];
        for (int i = 0; i < floatArray.length; i++) {
            doubleArray[i] = floatArray[i];
        }
        return doubleArray;
    }

}
