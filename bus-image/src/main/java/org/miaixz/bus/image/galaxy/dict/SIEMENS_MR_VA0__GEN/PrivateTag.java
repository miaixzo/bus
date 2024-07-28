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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_VA0__GEN;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MR VA0 GEN";

    /** (0019,xx10) VR=DS VM=1 Total Measurement Time Nominal */
    public static final int TotalMeasurementTimeNominal = 0x00190010;

    /** (0019,xx11) VR=DS VM=1 Total Measurement Time Current */
    public static final int TotalMeasurementTimeCurrent = 0x00190011;

    /** (0019,xx12) VR=DS VM=1 Start Delay Time */
    public static final int StartDelayTime = 0x00190012;

    /** (0019,xx13) VR=DS VM=1 Dwell Time */
    public static final int DwellTime = 0x00190013;

    /** (0019,xx14) VR=IS VM=1 Number of Phases */
    public static final int NumberOfPhases = 0x00190014;

    /** (0019,xx16) VR=UL VM=2 Sequence Control Mask */
    public static final int SequenceControlMask = 0x00190016;

    /** (0019,xx18) VR=UL VM=1 Measurement Status Mask */
    public static final int MeasurementStatusMask = 0x00190018;

    /** (0019,xx20) VR=IS VM=1 Number of Fourier Lines Nominal */
    public static final int NumberOfFourierLinesNominal = 0x00190020;

    /** (0019,xx21) VR=IS VM=1 Number of Fourier Lines Current */
    public static final int NumberOfFourierLinesCurrent = 0x00190021;

    /** (0019,xx26) VR=IS VM=1 Number of Fourier Lines after Zero */
    public static final int NumberOfFourierLinesAfterZero = 0x00190026;

    /** (0019,xx28) VR=IS VM=1 First Measured Fourier Line */
    public static final int FirstMeasuredFourierLine = 0x00190028;

    /** (0019,xx30) VR=IS VM=1 Acquisition Columns */
    public static final int AcquisitionColumns = 0x00190030;

    /** (0019,xx31) VR=IS VM=1 Reconstruction Columns */
    public static final int ReconstructionColumns = 0x00190031;

    /** (0019,xx40) VR=IS VM=1 Array Coil Element Number */
    public static final int ArrayCoilElementNumber = 0x00190040;

    /** (0019,xx41) VR=UL VM=1 Array Coil Element Select Mask */
    public static final int ArrayCoilElementSelectMask = 0x00190041;

    /** (0019,xx42) VR=UL VM=1 Array Coil Element Data Mask */
    public static final int ArrayCoilElementDataMask = 0x00190042;

    /** (0019,xx43) VR=IS VM=1-n Array Coil Element To ADC Connect */
    public static final int ArrayCoilElementToADCConnect = 0x00190043;

    /** (0019,xx44) VR=DS VM=1-n Array Coil Element Noise Level */
    public static final int ArrayCoilElementNoiseLevel = 0x00190044;

    /** (0019,xx45) VR=IS VM=1 Array Coil ADC Pair Number */
    public static final int ArrayCoilADCPairNumber = 0x00190045;

    /** (0019,xx46) VR=UL VM=1 Array Coil Combination Mask */
    public static final int ArrayCoilCombinationMask = 0x00190046;

    /** (0019,xx50) VR=IS VM=1 Number of Averages Current */
    public static final int NumberOfAveragesCurrent = 0x00190050;

    /** (0019,xx60) VR=DS VM=1 Flip Angle */
    public static final int FlipAngle = 0x00190060;

    /** (0019,xx70) VR=IS VM=1 Number of Prescans */
    public static final int NumberOfPrescans = 0x00190070;

    /** (0019,xx81) VR=CS VM=1 Filter Type for Raw Data */
    public static final int FilterTypeForRawData = 0x00190081;

    /** (0019,xx82) VR=DS VM=1-n Filter Parameter for Raw Data */
    public static final int FilterParameterForRawData = 0x00190082;

    /** (0019,xx83) VR=CS VM=1 Filter Type for Image Data */
    public static final int FilterTypeForImageData = 0x00190083;

    /** (0019,xx84) VR=DS VM=1-n Filter Parameter for Image Data */
    public static final int FilterParameterForImageData = 0x00190084;

    /** (0019,xx85) VR=CS VM=1 Filter Type for Phase Correction */
    public static final int FilterTypeForPhaseCorrection = 0x00190085;

    /** (0019,xx86) VR=DS VM=1-n Filter Parameter for Phase Correction */
    public static final int FilterParameterForPhaseCorrection = 0x00190086;

    /** (0019,xx87) VR=CS VM=1 Normalization Filter Type for Image Data */
    public static final int NormalizationFilterTypeForImageData = 0x00190087;

    /** (0019,xx88) VR=DS VM=1-n Normalization Filter Parameter for Image Data */
    public static final int NormalizationFilterParameterForImageData = 0x00190088;

    /** (0019,xx90) VR=IS VM=1 Number of Saturation Regions */
    public static final int NumberOfSaturationRegions = 0x00190090;

    /** (0019,xx91) VR=DS VM=6 Saturation Phase Encoding Vector Sagittal Component */
    public static final int SaturationPhaseEncodingVectorSagittalComponent = 0x00190091;

    /** (0019,xx92) VR=DS VM=6 Saturation Readout Vector Sagittal Component */
    public static final int SaturationReadoutVectorSagittalComponent = 0x00190092;

    /** (0019,xx93) VR=IS VM=1 EPI Stimulation Monitor Mode */
    public static final int EPIStimulationMonitorMode = 0x00190093;

    /** (0019,xx94) VR=DS VM=1 Image Rotation Angle */
    public static final int ImageRotationAngle = 0x00190094;

    /** (0019,xx96) VR=UL VM=3 Coil ID Mask */
    public static final int CoilIDMask = 0x00190096;

    /** (0019,xx97) VR=UL VM=2 Coil Class Mask */
    public static final int CoilClassMask = 0x00190097;

    /** (0019,xx98) VR=DS VM=3 Coil Position */
    public static final int CoilPosition = 0x00190098;

    /** (0019,xxA0) VR=DS VM=1 EPI Reconstruction Phase */
    public static final int EPIReconstructionPhase = 0x001900A0;

    /** (0019,xxA1) VR=DS VM=1 EPI Reconstruction Slope */
    public static final int EPIReconstructionSlope = 0x001900A1;

    /** (0021,xx20) VR=IS VM=1 Phase Correction Rows Sequence */
    public static final int PhaseCorrectionRowsSequence = 0x00210020;

    /** (0021,xx21) VR=IS VM=1 Phase Correction Columns Sequence */
    public static final int PhaseCorrectionColumnsSequence = 0x00210021;

    /** (0021,xx22) VR=IS VM=1 Phase Correction Rows Reconstruction */
    public static final int PhaseCorrectionRowsReconstruction = 0x00210022;

    /** (0021,xx24) VR=IS VM=1 Phase Correction Columns Reconstruction */
    public static final int PhaseCorrectionColumnsReconstruction = 0x00210024;

    /** (0021,xx30) VR=IS VM=1 Number of 3D Raw Partitions Nominal */
    public static final int NumberOf3DRawPartitionsNominal = 0x00210030;

    /** (0021,xx31) VR=IS VM=1 Number of 3D Raw Partitions Current */
    public static final int NumberOf3DRawPartitionsCurrent = 0x00210031;

    /** (0021,xx34) VR=IS VM=1 Number of 3D Image Partitions */
    public static final int NumberOf3DImagePartitions = 0x00210034;

    /** (0021,xx36) VR=IS VM=1 Actual 3D Image Partition Number */
    public static final int Actual3DImagePartitionNumber = 0x00210036;

    /** (0021,xx39) VR=DS VM=1 Slab Thickness */
    public static final int SlabThickness = 0x00210039;

    /** (0021,xx40) VR=IS VM=1 Number of Slices Nominal */
    public static final int NumberOfSlicesNominal = 0x00210040;

    /** (0021,xx41) VR=IS VM=1 Number of Slices Current */
    public static final int NumberOfSlicesCurrent = 0x00210041;

    /** (0021,xx42) VR=IS VM=1 Current Slice Number */
    public static final int CurrentSliceNumber = 0x00210042;

    /** (0021,xx43) VR=IS VM=1 Current Group Number */
    public static final int CurrentGroupNumber = 0x00210043;

    /** (0021,xx44) VR=DS VM=1 Current Slice Distance Factor */
    public static final int CurrentSliceDistanceFactor = 0x00210044;

    /** (0021,xx45) VR=IS VM=1 MIP Start Row */
    public static final int MIPStartRow = 0x00210045;

    /** (0021,xx46) VR=IS VM=1 MIP Stop Row */
    public static final int MIPStopRow = 0x00210046;

    /** (0021,xx47) VR=IS VM=1 MIP Start Column */
    public static final int MIPStartColumn = 0x00210047;

    /** (0021,xx48) VR=IS VM=1 MIP Stop Column */
    public static final int MIPStopColumn = 0x00210048;

    /** (0021,xx49) VR=IS VM=1 MIP Start Slice */
    public static final int MIPStartSlice = 0x00210049;

    /** (0021,xx4A) VR=IS VM=1 MIP Stop Slice */
    public static final int MIPStopSlice = 0x0021004A;

    /** (0021,xx4F) VR=CS VM=1 Order of Slices */
    public static final int OrderofSlices = 0x0021004F;

    /** (0021,xx50) VR=UL VM=1-n Signal Mask */
    public static final int SignalMask = 0x00210050;

    /** (0021,xx52) VR=IS VM=1 Delay After Trigger */
    public static final int DelayAfterTrigger = 0x00210052;

    /** (0021,xx53) VR=IS VM=1 RR Interval */
    public static final int RRInterval = 0x00210053;

    /** (0021,xx54) VR=DS VM=1 Number of Trigger Pulses */
    public static final int NumberOfTriggerPulses = 0x00210054;

    /** (0021,xx56) VR=DS VM=1 Repetition Time Effective */
    public static final int RepetitionTimeEffective = 0x00210056;

    /** (0021,xx57) VR=LO VM=1 Gate Phase */
    public static final int GatePhase = 0x00210057;

    /** (0021,xx58) VR=DS VM=1 Gate Threshold */
    public static final int GateThreshold = 0x00210058;

    /** (0021,xx59) VR=DS VM=1 Gated Ratio */
    public static final int GatedRatio = 0x00210059;

    /** (0021,xx60) VR=IS VM=1 Number of Interpolated Images */
    public static final int NumberOfInterpolatedImages = 0x00210060;

    /** (0021,xx70) VR=IS VM=1 Number of Echoes */
    public static final int NumberOfEchoes = 0x00210070;

    /** (0021,xx72) VR=DS VM=1 Second Echo Time */
    public static final int SecondEchoTime = 0x00210072;

    /** (0021,xx73) VR=DS VM=1 Second Repetition Time */
    public static final int SecondRepetitionTime = 0x00210073;

    /** (0021,xx80) VR=IS VM=1 Cardiac Code */
    public static final int CardiacCode = 0x00210080;

    /** (0021,xx91) VR=DS VM=6 Saturation Phase Encoding Vector Transverse Component */
    public static final int SaturationPhaseEncodingVectorTransverseComponent = 0x00210091;

    /** (0021,xx92) VR=DS VM=6 Saturation Readout Vector Transverse Component */
    public static final int SaturationReadoutVectorTransverseComponent = 0x00210092;

    /** (0021,xx93) VR=DS VM=1 EPI Change Value of Magnitude */
    public static final int EPIChangeValueOfMagnitude = 0x00210093;

    /** (0021,xx94) VR=DS VM=1 EPI Change Value of X Component */
    public static final int EPIChangeValueOfXComponent = 0x00210094;

    /** (0021,xx95) VR=DS VM=1 EPI Change Value of Y Component */
    public static final int EPIChangeValueOfYComponent = 0x00210095;

    /** (0021,xx96) VR=DS VM=1 EPI Change Value of Z Component */
    public static final int EPIChangeValueOfZComponent = 0x00210096;

}
