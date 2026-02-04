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
package org.miaixz.bus.image.galaxy.dict.GEMS_PARM_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.BitmapOfPrescanOptions:
                return "BitmapOfPrescanOptions";

            case PrivateTag.GradientOffsetInX:
                return "GradientOffsetInX";

            case PrivateTag.GradientOffsetInY:
                return "GradientOffsetInY";

            case PrivateTag.GradientOffsetInZ:
                return "GradientOffsetInZ";

            case PrivateTag.ImageIsOriginalOrUnoriginal:
                return "ImageIsOriginalOrUnoriginal";

            case PrivateTag.NumberOfEPIShots:
                return "NumberOfEPIShots";

            case PrivateTag.ViewsPerSegment:
                return "ViewsPerSegment";

            case PrivateTag.RespiratoryRateInBPM:
                return "RespiratoryRateInBPM";

            case PrivateTag.RespiratoryTriggerPoint:
                return "RespiratoryTriggerPoint";

            case PrivateTag.TypeOfReceiverUsed:
                return "TypeOfReceiverUsed";

            case PrivateTag.PeakRateOfChangeOfGradientField:
                return "PeakRateOfChangeOfGradientField";

            case PrivateTag.LimitsInUnitsOfPercent:
                return "LimitsInUnitsOfPercent";

            case PrivateTag.PSDEstimatedLimit:
                return "PSDEstimatedLimit";

            case PrivateTag.PSDEstimatedLimitInTeslaPerSecond:
                return "PSDEstimatedLimitInTeslaPerSecond";

            case PrivateTag.SARAvgHead:
                return "SARAvgHead";

            case PrivateTag.WindowValue:
                return "WindowValue";

            case PrivateTag.TotalInputViews:
                return "TotalInputViews";

            case PrivateTag.XrayChain:
                return "XrayChain";

            case PrivateTag.ReconKernelParameters:
                return "ReconKernelParameters";

            case PrivateTag.CalibrationParameters:
                return "CalibrationParameters";

            case PrivateTag.TotalOutputViews:
                return "TotalOutputViews";

            case PrivateTag.NumberOfOverranges:
                return "NumberOfOverranges";

            case PrivateTag.IBHImageScaleFactors:
                return "IBHImageScaleFactors";

            case PrivateTag.BBHCoefficients:
                return "BBHCoefficients";

            case PrivateTag.NumberOfBBHChainsToBlend:
                return "NumberOfBBHChainsToBlend";

            case PrivateTag.StartingChannelNumber:
                return "StartingChannelNumber";

            case PrivateTag.PPScanParameters:
                return "PPScanParameters";

            case PrivateTag.GEImageIntegrity:
                return "GEImageIntegrity";

            case PrivateTag.LevelValue:
                return "LevelValue";

            case PrivateTag.DeltaStartTime:
                return "DeltaStartTime";

            case PrivateTag.MaxOverrangesInAView:
                return "MaxOverrangesInAView";

            case PrivateTag.AvgOverrangesAllViews:
                return "AvgOverrangesAllViews";

            case PrivateTag.CorrectedAfterglowTerms:
                return "CorrectedAfterglowTerms";

            case PrivateTag.ReferenceChannels:
                return "ReferenceChannels";

            case PrivateTag.NoViewsRefChannelsBlocked:
                return "NoViewsRefChannelsBlocked";

            case PrivateTag.ScanPitchRatio:
                return "ScanPitchRatio";

            case PrivateTag.UniqueImageIdentifier:
                return "UniqueImageIdentifier";

            case PrivateTag.HistogramTables:
                return "HistogramTables";

            case PrivateTag.UserDefinedData:
                return "UserDefinedData";

            case PrivateTag.PrivateScanOptions:
                return "PrivateScanOptions";

            case PrivateTag.EffectiveEchoSpacing:
                return "EffectiveEchoSpacing";

            case PrivateTag.FilterMode:
                return "FilterMode";

            case PrivateTag.StringSlopField2:
                return "StringSlopField2";

            case PrivateTag.ImageType:
                return "ImageType";

            case PrivateTag.VasCollapseFlag:
                return "VasCollapseFlag";

            case PrivateTag.ReconCenterCoordinates:
                return "ReconCenterCoordinates";

            case PrivateTag.VasFlags:
                return "VasFlags";

            case PrivateTag.NegScanSpacing:
                return "NegScanSpacing";

            case PrivateTag.OffsetFrequency:
                return "OffsetFrequency";

            case PrivateTag.UserUsageTag:
                return "UserUsageTag";

            case PrivateTag.UserFillMapMSW:
                return "UserFillMapMSW";

            case PrivateTag.UserFillMapLSW:
                return "UserFillMapLSW";

            case PrivateTag.User25ToUser48:
                return "User25ToUser48";

            case PrivateTag.SlopInteger6ToSlopInteger9:
                return "SlopInteger6ToSlopInteger9";

            case PrivateTag.TriggerOnPosition:
                return "TriggerOnPosition";

            case PrivateTag.DegreeOfRotation:
                return "DegreeOfRotation";

            case PrivateTag.DASTriggerSource:
                return "DASTriggerSource";

            case PrivateTag.DASFpaGain:
                return "DASFpaGain";

            case PrivateTag.DASOutputSource:
                return "DASOutputSource";

            case PrivateTag.DASAdInput:
                return "DASAdInput";

            case PrivateTag.DASCalMode:
                return "DASCalMode";

            case PrivateTag.DASCalFrequency:
                return "DASCalFrequency";

            case PrivateTag.DASRegXm:
                return "DASRegXm";

            case PrivateTag.DASAutoZero:
                return "DASAutoZero";

            case PrivateTag.StartingChannelOfView:
                return "StartingChannelOfView";

            case PrivateTag.DASXmPattern:
                return "DASXmPattern";

            case PrivateTag.TGGCTriggerMode:
                return "TGGCTriggerMode";

            case PrivateTag.StartScanToXrayOnDelay:
                return "StartScanToXrayOnDelay";

            case PrivateTag.DurationOfXrayOn:
                return "DurationOfXrayOn";

            case PrivateTag.SlopInteger10ToSlopInteger17:
                return "SlopInteger10ToSlopInteger17";

            case PrivateTag.ScannerStudyEntityUID:
                return "ScannerStudyEntityUID";

            case PrivateTag.ScannerStudyID:
                return "ScannerStudyID";

            case PrivateTag.RawDataID:
                return "RawDataID";

            case PrivateTag.ReconFilter:
                return "ReconFilter";

            case PrivateTag.MotionCorrectionIndicator:
                return "MotionCorrectionIndicator";

            case PrivateTag.HelicalCorrectionIndicator:
                return "HelicalCorrectionIndicator";

            case PrivateTag.IBOCorrectionIndicator:
                return "IBOCorrectionIndicator";

            case PrivateTag.IXTCorrectionIndicator:
                return "IXTCorrectionIndicator";

            case PrivateTag.QcalCorrectionIndicator:
                return "QcalCorrectionIndicator";

            case PrivateTag.AVCorrectionIndicator:
                return "AVCorrectionIndicator";

            case PrivateTag.LMDKCorrectionIndicator:
                return "LMDKCorrectionIndicator";

            case PrivateTag.DetectorRow:
                return "DetectorRow";

            case PrivateTag.AreaSize:
                return "AreaSize";

            case PrivateTag.AutoMAMode:
                return "AutoMAMode";

            case PrivateTag.ScannerTableEntry:
                return "ScannerTableEntry";

            case PrivateTag.ParadigmName:
                return "ParadigmName";

            case PrivateTag.ParadigmDescription:
                return "ParadigmDescription";

            case PrivateTag.ParadigmUID:
                return "ParadigmUID";

            case PrivateTag.ExperimentType:
                return "ExperimentType";

            case PrivateTag.NumberOfRestVolumes:
                return "NumberOfRestVolumes";

            case PrivateTag.NumberOfActiveVolumes:
                return "NumberOfActiveVolumes";

            case PrivateTag.NumberOfDummyScans:
                return "NumberOfDummyScans";

            case PrivateTag.ApplicationName:
                return "ApplicationName";

            case PrivateTag.ApplicationVersion:
                return "ApplicationVersion";

            case PrivateTag.SlicesPerVolume:
                return "SlicesPerVolume";

            case PrivateTag.ExpectedTimePoints:
                return "ExpectedTimePoints";

            case PrivateTag.RegressorValues:
                return "RegressorValues";

            case PrivateTag.DelayAfterSliceGroup:
                return "DelayAfterSliceGroup";

            case PrivateTag.ReconModeFlagWord:
                return "ReconModeFlagWord";

            case PrivateTag.PACCSpecificInformation:
                return "PACCSpecificInformation";

            case PrivateTag.Reserved:
                return "Reserved";

            case PrivateTag.CoilIDData:
                return "CoilIDData";

            case PrivateTag.GECoilName:
                return "GECoilName";

            case PrivateTag.SystemConfigurationInformation:
                return "SystemConfigurationInformation";

            case PrivateTag.AssetRFactors:
                return "AssetRFactors";

            case PrivateTag.AdditionalAssetData:
                return "AdditionalAssetData";

            case PrivateTag.DebugDataTextFormat:
                return "DebugDataTextFormat";

            case PrivateTag.DebugDataBinaryFormat:
                return "DebugDataBinaryFormat";

            case PrivateTag.PUREAcquisitionCalibrationSeriesUID:
                return "PUREAcquisitionCalibrationSeriesUID";

            case PrivateTag.GoverningBodyDBdtSARDefinition:
                return "GoverningBodyDBdtSARDefinition";

            case PrivateTag.PrivateInPlanePhaseEncodingDirection:
                return "PrivateInPlanePhaseEncodingDirection";

            case PrivateTag.FMRIBinaryDataBlock:
                return "FMRIBinaryDataBlock";

            case PrivateTag.VoxelLocation:
                return "VoxelLocation";

            case PrivateTag.SATBandLocations:
                return "SATBandLocations";

            case PrivateTag.SpectroPrescanValues:
                return "SpectroPrescanValues";

            case PrivateTag.SpectroParameters:
                return "SpectroParameters";

            case PrivateTag.SARDefinition:
                return "SARDefinition";

            case PrivateTag.SARValue:
                return "SARValue";

            case PrivateTag.ImageErrorText:
                return "ImageErrorText";

            case PrivateTag.SpectroQuantitationValues:
                return "SpectroQuantitationValues";

            case PrivateTag.SpectroRatioValues:
                return "SpectroRatioValues";

            case PrivateTag.PrescanReuseString:
                return "PrescanReuseString";

            case PrivateTag.ContentQualification:
                return "ContentQualification";

            case PrivateTag.ImageFilteringParameters:
                return "ImageFilteringParameters";

            case PrivateTag.ASSETAcquisitionCalibrationSeriesUID:
                return "ASSETAcquisitionCalibrationSeriesUID";

            case PrivateTag.ExtendedOptions:
                return "ExtendedOptions";

            case PrivateTag.RxStackIdentification:
                return "RxStackIdentification";
        }
        return "";
    }

}
