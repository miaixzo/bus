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

public class MatOfPoint extends Mat {
    // 32SC2
    private static final int _depth = CvType.CV_32S;
    private static final int _channels = 2;

    public MatOfPoint() {
        super();
    }

    protected MatOfPoint(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfPoint(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfPoint(Point... a) {
        super();
        fromArray(a);
    }

    public static MatOfPoint fromNativeAddr(long addr) {
        return new MatOfPoint(addr);
    }

    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(Point... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length;
        alloc(num);
        int buff[] = new int[num * _channels];
        for (int i = 0; i < num; i++) {
            Point p = a[i];
            buff[_channels * i + 0] = (int) p.x;
            buff[_channels * i + 1] = (int) p.y;
        }
        put(0, 0, buff); // TODO: check ret val!
    }

    public Point[] toArray() {
        int num = (int) total();
        Point[] ap = new Point[num];
        if (num == 0)
            return ap;
        int buff[] = new int[num * _channels];
        get(0, 0, buff); // TODO: check ret val!
        for (int i = 0; i < num; i++)
            ap[i] = new Point(buff[i * _channels], buff[i * _channels + 1]);
        return ap;
    }

    public void fromList(List<Point> lp) {
        Point ap[] = lp.toArray(new Point[0]);
        fromArray(ap);
    }

    public List<Point> toList() {
        Point[] ap = toArray();
        return Arrays.asList(ap);
    }

}
