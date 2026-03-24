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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_005;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
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
