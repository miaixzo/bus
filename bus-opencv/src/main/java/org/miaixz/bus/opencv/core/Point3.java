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
 * A three-dimensional point with double-precision coordinates.
 */
public class Point3 {

    /**
     * Horizontal coordinate.
     */
    public double x;

    /**
     * Vertical coordinate.
     */
    public double y;

    /**
     * Third-axis coordinate.
     */
    public double z;

    /**
     * Creates a configured Point3 value using the supplied values.
     *
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param z third coordinate or optimization result vector
     */
    public Point3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates an empty Point3 value.
     */
    public Point3() {
        this(0, 0, 0);
    }

    /**
     * Creates a configured Point3 value using the supplied values.
     *
     * @param p p coordinates
     * @throws NullPointerException if p is null
     */
    public Point3(Point p) {
        x = p.x;
        y = p.y;
        z = 0;
    }

    /**
     * Creates a configured Point3 value using the supplied values.
     *
     * @param vals component values in the order used by {@code Point3}; missing array entries are initialized by this
     *             implementation
     */
    public Point3(double[] vals) {
        this();
        set(vals);
    }

    /**
     * Updates this object from the supplied values.
     *
     * @param vals component values in the order used by {@code Point3}; missing array entries are initialized by this
     *             implementation
     */
    public void set(double[] vals) {
        if (vals != null) {
            x = vals.length > 0 ? vals[0] : 0;
            y = vals.length > 1 ? vals[1] : 0;
            z = vals.length > 2 ? vals[2] : 0;
        } else {
            x = 0;
            y = 0;
            z = 0;
        }
    }

    /**
     * Returns an independent value copy of this object.
     *
     * @return an independent {@code Point3} value with copies of this object's components
     */
    public Point3 clone() {
        return new Point3(x, y, z);
    }

    /**
     * Computes the dot product with the supplied vector.
     *
     * @param p p coordinates
     * @return the three-dimensional dot product
     * @throws NullPointerException if p is null
     */
    public double dot(Point3 p) {
        return x * p.x + y * p.y + z * p.z;
    }

    /**
     * Computes the cross product with the supplied vector.
     *
     * @param p p coordinates
     * @return the three-dimensional cross-product vector
     * @throws NullPointerException if p is null
     */
    public Point3 cross(Point3 p) {
        return new Point3(y * p.z - z * p.y, z * p.x - x * p.z, x * p.y - y * p.x);
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
        temp = Double.doubleToLongBits(z);
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
        if (!(obj instanceof Point3))
            return false;
        Point3 it = (Point3) obj;
        return x == it.x && y == it.y && z == it.z;
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the Point3 field values formatted as text
     */
    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + z + "}";
    }
}
