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
 * A native matrix representation of a vector of floating-point rectangles, with Java array and list conversion helpers.
 */
public class MatOfRect2d extends Mat {

    /**
     * Required element depth of the encoded native vector.
     */
    private static final int _depth = CvType.CV_64F;
    /**
     * Number of channels used to encode one native vector element.
     */
    private static final int _channels = 4;

    /**
     * Creates an empty encoded vector.
     */
    public MatOfRect2d() {
        super();
    }

    /**
     * Wraps an existing native MatOfRect2d handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; must be nonzero and must not be owned by another cleanup wrapper
     * @throws IllegalArgumentException      if the nonempty matrix does not satisfy this vector's channel, depth or
     *                                       continuity requirements
     * @throws UnsupportedOperationException if the native binding handle is zero
     */
    protected MatOfRect2d(long addr) {
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
    public static MatOfRect2d fromNativeAddr(long addr) {
        return new MatOfRect2d(addr);
    }

    /**
     * Creates a typed vector header sharing the source matrix storage and validates its nonempty layout.
     *
     * @param m matrix whose storage is shared through a new typed vector header
     * @throws IllegalArgumentException if the nonempty matrix does not satisfy this vector's channel, depth or
     *                                  continuity requirements
     * @throws NullPointerException     if m is null
     */
    public MatOfRect2d(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
    }

    /**
     * Creates an encoded vector by copying the array through {@code fromArray}.
     *
     * @param a double-precision rectangles in x, y, width, height order to copy; null or an empty array produces an
     *          empty vector
     */
    public MatOfRect2d(Rect2d... a) {
        super();
        fromArray(a);
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
     * @param a double-precision rectangles in x, y, width, height order to copy; null or an empty array leaves the
     *          current contents unchanged
     */
    public void fromArray(Rect2d... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length;
        alloc(num);
        double buff[] = new double[num * _channels];
        for (int i = 0; i < num; i++) {
            Rect2d r = a[i];
            buff[_channels * i + 0] = (double) r.x;
            buff[_channels * i + 1] = (double) r.y;
            buff[_channels * i + 2] = (double) r.width;
            buff[_channels * i + 3] = (double) r.height;
        }
        put(0, 0, buff);
    }

    /**
     * Copies the vector contents into a Java array.
     *
     * @return a new array of double-precision rectangles in x, y, width, height order
     */
    public Rect2d[] toArray() {
        int num = (int) total();
        Rect2d[] a = new Rect2d[num];
        if (num == 0)
            return a;
        double buff[] = new double[num * _channels];
        get(0, 0, buff);
        for (int i = 0; i < num; i++)
            a[i] = new Rect2d(buff[i * _channels], buff[i * _channels + 1], buff[i * _channels + 2],
                    buff[i * _channels + 3]);
        return a;
    }

    /**
     * Copies the list entries into this encoded vector through {@code fromArray}. An empty list follows that method's
     * empty-array behavior.
     *
     * @param lr list of rectangle records to encode
     * @throws NullPointerException if lr is null
     */
    public void fromList(List<Rect2d> lr) {
        Rect2d ap[] = lr.toArray(new Rect2d[0]);
        fromArray(ap);
    }

    /**
     * Copies the vector contents into a Java list.
     *
     * @return a fixed-size list backed by a new array of double-precision rectangles in x, y, width, height order
     */
    public List<Rect2d> toList() {
        Rect2d[] ar = toArray();
        return Arrays.asList(ar);
    }
}
