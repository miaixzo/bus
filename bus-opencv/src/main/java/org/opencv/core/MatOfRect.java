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

public class MatOfRect extends Mat {
    // 32SC4
    private static final int _depth = CvType.CV_32S;
    private static final int _channels = 4;

    public MatOfRect() {
        super();
    }

    protected MatOfRect(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfRect(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfRect(Rect... a) {
        super();
        fromArray(a);
    }

    public static MatOfRect fromNativeAddr(long addr) {
        return new MatOfRect(addr);
    }

    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(Rect... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length;
        alloc(num);
        int buff[] = new int[num * _channels];
        for (int i = 0; i < num; i++) {
            Rect r = a[i];
            buff[_channels * i + 0] = (int) r.x;
            buff[_channels * i + 1] = (int) r.y;
            buff[_channels * i + 2] = (int) r.width;
            buff[_channels * i + 3] = (int) r.height;
        }
        put(0, 0, buff); // TODO: check ret val!
    }

    public Rect[] toArray() {
        int num = (int) total();
        Rect[] a = new Rect[num];
        if (num == 0)
            return a;
        int buff[] = new int[num * _channels];
        get(0, 0, buff); // TODO: check ret val!
        for (int i = 0; i < num; i++)
            a[i] = new Rect(buff[i * _channels], buff[i * _channels + 1], buff[i * _channels + 2],
                    buff[i * _channels + 3]);
        return a;
    }

    public void fromList(List<Rect> lr) {
        Rect ap[] = lr.toArray(new Rect[0]);
        fromArray(ap);
    }

    public List<Rect> toList() {
        Rect[] ar = toArray();
        return Arrays.asList(ar);
    }
}
