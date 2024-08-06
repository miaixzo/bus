/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org opencv.org and other contributors.         ~
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

public class MatOfPoint3f extends Mat {
    // 32FC3
    private static final int _depth = CvType.CV_32F;
    private static final int _channels = 3;

    public MatOfPoint3f() {
        super();
    }

    protected MatOfPoint3f(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfPoint3f(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfPoint3f(Point3... a) {
        super();
        fromArray(a);
    }

    public static MatOfPoint3f fromNativeAddr(long addr) {
        return new MatOfPoint3f(addr);
    }

    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(Point3... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length;
        alloc(num);
        float buff[] = new float[num * _channels];
        for (int i = 0; i < num; i++) {
            Point3 p = a[i];
            buff[_channels * i + 0] = (float) p.x;
            buff[_channels * i + 1] = (float) p.y;
            buff[_channels * i + 2] = (float) p.z;
        }
        put(0, 0, buff); // TODO: check ret val!
    }

    public Point3[] toArray() {
        int num = (int) total();
        Point3[] ap = new Point3[num];
        if (num == 0)
            return ap;
        float buff[] = new float[num * _channels];
        get(0, 0, buff); // TODO: check ret val!
        for (int i = 0; i < num; i++)
            ap[i] = new Point3(buff[i * _channels], buff[i * _channels + 1], buff[i * _channels + 2]);
        return ap;
    }

    public void fromList(List<Point3> lp) {
        Point3 ap[] = lp.toArray(new Point3[0]);
        fromArray(ap);
    }

    public List<Point3> toList() {
        Point3[] ap = toArray();
        return Arrays.asList(ap);
    }
}
