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

//javadoc:Range
/**
 * Provides the {@code Range} API.
 */
public class Range {

    /**
     * OpenCV constants used by this API.
     */
    public int start, end;

    /**
     * Creates a new {@code Range} instance.
     *
     * @param s the {@code s} value
     * @param e the {@code e} value
     */
    public Range(int s, int e) {
        this.start = s;
        this.end = e;
    }

    /**
     * Creates a new {@code Range} instance.
     */
    public Range() {
        this(0, 0);
    }

    /**
     * Creates a new {@code Range} instance.
     *
     * @param vals the {@code vals} value
     */
    public Range(double[] vals) {
        set(vals);
    }

    /**
     * Performs the {@code all} operation.
     *
     * @return the operation result
     */
    public static Range all() {
        return new Range(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Performs the {@code set} operation.
     *
     * @param vals the {@code vals} value
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
     * Performs the {@code size} operation.
     *
     * @return the operation result
     */
    public int size() {
        return empty() ? 0 : end - start;
    }

    /**
     * Performs the {@code empty} operation.
     *
     * @return the operation result
     */
    public boolean empty() {
        return end <= start;
    }

    /**
     * Performs the {@code intersection} operation.
     *
     * @param r1 the {@code r1} value
     * @return the operation result
     */
    public Range intersection(Range r1) {
        Range r = new Range(Math.max(r1.start, this.start), Math.min(r1.end, this.end));
        r.end = Math.max(r.end, r.start);
        return r;
    }

    /**
     * Performs the {@code shift} operation.
     *
     * @param delta the {@code delta} value
     * @return the operation result
     */
    public Range shift(int delta) {
        return new Range(start + delta, end + delta);
    }

    public Range clone() {
        return new Range(start, end);
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Range))
            return false;
        Range it = (Range) obj;
        return start == it.start && end == it.end;
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + ")";
    }
}
