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
 * A width and height pair represented by double-precision values.
 */
public class Size {

    /**
     * Width of the geometric extent.
     */
    public double width;

    /**
     * Height of the geometric extent.
     */
    public double height;

    /**
     * Creates a configured Size value using the supplied values.
     *
     * @param width  width in pixels or the output line-width estimates
     * @param height height of the size or rectangle
     */
    public Size(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Creates an empty Size value.
     */
    public Size() {
        this(0, 0);
    }

    /**
     * Creates a configured Size value using the supplied values.
     *
     * @param p p coordinates
     * @throws NullPointerException if p is null
     */
    public Size(Point p) {
        width = p.x;
        height = p.y;
    }

    /**
     * Creates a configured Size value using the supplied values.
     *
     * @param vals component values in the order used by {@code Size}; missing array entries are initialized by this
     *             implementation
     */
    public Size(double[] vals) {
        set(vals);
    }

    /**
     * Updates this object from the supplied values.
     *
     * @param vals component values in the order used by {@code Size}; missing array entries are initialized by this
     *             implementation
     */
    public void set(double[] vals) {
        if (vals != null) {
            width = vals.length > 0 ? vals[0] : 0;
            height = vals.length > 1 ? vals[1] : 0;
        } else {
            width = 0;
            height = 0;
        }
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
     * Returns an independent value copy of this object.
     *
     * @return an independent {@code Size} value with copies of this object's components
     */
    public Size clone() {
        return new Size(width, height);
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
        if (!(obj instanceof Size))
            return false;
        Size it = (Size) obj;
        return width == it.width && height == it.height;
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the Size field values formatted as text
     */
    @Override
    public String toString() {
        return (int) width + "x" + (int) height;
    }

}
