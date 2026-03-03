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
package org.miaixz.bus.image.galaxy.dict.GEMS_PARM_01;

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

            case PrivateTag.ReconFilter:
            case PrivateTag.PrivateInPlanePhaseEncodingDirection:
            case PrivateTag.ContentQualification:
                return VR.CS;

            case PrivateTag.PeakRateOfChangeOfGradientField:
            case PrivateTag.LimitsInUnitsOfPercent:
            case PrivateTag.PSDEstimatedLimit:
            case PrivateTag.PSDEstimatedLimitInTeslaPerSecond:
            case PrivateTag.SARAvgHead:
            case PrivateTag.IBHImageScaleFactors:
            case PrivateTag.BBHCoefficients:
            case PrivateTag.DeltaStartTime:
            case PrivateTag.AvgOverrangesAllViews:
            case PrivateTag.ReconCenterCoordinates:
            case PrivateTag.ScannerTableEntry:
            case PrivateTag.Reserved:
            case PrivateTag.AssetRFactors:
            case PrivateTag.VoxelLocation:
            case PrivateTag.SATBandLocations:
            case PrivateTag.SpectroPrescanValues:
            case PrivateTag.SpectroParameters:
            case PrivateTag.SARValue:
            case PrivateTag.SpectroQuantitationValues:
            case PrivateTag.SpectroRatioValues:
                return VR.DS;

            case PrivateTag.NegScanSpacing:
            case PrivateTag.User25ToUser48:
            case PrivateTag.TriggerOnPosition:
            case PrivateTag.DegreeOfRotation:
            case PrivateTag.StartScanToXrayOnDelay:
            case PrivateTag.DurationOfXrayOn:
            case PrivateTag.RegressorValues:
            case PrivateTag.DelayAfterSliceGroup:
                return VR.FL;

            case PrivateTag.OffsetFrequency:
            case PrivateTag.SlopInteger6ToSlopInteger9:
            case PrivateTag.SlopInteger10ToSlopInteger17:
            case PrivateTag.DetectorRow:
            case PrivateTag.RxStackIdentification:
                return VR.IS;

            case PrivateTag.ParadigmName:
            case PrivateTag.PACCSpecificInformation:
            case PrivateTag.CoilIDData:
            case PrivateTag.GECoilName:
            case PrivateTag.SystemConfigurationInformation:
            case PrivateTag.AdditionalAssetData:
            case PrivateTag.GoverningBodyDBdtSARDefinition:
            case PrivateTag.SARDefinition:
            case PrivateTag.ImageErrorText:
            case PrivateTag.PrescanReuseString:
            case PrivateTag.ImageFilteringParameters:
            case PrivateTag.ExtendedOptions:
                return VR.LO;

            case PrivateTag.UniqueImageIdentifier:
            case PrivateTag.HistogramTables:
            case PrivateTag.UserDefinedData:
            case PrivateTag.DebugDataBinaryFormat:
            case PrivateTag.FMRIBinaryDataBlock:
                return VR.OB;

            case PrivateTag.ScanPitchRatio:
            case PrivateTag.FilterMode:
            case PrivateTag.StringSlopField2:
            case PrivateTag.ScannerStudyID:
            case PrivateTag.RawDataID:
            case PrivateTag.AutoMAMode:
            case PrivateTag.ApplicationName:
            case PrivateTag.ApplicationVersion:
                return VR.SH;

            case PrivateTag.StartingChannelNumber:
            case PrivateTag.MaxOverrangesInAView:
            case PrivateTag.DASTriggerSource:
            case PrivateTag.DASFpaGain:
            case PrivateTag.DASOutputSource:
            case PrivateTag.DASAdInput:
            case PrivateTag.DASCalMode:
            case PrivateTag.DASCalFrequency:
            case PrivateTag.DASRegXm:
            case PrivateTag.DASAutoZero:
            case PrivateTag.DASXmPattern:
                return VR.SL;

            case PrivateTag.BitmapOfPrescanOptions:
            case PrivateTag.GradientOffsetInX:
            case PrivateTag.GradientOffsetInY:
            case PrivateTag.GradientOffsetInZ:
            case PrivateTag.ImageIsOriginalOrUnoriginal:
            case PrivateTag.NumberOfEPIShots:
            case PrivateTag.ViewsPerSegment:
            case PrivateTag.RespiratoryRateInBPM:
            case PrivateTag.RespiratoryTriggerPoint:
            case PrivateTag.TypeOfReceiverUsed:
            case PrivateTag.XrayChain:
            case PrivateTag.ReconKernelParameters:
            case PrivateTag.CalibrationParameters:
            case PrivateTag.TotalOutputViews:
            case PrivateTag.NumberOfOverranges:
            case PrivateTag.NumberOfBBHChainsToBlend:
            case PrivateTag.PPScanParameters:
            case PrivateTag.GEImageIntegrity:
            case PrivateTag.LevelValue:
            case PrivateTag.CorrectedAfterglowTerms:
            case PrivateTag.ReferenceChannels:
            case PrivateTag.PrivateScanOptions:
            case PrivateTag.EffectiveEchoSpacing:
            case PrivateTag.ImageType:
            case PrivateTag.VasCollapseFlag:
            case PrivateTag.VasFlags:
            case PrivateTag.StartingChannelOfView:
            case PrivateTag.TGGCTriggerMode:
                return VR.SS;

            case PrivateTag.ParadigmDescription:
                return VR.ST;

            case PrivateTag.ScannerStudyEntityUID:
            case PrivateTag.ParadigmUID:
            case PrivateTag.PUREAcquisitionCalibrationSeriesUID:
            case PrivateTag.ASSETAcquisitionCalibrationSeriesUID:
                return VR.UI;

            case PrivateTag.UserUsageTag:
            case PrivateTag.UserFillMapMSW:
            case PrivateTag.UserFillMapLSW:
                return VR.UL;

            case PrivateTag.WindowValue:
            case PrivateTag.TotalInputViews:
            case PrivateTag.NoViewsRefChannelsBlocked:
            case PrivateTag.MotionCorrectionIndicator:
            case PrivateTag.HelicalCorrectionIndicator:
            case PrivateTag.IBOCorrectionIndicator:
            case PrivateTag.IXTCorrectionIndicator:
            case PrivateTag.QcalCorrectionIndicator:
            case PrivateTag.AVCorrectionIndicator:
            case PrivateTag.LMDKCorrectionIndicator:
            case PrivateTag.AreaSize:
            case PrivateTag.ExperimentType:
            case PrivateTag.NumberOfRestVolumes:
            case PrivateTag.NumberOfActiveVolumes:
            case PrivateTag.NumberOfDummyScans:
            case PrivateTag.SlicesPerVolume:
            case PrivateTag.ExpectedTimePoints:
            case PrivateTag.ReconModeFlagWord:
                return VR.US;

            case PrivateTag.DebugDataTextFormat:
                return VR.UT;
        }
        return VR.UN;
    }

}
