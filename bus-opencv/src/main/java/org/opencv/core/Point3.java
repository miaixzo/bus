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

//javadoc:Point3_
/**
 * Provides the {@code Point3} API.
 */
public class Point3 {

    /**
     * OpenCV constants used by this API.
     */
    public double x, y, z;

    /**
     * Creates a new {@code Point3} instance.
     *
     * @param x the {@code x} value
     * @param y the {@code y} value
     * @param z the {@code z} value
     */
    public Point3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a new {@code Point3} instance.
     */
    public Point3() {
        this(0, 0, 0);
    }

    /**
     * Creates a new {@code Point3} instance.
     *
     * @param p the {@code p} value
     */
    public Point3(Point p) {
        x = p.x;
        y = p.y;
        z = 0;
    }

    /**
     * Creates a new {@code Point3} instance.
     *
     * @param vals the {@code vals} value
     */
    public Point3(double[] vals) {
        this();
        set(vals);
    }

    /**
     * Performs the {@code set} operation.
     *
     * @param vals the {@code vals} value
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

    public Point3 clone() {
        return new Point3(x, y, z);
    }

    /**
     * Performs the {@code dot} operation.
     *
     * @param p the {@code p} value
     * @return the operation result
     */
    public double dot(Point3 p) {
        return x * p.x + y * p.y + z * p.z;
    }

    /**
     * Performs the {@code cross} operation.
     *
     * @param p the {@code p} value
     * @return the operation result
     */
    public Point3 cross(Point3 p) {
        return new Point3(y * p.z - z * p.y, z * p.x - x * p.z, x * p.y - y * p.x);
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Point3))
            return false;
        Point3 it = (Point3) obj;
        return x == it.x && y == it.y && z == it.z;
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + z + "}";
    }
}
