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
 * A half-open integer interval whose start is inclusive and end is exclusive.
 */
public class Range {

    /**
     * Inclusive lower bound of the range.
     */
    public int start;

    /**
     * Exclusive upper bound of the range.
     */
    public int end;

    /**
     * Creates a configured Range value using the supplied values.
     *
     * @param s channel values used to initialize or update the matrix
     * @param e exclusive upper bound of the interval
     */
    public Range(int s, int e) {
        this.start = s;
        this.end = e;
    }

    /**
     * Creates an empty Range value.
     */
    public Range() {
        this(0, 0);
    }

    /**
     * Creates a configured Range value using the supplied values.
     *
     * @param vals component values in the order used by {@code Range}; missing array entries are initialized by this
     *             implementation
     */
    public Range(double[] vals) {
        set(vals);
    }

    /**
     * Updates this object from the supplied values.
     *
     * @param vals component values in the order used by {@code Range}; missing array entries are initialized by this
     *             implementation
     */
    public void set(double[] vals) {
        if (vals != null) {
            start = vals.length > 0 ? (int) vals[0] : 0;
            end = vals.length > 1 ? (int) vals[1] : 0;
        } else {
            start = 0;
            end = 0;
        }

    }

    /**
     * Returns the number of positions in this half-open interval.
     *
     * @return end minus start
     */
    public int size() {
        return empty() ? 0 : end - start;
    }

    /**
     * Reports whether this object has no usable contents.
     *
     * @return true if no usable contents are present; false otherwise
     */
    public boolean empty() {
        return end <= start;
    }

    /**
     * Creates an unrestricted range using Integer.MIN_VALUE and Integer.MAX_VALUE.
     *
     * @return an unrestricted range using the native range sentinels
     */
    public static Range all() {
        return new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the overlapping half-open interval of this range and the supplied range.
     *
     * @param r1 r1 index interval
     * @return the overlap of the two half-open intervals
     * @throws NullPointerException if r1 is null
     */
    public Range intersection(Range r1) {
        Range r = new Range(Math.max(r1.start, this.start), Math.min(r1.end, this.end));
        r.end = Math.max(r.end, r.start);
        return r;
    }

    /**
     * Returns a range translated by the supplied offset.
     *
     * @param delta integer offset added to both interval bounds
     * @return an interval translated by delta
     */
    public Range shift(int delta) {
        return new Range(start + delta, end + delta);
    }

    /**
     * Returns an independent value copy of this object.
     *
     * @return an independent {@code Range} value with copies of this object's components
     */
    public Range clone() {
        return new Range(start, end);
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
        temp = Double.doubleToLongBits(start);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(end);
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
        if (!(obj instanceof Range))
            return false;
        Range it = (Range) obj;
        return start == it.start && end == it.end;
    }

    /**
     * Returns a textual representation of this object.
     *
     * @return the Range field values formatted as text
     */
    @Override
    public String toString() {
        return "[" + start + ", " + end + ")";
    }
}
