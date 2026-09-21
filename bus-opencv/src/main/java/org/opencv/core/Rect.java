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
package org.opencv.core;

//javadoc:Rect_
/**
 * Provides the {@code Rect} API.
 */
public class Rect {

    /**
     * OpenCV constants used by this API.
     */
    public int x, y, width, height;

    /**
     * Creates a new {@code Rect} instance.
     *
     * @param x the {@code x} value
     * @param y the {@code y} value
     * @param width the {@code width} value
     * @param height the {@code height} value
     */
    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Creates a new {@code Rect} instance.
     */
    public Rect() {
        this(0, 0, 0, 0);
    }

    /**
     * Creates a new {@code Rect} instance.
     *
     * @param p1 the {@code p1} value
     * @param p2 the {@code p2} value
     */
    public Rect(Point p1, Point p2) {
        x = (int) (p1.x < p2.x ? p1.x : p2.x);
        y = (int) (p1.y < p2.y ? p1.y : p2.y);
        width = (int) (p1.x > p2.x ? p1.x : p2.x) - x;
        height = (int) (p1.y > p2.y ? p1.y : p2.y) - y;
    }

    /**
     * Creates a new {@code Rect} instance.
     *
     * @param p the {@code p} value
     * @param s the {@code s} value
     */
    public Rect(Point p, Size s) {
        this((int) p.x, (int) p.y, (int) s.width, (int) s.height);
    }

    /**
     * Creates a new {@code Rect} instance.
     *
     * @param vals the {@code vals} value
     */
    public Rect(double[] vals) {
        set(vals);
    }

    /**
     * Performs the {@code set} operation.
     *
     * @param vals the {@code vals} value
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

    public Rect clone() {
        return new Rect(x, y, width, height);
    }

    /**
     * Performs the {@code tl} operation.
     *
     * @return the operation result
     */
    public Point tl() {
        return new Point(x, y);
    }

    /**
     * Performs the {@code br} operation.
     *
     * @return the operation result
     */
    public Point br() {
        return new Point(x + width, y + height);
    }

    /**
     * Performs the {@code size} operation.
     *
     * @return the operation result
     */
    public Size size() {
        return new Size(width, height);
    }

    /**
     * Performs the {@code area} operation.
     *
     * @return the operation result
     */
    public double area() {
        return width * height;
    }

    /**
     * Performs the {@code empty} operation.
     *
     * @return the operation result
     */
    public boolean empty() {
        return width <= 0 || height <= 0;
    }

    /**
     * Performs the {@code contains} operation.
     *
     * @param p the {@code p} value
     * @return the operation result
     */
    public boolean contains(Point p) {
        return x <= p.x && p.x < x + width && y <= p.y && p.y < y + height;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Rect))
            return false;
        Rect it = (Rect) obj;
        return x == it.x && y == it.y && width == it.width && height == it.height;
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + width + "x" + height + "}";
    }
}
