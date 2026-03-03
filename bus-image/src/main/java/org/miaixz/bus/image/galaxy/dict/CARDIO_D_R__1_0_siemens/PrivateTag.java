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
