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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_VA0__COAD;

/**
 * @author Kimi Liu
 * @since Java 17+
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
