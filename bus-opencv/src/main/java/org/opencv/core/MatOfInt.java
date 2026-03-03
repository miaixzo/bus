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

public class MatOfInt extends Mat {

    // 32SC1
    private static final int _depth = CvType.CV_32S;
    private static final int _channels = 1;

    public MatOfInt() {
        super();
    }

    protected MatOfInt(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfInt(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfInt(int... a) {
        super();
        fromArray(a);
    }

    public static MatOfInt fromNativeAddr(long addr) {
        return new MatOfInt(addr);
    }

    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(int... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length / _channels;
        alloc(num);
        put(0, 0, a); // TODO: check ret val!
    }

    public int[] toArray() {
        int num = checkVector(_channels, _depth);
        if (num < 0)
            throw new RuntimeException("Native Mat has unexpected type or size: " + toString());
        int[] a = new int[num * _channels];
        if (num == 0)
            return a;
        get(0, 0, a); // TODO: check ret val!
        return a;
    }

    public void fromList(List<Integer> lb) {
        if (lb == null || lb.size() == 0)
            return;
        Integer ab[] = lb.toArray(new Integer[0]);
        int a[] = new int[ab.length];
        for (int i = 0; i < ab.length; i++)
            a[i] = ab[i];
        fromArray(a);
    }

    public List<Integer> toList() {
        int[] a = toArray();
        Integer ab[] = new Integer[a.length];
        for (int i = 0; i < a.length; i++)
            ab[i] = a[i];
        return Arrays.asList(ab);
    }

}
