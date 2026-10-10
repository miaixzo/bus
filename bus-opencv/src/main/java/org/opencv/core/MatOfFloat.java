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

import java.util.Arrays;
import java.util.List;

/**
 * Provides the {@code MatOfFloat} API.
 */
public class MatOfFloat extends Mat {

    // 32FC1
    private static final int _depth = CvType.CV_32F;
    private static final int _channels = 1;

    /**
     * Creates a new {@code MatOfFloat} instance.
     */
    public MatOfFloat() {
        super();
    }

    /**
     * Creates a new {@code MatOfFloat} instance.
     *
     * @param addr the {@code addr} value
     */
    protected MatOfFloat(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    /**
     * Creates a new {@code MatOfFloat} instance.
     *
     * @param m the {@code m} value
     */
    public MatOfFloat(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    /**
     * Creates a new {@code MatOfFloat} instance.
     *
     * @param a the {@code a} value
     */
    public MatOfFloat(float... a) {
        super();
        fromArray(a);
    }

    /**
     * Performs the {@code fromNativeAddr} operation.
     *
     * @param addr the {@code addr} value
     * @return the operation result
     */
    public static MatOfFloat fromNativeAddr(long addr) {
        return new MatOfFloat(addr);
    }

    /**
     * Performs the {@code alloc} operation.
     *
     * @param elemNumber the {@code elemNumber} value
     */
    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    /**
     * Performs the {@code fromArray} operation.
     *
     * @param a the {@code a} value
     */
    public void fromArray(float... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length / _channels;
        alloc(num);
        put(0, 0, a); // TODO: check ret val!
    }

    /**
     * Performs the {@code toArray} operation.
     *
     * @return the operation result
     */
    public float[] toArray() {
        int num = checkVector(_channels, _depth);
        if (num < 0)
            throw new RuntimeException("Native Mat has unexpected type or size: " + toString());
        float[] a = new float[num * _channels];
        if (num == 0)
            return a;
        get(0, 0, a); // TODO: check ret val!
        return a;
    }

    /**
     * Performs the {@code fromList} operation.
     *
     * @param lb the {@code lb} value
     */
    public void fromList(List<Float> lb) {
        if (lb == null || lb.size() == 0)
            return;
        Float ab[] = lb.toArray(new Float[0]);
        float a[] = new float[ab.length];
        for (int i = 0; i < ab.length; i++)
            a[i] = ab[i];
        fromArray(a);
    }

    /**
     * Performs the {@code toList} operation.
     *
     * @return the operation result
     */
    public List<Float> toList() {
        float[] a = toArray();
        Float ab[] = new Float[a.length];
        for (int i = 0; i < a.length; i++)
            ab[i] = a[i];
        return Arrays.asList(ab);
    }
}
