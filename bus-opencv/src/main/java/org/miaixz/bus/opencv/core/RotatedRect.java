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
 * A rotated rectangle represented by its center, dimensions, and rotation angle.
 */
public class RotatedRect {

    /**
     * Center coordinates of the rotated rectangle.
     */
    public Point center;
    /**
     * Width and height of the rotated rectangle.
     */
    public Size size;
    /**
     * Rotation angle in degrees.
     */
    public double angle;

    /**
     * Creates an empty RotatedRect value.
     */
    public RotatedRect() {
        this.center = new Point();
        this.size = new Size();
        this.angle = 0;
    }

    /**
     * Creates a configured RotatedRect value using the supplied values.
     *
     * @param c c coordinates
     * @param s channel values used to initialize or update the matrix
     * @param a component values in the order used by {@code RotatedRect}; missing array entries are initialized by this
     *          implementation
     * @throws NullPointerException if c, s is null
     */
    public RotatedRect(Point c, Size s, double a) {
        this.center = c.clone();
        this.size = s.clone();
        this.angle = a;
    }

    /**
     * Creates a configured RotatedRect value using the supplied values.
     *
     * @param vals component values in the order used by {@code RotatedRect}; missing array entries are initialized by
     *             this implementation
     */
    public RotatedRect(double[] vals) {
        this();
        set(vals);
    }

    /**
     * Updates this object from the supplied values.
     *
     * @param vals component values in the order used by {@code RotatedRect}; missing array entries are initialized by
     *             this implementation
     */
    public void set(double[] vals) {
        if (vals != null) {
            center.x = vals.length > 0 ? (double) vals[0] : 0;
            center.y = vals.length > 1 ? (double) vals[1] : 0;
            size.width = vals.length > 2 ? (double) vals[2] : 0;
            size.height = vals.length > 3 ? (double) vals[3] : 0;
            angle = vals.length > 4 ? (double) vals[4] : 0;
        } else {
            center.x = 0;
            center.y = 0;
            size.width = 0;
            size.height = 0;
            angle = 0;
        }
    }

    /**
     * Writes the four vertices of the rotated rectangle into the supplied array.
     *
     * @param pt array of length at least four that receives the rectangle vertices
     */
    public void points(Point pt[]) {
        double _angle = angle * Math.PI / 180.0;
        double b = (double) Math.cos(_angle) * 0.5f;
        double a = (double) Math.sin(_angle) * 0.5f;

        pt[0] = new Point(center.x - a * size.height - b * size.width, center.y + b * size.height - a * size.width);

        pt[1] = new Point(center.x + a * size.height - b * size.width, center.y - b * size.height - a * size.width);

        pt[2] = new Point(2 * center.x - pt[0].x, 2 * center.y - pt[0].y);

        pt[3] = new Point(2 * center.x - pt[1].x, 2 * center.y - pt[1].y);
    }

    /**
     * Returns the enclosing axis-aligned rectangle with integer coordinates.
     *
     * @return the smallest integer-coordinate axis-aligned rectangle containing all four vertices
     */
    public Rect boundingRect() {
        Point pt[] = new Point[4];
        points(pt);
        Rect r = new Rect((int) Math.floor(Math.min(Math.min(Math.min(pt[0].x, pt[1].x), pt[2].x), pt[3].x)),
                (int) Math.floor(Math.min(Math.min(Math.min(pt[0].y, pt[1].y), pt[2].y), pt[3].y)),
                (int) Math.ceil(Math.max(Math.max(Math.max(pt[0].x, pt[1].x), pt[2].x), pt[3].x)),
                (int) Math.ceil(Math.max(Math.max(Math.max(pt[0].y, pt[1].y), pt[2].y), pt[3].y)));
        r.width -= r.x - 1;
        r.height -= r.y - 1;
        return r;
    }

    /**
     * Returns an independent value copy of this object.
     *
     * @return an independent {@code RotatedRect} value with copies of this object's components
     */
    public RotatedRect clone() {
        return new RotatedRect(center, size, angle);
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
        temp = Double.doubleToLongBits(center.x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(center.y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(size.width);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(size.height);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(angle);
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
        if (!(obj instanceof RotatedRect))
            return false;
        RotatedRect it = (RotatedRect) obj;
        return center.equals(it.center) && size.equals(it.size) && angle == it.angle;
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the RotatedRect field values formatted as text
     */
    @Override
    public String toString() {
        return "{ " + center + " " + size + " * " + angle + " }";
    }
}
