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
