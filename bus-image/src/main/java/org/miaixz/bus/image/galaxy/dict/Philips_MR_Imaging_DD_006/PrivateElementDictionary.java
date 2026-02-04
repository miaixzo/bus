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
