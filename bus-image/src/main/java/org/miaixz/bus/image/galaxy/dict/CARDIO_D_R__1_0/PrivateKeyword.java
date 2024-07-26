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
package org.miaixz.bus.image.galaxy.dict.CARDIO_D_R__1_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.FileLocation:
            return "FileLocation";
        case PrivateTag.FileSize:
            return "FileSize";
        case PrivateTag.AlternateImageSequence:
            return "AlternateImageSequence";
        case PrivateTag.ImageBlankingShape:
            return "ImageBlankingShape";
        case PrivateTag.ImageBlankingLeftVerticalEdge:
            return "ImageBlankingLeftVerticalEdge";
        case PrivateTag.ImageBlankingRightVerticalEdge:
            return "ImageBlankingRightVerticalEdge";
        case PrivateTag.ImageBlankingUpperHorizontalEdge:
            return "ImageBlankingUpperHorizontalEdge";
        case PrivateTag.ImageBlankingLowerHorizontalEdge:
            return "ImageBlankingLowerHorizontalEdge";
        case PrivateTag.CenterOfCircularImageBlanking:
            return "CenterOfCircularImageBlanking";
        case PrivateTag.RadiusOfCircularImageBlanking:
            return "RadiusOfCircularImageBlanking";
        case PrivateTag.MaximumImageFrameSize:
            return "MaximumImageFrameSize";
        case PrivateTag.ImageSequenceNumber:
            return "ImageSequenceNumber";
        case PrivateTag.EdgeEnhancementSequence:
            return "EdgeEnhancementSequence";
        case PrivateTag.ConvolutionKernelSize:
            return "ConvolutionKernelSize";
        case PrivateTag.ConvolutionKernelCoefficients:
            return "ConvolutionKernelCoefficients";
        case PrivateTag.EdgeEnhancementGain:
            return "EdgeEnhancementGain";
        }
        return "";
    }

}
