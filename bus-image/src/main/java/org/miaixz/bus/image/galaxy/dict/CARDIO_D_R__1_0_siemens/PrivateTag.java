/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.CARDIO_D_R__1_0_siemens;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "CARDIO-D.R. 1.0";

    /** (0029,xx00) VR=SQ VM=1 Edge Enhancement Sequence */
    public static final int EdgeEnhancementSequence = 0x00290000;

    /** (0029,xx01) VR=US VM=2 Convolution Kernel Size */
    public static final int ConvolutionKernelSize = 0x00290001;

    /** (0029,xx02) VR=US VM=1-n Convolution Kernel Coefficients */
    public static final int ConvolutionKernelCoefficients = 0x00290002;

    /** (0029,xx03) VR=FL VM=1 Edge Enhancement Gain */
    public static final int EdgeEnhancementGain = 0x00290003;

    /**
     * (0029,xxAC) VR=FL VM=1 Displayed Area Bottom Right Hand Corner Fractional
     */
    public static final int DisplayedAreaBottomRightHandCornerFractional = 0x002900AC;

    /**
     * (0029,xxAD) VR=FL VM=1 Displayed Area Top Left Hand Corner Fractional
     */
    public static final int DisplayedAreaTopLeftHandCornerFractional = 0x002900AD;

}
