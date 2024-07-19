/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble.codec.jpeg;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JPEGLSCodingParam {

    private final int maxVal;
    private final int t1;
    private final int t2;
    private final int t3;
    private final int reset;
    private int offset;

    public JPEGLSCodingParam(int maxVal, int t1, int t2, int t3, int reset) {
        super();
        this.maxVal = maxVal;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.reset = reset;
    }

    private static JPEGLSCodingParam getDefaultJPEGLSEncodingParam(
            int maxVal, int clampedMaxVal, int near) {
        int factor = (clampedMaxVal + 128) >> 8;
        int t1 = factor + 2 + 3 * near;
        if (t1 > maxVal || t1 < near + 1)
            t1 = near + 1;
        int t2 = factor * 4 + 3 + 5 * near;
        if (t2 > maxVal || t2 < t1)
            t2 = t1;
        int t3 = factor * 17 + 4 + 7 * near;
        if (t3 > maxVal || t3 < t2)
            t3 = t2;
        return new JPEGLSCodingParam(maxVal, t1, t2, t3, 64);
    }

    public static JPEGLSCodingParam getDefaultJPEGLSCodingParam(int p, int near) {
        int maxVal = (1 << p) - 1;
        return getDefaultJPEGLSEncodingParam(maxVal, Math.min(maxVal, 4095), near);
    }

    public static JPEGLSCodingParam getJAIJPEGLSCodingParam(int p) {
        int maxVal = (1 << p) - 1;
        return getDefaultJPEGLSEncodingParam(maxVal, maxVal, 0);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public final int getMaxVal() {
        return maxVal;
    }

    public final int getT1() {
        return t1;
    }

    public final int getT2() {
        return t2;
    }

    public final int getT3() {
        return t3;
    }

    public final int getReset() {
        return reset;
    }

    public byte[] getBytes() {
        return new byte[]{
                -1, (byte) JPEG.LSE, 0, 13, 1,
                (byte) (maxVal >> 8), (byte) (maxVal),
                (byte) (t1 >> 8), (byte) (t1),
                (byte) (t2 >> 8), (byte) (t2),
                (byte) (t3 >> 8), (byte) (t3),
                (byte) (reset >> 8), (byte) (reset)};
    }

    @Override
    public String toString() {
        return "JPEGLSCodingParam[MAXVAL=" + maxVal
                + ", T1=" + t1
                + ", T2=" + t2
                + ", T3=" + t3
                + ", RESET=" + reset
                + "]";
    }

}
