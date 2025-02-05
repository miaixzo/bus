/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org opencv.org and other contributors.         ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.opencv.core;

import java.util.Arrays;
import java.util.List;

public class MatOfKeyPoint extends Mat {
    // 32FC7
    private static final int _depth = CvType.CV_32F;
    private static final int _channels = 7;

    public MatOfKeyPoint() {
        super();
    }

    protected MatOfKeyPoint(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfKeyPoint(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfKeyPoint(KeyPoint... a) {
        super();
        fromArray(a);
    }

    public static MatOfKeyPoint fromNativeAddr(long addr) {
        return new MatOfKeyPoint(addr);
    }

    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

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
        put(0, 0, buff); // TODO: check ret val!
    }

    public KeyPoint[] toArray() {
        int num = (int) total();
        KeyPoint[] a = new KeyPoint[num];
        if (num == 0)
            return a;
        float buff[] = new float[num * _channels];
        get(0, 0, buff); // TODO: check ret val!
        for (int i = 0; i < num; i++)
            a[i] = new KeyPoint(buff[_channels * i + 0], buff[_channels * i + 1], buff[_channels * i + 2],
                    buff[_channels * i + 3], buff[_channels * i + 4], (int) buff[_channels * i + 5],
                    (int) buff[_channels * i + 6]);
        return a;
    }

    public void fromList(List<KeyPoint> lkp) {
        KeyPoint akp[] = lkp.toArray(new KeyPoint[0]);
        fromArray(akp);
    }

    public List<KeyPoint> toList() {
        KeyPoint[] akp = toArray();
        return Arrays.asList(akp);
    }
}
