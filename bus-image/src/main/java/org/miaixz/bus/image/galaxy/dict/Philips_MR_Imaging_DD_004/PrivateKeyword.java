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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_004;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.SpectrumExtraNumber:
            return "SpectrumExtraNumber";
        case PrivateTag.SpectrumKxCoordinate:
            return "SpectrumKxCoordinate";
        case PrivateTag.SpectrumKyCoordinate:
            return "SpectrumKyCoordinate";
        case PrivateTag.SpectrumLocationNumber:
            return "SpectrumLocationNumber";
        case PrivateTag.SpectrumMixNumber:
            return "SpectrumMixNumber";
        case PrivateTag.SpectrumXCoordinate:
            return "SpectrumXCoordinate";
        case PrivateTag.SpectrumYCoordinate:
            return "SpectrumYCoordinate";
        case PrivateTag.SpectrumDCLevel:
            return "SpectrumDCLevel";
        case PrivateTag.SpectrumNoiseLevel:
            return "SpectrumNoiseLevel";
        case PrivateTag.SpectrumBeginTime:
            return "SpectrumBeginTime";
        case PrivateTag.SpectrumEchoTime:
            return "SpectrumEchoTime";
        case PrivateTag.SpectrumInversionTime:
            return "SpectrumInversionTime";
        case PrivateTag.SpectrumNumber:
            return "SpectrumNumber";
        case PrivateTag.SpectrumNumberOfAverages:
            return "SpectrumNumberOfAverages";
        case PrivateTag.SpectrumNumberOfSamples:
            return "SpectrumNumberOfSamples";
        case PrivateTag.SpectrumScanSequenceNumber:
            return "SpectrumScanSequenceNumber";
        case PrivateTag.SpectrumNumberOfPeaks:
            return "SpectrumNumberOfPeaks";
        case PrivateTag.SpectrumPeak:
            return "SpectrumPeak";
        case PrivateTag.SpectrumPeakIntensity:
            return "SpectrumPeakIntensity";
        case PrivateTag.SpectrumPeakLabel:
            return "SpectrumPeakLabel";
        case PrivateTag.SpectrumPeakPhase:
            return "SpectrumPeakPhase";
        case PrivateTag.SpectrumPeakPosition:
            return "SpectrumPeakPosition";
        case PrivateTag.SpectrumPeakType:
            return "SpectrumPeakType";
        case PrivateTag.SpectrumPeakWidth:
            return "SpectrumPeakWidth";
        case PrivateTag.SpectroSIB0Correction:
            return "SpectroSIB0Correction";
        case PrivateTag.SpectroB0EchoTopPosition:
            return "SpectroB0EchoTopPosition";
        case PrivateTag.SpectroComplexComponent:
            return "SpectroComplexComponent";
        case PrivateTag.SpectroDataOrigin:
            return "SpectroDataOrigin";
        case PrivateTag.SpectroEchoTopPosition:
            return "SpectroEchoTopPosition";
        case PrivateTag.InPlaneTransforms:
            return "InPlaneTransforms";
        case PrivateTag.NumberOfSpectraAcquired:
            return "NumberOfSpectraAcquired";
        case PrivateTag.PhaseEncodingEchoTopPositions:
            return "PhaseEncodingEchoTopPositions";
        case PrivateTag.PhysicalQuantityForChemicalShift:
            return "PhysicalQuantityForChemicalShift";
        case PrivateTag.PhysicalQuantitySpatial:
            return "PhysicalQuantitySpatial";
        case PrivateTag.ReferenceFrequency:
            return "ReferenceFrequency";
        case PrivateTag.SampleOffset:
            return "SampleOffset";
        case PrivateTag.SamplePitch:
            return "SamplePitch";
        case PrivateTag.SearchIntervalForPeaks:
            return "SearchIntervalForPeaks";
        case PrivateTag.SignalDomainForChemicalShift:
            return "SignalDomainForChemicalShift";
        case PrivateTag.SignalDomainSpatial:
            return "SignalDomainSpatial";
        case PrivateTag.SignalType:
            return "SignalType";
        case PrivateTag.SpectroAdditionalRotations:
            return "SpectroAdditionalRotations";
        case PrivateTag.SpectroDisplayRanges:
            return "SpectroDisplayRanges";
        case PrivateTag.SpectroEchoAcquisition:
            return "SpectroEchoAcquisition";
        case PrivateTag.SpectroFrequencyUnit:
            return "SpectroFrequencyUnit";
        case PrivateTag.SpectroGamma:
            return "SpectroGamma";
        case PrivateTag.SpectroHiddenLineRemoval:
            return "SpectroHiddenLineRemoval";
        case PrivateTag.SpectroHorizontalShift:
            return "SpectroHorizontalShift";
        case PrivateTag.SpectroHorizontalWindow:
            return "SpectroHorizontalWindow";
        case PrivateTag.SpectroNumberOfDisplayRanges:
            return "SpectroNumberOfDisplayRanges";
        case PrivateTag.SpectroNumberOfEchoPulses:
            return "SpectroNumberOfEchoPulses";
        case PrivateTag.SpectroProcessingHistory:
            return "SpectroProcessingHistory";
        case PrivateTag.SpectroScanType:
            return "SpectroScanType";
        case PrivateTag.SpectroSICSIntervals:
            return "SpectroSICSIntervals";
        case PrivateTag.SpectroSIMode:
            return "SpectroSIMode";
        case PrivateTag.SpectroSpectralBW:
            return "SpectroSpectralBW";
        case PrivateTag.SpectroTitleLine:
            return "SpectroTitleLine";
        case PrivateTag.SpectroTurboEchoSpacing:
            return "SpectroTurboEchoSpacing";
        case PrivateTag.SpectroVerticalShift:
            return "SpectroVerticalShift";
        case PrivateTag.SpectroVerticalWindow:
            return "SpectroVerticalWindow";
        case PrivateTag.SpectroOffset:
            return "SpectroOffset";
        case PrivateTag.SpectrumPitch:
            return "SpectrumPitch";
        case PrivateTag.VolumeSelection:
            return "VolumeSelection";
        case PrivateTag.NumberMixesSpectro:
            return "NumberMixesSpectro";
        case PrivateTag.SeriesSPMix:
            return "SeriesSPMix";
        case PrivateTag.SPMixTResolution:
            return "SPMixTResolution";
        case PrivateTag.SPMixKXResolution:
            return "SPMixKXResolution";
        case PrivateTag.SPMixKYResolution:
            return "SPMixKYResolution";
        case PrivateTag.SPMixFResolution:
            return "SPMixFResolution";
        case PrivateTag.SPMixXResolution:
            return "SPMixXResolution";
        case PrivateTag.SPMixYResolution:
            return "SPMixYResolution";
        case PrivateTag.SPMixNumberOfSpectraIntended:
            return "SPMixNumberOfSpectraIntended";
        case PrivateTag.SPMixNumberOfAverages:
            return "SPMixNumberOfAverages";
        case PrivateTag.NumberOfMFImageObjects:
            return "NumberOfMFImageObjects";
        case PrivateTag.ScanoGramSurveyNumberOfImages:
            return "ScanoGramSurveyNumberOfImages";
        case PrivateTag.NumberOfProcedureCodes:
            return "NumberOfProcedureCodes";
        case PrivateTag.SortAttributes:
            return "SortAttributes";
        case PrivateTag.NumberOfSortAttributes:
            return "NumberOfSortAttributes";
        case PrivateTag.ImageDisplayDirection:
            return "ImageDisplayDirection";
        case PrivateTag.InsetScanogram:
            return "InsetScanogram";
        case PrivateTag.DisplayLayoutNumberOfColumns:
            return "DisplayLayoutNumberOfColumns";
        case PrivateTag.DisplayLayoutNumberOfRows:
            return "DisplayLayoutNumberOfRows";
        case PrivateTag.ViewingProtocol:
            return "ViewingProtocol";
        case PrivateTag.StackCoilFunction:
            return "StackCoilFunction";
        case PrivateTag.PatientNameJobInParams:
            return "PatientNameJobInParams";
        case PrivateTag.GeolinkID:
            return "GeolinkID";
        case PrivateTag.StationNumber:
            return "StationNumber";
        case PrivateTag.ProcessingHistory:
            return "ProcessingHistory";
        case PrivateTag.ViewProcedureString:
            return "ViewProcedureString";
        case PrivateTag.FlowImagesPresent:
            return "FlowImagesPresent";
        case PrivateTag.AnatomicRegionCodeValue:
            return "AnatomicRegionCodeValue";
        case PrivateTag.MobiviewEnabled:
            return "MobiviewEnabled";
        case PrivateTag.IViewBoldEnabled:
            return "IViewBoldEnabled";
        }
        return "";
    }

}
