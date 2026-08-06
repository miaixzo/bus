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
package org.miaixz.bus.image.nimble.codec.jpeg;

/**
 * Represents the JPEGLSCodingParam type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JPEGLSCodingParam {

    /**
     * The max val value.
     */
    private final int maxVal;

    /**
     * The t1 value.
     */
    private final int t1;

    /**
     * The t2 value.
     */
    private final int t2;

    /**
     * The t3 value.
     */
    private final int t3;

    /**
     * The reset value.
     */
    private final int reset;

    /**
     * The offset value.
     */
    private int offset;

    /**
     * Creates a new instance.
     *
     * @param maxVal the max val.
     * @param t1     the t1.
     * @param t2     the t2.
     * @param t3     the t3.
     * @param reset  the reset.
     */
    public JPEGLSCodingParam(int maxVal, int t1, int t2, int t3, int reset) {
        super();
        this.maxVal = maxVal;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.reset = reset;
    }

    /**
     * Gets the default jpegls encoding param.
     *
     * @param maxVal        the max val.
     * @param clampedMaxVal the clamped max val.
     * @param near          the near.
     * @return the default jpegls encoding param.
     */
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

    /**
     * Gets the default jpegls coding param.
     *
     * @param p    the p.
     * @param near the near.
     * @return the default jpegls coding param.
     */
    public static JPEGLSCodingParam getDefaultJPEGLSCodingParam(int p, int near) {
        int maxVal = (1 << p) - 1;
        return getDefaultJPEGLSEncodingParam(maxVal, Math.min(maxVal, 4095), near);
    }

    /**
     * Gets the jaijpegls coding param.
     *
     * @param p the p.
     * @return the jaijpegls coding param.
     */
    public static JPEGLSCodingParam getJAIJPEGLSCodingParam(int p) {
        int maxVal = (1 << p) - 1;
        return getDefaultJPEGLSEncodingParam(maxVal, maxVal, 0);
    }

    /**
     * Gets the offset.
     *
     * @return the offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the offset.
     *
     * @param offset the offset.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Gets the max val.
     *
     * @return the max val.
     */
    public final int getMaxVal() {
        return maxVal;
    }

    /**
     * Gets the t1.
     *
     * @return the t1.
     */
    public final int getT1() {
        return t1;
    }

    /**
     * Gets the t2.
     *
     * @return the t2.
     */
    public final int getT2() {
        return t2;
    }

    /**
     * Gets the t3.
     *
     * @return the t3.
     */
    public final int getT3() {
        return t3;
    }

    /**
     * Gets the reset.
     *
     * @return the reset.
     */
    public final int getReset() {
        return reset;
    }

    /**
     * Gets the bytes.
     *
     * @return the bytes.
     */
    public byte[] getBytes() {
        return new byte[] { -1, (byte) JPEG.LSE, 0, 13, 1, (byte) (maxVal >> 8), (byte) (maxVal), (byte) (t1 >> 8),
                (byte) (t1), (byte) (t2 >> 8), (byte) (t2), (byte) (t3 >> 8), (byte) (t3), (byte) (reset >> 8),
                (byte) (reset) };
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "JPEGLSCodingParam[MAXVAL=" + maxVal + ", T1=" + t1 + ", T2=" + t2 + ", T3=" + t3 + ", RESET=" + reset
                + "]";
    }

}
