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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.VolumeViewEnabled:
            return "VolumeViewEnabled";
        case PrivateTag.NumberOfStudyReference:
            return "NumberOfStudyReference";
        case PrivateTag.SPSCode:
            return "SPSCode";
        case PrivateTag.NumberOfSPSCodes:
            return "NumberOfSPSCodes";
        case PrivateTag._2005_xx04_:
            return "_2005_xx04_";
        case PrivateTag.NumberOfPSSpecificCharacterSets:
            return "NumberOfPSSpecificCharacterSets";
        case PrivateTag.NumberOfSpecificCharacterSet:
            return "NumberOfSpecificCharacterSet";
        case PrivateTag.RescaleInterceptOriginal:
            return "RescaleInterceptOriginal";
        case PrivateTag.RescaleSlopeOriginal:
            return "RescaleSlopeOriginal";
        case PrivateTag.RescaleTypeOriginal:
            return "RescaleTypeOriginal";
        case PrivateTag.PrivateSharedSequence:
            return "PrivateSharedSequence";
        case PrivateTag.PrivatePerFrameSequence:
            return "PrivatePerFrameSequence";
        case PrivateTag.MFConvTreatSpectroMixNumber:
            return "MFConvTreatSpectroMixNumber";
        case PrivateTag.MFPrivateReferencedSOPInstanceUID:
            return "MFPrivateReferencedSOPInstanceUID";
        case PrivateTag.DiffusionBValueNumber:
            return "DiffusionBValueNumber";
        case PrivateTag.GradientOrientationNumber:
            return "GradientOrientationNumber";
        case PrivateTag.NumberOfDiffusionBValues:
            return "NumberOfDiffusionBValues";
        case PrivateTag.NumberOfDiffusionGradientOrientations:
            return "NumberOfDiffusionGradientOrientations";
        case PrivateTag.PlanMode:
            return "PlanMode";
        case PrivateTag.DiffusionBMatrix:
            return "DiffusionBMatrix";
        case PrivateTag.OperatingModeType:
            return "OperatingModeType";
        case PrivateTag.OperatingMode:
            return "OperatingMode";
        case PrivateTag.FatSaturationTechnique:
            return "FatSaturationTechnique";
        case PrivateTag.VersionNumberDeletedImages:
            return "VersionNumberDeletedImages";
        case PrivateTag.VersionNumberDeletedSpectra:
            return "VersionNumberDeletedSpectra";
        case PrivateTag.VersionNumberDeletedBlobsets:
            return "VersionNumberDeletedBlobsets";
        case PrivateTag.LUT1Offset:
            return "LUT1Offset";
        case PrivateTag.LUT1Range:
            return "LUT1Range";
        case PrivateTag.LUT1BeginColor:
            return "LUT1BeginColor";
        case PrivateTag.LUT1EndColor:
            return "LUT1EndColor";
        case PrivateTag.LUT2Offset:
            return "LUT2Offset";
        case PrivateTag.LUT2Range:
            return "LUT2Range";
        case PrivateTag.LUT2BeginColor:
            return "LUT2BeginColor";
        case PrivateTag.LUT2EndColor:
            return "LUT2EndColor";
        case PrivateTag.ViewingHardcopyOnly:
            return "ViewingHardcopyOnly";
        case PrivateTag._2005_xx27_:
            return "_2005_xx27_";
        case PrivateTag.NumberOfLabelTypes:
            return "NumberOfLabelTypes";
        case PrivateTag.LabelType:
            return "LabelType";
        case PrivateTag.ExamPrintStatus:
            return "ExamPrintStatus";
        case PrivateTag.ExamExportStatus:
            return "ExamExportStatus";
        case PrivateTag.ExamStorageCommitStatus:
            return "ExamStorageCommitStatus";
        case PrivateTag.ExamMediaWriteStatus:
            return "ExamMediaWriteStatus";
        case PrivateTag.DBdt:
            return "DBdt";
        case PrivateTag.ProtonSAR:
            return "ProtonSAR";
        case PrivateTag.NonProtonSAR:
            return "NonProtonSAR";
        case PrivateTag.LocalSAR:
            return "LocalSAR";
        case PrivateTag.SafetyOverrideMode:
            return "SafetyOverrideMode";
        case PrivateTag.EVDVDJobInParamsDatetime:
            return "EVDVDJobInParamsDatetime";
        case PrivateTag.DVDJobInParamsVolumeLabel:
            return "DVDJobInParamsVolumeLabel";
        case PrivateTag.SpectroExamcard:
            return "SpectroExamcard";
        case PrivateTag.ReferencedSeriesInstanceUID:
            return "ReferencedSeriesInstanceUID";
        case PrivateTag.ColorLUTType:
            return "ColorLUTType";
        case PrivateTag._2005_xx38_:
            return "_2005_xx38_";
        case PrivateTag._2005_xx39_:
            return "_2005_xx39_";
        case PrivateTag.DataDictionaryContentsVersion:
            return "DataDictionaryContentsVersion";
        case PrivateTag.IsCoilSurvey:
            return "IsCoilSurvey";
        case PrivateTag.StackTablePosLong:
            return "StackTablePosLong";
        case PrivateTag.StackTablePosLat:
            return "StackTablePosLat";
        case PrivateTag.StackPosteriorCoilPos:
            return "StackPosteriorCoilPos";
        case PrivateTag.AIMDLimitsApplied:
            return "AIMDLimitsApplied";
        case PrivateTag.AIMDHeadSARLimit:
            return "AIMDHeadSARLimit";
        case PrivateTag.AIMDWholeBodySARLimit:
            return "AIMDWholeBodySARLimit";
        case PrivateTag.AIMDB1RMSLimit:
            return "AIMDB1RMSLimit";
        case PrivateTag.AIMDdbDtLimit:
            return "AIMDdbDtLimit";
        case PrivateTag.TFEFactor:
            return "TFEFactor";
        case PrivateTag.AttenuationCorrection:
            return "AttenuationCorrection";
        case PrivateTag.FWHMShim:
            return "FWHMShim";
        case PrivateTag.PowerOptimization:
            return "PowerOptimization";
        case PrivateTag.CoilQ:
            return "CoilQ";
        case PrivateTag.ReceiverGain:
            return "ReceiverGain";
        case PrivateTag.DataWindowDuration:
            return "DataWindowDuration";
        case PrivateTag.MixingTime:
            return "MixingTime";
        case PrivateTag.FirstEchoTime:
            return "FirstEchoTime";
        case PrivateTag.IsB0Series:
            return "IsB0Series";
        case PrivateTag.IsB1Series:
            return "IsB1Series";
        case PrivateTag.VolumeSelect:
            return "VolumeSelect";
        case PrivateTag.NumberOfPatientOtherIDs:
            return "NumberOfPatientOtherIDs";
        case PrivateTag.OriginalSeriesNumber:
            return "OriginalSeriesNumber";
        case PrivateTag.OriginalSeriesInstanceUID:
            return "OriginalSeriesInstanceUID";
        case PrivateTag.SplitSeriesJobParams:
            return "SplitSeriesJobParams";
        case PrivateTag.PreferredDimensionForSplitting:
            return "PreferredDimensionForSplitting";
        case PrivateTag.VelocityEncodingDirection:
            return "VelocityEncodingDirection";
        case PrivateTag.ContrastBolusNumberOfInjections:
            return "ContrastBolusNumberOfInjections";
        case PrivateTag.ContrastBolusAgentCode:
            return "ContrastBolusAgentCode";
        case PrivateTag.ContrastBolusAdminRouteCode:
            return "ContrastBolusAdminRouteCode";
        case PrivateTag.ContrastBolusVolume:
            return "ContrastBolusVolume";
        case PrivateTag.ContrastBolusIngredientConcentration:
            return "ContrastBolusIngredientConcentration";
        case PrivateTag.ContrastBolusDynamicNumber:
            return "ContrastBolusDynamicNumber";
        case PrivateTag.ContrastBolusSequence:
            return "ContrastBolusSequence";
        case PrivateTag.ContrastBolusID:
            return "ContrastBolusID";
        case PrivateTag.LUTToRGBJobParams:
            return "LUTToRGBJobParams";
        case PrivateTag.OriginalVOILUTSequence:
            return "OriginalVOILUTSequence";
        case PrivateTag.OriginalModalityLUTSequence:
            return "OriginalModalityLUTSequence";
        case PrivateTag.SpecificEnergyDose:
            return "SpecificEnergyDose";
        }
        return "";
    }

}
