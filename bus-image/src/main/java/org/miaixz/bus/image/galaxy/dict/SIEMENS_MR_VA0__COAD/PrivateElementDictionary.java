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

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Represents the PrivateElementDictionary type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateElementDictionary extends ElementDictionary {

    /**
     * The private creator value.
     */
    public static final String PrivateCreator = "";

    /**
     * Creates a new instance.
     */
    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    /**
     * Executes the keyword of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    /**
     * Executes the vr of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag.CalculationSubmode:
            case PrivateTag.PhaseCodingDirection:
                return VR.CS;

            case PrivateTag.MagneticFieldStrength:
            case PrivateTag.ADCVoltage:
            case PrivateTag.ADCOffset:
            case PrivateTag.TransmitterAmplitude:
            case PrivateTag.TransmitterAttenuator:
            case PrivateTag.TransmitterCalibration:
            case PrivateTag.TransmitterReference:
            case PrivateTag.ReceiverTotalGain:
            case PrivateTag.ReceiverAmplifierGain:
            case PrivateTag.ReceiverPreamplifierGain:
            case PrivateTag.ReceiverCableAttenuation:
            case PrivateTag.ReceiverReferenceGain:
            case PrivateTag.ReceiverFilterFrequency:
            case PrivateTag.ReconstructionScaleFactor:
            case PrivateTag.ReferenceScaleFactor:
            case PrivateTag.PhaseGradientAmplitude:
            case PrivateTag.ReadoutGradientAmplitude:
            case PrivateTag.SelectionGradientAmplitude:
            case PrivateTag.GradientDelayTime:
            case PrivateTag.TotalGradientDelayTime:
            case PrivateTag.SaturationPhaseEncodingVectorCoronalComponent:
            case PrivateTag.SaturationReadoutVectorCoronalComponent:
            case PrivateTag.EPIReconstructionSlope:
            case PrivateTag.RFPowerErrorIndicator:
            case PrivateTag.SpecificAbsorptionRateWholeBody:
            case PrivateTag.SpecificEnergyDose:
            case PrivateTag.EPICapacity:
            case PrivateTag.EPIInductance:
            case PrivateTag.EPISwitchDelayTime:
            case PrivateTag.FlowSensitivity:
            case PrivateTag.FieldOfViewRatio:
                return VR.DS;

            case PrivateTag.NumberOfTransmitterAmplitudes:
            case PrivateTag.RFWatchdogMask:
            case PrivateTag.EPISwitchConfigurationCode:
            case PrivateTag.EPISwitchHardwareCode:
            case PrivateTag.BaseRawMatrixSize:
            case PrivateTag.TwoDOversamplingLines:
            case PrivateTag.ThreeDPhaseOversamplingPartitions:
            case PrivateTag.EchoLinePosition:
            case PrivateTag.EchoColumnPosition:
            case PrivateTag.LinesPerSegment:
                return VR.IS;

            case PrivateTag.SensitivityCorrectionLabel:
                return VR.LO;

            case PrivateTag.AdjustmentStatusMask:
                return VR.UL;
        }
        return VR.UN;
    }

}
