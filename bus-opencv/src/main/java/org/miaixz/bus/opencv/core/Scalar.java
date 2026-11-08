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
 * A four-component scalar used for channel values, colors, and matrix initialization.
 */
public class Scalar {

    /**
     * Four channel values stored in component order.
     */
    public double val[];

    /**
     * Creates a configured Scalar value using the supplied values.
     *
     * @param v0 scalar channel value at index 0
     * @param v1 scalar channel value at index 1
     * @param v2 scalar channel value at index 2
     * @param v3 scalar channel value at index 3
     */
    public Scalar(double v0, double v1, double v2, double v3) {
        val = new double[] { v0, v1, v2, v3 };
    }

    /**
     * Creates a configured Scalar value using the supplied values.
     *
     * @param v0 scalar channel value at index 0
     * @param v1 scalar channel value at index 1
     * @param v2 scalar channel value at index 2
     */
    public Scalar(double v0, double v1, double v2) {
        val = new double[] { v0, v1, v2, 0 };
    }

    /**
     * Creates a configured Scalar value using the supplied values.
     *
     * @param v0 scalar channel value at index 0
     * @param v1 scalar channel value at index 1
     */
    public Scalar(double v0, double v1) {
        val = new double[] { v0, v1, 0, 0 };
    }

    /**
     * Creates a configured Scalar value using the supplied values.
     *
     * @param v0 scalar channel value at index 0
     */
    public Scalar(double v0) {
        val = new double[] { v0, 0, 0, 0 };
    }

    /**
     * Creates a configured Scalar value using the supplied values.
     *
     * @param vals component values in the order used by {@code Scalar}; missing array entries are initialized by this
     *             implementation
     */
    public Scalar(double[] vals) {
        if (vals != null && vals.length == 4)
            val = vals.clone();
        else {
            val = new double[4];
            set(vals);
        }
    }

    /**
     * Updates this object from the supplied values.
     *
     * @param vals component values in the order used by {@code Scalar}; missing array entries are initialized by this
     *             implementation
     */
    public void set(double[] vals) {
        if (vals != null) {
            val[0] = vals.length > 0 ? vals[0] : 0;
            val[1] = vals.length > 1 ? vals[1] : 0;
            val[2] = vals.length > 2 ? vals[2] : 0;
            val[3] = vals.length > 3 ? vals[3] : 0;
        } else
            val[0] = val[1] = val[2] = val[3] = 0;
    }

    /**
     * Creates a scalar with all four channel values equal to v.
     *
     * @param v value assigned to or compared with this object
     * @return a scalar with all four components equal to v
     */
    public static Scalar all(double v) {
        return new Scalar(v, v, v, v);
    }

    /**
     * Returns an independent value copy of this object.
     *
     * @return an independent {@code Scalar} value with copies of this object's components
     */
    public Scalar clone() {
        return new Scalar(val);
    }

    /**
     * Returns the component-wise product with the supplied scalar values.
     *
     * @param it    it channel values
     * @param scale multiplicative scale applied by the operation
     * @return the component-wise scaled scalar product
     * @throws NullPointerException if it is null
     */
    public Scalar mul(Scalar it, double scale) {
        return new Scalar(val[0] * it.val[0] * scale, val[1] * it.val[1] * scale, val[2] * it.val[2] * scale,
                val[3] * it.val[3] * scale);
    }

    /**
     * Returns the component-wise product with the supplied scalar values.
     *
     * @param it it channel values
     * @return the component-wise scaled scalar product
     */
    public Scalar mul(Scalar it) {
        return mul(it, 1);
    }

    /**
     * Returns the scalar conjugate with the last three components negated.
     *
     * @return a scalar with components one through three negated
     */
    public Scalar conj() {
        return new Scalar(val[0], -val[1], -val[2], -val[3]);
    }

    /**
     * Reports whether the scalar has no imaginary components.
     *
     * @return true if the last three components are zero
     */
    public boolean isReal() {
        return val[1] == 0 && val[2] == 0 && val[3] == 0;
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
        result = prime * result + java.util.Arrays.hashCode(val);
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
        if (!(obj instanceof Scalar))
            return false;
        Scalar it = (Scalar) obj;
        if (!java.util.Arrays.equals(val, it.val))
            return false;
        return true;
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the Scalar field values formatted as text
     */
    @Override
    public String toString() {
        return "[" + val[0] + ", " + val[1] + ", " + val[2] + ", " + val[3] + "]";
    }

}
