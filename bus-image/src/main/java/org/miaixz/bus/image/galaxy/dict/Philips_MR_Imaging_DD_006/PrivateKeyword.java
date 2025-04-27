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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.MREFrequency:
            return "MREFrequency";
        case PrivateTag.MREAmplitude:
            return "MREAmplitude";
        case PrivateTag.MREMEGFrequency:
            return "MREMEGFrequency";
        case PrivateTag.MREMEGPairs:
            return "MREMEGPairs";
        case PrivateTag.MREMEGDirection:
            return "MREMEGDirection";
        case PrivateTag.MREMEGAmplitude:
            return "MREMEGAmplitude";
        case PrivateTag.MRENumberOfPhaseDelays:
            return "MRENumberOfPhaseDelays";
        case PrivateTag.MRENumberOfMotionCycles:
            return "MRENumberOfMotionCycles";
        case PrivateTag.MREMotionMEGPhaseDelay:
            return "MREMotionMEGPhaseDelay";
        case PrivateTag.MREInversionAlgorithmVersion:
            return "MREInversionAlgorithmVersion";
        case PrivateTag.SagittalSliceOrder:
            return "SagittalSliceOrder";
        case PrivateTag.CoronalSliceOrder:
            return "CoronalSliceOrder";
        case PrivateTag.TransversalSliceOrder:
            return "TransversalSliceOrder";
        case PrivateTag.SeriesOrientation:
            return "SeriesOrientation";
        case PrivateTag.MRStackReverse:
            return "MRStackReverse";
        case PrivateTag.MREPhaseDelayNumber:
            return "MREPhaseDelayNumber";
        case PrivateTag.NumberOfInversionDelays:
            return "NumberOfInversionDelays";
        case PrivateTag.InversionDelayTime:
            return "InversionDelayTime";
        case PrivateTag.InversionDelayNumber:
            return "InversionDelayNumber";
        case PrivateTag.MaxDBDT:
            return "MaxDBDT";
        case PrivateTag.MaxSAR:
            return "MaxSAR";
        case PrivateTag.SARType:
            return "SARType";
        case PrivateTag.MetalImplantStatus:
            return "MetalImplantStatus";
        case PrivateTag.OrientationMirrorFlip:
            return "OrientationMirrorFlip";
        case PrivateTag.SAROperationMode:
            return "SAROperationMode";
        case PrivateTag.SpatialGradient:
            return "SpatialGradient";
        case PrivateTag.AdditionalConstraints:
            return "AdditionalConstraints";
        case PrivateTag.GradientSlewRate:
            return "GradientSlewRate";
        case PrivateTag._2005_xx86_:
            return "_2005_xx86_";
        case PrivateTag.B1RMS:
            return "B1RMS";
        case PrivateTag.ContrastInformationSequence:
            return "ContrastInformationSequence";
        }
        return "";
    }

}
