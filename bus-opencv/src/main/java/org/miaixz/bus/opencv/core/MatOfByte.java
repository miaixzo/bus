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

import java.util.Arrays;
import java.util.List;

/**
 * A native matrix representation of a vector of byte values, with Java array and list conversion helpers.
 */
public class MatOfByte extends Mat {

    /**
     * Required element depth of the encoded native vector.
     */
    private static final int _depth = CvType.CV_8U;
    /**
     * Number of channels used to encode one native vector element.
     */
    private static final int _channels = 1;

    /**
     * Creates an empty encoded vector.
     */
    public MatOfByte() {
        super();
    }

    /**
     * Wraps an existing native MatOfByte handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; must be nonzero and must not be owned by another cleanup wrapper
     * @throws IllegalArgumentException      if the nonempty matrix does not satisfy this vector's channel, depth or
     *                                       continuity requirements
     * @throws UnsupportedOperationException if the native binding handle is zero
     */
    protected MatOfByte(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
    }

    /**
     * Wraps an existing native vector handle as this typed matrix representation.
     *
     * @param addr native binding handle to retain; must be nonzero and must not be owned by another cleanup wrapper
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     * @throws UnsupportedOperationException if the native binding handle is zero
     * @throws IllegalArgumentException      if the nonempty matrix does not satisfy this vector's channel, depth or
     *                                       continuity requirements
     */
    public static MatOfByte fromNativeAddr(long addr) {
        return new MatOfByte(addr);
    }

    /**
     * Creates a typed vector header sharing the source matrix storage and validates its nonempty layout.
     *
     * @param m matrix whose storage is shared through a new typed vector header
     * @throws IllegalArgumentException if the nonempty matrix does not satisfy this vector's channel, depth or
     *                                  continuity requirements
     * @throws NullPointerException     if m is null
     */
    public MatOfByte(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
    }

    /**
     * Creates an encoded vector by copying the array through {@code fromArray}.
     *
     * @param a byte values to copy; null or an empty array produces an empty vector
     */
    public MatOfByte(byte... a) {
        super();
        fromArray(a);
    }

    /**
     * Creates an encoded vector by copying the array through {@code fromArray}.
     *
     * @param offset zero-based offset of the first source-array entry to copy
     * @param length number of array entries to copy
     * @param a      byte values to copy; the selected array slice is copied
     */
    public MatOfByte(int offset, int length, byte... a) {
        super();
        fromArray(offset, length, a);
    }

    /**
     * Allocates one column of vector elements when elemNumber is positive. A nonpositive count leaves the existing
     * matrix unchanged.
     *
     * @param elemNumber number of vector elements to allocate; nonpositive values leave the matrix unchanged
     */
    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    /**
     * Copies the array into this encoded vector. A null or empty array leaves the existing contents unchanged.
     *
     * @param a byte values to copy; null or an empty array leaves the current contents unchanged
     */
    public void fromArray(byte... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length / _channels;
        alloc(num);
        put(0, 0, a);
    }

    /**
     * Replaces the vector contents with values from the supplied array.
     *
     * @param offset zero-based offset of the first source-array entry to copy
     * @param length number of array entries to copy
     * @param a      byte values to copy; the selected array slice is copied
     * @throws IllegalArgumentException if offset is negative; or if length is negative or offset plus length exceeds
     *                                  the source array length
     * @throws NullPointerException     if the source array is null
     */
    public void fromArray(int offset, int length, byte... a) {
        if (offset < 0)
            throw new IllegalArgumentException("offset < 0");
        if (a == null)
            throw new NullPointerException();
        if (length < 0 || length + offset > a.length)
            throw new IllegalArgumentException("invalid 'length' parameter: " + Integer.toString(length));
        if (a.length == 0)
            return;
        int num = length / _channels;
        alloc(num);
        put(0, 0, a, offset, length);
    }

    /**
     * Copies the vector contents into a Java array.
     *
     * @return a new array of byte values
     * @throws RuntimeException if the matrix cannot be represented as a vector with this class's required depth and
     *                          channel count
     */
    public byte[] toArray() {
        int num = checkVector(_channels, _depth);
        if (num < 0)
            throw new RuntimeException("Native Mat has unexpected type or size: " + toString());
        byte[] a = new byte[num * _channels];
        if (num == 0)
            return a;
        get(0, 0, a);
        return a;
    }

    /**
     * Copies the list entries into this encoded vector through {@code fromArray}. An empty list follows that method's
     * empty-array behavior.
     *
     * @param lb list of numeric values copied in iteration order
     */
    public void fromList(List<Byte> lb) {
        if (lb == null || lb.size() == 0)
            return;
        Byte ab[] = lb.toArray(new Byte[0]);
        byte a[] = new byte[ab.length];
        for (int i = 0; i < ab.length; i++)
            a[i] = ab[i];
        fromArray(a);
    }

    /**
     * Copies the vector contents into a Java list.
     *
     * @return a fixed-size list backed by a new array of byte values
     * @throws RuntimeException if the matrix cannot be represented as a vector with this class's required depth and
     *                          channel count
     */
    public List<Byte> toList() {
        byte[] a = toArray();
        Byte ab[] = new Byte[a.length];
        for (int i = 0; i < a.length; i++)
            ab[i] = a[i];
        return Arrays.asList(ab);
    }
}
