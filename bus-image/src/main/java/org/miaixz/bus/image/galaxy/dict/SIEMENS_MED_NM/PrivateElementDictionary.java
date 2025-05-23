/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_NM;

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

        case PrivateTag.ModalityImageHeaderType:
            return VR.CS;
        case PrivateTag.FloodCorrectionMatrixDetector1:
        case PrivateTag.FloodCorrectionMatrixDetector2:
        case PrivateTag.CORDataForDetector1:
        case PrivateTag.CORDataForDetector2:
        case PrivateTag.MHRDataForDetector1:
        case PrivateTag.MHRDataForDetector2:
        case PrivateTag.NCODataForDetector1:
        case PrivateTag.NCODataForDetector2:
        case PrivateTag._0033_xx1A_:
        case PrivateTag.BedCorrectionAngle:
        case PrivateTag.GantryCorrectionAngle:
        case PrivateTag.BackProjectionCorrectionAngleHead1:
        case PrivateTag.BackProjectionCorrectionAngleHead2:
        case PrivateTag.CrystalThickness:
        case PrivateTag.CameraConfigAngle:
        case PrivateTag.WholebodyBedStep:
        case PrivateTag.WeightFactorTableForCoincidenceAcquisitions:
        case PrivateTag.CoincidenceWeightFactorTable:
        case PrivateTag.PixelScaleFactor:
        case PrivateTag.HorizontalTablePositionOfCTScan:
        case PrivateTag.EffectiveEnergyOfCTScan:
        case PrivateTag.BroadBeamFactor:
        case PrivateTag.EffectiveEmissionEnergy:
        case PrivateTag.GatedFrameDuration:
        case PrivateTag.DetectorViewAngle:
        case PrivateTag.ViewDependentYShiftMHRForDetector1:
        case PrivateTag.ViewDependentYShiftMHRForDetector2:
        case PrivateTag.CollimatorThickness:
        case PrivateTag.CollimatorAngularResolution:
        case PrivateTag.DoseCalibrationFactor:
        case PrivateTag.RescaleIntercept:
        case PrivateTag.RescaleSlope:
        case PrivateTag.FrameReferenceTime:
        case PrivateTag.DecayFactor:
        case PrivateTag.XPrincipalRayOffset:
        case PrivateTag.YPrincipalRayOffset:
        case PrivateTag.XPrincipalRayAngle:
        case PrivateTag.YPrincipalRayAngle:
        case PrivateTag.XShortFocalLength:
        case PrivateTag.YShortFocalLength:
        case PrivateTag.XLongFocalLength:
        case PrivateTag.YLongFocalLength:
        case PrivateTag.XFocalScaling:
        case PrivateTag.YFocalScaling:
        case PrivateTag.XMotionCorrectionShift:
        case PrivateTag.YMotionCorrectionShift:
        case PrivateTag.XHeartCenter:
        case PrivateTag.YHeartCenter:
        case PrivateTag.ZHeartCenter:
        case PrivateTag.ReconSelectedAngularRange:
        case PrivateTag.ReconTransverseAngle:
        case PrivateTag.ReconSagittalAngle:
        case PrivateTag.ReconXMaskSize:
        case PrivateTag.ReconYMaskSize:
        case PrivateTag.ReconXImageCenter:
        case PrivateTag.ReconYImageCenter:
        case PrivateTag.ReconZImageCenter:
        case PrivateTag.ReconXZoom:
        case PrivateTag.ReconYZoom:
        case PrivateTag.ReconThreshold:
        case PrivateTag.ReconOutputPixelSize:
        case PrivateTag.ScatterEstimationLowerWindowWeights:
        case PrivateTag.ScatterEstimationUpperWindowWeights:
        case PrivateTag.ReconZMaskSize:
        case PrivateTag.ReconXMaskCenter:
        case PrivateTag.ReconYMaskCenter:
        case PrivateTag.ReconZMaskCenter:
            return VR.FL;
        case PrivateTag.LongLinearDriveInformationForDetector1:
        case PrivateTag.LongLinearDriveInformationForDetector2:
        case PrivateTag.TrunnionInformationForDetector1:
        case PrivateTag.TrunnionInformationForDetector2:
        case PrivateTag.OriginalWholebodyPosition:
        case PrivateTag.WholebodyScanRange:
        case PrivateTag.TransformationMatrix:
            return VR.FD;
        case PrivateTag.ModalityImageHeaderVersion:
        case PrivateTag.PresetNameUsedForAcquisition:
        case PrivateTag.CrystalType:
        case PrivateTag.SpecializedTomoType:
        case PrivateTag.EnergyWindowType:
        case PrivateTag.BlankScanImageForProfile:
        case PrivateTag.SiemensProfile2ImageSubtype:
        case PrivateTag.PlanarProcessingString:
        case PrivateTag.SyngoMIDICOMOriginalImageType:
        case PrivateTag.Units:
        case PrivateTag.DecayCorrection:
        case PrivateTag.CountsSource:
        case PrivateTag.ImagePixelContentType:
        case PrivateTag.ReconOrientation:
        case PrivateTag.ScatterEstimationMethod:
        case PrivateTag.ScatterEstimationMethodMode:
        case PrivateTag.ScatterEstimationWindowMode:
        case PrivateTag.ScatterEstimationFilter:
        case PrivateTag.ReconRawTomoInputUID:
        case PrivateTag.ReconCTInputUID:
            return VR.LO;
        case PrivateTag.ToshibaCBFActivityResults:
        case PrivateTag.RelatedCTSeriesInstanceUID:
        case PrivateTag.DistortedSeriesInstanceUID:
        case PrivateTag.RawTomoSeriesUID:
        case PrivateTag.LowResCTSeriesUID:
        case PrivateTag.HighResCTSeriesUID:
            return VR.LT;
        case PrivateTag.ECATMainHeader:
        case PrivateTag.ECATImageSubheader:
        case PrivateTag.ModalityImageHeaderInfo:
            return VR.OB;
        case PrivateTag.MinimumPixelInFrame:
        case PrivateTag.MaximumPixelInFrame:
        case PrivateTag._7FE3_xx16_:
        case PrivateTag._7FE3_xx1B_:
        case PrivateTag._7FE3_xx1C_:
        case PrivateTag._7FE3_xx1E_:
        case PrivateTag._7FE3_xx26_:
        case PrivateTag._7FE3_xx27_:
        case PrivateTag._7FE3_xx28_:
        case PrivateTag.NumberOfRWavesInFrame:
            return VR.OW;
        case PrivateTag.SiemensICONDataType:
        case PrivateTag.RepeatStartTime:
        case PrivateTag.RepeatStopTime:
        case PrivateTag.EffectiveRepeatTime:
        case PrivateTag._0019_xx93_:
        case PrivateTag._0019_xxA3_:
        case PrivateTag._0031_xx0F_:
        case PrivateTag._0031_xx10_:
        case PrivateTag._0031_xx15_:
        case PrivateTag._0031_xx16_:
        case PrivateTag.MHRCalibrations:
        case PrivateTag.CoinGantryStep:
        case PrivateTag.StarburstFlagsAtImageAcqTime:
        case PrivateTag.WholeBodyTomoPositionIndex:
        case PrivateTag.WholeBodyTomoNumberOfPositions:
        case PrivateTag._0055_xx4C_:
        case PrivateTag._0055_xx4D_:
        case PrivateTag._0055_xx55_:
        case PrivateTag.RadionuclideHalfLife:
        case PrivateTag.NumberofRadiopharmaceuticalInformationSequence:
        case PrivateTag.RadionuclidePositronFraction:
            return VR.SL;
        case PrivateTag._0019_xx08_:
        case PrivateTag._0019_xx16_:
        case PrivateTag.NumberOfRepeatsPerPhase:
        case PrivateTag.CyclesPerRepeat:
        case PrivateTag.AcquiredCyclesPerRepeat:
        case PrivateTag._0019_xxA1_:
        case PrivateTag._0031_xx0C_:
        case PrivateTag._0031_xx12_:
        case PrivateTag._0031_xx17_:
        case PrivateTag._0031_xx21_:
        case PrivateTag.BedUDCorrectionData:
        case PrivateTag.GantryLRCorrectionData:
        case PrivateTag.StartandEndRowIlluminatedByWindPosition:
        case PrivateTag.RepeatNumberOfTheOriginalDynamicSPECT:
        case PrivateTag.PhaseNumberOfTheOriginalDynamicSPECT:
        case PrivateTag.PromptWindowWidth:
        case PrivateTag.RandomWindowWidth:
        case PrivateTag._0055_xx20_:
        case PrivateTag._0055_xx22_:
        case PrivateTag._0055_xx24_:
        case PrivateTag._0055_xx30_:
        case PrivateTag._0055_xx32_:
        case PrivateTag._0055_xx34_:
        case PrivateTag._0055_xx40_:
        case PrivateTag._0055_xx42_:
        case PrivateTag._0055_xx44_:
        case PrivateTag._0055_xx51_:
        case PrivateTag._0055_xx52_:
        case PrivateTag._0055_xx6D_:
        case PrivateTag._0055_xxA8_:
        case PrivateTag.UsefulFieldOfView:
        case PrivateTag._0055_xxC2_:
        case PrivateTag._0055_xxC3_:
        case PrivateTag._0055_xxC4_:
        case PrivateTag._0055_xxD0_:
        case PrivateTag.AutoSaveCorrectedSeries:
        case PrivateTag.ReconRange:
            return VR.SS;
        case PrivateTag._0009_xx80_:
        case PrivateTag._0011_xx10_:
        case PrivateTag._0017_xx00_:
        case PrivateTag._0017_xx20_:
        case PrivateTag._0017_xx70_:
        case PrivateTag._0017_xx80_:
        case PrivateTag._0019_xxC3_:
        case PrivateTag._0021_xx10_:
        case PrivateTag._0031_xx01_:
        case PrivateTag._0031_xx13_:
        case PrivateTag._0031_xx14_:
        case PrivateTag._0031_xx20_:
        case PrivateTag._0041_xx30_:
        case PrivateTag._0041_xx32_:
        case PrivateTag._0055_xx53_:
        case PrivateTag._0055_xx5C_:
            return VR.ST;
        case PrivateTag.DICOMReaderFlag:
        case PrivateTag.TriggerTimeOfCTSlice:
            return VR.US;
        }
        return VR.UN;
    }

}
