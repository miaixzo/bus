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
package org.miaixz.bus.image.galaxy.dict.CARDIO_D_R__1_0;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "CARDIO-D.R. 1.0";

    /** (0009,xx00) VR=UL VM=1 File Location */
    public static final int FileLocation = 0x00090000;

    /** (0009,xx01) VR=UL VM=1 File Size */
    public static final int FileSize = 0x00090001;

    /** (0009,xx40) VR=SQ VM=1 Alternate Image Sequence */
    public static final int AlternateImageSequence = 0x00090040;

    /** (0019,xx00) VR=CS VM=1 Image Blanking Shape */
    public static final int ImageBlankingShape = 0x00190000;

    /** (0019,xx02) VR=IS VM=1 Image Blanking Left Vertical Edge */
    public static final int ImageBlankingLeftVerticalEdge = 0x00190002;

    /** (0019,xx04) VR=IS VM=1 Image Blanking Right Vertical Edge */
    public static final int ImageBlankingRightVerticalEdge = 0x00190004;

    /**
     * (0019,xx06) VR=IS VM=1 Image Blanking Upper Horizontal Edge
     */
    public static final int ImageBlankingUpperHorizontalEdge = 0x00190006;

    /**
     * (0019,xx08) VR=IS VM=1 Image Blanking Lower Horizontal Edge
     */
    public static final int ImageBlankingLowerHorizontalEdge = 0x00190008;

    /** (0019,xx10) VR=IS VM=1 Center of Circular Image Blanking */
    public static final int CenterOfCircularImageBlanking = 0x00190010;

    /** (0019,xx12) VR=IS VM=1 Radius of Circular Image Blanking */
    public static final int RadiusOfCircularImageBlanking = 0x00190012;

    /** (0019,xx30) VR=UL VM=1 Maximum Image Frame Size */
    public static final int MaximumImageFrameSize = 0x00190030;

    /** (0021,xx13) VR=IS VM=1 Image Sequence Number */
    public static final int ImageSequenceNumber = 0x00210013;

    /** (0029,xx00) VR=SQ VM=1 Edge Enhancement Sequence */
    public static final int EdgeEnhancementSequence = 0x00290000;

    /** (0029,xx01) VR=US VM=2 Convolution Kernel Size */
    public static final int ConvolutionKernelSize = 0x00290001;

    /** (0029,xx02) VR=US VM=1-n Convolution Kernel Coefficients */
    public static final int ConvolutionKernelCoefficients = 0x00290002;

    /** (0029,xx03) VR=FL VM=1 Edge Enhancement Gain */
    public static final int EdgeEnhancementGain = 0x00290003;

}
