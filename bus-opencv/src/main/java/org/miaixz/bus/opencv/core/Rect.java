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
package org.miaixz.bus.opencv.core;

/**
 * An axis-aligned rectangle with integer coordinates and dimensions.
 */
public class Rect {

    /**
     * Horizontal coordinate of the rectangle's top-left corner.
     */
    public int x;

    /**
     * Vertical coordinate of the rectangle's top-left corner.
     */
    public int y;

    /**
     * Width of the geometric extent.
     */
    public int width;

    /**
     * Height of the geometric extent.
     */
    public int height;

    /**
     * Creates a configured Rect value using the supplied values.
     *
     * @param x      horizontal coordinate
     * @param y      vertical coordinate
     * @param width  width in pixels or the output line-width estimates
     * @param height height of the size or rectangle
     */
    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Creates an empty Rect value.
     */
    public Rect() {
        this(0, 0, 0, 0);
    }

    /**
     * Creates a configured Rect value using the supplied values.
     *
     * @param p1 p1 coordinates
     * @param p2 p2 coordinates
     * @throws NullPointerException if p1, p2 is null
     */
    public Rect(Point p1, Point p2) {
        x = (int) (p1.x < p2.x ? p1.x : p2.x);
        y = (int) (p1.y < p2.y ? p1.y : p2.y);
        width = (int) (p1.x > p2.x ? p1.x : p2.x) - x;
        height = (int) (p1.y > p2.y ? p1.y : p2.y) - y;
    }

    /**
     * Creates a configured Rect value using the supplied values.
     *
     * @param p p coordinates
     * @param s channel values used to initialize or update the matrix
     * @throws NullPointerException if p, s is null
     */
    public Rect(Point p, Size s) {
        this((int) p.x, (int) p.y, (int) s.width, (int) s.height);
    }

    /**
     * Creates a configured Rect value using the supplied values.
     *
     * @param vals component values in the order used by {@code Rect}; missing array entries are initialized by this
     *             implementation
     */
    public Rect(double[] vals) {
        set(vals);
    }

    /**
     * Updates this object from the supplied values.
     *
     * @param vals component values in the order used by {@code Rect}; missing array entries are initialized by this
     *             implementation
     */
    public void set(double[] vals) {
        if (vals != null) {
            x = vals.length > 0 ? (int) vals[0] : 0;
            y = vals.length > 1 ? (int) vals[1] : 0;
            width = vals.length > 2 ? (int) vals[2] : 0;
            height = vals.length > 3 ? (int) vals[3] : 0;
        } else {
            x = 0;
            y = 0;
            width = 0;
            height = 0;
        }
    }

    /**
     * Returns an independent value copy of this object.
     *
     * @return an independent {@code Rect} value with copies of this object's components
     */
    public Rect clone() {
        return new Rect(x, y, width, height);
    }

    /**
     * Returns the top-left corner of the rectangle.
     *
     * @return the inclusive top-left corner
     */
    public Point tl() {
        return new Point(x, y);
    }

    /**
     * Returns the bottom-right corner of the rectangle.
     *
     * @return the exclusive bottom-right corner
     */
    public Point br() {
        return new Point(x + width, y + height);
    }

    /**
     * Returns the width and height of the rectangle.
     *
     * @return a new size with this rectangle's width and height
     */
    public Size size() {
        return new Size(width, height);
    }

    /**
     * Returns the area represented by this size or rectangle.
     *
     * @return width multiplied by height
     */
    public double area() {
        return width * height;
    }

    /**
     * Reports whether this object has no usable contents.
     *
     * @return true if no usable contents are present; false otherwise
     */
    public boolean empty() {
        return width <= 0 || height <= 0;
    }

    /**
     * Tests whether the supplied point lies within this region.
     *
     * @param p p coordinates
     * @return true if the point lies within the inclusive top/left and exclusive bottom/right rectangle bounds
     * @throws NullPointerException if p is null
     */
    public boolean contains(Point p) {
        return x <= p.x && p.x < x + width && y <= p.y && p.y < y + height;
    }

    /**
     * Returns a hash code derived from this object's value.
     *
     * @return a hash derived from the fields used by this class's equality comparison
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(height);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(width);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Compares this value with another object for value equality.
     *
     * @param obj object to compare, including null or a different Java type
     * @return true if the objects have equal values; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Rect))
            return false;
        Rect it = (Rect) obj;
        return x == it.x && y == it.y && width == it.width && height == it.height;
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the Rect field values formatted as text
     */
    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + width + "x" + height + "}";
    }
}
