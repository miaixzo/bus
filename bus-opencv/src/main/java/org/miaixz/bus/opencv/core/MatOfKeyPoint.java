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
 * A native matrix representation of a vector of feature keypoints, with Java array and list conversion helpers.
 */
public class MatOfKeyPoint extends Mat {

    /**
     * Required element depth of the encoded native vector.
     */
    private static final int _depth = CvType.CV_32F;
    /**
     * Number of channels used to encode one native vector element.
     */
    private static final int _channels = 7;

    /**
     * Creates an empty encoded vector.
     */
    public MatOfKeyPoint() {
        super();
    }

    /**
     * Wraps an existing native MatOfKeyPoint handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; must be nonzero and must not be owned by another cleanup wrapper
     * @throws IllegalArgumentException      if the nonempty matrix does not satisfy this vector's channel, depth or
     *                                       continuity requirements
     * @throws UnsupportedOperationException if the native binding handle is zero
     */
    protected MatOfKeyPoint(long addr) {
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
    public static MatOfKeyPoint fromNativeAddr(long addr) {
        return new MatOfKeyPoint(addr);
    }

    /**
     * Creates a typed vector header sharing the source matrix storage and validates its nonempty layout.
     *
     * @param m matrix whose storage is shared through a new typed vector header
     * @throws IllegalArgumentException if the nonempty matrix does not satisfy this vector's channel, depth or
     *                                  continuity requirements
     * @throws NullPointerException     if m is null
     */
    public MatOfKeyPoint(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
    }

    /**
     * Creates an encoded vector by copying the array through {@code fromArray}.
     *
     * @param a feature keypoint records to copy; null or an empty array produces an empty vector
     */
    public MatOfKeyPoint(KeyPoint... a) {
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
     * @param a feature keypoint records to copy; null or an empty array leaves the current contents unchanged
     */
    public void fromArray(KeyPoint... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length;
        alloc(num);
        float buff[] = new float[num * _channels];
        for (int i = 0; i < num; i++) {
            KeyPoint kp = a[i];
            buff[_channels * i + 0] = (float) kp.pt.x;
            buff[_channels * i + 1] = (float) kp.pt.y;
            buff[_channels * i + 2] = kp.size;
            buff[_channels * i + 3] = kp.angle;
            buff[_channels * i + 4] = kp.response;
            buff[_channels * i + 5] = kp.octave;
            buff[_channels * i + 6] = kp.class_id;
        }
        put(0, 0, buff);
    }

    /**
     * Copies the vector contents into a Java array.
     *
     * @return a new array of feature keypoint records
     */
    public KeyPoint[] toArray() {
        int num = (int) total();
        KeyPoint[] a = new KeyPoint[num];
        if (num == 0)
            return a;
        float buff[] = new float[num * _channels];
        get(0, 0, buff);
        for (int i = 0; i < num; i++)
            a[i] = new KeyPoint(buff[_channels * i + 0], buff[_channels * i + 1], buff[_channels * i + 2],
                    buff[_channels * i + 3], buff[_channels * i + 4], (int) buff[_channels * i + 5],
                    (int) buff[_channels * i + 6]);
        return a;
    }

    /**
     * Copies the list entries into this encoded vector through {@code fromArray}. An empty list follows that method's
     * empty-array behavior.
     *
     * @param lkp list of feature keypoints to encode
     * @throws NullPointerException if lkp is null
     */
    public void fromList(List<KeyPoint> lkp) {
        KeyPoint akp[] = lkp.toArray(new KeyPoint[0]);
        fromArray(akp);
    }

    /**
     * Copies the vector contents into a Java list.
     *
     * @return a fixed-size list backed by a new array of feature keypoint records
     */
    public List<KeyPoint> toList() {
        KeyPoint[] akp = toArray();
        return Arrays.asList(akp);
    }
}
