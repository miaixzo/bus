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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_VA0__RAW;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
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

            case PrivateTag.SequenceType:
                return VR.CS;

            case PrivateTag.AcquiredSpectralRange:
            case PrivateTag.VOIPosition:
            case PrivateTag.VOISize:
            case PrivateTag.SpatialGridShift:
            case PrivateTag.SignalLimitsMinimum:
            case PrivateTag.SignalLimitsMaximum:
            case PrivateTag.SpecInfoMask:
            case PrivateTag.EPITimeRateOfChangeOfMagnitude:
            case PrivateTag.EPITimeRateOfChangeOfXComponent:
            case PrivateTag.EPITimeRateOfChangeOfYComponent:
            case PrivateTag.EPITimeRateOfChangeOfZComponent:
            case PrivateTag.EPITimeRateOfChangeLegalLimit1:
            case PrivateTag.EPIFieldCalculationSafetyFactor:
            case PrivateTag.EPILegalLimit1OfChangeValue:
            case PrivateTag.EPILegalLimit2OfChangeValue:
            case PrivateTag.EPIRiseTime:
            case PrivateTag.ArrayCoilADCOffset:
            case PrivateTag.ArrayCoilPreamplifierGain:
            case PrivateTag.SaturationNormalVector:
            case PrivateTag.SaturationPositionVector:
            case PrivateTag.SaturationThickness:
            case PrivateTag.SaturationWidth:
            case PrivateTag.SaturationDistance:
                return VR.DS;

            case PrivateTag.VectorSizeOriginal:
            case PrivateTag.VectorSizeExtended:
            case PrivateTag.CSIMatrixSizeOriginal:
            case PrivateTag.CSIMatrixSizeExtended:
            case PrivateTag.EPIOperationModeFlag:
                return VR.IS;

            case PrivateTag.SaturationType:
                return VR.LO;
        }
        return VR.UN;
    }

}
