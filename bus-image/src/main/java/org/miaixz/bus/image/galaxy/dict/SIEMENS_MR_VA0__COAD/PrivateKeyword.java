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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_VA0__COAD;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.MagneticFieldStrength:
                return "MagneticFieldStrength";

            case PrivateTag.ADCVoltage:
                return "ADCVoltage";

            case PrivateTag.ADCOffset:
                return "ADCOffset";

            case PrivateTag.TransmitterAmplitude:
                return "TransmitterAmplitude";

            case PrivateTag.NumberOfTransmitterAmplitudes:
                return "NumberOfTransmitterAmplitudes";

            case PrivateTag.TransmitterAttenuator:
                return "TransmitterAttenuator";

            case PrivateTag.TransmitterCalibration:
                return "TransmitterCalibration";

            case PrivateTag.TransmitterReference:
                return "TransmitterReference";

            case PrivateTag.ReceiverTotalGain:
                return "ReceiverTotalGain";

            case PrivateTag.ReceiverAmplifierGain:
                return "ReceiverAmplifierGain";

            case PrivateTag.ReceiverPreamplifierGain:
                return "ReceiverPreamplifierGain";

            case PrivateTag.ReceiverCableAttenuation:
                return "ReceiverCableAttenuation";

            case PrivateTag.ReceiverReferenceGain:
                return "ReceiverReferenceGain";

            case PrivateTag.ReceiverFilterFrequency:
                return "ReceiverFilterFrequency";

            case PrivateTag.ReconstructionScaleFactor:
                return "ReconstructionScaleFactor";

            case PrivateTag.ReferenceScaleFactor:
                return "ReferenceScaleFactor";

            case PrivateTag.PhaseGradientAmplitude:
                return "PhaseGradientAmplitude";

            case PrivateTag.ReadoutGradientAmplitude:
                return "ReadoutGradientAmplitude";

            case PrivateTag.SelectionGradientAmplitude:
                return "SelectionGradientAmplitude";

            case PrivateTag.GradientDelayTime:
                return "GradientDelayTime";

            case PrivateTag.TotalGradientDelayTime:
                return "TotalGradientDelayTime";

            case PrivateTag.SensitivityCorrectionLabel:
                return "SensitivityCorrectionLabel";

            case PrivateTag.SaturationPhaseEncodingVectorCoronalComponent:
                return "SaturationPhaseEncodingVectorCoronalComponent";

            case PrivateTag.SaturationReadoutVectorCoronalComponent:
                return "SaturationReadoutVectorCoronalComponent";

            case PrivateTag.RFWatchdogMask:
                return "RFWatchdogMask";

            case PrivateTag.EPIReconstructionSlope:
                return "EPIReconstructionSlope";

            case PrivateTag.RFPowerErrorIndicator:
                return "RFPowerErrorIndicator";

            case PrivateTag.SpecificAbsorptionRateWholeBody:
                return "SpecificAbsorptionRateWholeBody";

            case PrivateTag.SpecificEnergyDose:
                return "SpecificEnergyDose";

            case PrivateTag.AdjustmentStatusMask:
                return "AdjustmentStatusMask";

            case PrivateTag.EPICapacity:
                return "EPICapacity";

            case PrivateTag.EPIInductance:
                return "EPIInductance";

            case PrivateTag.EPISwitchConfigurationCode:
                return "EPISwitchConfigurationCode";

            case PrivateTag.EPISwitchHardwareCode:
                return "EPISwitchHardwareCode";

            case PrivateTag.EPISwitchDelayTime:
                return "EPISwitchDelayTime";

            case PrivateTag.FlowSensitivity:
                return "FlowSensitivity";

            case PrivateTag.CalculationSubmode:
                return "CalculationSubmode";

            case PrivateTag.FieldOfViewRatio:
                return "FieldOfViewRatio";

            case PrivateTag.BaseRawMatrixSize:
                return "BaseRawMatrixSize";

            case PrivateTag.TwoDOversamplingLines:
                return "TwoDOversamplingLines";

            case PrivateTag.ThreeDPhaseOversamplingPartitions:
                return "ThreeDPhaseOversamplingPartitions";

            case PrivateTag.EchoLinePosition:
                return "EchoLinePosition";

            case PrivateTag.EchoColumnPosition:
                return "EchoColumnPosition";

            case PrivateTag.LinesPerSegment:
                return "LinesPerSegment";

            case PrivateTag.PhaseCodingDirection:
                return "PhaseCodingDirection";
        }
        return "";
    }

}
