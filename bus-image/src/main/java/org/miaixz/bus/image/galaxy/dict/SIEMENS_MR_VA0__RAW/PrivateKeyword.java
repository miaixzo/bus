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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.SequenceType:
                return "SequenceType";

            case PrivateTag.VectorSizeOriginal:
                return "VectorSizeOriginal";

            case PrivateTag.VectorSizeExtended:
                return "VectorSizeExtended";

            case PrivateTag.AcquiredSpectralRange:
                return "AcquiredSpectralRange";

            case PrivateTag.VOIPosition:
                return "VOIPosition";

            case PrivateTag.VOISize:
                return "VOISize";

            case PrivateTag.CSIMatrixSizeOriginal:
                return "CSIMatrixSizeOriginal";

            case PrivateTag.CSIMatrixSizeExtended:
                return "CSIMatrixSizeExtended";

            case PrivateTag.SpatialGridShift:
                return "SpatialGridShift";

            case PrivateTag.SignalLimitsMinimum:
                return "SignalLimitsMinimum";

            case PrivateTag.SignalLimitsMaximum:
                return "SignalLimitsMaximum";

            case PrivateTag.SpecInfoMask:
                return "SpecInfoMask";

            case PrivateTag.EPITimeRateOfChangeOfMagnitude:
                return "EPITimeRateOfChangeOfMagnitude";

            case PrivateTag.EPITimeRateOfChangeOfXComponent:
                return "EPITimeRateOfChangeOfXComponent";

            case PrivateTag.EPITimeRateOfChangeOfYComponent:
                return "EPITimeRateOfChangeOfYComponent";

            case PrivateTag.EPITimeRateOfChangeOfZComponent:
                return "EPITimeRateOfChangeOfZComponent";

            case PrivateTag.EPITimeRateOfChangeLegalLimit1:
                return "EPITimeRateOfChangeLegalLimit1";

            case PrivateTag.EPIOperationModeFlag:
                return "EPIOperationModeFlag";

            case PrivateTag.EPIFieldCalculationSafetyFactor:
                return "EPIFieldCalculationSafetyFactor";

            case PrivateTag.EPILegalLimit1OfChangeValue:
                return "EPILegalLimit1OfChangeValue";

            case PrivateTag.EPILegalLimit2OfChangeValue:
                return "EPILegalLimit2OfChangeValue";

            case PrivateTag.EPIRiseTime:
                return "EPIRiseTime";

            case PrivateTag.ArrayCoilADCOffset:
                return "ArrayCoilADCOffset";

            case PrivateTag.ArrayCoilPreamplifierGain:
                return "ArrayCoilPreamplifierGain";

            case PrivateTag.SaturationType:
                return "SaturationType";

            case PrivateTag.SaturationNormalVector:
                return "SaturationNormalVector";

            case PrivateTag.SaturationPositionVector:
                return "SaturationPositionVector";

            case PrivateTag.SaturationThickness:
                return "SaturationThickness";

            case PrivateTag.SaturationWidth:
                return "SaturationWidth";

            case PrivateTag.SaturationDistance:
                return "SaturationDistance";
        }
        return "";
    }

}
