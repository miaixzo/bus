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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_006;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

        case PrivateTag.MREMEGDirection:
        case PrivateTag.SagittalSliceOrder:
        case PrivateTag.CoronalSliceOrder:
        case PrivateTag.TransversalSliceOrder:
        case PrivateTag.SeriesOrientation:
        case PrivateTag.MetalImplantStatus:
        case PrivateTag.OrientationMirrorFlip:
        case PrivateTag.SAROperationMode:
            return VR.CS;
        case PrivateTag.MaxDBDT:
        case PrivateTag.MaxSAR:
        case PrivateTag.GradientSlewRate:
        case PrivateTag.B1RMS:
            return VR.DS;
        case PrivateTag.MREFrequency:
        case PrivateTag.MREAmplitude:
        case PrivateTag.MREMEGFrequency:
        case PrivateTag.MREMEGPairs:
        case PrivateTag.MREMEGAmplitude:
        case PrivateTag.MRENumberOfPhaseDelays:
        case PrivateTag.MREMotionMEGPhaseDelay:
        case PrivateTag.InversionDelayTime:
            return VR.FL;
        case PrivateTag.MRENumberOfMotionCycles:
        case PrivateTag.MRStackReverse:
        case PrivateTag.MREPhaseDelayNumber:
        case PrivateTag.NumberOfInversionDelays:
        case PrivateTag.InversionDelayNumber:
        case PrivateTag.SpatialGradient:
            return VR.IS;
        case PrivateTag.MREInversionAlgorithmVersion:
        case PrivateTag.SARType:
        case PrivateTag.AdditionalConstraints:
        case PrivateTag._2005_xx86_:
            return VR.LT;
        case PrivateTag.ContrastInformationSequence:
            return VR.SQ;
        }
        return VR.UN;
    }
}
