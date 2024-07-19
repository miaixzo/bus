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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_005;

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
        
            case PrivateTag.VolumeViewEnabled:
            case PrivateTag.PlanMode:
            case PrivateTag.OperatingModeType:
            case PrivateTag.OperatingMode:
            case PrivateTag.FatSaturationTechnique:
            case PrivateTag.ViewingHardcopyOnly:
            case PrivateTag._2005_xx27_:
            case PrivateTag.LabelType:
            case PrivateTag.ExamPrintStatus:
            case PrivateTag.ExamExportStatus:
            case PrivateTag.ExamStorageCommitStatus:
            case PrivateTag.ExamMediaWriteStatus:
            case PrivateTag.SafetyOverrideMode:
            case PrivateTag.SpectroExamcard:
            case PrivateTag.ColorLUTType:
            case PrivateTag.IsCoilSurvey:
            case PrivateTag.AIMDLimitsApplied:
            case PrivateTag.AttenuationCorrection:
            case PrivateTag.IsB0Series:
            case PrivateTag.IsB1Series:
            case PrivateTag.VolumeSelect:
            case PrivateTag.SplitSeriesJobParams:
            case PrivateTag.LUTToRGBJobParams:
                return VR.CS;
            case PrivateTag.RescaleInterceptOriginal:
            case PrivateTag.RescaleSlopeOriginal:
            case PrivateTag.ContrastBolusVolume:
            case PrivateTag.ContrastBolusIngredientConcentration:
                return VR.DS;
            case PrivateTag.EVDVDJobInParamsDatetime:
            case PrivateTag.DVDJobInParamsVolumeLabel:
                return VR.DT;
            case PrivateTag.DBdt:
            case PrivateTag.ProtonSAR:
            case PrivateTag.NonProtonSAR:
            case PrivateTag.LocalSAR:
            case PrivateTag.StackTablePosLong:
            case PrivateTag.StackTablePosLat:
            case PrivateTag.StackPosteriorCoilPos:
            case PrivateTag.AIMDHeadSARLimit:
            case PrivateTag.AIMDWholeBodySARLimit:
            case PrivateTag.AIMDB1RMSLimit:
            case PrivateTag.AIMDdbDtLimit:
            case PrivateTag.FWHMShim:
            case PrivateTag.PowerOptimization:
            case PrivateTag.CoilQ:
            case PrivateTag.ReceiverGain:
            case PrivateTag.DataWindowDuration:
            case PrivateTag.MixingTime:
            case PrivateTag.FirstEchoTime:
            case PrivateTag.SpecificEnergyDose:
                return VR.FL;
            case PrivateTag.DiffusionBMatrix:
            case PrivateTag.VelocityEncodingDirection:
                return VR.FD;
            case PrivateTag.MFConvTreatSpectroMixNumber:
            case PrivateTag.DiffusionBValueNumber:
            case PrivateTag.GradientOrientationNumber:
            case PrivateTag.VersionNumberDeletedImages:
            case PrivateTag.VersionNumberDeletedSpectra:
            case PrivateTag.VersionNumberDeletedBlobsets:
            case PrivateTag.TFEFactor:
            case PrivateTag.OriginalSeriesNumber:
            case PrivateTag.ContrastBolusDynamicNumber:
            case PrivateTag.ContrastBolusID:
                return VR.IS;
            case PrivateTag.RescaleTypeOriginal:
                return VR.LO;
            case PrivateTag._2005_xx38_:
            case PrivateTag._2005_xx39_:
            case PrivateTag.DataDictionaryContentsVersion:
            case PrivateTag.ContrastBolusAgentCode:
            case PrivateTag.ContrastBolusAdminRouteCode:
                return VR.LT;
            case PrivateTag.NumberOfDiffusionBValues:
            case PrivateTag.NumberOfDiffusionGradientOrientations:
            case PrivateTag.NumberOfLabelTypes:
                return VR.SL;
            case PrivateTag.SPSCode:
            case PrivateTag.PrivateSharedSequence:
            case PrivateTag.PrivatePerFrameSequence:
            case PrivateTag.ContrastBolusSequence:
            case PrivateTag.OriginalVOILUTSequence:
            case PrivateTag.OriginalModalityLUTSequence:
                return VR.SQ;
            case PrivateTag._2005_xx04_:
            case PrivateTag.NumberOfPSSpecificCharacterSets:
            case PrivateTag.NumberOfSpecificCharacterSet:
            case PrivateTag.NumberOfPatientOtherIDs:
            case PrivateTag.PreferredDimensionForSplitting:
            case PrivateTag.ContrastBolusNumberOfInjections:
                return VR.SS;
            case PrivateTag.MFPrivateReferencedSOPInstanceUID:
            case PrivateTag.ReferencedSeriesInstanceUID:
            case PrivateTag.OriginalSeriesInstanceUID:
                return VR.UI;
            case PrivateTag.NumberOfStudyReference:
            case PrivateTag.NumberOfSPSCodes:
            case PrivateTag.LUT1Offset:
            case PrivateTag.LUT1Range:
            case PrivateTag.LUT1BeginColor:
            case PrivateTag.LUT1EndColor:
            case PrivateTag.LUT2Offset:
            case PrivateTag.LUT2Range:
            case PrivateTag.LUT2BeginColor:
            case PrivateTag.LUT2EndColor:
                return VR.UL;
        }
        return VR.UN;
    }
}
