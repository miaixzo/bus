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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_006;

/**
 * @author Kimi Liu
 * @since Java 21+
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
