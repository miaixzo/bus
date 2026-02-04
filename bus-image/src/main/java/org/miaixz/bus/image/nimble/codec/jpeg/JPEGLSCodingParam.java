/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

    private static JPEGLSCodingParam getDefaultJPEGLSEncodingParam(int maxVal, int clampedMaxVal, int near) {
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
        return new byte[] { -1, (byte) JPEG.LSE, 0, 13, 1, (byte) (maxVal >> 8), (byte) (maxVal), (byte) (t1 >> 8),
                (byte) (t1), (byte) (t2 >> 8), (byte) (t2), (byte) (t3 >> 8), (byte) (t3), (byte) (reset >> 8),
                (byte) (reset) };
    }

    @Override
    public String toString() {
        return "JPEGLSCodingParam[MAXVAL=" + maxVal + ", T1=" + t1 + ", T2=" + t2 + ", T3=" + t3 + ", RESET=" + reset
                + "]";
    }

}
