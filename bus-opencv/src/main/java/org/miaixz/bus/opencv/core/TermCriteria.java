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
 * Termination criteria based on an iteration limit, an accuracy threshold, or both.
 */
public class TermCriteria {

    /**
     * The maximum number of iterations or elements to compute.
     */
    public static final int COUNT = 1;
    /**
     * Alias of the COUNT option code.
     */
    public static final int MAX_ITER = COUNT;
    /**
     * The desired accuracy or change in parameters at which the iterative algorithm stops.
     */
    public static final int EPS = 2;

    /**
     * Combination of iteration-count and convergence-tolerance criteria.
     */
    public int type;
    /**
     * Maximum iteration count.
     */
    public int maxCount;
    /**
     * Convergence tolerance.
     */
    public double epsilon;

    /**
     * Creates a configured TermCriteria value using the supplied values.
     *
     * @param type     the type of termination criteria: COUNT, EPS or COUNT + EPS
     * @param maxCount the maximum number of iterations/elements
     * @param epsilon  the desired accuracy
     */
    public TermCriteria(int type, int maxCount, double epsilon) {
        this.type = type;
        this.maxCount = maxCount;
        this.epsilon = epsilon;
    }

    /**
     * Creates an empty TermCriteria value.
     */
    public TermCriteria() {
        this(0, 0, 0.0);
    }

    /**
     * Creates a configured TermCriteria value using the supplied values.
     *
     * @param vals component values in the order used by {@code TermCriteria}; missing array entries are initialized by
     *             this implementation
     */
    public TermCriteria(double[] vals) {
        set(vals);
    }

    /**
     * Updates this object from the supplied values.
     *
     * @param vals component values in the order used by {@code TermCriteria}; missing array entries are initialized by
     *             this implementation
     */
    public void set(double[] vals) {
        if (vals != null) {
            type = vals.length > 0 ? (int) vals[0] : 0;
            maxCount = vals.length > 1 ? (int) vals[1] : 0;
            epsilon = vals.length > 2 ? (double) vals[2] : 0;
        } else {
            type = 0;
            maxCount = 0;
            epsilon = 0;
        }
    }

    /**
     * Returns an independent value copy of this object.
     *
     * @return an independent {@code TermCriteria} value with copies of this object's components
     */
    public TermCriteria clone() {
        return new TermCriteria(type, maxCount, epsilon);
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
        temp = Double.doubleToLongBits(type);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxCount);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(epsilon);
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
        if (!(obj instanceof TermCriteria))
            return false;
        TermCriteria it = (TermCriteria) obj;
        return type == it.type && maxCount == it.maxCount && epsilon == it.epsilon;
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the TermCriteria field values formatted as text
     */
    @Override
    public String toString() {
        return "{ type: " + type + ", maxCount: " + maxCount + ", epsilon: " + epsilon + "}";
    }
}
