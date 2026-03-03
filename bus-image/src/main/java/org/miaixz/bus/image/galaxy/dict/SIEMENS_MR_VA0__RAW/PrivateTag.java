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
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MR VA0 RAW";

    /** (0021,xx00) VR=CS VM=1 Sequence Type */
    public static final int SequenceType = 0x00210000;

    /** (0021,xx01) VR=IS VM=1 Vector Size Original */
    public static final int VectorSizeOriginal = 0x00210001;

    /** (0021,xx02) VR=IS VM=1 Vector Size Extended */
    public static final int VectorSizeExtended = 0x00210002;

    /** (0021,xx03) VR=DS VM=1 Acquired Spectral Range */
    public static final int AcquiredSpectralRange = 0x00210003;

    /** (0021,xx04) VR=DS VM=3 VOI Position */
    public static final int VOIPosition = 0x00210004;

    /** (0021,xx05) VR=DS VM=3 VOI Size */
    public static final int VOISize = 0x00210005;

    /** (0021,xx06) VR=IS VM=3 CSI Matrix Size Original */
    public static final int CSIMatrixSizeOriginal = 0x00210006;

    /** (0021,xx07) VR=IS VM=3 CSI Matrix Size Extended */
    public static final int CSIMatrixSizeExtended = 0x00210007;

    /** (0021,xx08) VR=DS VM=3 Spatial Grid Shift */
    public static final int SpatialGridShift = 0x00210008;

    /** (0021,xx09) VR=DS VM=1 Signal Limits Minimum */
    public static final int SignalLimitsMinimum = 0x00210009;

    /** (0021,xx10) VR=DS VM=1 Signal Limits Maximum */
    public static final int SignalLimitsMaximum = 0x00210010;

    /** (0021,xx11) VR=DS VM=1 Spec Info Mask */
    public static final int SpecInfoMask = 0x00210011;

    /** (0021,xx12) VR=DS VM=1 EPI Time Rate of Change of Magnitude */
    public static final int EPITimeRateOfChangeOfMagnitude = 0x00210012;

    /**
     * (0021,xx13) VR=DS VM=1 EPI Time Rate of Change of X Component
     */
    public static final int EPITimeRateOfChangeOfXComponent = 0x00210013;

    /**
     * (0021,xx14) VR=DS VM=1 EPI Time Rate of Change of Y Component
     */
    public static final int EPITimeRateOfChangeOfYComponent = 0x00210014;

    /**
     * (0021,xx15) VR=DS VM=1 EPI Time Rate of Change of Z Component
     */
    public static final int EPITimeRateOfChangeOfZComponent = 0x00210015;

    /**
     * (0021,xx16) VR=DS VM=1 EPI Time Rate of Change Legal Limit 1
     */
    public static final int EPITimeRateOfChangeLegalLimit1 = 0x00210016;

    /** (0021,xx17) VR=IS VM=1 EPI Operation Mode Flag */
    public static final int EPIOperationModeFlag = 0x00210017;

    /** (0021,xx18) VR=DS VM=1 EPI Field Calculation Safety Factor */
    public static final int EPIFieldCalculationSafetyFactor = 0x00210018;

    /** (0021,xx19) VR=DS VM=1 EPI Legal Limit 1 of Change Value */
    public static final int EPILegalLimit1OfChangeValue = 0x00210019;

    /** (0021,xx20) VR=DS VM=1 EPI Legal Limit 2 of Change Value */
    public static final int EPILegalLimit2OfChangeValue = 0x00210020;

    /** (0021,xx21) VR=DS VM=1 EPI Rise Time */
    public static final int EPIRiseTime = 0x00210021;

    /** (0021,xx30) VR=DS VM=16 Array Coil ADC Offset */
    public static final int ArrayCoilADCOffset = 0x00210030;

    /** (0021,xx31) VR=DS VM=16 Array Coil Preamplifier Gain */
    public static final int ArrayCoilPreamplifierGain = 0x00210031;

    /** (0021,xx50) VR=LO VM=1 Saturation Type */
    public static final int SaturationType = 0x00210050;

    /** (0021,xx51) VR=DS VM=3 Saturation Normal Vector */
    public static final int SaturationNormalVector = 0x00210051;

    /** (0021,xx52) VR=DS VM=3 Saturation Position Vector */
    public static final int SaturationPositionVector = 0x00210052;

    /** (0021,xx53) VR=DS VM=6 Saturation Thickness */
    public static final int SaturationThickness = 0x00210053;

    /** (0021,xx54) VR=DS VM=6 Saturation Width */
    public static final int SaturationWidth = 0x00210054;

    /** (0021,xx55) VR=DS VM=6 Saturation Distance */
    public static final int SaturationDistance = 0x00210055;

}
