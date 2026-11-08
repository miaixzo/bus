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
 * A two-dimensional point with double-precision coordinates.
 */
public class Point {

    /**
     * Horizontal coordinate.
     */
    public double x;

    /**
     * Vertical coordinate.
     */
    public double y;

    /**
     * Creates a configured Point value using the supplied values.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates an empty Point value.
     */
    public Point() {
        this(0, 0);
    }

    /**
     * Creates a configured Point value using the supplied values.
     *
     * @param vals component values in the order used by {@code Point}; missing array entries are initialized by this
     *             implementation
     */
    public Point(double[] vals) {
        this();
        set(vals);
    }

    /**
     * Updates this object from the supplied values.
     *
     * @param vals component values in the order used by {@code Point}; missing array entries are initialized by this
     *             implementation
     */
    public void set(double[] vals) {
        if (vals != null) {
            x = vals.length > 0 ? vals[0] : 0;
            y = vals.length > 1 ? vals[1] : 0;
        } else {
            x = 0;
            y = 0;
        }
    }

    /**
     * Returns an independent value copy of this object.
     *
     * @return an independent {@code Point} value with copies of this object's components
     */
    public Point clone() {
        return new Point(x, y);
    }

    /**
     * Computes the dot product with the supplied vector.
     *
     * @param p p coordinates
     * @return the two-dimensional dot product
     * @throws NullPointerException if p is null
     */
    public double dot(Point p) {
        return x * p.x + y * p.y;
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
        if (!(obj instanceof Point))
            return false;
        Point it = (Point) obj;
        return x == it.x && y == it.y;
    }

    /**
     * Tests whether this point lies inside the supplied rectangle.
     *
     * @param r r region
     * @return true if this point lies inside the rectangle
     * @throws NullPointerException if r is null
     */
    public boolean inside(Rect r) {
        return r.contains(this);
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the Point field values formatted as text
     */
    @Override
    public String toString() {
        return "{" + x + ", " + y + "}";
    }
}
