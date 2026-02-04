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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_VA0__COAD;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MR VA0 COAD";

    /** (0019,xx12) VR=DS VM=1 Magnetic Field Strength */
    public static final int MagneticFieldStrength = 0x00190012;

    /** (0019,xx14) VR=DS VM=1 ADC Voltage */
    public static final int ADCVoltage = 0x00190014;

    /** (0019,xx16) VR=DS VM=2 ADC Offset */
    public static final int ADCOffset = 0x00190016;

    /** (0019,xx20) VR=DS VM=1 Transmitter Amplitude */
    public static final int TransmitterAmplitude = 0x00190020;

    /** (0019,xx21) VR=IS VM=1 Number of Transmitter Amplitudes */
    public static final int NumberOfTransmitterAmplitudes = 0x00190021;

    /** (0019,xx22) VR=DS VM=1 Transmitter Attenuator */
    public static final int TransmitterAttenuator = 0x00190022;

    /** (0019,xx24) VR=DS VM=1 Transmitter Calibration */
    public static final int TransmitterCalibration = 0x00190024;

    /** (0019,xx26) VR=DS VM=1 Transmitter Reference */
    public static final int TransmitterReference = 0x00190026;

    /** (0019,xx50) VR=DS VM=1 Receiver Total Gain */
    public static final int ReceiverTotalGain = 0x00190050;

    /** (0019,xx51) VR=DS VM=1 Receiver Amplifier Gain */
    public static final int ReceiverAmplifierGain = 0x00190051;

    /** (0019,xx52) VR=DS VM=1 Receiver Preamplifier Gain */
    public static final int ReceiverPreamplifierGain = 0x00190052;

    /** (0019,xx54) VR=DS VM=1 Receiver Cable Attenuation */
    public static final int ReceiverCableAttenuation = 0x00190054;

    /** (0019,xx55) VR=DS VM=1 Receiver Reference Gain */
    public static final int ReceiverReferenceGain = 0x00190055;

    /** (0019,xx56) VR=DS VM=1 Receiver Filter Frequency */
    public static final int ReceiverFilterFrequency = 0x00190056;

    /** (0019,xx60) VR=DS VM=1 Reconstruction Scale Factor */
    public static final int ReconstructionScaleFactor = 0x00190060;

    /** (0019,xx62) VR=DS VM=1 Reference Scale Factor */
    public static final int ReferenceScaleFactor = 0x00190062;

    /** (0019,xx70) VR=DS VM=1 Phase Gradient Amplitude */
    public static final int PhaseGradientAmplitude = 0x00190070;

    /** (0019,xx71) VR=DS VM=1 Readout Gradient Amplitude */
    public static final int ReadoutGradientAmplitude = 0x00190071;

    /** (0019,xx72) VR=DS VM=1 Selection Gradient Amplitude */
    public static final int SelectionGradientAmplitude = 0x00190072;

    /** (0019,xx80) VR=DS VM=3 Gradient Delay Time */
    public static final int GradientDelayTime = 0x00190080;

    /** (0019,xx82) VR=DS VM=1 Total Gradient Delay Time */
    public static final int TotalGradientDelayTime = 0x00190082;

    /** (0019,xx90) VR=LO VM=1 Sensitivity Correction Label */
    public static final int SensitivityCorrectionLabel = 0x00190090;

    /**
     * (0019,xx91) VR=DS VM=6 Saturation Phase Encoding Vector Coronal Component
     */
    public static final int SaturationPhaseEncodingVectorCoronalComponent = 0x00190091;

    /**
     * (0019,xx92) VR=DS VM=6 Saturation Readout Vector Coronal Component
     */
    public static final int SaturationReadoutVectorCoronalComponent = 0x00190092;

    /** (0019,xxA0) VR=IS VM=1 RF Watchdog Mask */
    public static final int RFWatchdogMask = 0x001900A0;

    /** (0019,xxA1) VR=DS VM=1 EPI Reconstruction Slope */
    public static final int EPIReconstructionSlope = 0x001900A1;

    /** (0019,xxA2) VR=DS VM=1 RF Power Error Indicator */
    public static final int RFPowerErrorIndicator = 0x001900A2;

    /** (0019,xxA5) VR=DS VM=3 Specific Absorption Rate Whole Body */
    public static final int SpecificAbsorptionRateWholeBody = 0x001900A5;

    /** (0019,xxA6) VR=DS VM=3 Specific Energy Dose */
    public static final int SpecificEnergyDose = 0x001900A6;

    /** (0019,xxB0) VR=UL VM=1 Adjustment Status Mask */
    public static final int AdjustmentStatusMask = 0x001900B0;

    /** (0019,xxC1) VR=DS VM=6 EPI Capacity */
    public static final int EPICapacity = 0x001900C1;

    /** (0019,xxC2) VR=DS VM=3 EPI Inductance */
    public static final int EPIInductance = 0x001900C2;

    /** (0019,xxC3) VR=IS VM=1-n EPI Switch Configuration Code */
    public static final int EPISwitchConfigurationCode = 0x001900C3;

    /** (0019,xxC4) VR=IS VM=1-n EPI Switch Hardware Code */
    public static final int EPISwitchHardwareCode = 0x001900C4;

    /** (0019,xxC5) VR=DS VM=1-n EPI Switch Delay Time */
    public static final int EPISwitchDelayTime = 0x001900C5;

    /** (0019,xxD1) VR=DS VM=1 Flow Sensitivity */
    public static final int FlowSensitivity = 0x001900D1;

    /** (0019,xxD2) VR=CS VM=1 Calculation Submode */
    public static final int CalculationSubmode = 0x001900D2;

    /** (0019,xxD3) VR=DS VM=1 Field of View Ratio */
    public static final int FieldOfViewRatio = 0x001900D3;

    /** (0019,xxD4) VR=IS VM=1 Base Raw Matrix Size */
    public static final int BaseRawMatrixSize = 0x001900D4;

    /** (0019,xxD5) VR=IS VM=1 2D Oversampling Lines */
    public static final int TwoDOversamplingLines = 0x001900D5;

    /** (0019,xxD6) VR=IS VM=1 3D Phase Oversampling Partitions */
    public static final int ThreeDPhaseOversamplingPartitions = 0x001900D6;

    /** (0019,xxD7) VR=IS VM=1 Echo Line Position */
    public static final int EchoLinePosition = 0x001900D7;

    /** (0019,xxD8) VR=IS VM=1 Echo Column Position */
    public static final int EchoColumnPosition = 0x001900D8;

    /** (0019,xxD9) VR=IS VM=1 Lines Per Segment */
    public static final int LinesPerSegment = 0x001900D9;

    /** (0019,xxDA) VR=CS VM=1 Phase Coding Direction */
    public static final int PhaseCodingDirection = 0x001900DA;

}
