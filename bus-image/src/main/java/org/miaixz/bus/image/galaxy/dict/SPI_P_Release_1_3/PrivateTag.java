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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Release_1_3;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SPI-P Release 1;3";

    /** (0029,xx00) VR=LT VM=1 Image Enhancement ID */
    public static final int ImageEnhancementID = 0x00290000;

    /** (0029,xx01) VR=LT VM=1 Image Enhancement */
    public static final int ImageEnhancement = 0x00290001;

    /** (0029,xx02) VR=LT VM=1 Convolution ID */
    public static final int ConvolutionID = 0x00290002;

    /** (0029,xx03) VR=LT VM=1 Convolution Type */
    public static final int ConvolutionType = 0x00290003;

    /** (0029,xx04) VR=LT VM=1 Convolution Kernel Size ID */
    public static final int ConvolutionKernelSizeID = 0x00290004;

    /** (0029,xx05) VR=US VM=2 Convolution Kernel Size */
    public static final int ConvolutionKernelSize = 0x00290005;

    /** (0029,xx06) VR=US VM=1-n Convolution Kernel */
    public static final int ConvolutionKernel = 0x00290006;

    /** (0029,xx0C) VR=DS VM=1 Enhancement Gain */
    public static final int EnhancementGain = 0x0029000C;

    /** (0029,xx1E) VR=CS VM=1 Image Enhancement Enable Status */
    public static final int ImageEnhancementEnableStatus = 0x0029001E;

    /** (0029,xx1F) VR=CS VM=1 Image Enhancement Select Status */
    public static final int ImageEnhancementSelectStatus = 0x0029001F;

}
