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

/**
 * Utilities and constants for encoding matrix element depth and channel count.
 */
public final class CvType {

    /**
     * Creates a Java API container. The utility operations are available through static methods.
     */
    public CvType() {
        // No initialization required.
    }

    /**
     * Depth code for unsigned 8-bit integer matrix channels.
     */
    public static final int CV_8U = 0;

    /**
     * Depth code for signed 8-bit integer matrix channels.
     */
    public static final int CV_8S = 1;

    /**
     * Depth code for unsigned 16-bit integer matrix channels.
     */
    public static final int CV_16U = 2;

    /**
     * Depth code for signed 16-bit integer matrix channels.
     */
    public static final int CV_16S = 3;

    /**
     * Depth code for signed 32-bit integer matrix channels.
     */
    public static final int CV_32S = 4;

    /**
     * Depth code for 32-bit floating-point matrix channels.
     */
    public static final int CV_32F = 5;

    /**
     * Depth code for 64-bit floating-point matrix channels.
     */
    public static final int CV_64F = 6;

    /**
     * Depth code for 16-bit floating-point matrix channels.
     */
    public static final int CV_16F = 7;

    /**
     * Depth code for 16-bit bfloat16 floating-point matrix channels.
     */
    public static final int CV_16BF = 8;

    /**
     * Depth code for Boolean matrix channels.
     */
    public static final int CV_Bool = 9;

    /**
     * Depth code for unsigned 64-bit integer matrix channels.
     */
    public static final int CV_64U = 10;

    /**
     * Depth code for signed 64-bit integer matrix channels.
     */
    public static final int CV_64S = 11;

    /**
     * Depth code for unsigned 32-bit integer matrix channels.
     */
    public static final int CV_32U = 12;

    /**
     * Matrix type code for 1 channel of unsigned 8-bit integer values.
     */
    public static final int CV_8UC1 = CV_8UC(1);

    /**
     * Matrix type code for 2 channels of unsigned 8-bit integer values.
     */
    public static final int CV_8UC2 = CV_8UC(2);

    /**
     * Matrix type code for 3 channels of unsigned 8-bit integer values.
     */
    public static final int CV_8UC3 = CV_8UC(3);

    /**
     * Matrix type code for 4 channels of unsigned 8-bit integer values.
     */
    public static final int CV_8UC4 = CV_8UC(4);

    /**
     * Matrix type code for 1 channel of signed 8-bit integer values.
     */
    public static final int CV_8SC1 = CV_8SC(1);

    /**
     * Matrix type code for 2 channels of signed 8-bit integer values.
     */
    public static final int CV_8SC2 = CV_8SC(2);

    /**
     * Matrix type code for 3 channels of signed 8-bit integer values.
     */
    public static final int CV_8SC3 = CV_8SC(3);

    /**
     * Matrix type code for 4 channels of signed 8-bit integer values.
     */
    public static final int CV_8SC4 = CV_8SC(4);

    /**
     * Matrix type code for 1 channel of unsigned 16-bit integer values.
     */
    public static final int CV_16UC1 = CV_16UC(1);

    /**
     * Matrix type code for 2 channels of unsigned 16-bit integer values.
     */
    public static final int CV_16UC2 = CV_16UC(2);

    /**
     * Matrix type code for 3 channels of unsigned 16-bit integer values.
     */
    public static final int CV_16UC3 = CV_16UC(3);

    /**
     * Matrix type code for 4 channels of unsigned 16-bit integer values.
     */
    public static final int CV_16UC4 = CV_16UC(4);

    /**
     * Matrix type code for 1 channel of signed 16-bit integer values.
     */
    public static final int CV_16SC1 = CV_16SC(1);

    /**
     * Matrix type code for 2 channels of signed 16-bit integer values.
     */
    public static final int CV_16SC2 = CV_16SC(2);

    /**
     * Matrix type code for 3 channels of signed 16-bit integer values.
     */
    public static final int CV_16SC3 = CV_16SC(3);

    /**
     * Matrix type code for 4 channels of signed 16-bit integer values.
     */
    public static final int CV_16SC4 = CV_16SC(4);

    /**
     * Matrix type code for 1 channel of signed 32-bit integer values.
     */
    public static final int CV_32SC1 = CV_32SC(1);

    /**
     * Matrix type code for 2 channels of signed 32-bit integer values.
     */
    public static final int CV_32SC2 = CV_32SC(2);

    /**
     * Matrix type code for 3 channels of signed 32-bit integer values.
     */
    public static final int CV_32SC3 = CV_32SC(3);

    /**
     * Matrix type code for 4 channels of signed 32-bit integer values.
     */
    public static final int CV_32SC4 = CV_32SC(4);

    /**
     * Matrix type code for 1 channel of unsigned 32-bit integer values.
     */
    public static final int CV_32UC1 = CV_32UC(1);

    /**
     * Matrix type code for 2 channels of unsigned 32-bit integer values.
     */
    public static final int CV_32UC2 = CV_32UC(2);

    /**
     * Matrix type code for 3 channels of unsigned 32-bit integer values.
     */
    public static final int CV_32UC3 = CV_32UC(3);

    /**
     * Matrix type code for 4 channels of unsigned 32-bit integer values.
     */
    public static final int CV_32UC4 = CV_32UC(4);

    /**
     * Matrix type code for 1 channel of signed 64-bit integer values.
     */
    public static final int CV_64SC1 = CV_64SC(1);

    /**
     * Matrix type code for 2 channels of signed 64-bit integer values.
     */
    public static final int CV_64SC2 = CV_64SC(2);

    /**
     * Matrix type code for 3 channels of signed 64-bit integer values.
     */
    public static final int CV_64SC3 = CV_64SC(3);

    /**
     * Matrix type code for 4 channels of signed 64-bit integer values.
     */
    public static final int CV_64SC4 = CV_64SC(4);

    /**
     * Matrix type code for 1 channel of unsigned 64-bit integer values.
     */
    public static final int CV_64UC1 = CV_64UC(1);

    /**
     * Matrix type code for 2 channels of unsigned 64-bit integer values.
     */
    public static final int CV_64UC2 = CV_64UC(2);

    /**
     * Matrix type code for 3 channels of unsigned 64-bit integer values.
     */
    public static final int CV_64UC3 = CV_64UC(3);

    /**
     * Matrix type code for 4 channels of unsigned 64-bit integer values.
     */
    public static final int CV_64UC4 = CV_64UC(4);

    /**
     * Matrix type code for 1 channel of 32-bit floating-point values.
     */
    public static final int CV_32FC1 = CV_32FC(1);

    /**
     * Matrix type code for 2 channels of 32-bit floating-point values.
     */
    public static final int CV_32FC2 = CV_32FC(2);

    /**
     * Matrix type code for 3 channels of 32-bit floating-point values.
     */
    public static final int CV_32FC3 = CV_32FC(3);

    /**
     * Matrix type code for 4 channels of 32-bit floating-point values.
     */
    public static final int CV_32FC4 = CV_32FC(4);

    /**
     * Matrix type code for 1 channel of 64-bit floating-point values.
     */
    public static final int CV_64FC1 = CV_64FC(1);

    /**
     * Matrix type code for 2 channels of 64-bit floating-point values.
     */
    public static final int CV_64FC2 = CV_64FC(2);

    /**
     * Matrix type code for 3 channels of 64-bit floating-point values.
     */
    public static final int CV_64FC3 = CV_64FC(3);

    /**
     * Matrix type code for 4 channels of 64-bit floating-point values.
     */
    public static final int CV_64FC4 = CV_64FC(4);

    /**
     * Matrix type code for 1 channel of 16-bit floating-point values.
     */
    public static final int CV_16FC1 = CV_16FC(1);

    /**
     * Matrix type code for 2 channels of 16-bit floating-point values.
     */
    public static final int CV_16FC2 = CV_16FC(2);

    /**
     * Matrix type code for 3 channels of 16-bit floating-point values.
     */
    public static final int CV_16FC3 = CV_16FC(3);

    /**
     * Matrix type code for 4 channels of 16-bit floating-point values.
     */
    public static final int CV_16FC4 = CV_16FC(4);

    /**
     * Matrix type code for 1 channel of 16-bit bfloat16 floating-point values.
     */
    public static final int CV_16BFC1 = CV_16BFC(1);

    /**
     * Matrix type code for 2 channels of 16-bit bfloat16 floating-point values.
     */
    public static final int CV_16BFC2 = CV_16BFC(2);

    /**
     * Matrix type code for 3 channels of 16-bit bfloat16 floating-point values.
     */
    public static final int CV_16BFC3 = CV_16BFC(3);

    /**
     * Matrix type code for 4 channels of 16-bit bfloat16 floating-point values.
     */
    public static final int CV_16BFC4 = CV_16BFC(4);

    /**
     * Matrix type code for 1 channel of Boolean values.
     */
    public static final int CV_BoolC1 = CV_BoolC(1);

    /**
     * Matrix type code for 2 channels of Boolean values.
     */
    public static final int CV_BoolC2 = CV_BoolC(2);

    /**
     * Matrix type code for 3 channels of Boolean values.
     */
    public static final int CV_BoolC3 = CV_BoolC(3);

    /**
     * Matrix type code for 4 channels of Boolean values.
     */
    public static final int CV_BoolC4 = CV_BoolC(4);

    /**
     * Maximum channel count represented by the matrix type encoding.
     */
    private static final int CV_CN_MAX = 128;

    /**
     * Number of bits reserved for the depth part of a packed matrix type code.
     */
    private static final int CV_CN_SHIFT = 5;

    /**
     * Number of depth codes reserved by the matrix type encoding.
     */
    private static final int CV_DEPTH_MAX = (1 << CV_CN_SHIFT);

    /**
     * Combines an element-depth code and channel count into a matrix type code.
     *
     * @param depth    single-channel element depth code
     * @param channels number of channels per matrix element
     * @return the packed depth-and-channel type code
     * @throws UnsupportedOperationException if channels is outside 1 through CV_CN_MAX - 1; or if depth is negative or
     *                                       is at least CV_DEPTH_MAX
     */
    public static final int makeType(int depth, int channels) {
        if (channels <= 0 || channels >= CV_CN_MAX) {
            throw new UnsupportedOperationException("Channels count should be 1.." + (CV_CN_MAX - 1));
        }
        if (depth < 0 || depth >= CV_DEPTH_MAX) {
            throw new UnsupportedOperationException("Data type depth should be 0.." + (CV_DEPTH_MAX - 1));
        }
        return (depth & (CV_DEPTH_MAX - 1)) + ((channels - 1) << CV_CN_SHIFT);
    }

    /**
     * Creates a matrix type code for unsigned 8-bit integer values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 8U} depth with the requested channel count
     */
    public static final int CV_8UC(int ch) {
        return makeType(CV_8U, ch);
    }

    /**
     * Creates a matrix type code for signed 8-bit integer values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 8S} depth with the requested channel count
     */
    public static final int CV_8SC(int ch) {
        return makeType(CV_8S, ch);
    }

    /**
     * Creates a matrix type code for unsigned 16-bit integer values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 16U} depth with the requested channel count
     */
    public static final int CV_16UC(int ch) {
        return makeType(CV_16U, ch);
    }

    /**
     * Creates a matrix type code for signed 16-bit integer values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 16S} depth with the requested channel count
     */
    public static final int CV_16SC(int ch) {
        return makeType(CV_16S, ch);
    }

    /**
     * Creates a matrix type code for signed 32-bit integer values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 32S} depth with the requested channel count
     */
    public static final int CV_32SC(int ch) {
        return makeType(CV_32S, ch);
    }

    /**
     * Creates a matrix type code for unsigned 32-bit integer values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 32U} depth with the requested channel count
     */
    public static final int CV_32UC(int ch) {
        return makeType(CV_32U, ch);
    }

    /**
     * Creates a matrix type code for signed 64-bit integer values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 64S} depth with the requested channel count
     */
    public static final int CV_64SC(int ch) {
        return makeType(CV_64S, ch);
    }

    /**
     * Creates a matrix type code for unsigned 64-bit integer values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 64U} depth with the requested channel count
     */
    public static final int CV_64UC(int ch) {
        return makeType(CV_64U, ch);
    }

    /**
     * Creates a matrix type code for 32-bit floating-point values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 32F} depth with the requested channel count
     */
    public static final int CV_32FC(int ch) {
        return makeType(CV_32F, ch);
    }

    /**
     * Creates a matrix type code for 64-bit floating-point values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 64F} depth with the requested channel count
     */
    public static final int CV_64FC(int ch) {
        return makeType(CV_64F, ch);
    }

    /**
     * Creates a matrix type code for 16-bit floating-point values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 16F} depth with the requested channel count
     */
    public static final int CV_16FC(int ch) {
        return makeType(CV_16F, ch);
    }

    /**
     * Creates a matrix type code for 16-bit bfloat16 floating-point values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code 16BF} depth with the requested channel count
     */
    public static final int CV_16BFC(int ch) {
        return makeType(CV_16BF, ch);
    }

    /**
     * Creates a matrix type code for Boolean values and the requested channel count.
     *
     * @param ch channel or vector component index
     * @return the packed type code combining {@code Bool} depth with the requested channel count
     */
    public static final int CV_BoolC(int ch) {
        return makeType(CV_Bool, ch);
    }

    /**
     * Extracts the channel count from a packed matrix type code.
     *
     * @param type packed matrix channel-depth and channel-count code
     * @return the number of channels encoded in type
     */
    public static final int channels(int type) {
        return (type >> CV_CN_SHIFT) + 1;
    }

    /**
     * Extracts the element depth from a packed matrix type code.
     *
     * @param type packed matrix channel-depth and channel-count code
     * @return the channel-depth code encoded in type
     */
    public static final int depth(int type) {
        return type & (CV_DEPTH_MAX - 1);
    }

    /**
     * Reports whether a matrix type has an integer element depth.
     *
     * @param type packed matrix channel-depth and channel-count code
     * @return true for the integer depth codes recognized by this implementation
     */
    public static final boolean isInteger(int type) {
        return depth(type) < CV_32F;
    }

    /**
     * Returns the size in bytes of a matrix element with the supplied type code.
     *
     * @param type packed matrix channel-depth and channel-count code
     * @return the byte size of one complete matrix element
     * @throws UnsupportedOperationException if the encoded depth has no representation supported by this conversion
     */
    public static final int ELEM_SIZE(int type) {
        switch (depth(type)) {
            case CV_Bool:
            case CV_8U:
            case CV_8S:
                return channels(type);

            case CV_16U:
            case CV_16S:
            case CV_16F:
            case CV_16BF:
                return 2 * channels(type);

            case CV_32S:
            case CV_32U:
            case CV_32F:
                return 4 * channels(type);

            case CV_64U:
            case CV_64S:
            case CV_64F:
                return 8 * channels(type);

            default:
                throw new UnsupportedOperationException("Unsupported CvType value: " + type);
        }
    }

    /**
     * Returns the symbolic depth and channel representation of a matrix type code.
     *
     * @param type packed matrix channel-depth and channel-count code
     * @return the symbolic depth name followed by its channel count
     * @throws UnsupportedOperationException if the encoded depth has no representation supported by this conversion
     */
    public static final String typeToString(int type) {
        String s;
        switch (depth(type)) {
            case CV_8U:
                s = "CV_8U";
                break;

            case CV_8S:
                s = "CV_8S";
                break;

            case CV_16U:
                s = "CV_16U";
                break;

            case CV_16S:
                s = "CV_16S";
                break;

            case CV_32S:
                s = "CV_32S";
                break;

            case CV_32U:
                s = "CV_32U";
                break;

            case CV_32F:
                s = "CV_32F";
                break;

            case CV_64U:
                s = "CV_64U";
                break;

            case CV_64S:
                s = "CV_64S";
                break;

            case CV_64F:
                s = "CV_64F";
                break;

            case CV_16F:
                s = "CV_16F";
                break;

            case CV_16BF:
                s = "CV_16BF";
                break;

            case CV_Bool:
                s = "CV_Bool";
                break;

            default:
                throw new UnsupportedOperationException("Unsupported CvType value: " + type);
        }

        int ch = channels(type);
        if (ch <= 4)
            return s + "C" + ch;
        else
            return s + "C(" + ch + ")";
    }

}
