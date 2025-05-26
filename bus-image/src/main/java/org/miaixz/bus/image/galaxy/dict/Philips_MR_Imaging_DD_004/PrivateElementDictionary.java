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

        case PrivateTag.SpectrumPeakType:
        case PrivateTag.SpectroSIB0Correction:
        case PrivateTag.SpectroComplexComponent:
        case PrivateTag.SpectroDataOrigin:
        case PrivateTag.InPlaneTransforms:
        case PrivateTag.PhysicalQuantityForChemicalShift:
        case PrivateTag.PhysicalQuantitySpatial:
        case PrivateTag.SignalDomainForChemicalShift:
        case PrivateTag.SignalDomainSpatial:
        case PrivateTag.SignalType:
        case PrivateTag.SpectroAdditionalRotations:
        case PrivateTag.SpectroEchoAcquisition:
        case PrivateTag.SpectroFrequencyUnit:
        case PrivateTag.SpectroHiddenLineRemoval:
        case PrivateTag.SpectroScanType:
        case PrivateTag.SpectroSIMode:
        case PrivateTag.VolumeSelection:
        case PrivateTag.SortAttributes:
        case PrivateTag.ImageDisplayDirection:
        case PrivateTag.InsetScanogram:
        case PrivateTag.StackCoilFunction:
        case PrivateTag.ProcessingHistory:
        case PrivateTag.FlowImagesPresent:
        case PrivateTag.MobiviewEnabled:
        case PrivateTag.IViewBoldEnabled:
            return VR.CS;
        case PrivateTag.SpectrumDCLevel:
        case PrivateTag.SpectrumNoiseLevel:
        case PrivateTag.SpectrumBeginTime:
        case PrivateTag.SpectrumEchoTime:
        case PrivateTag.SpectrumInversionTime:
        case PrivateTag.SpectrumPeakIntensity:
        case PrivateTag.SpectrumPeakPhase:
        case PrivateTag.SpectrumPeakPosition:
        case PrivateTag.SpectrumPeakWidth:
        case PrivateTag.SpectroB0EchoTopPosition:
        case PrivateTag.SpectroEchoTopPosition:
        case PrivateTag.PhaseEncodingEchoTopPositions:
        case PrivateTag.ReferenceFrequency:
        case PrivateTag.SampleOffset:
        case PrivateTag.SamplePitch:
        case PrivateTag.SpectroGamma:
        case PrivateTag.SpectroHorizontalShift:
        case PrivateTag.SpectroHorizontalWindow:
        case PrivateTag.SpectroSICSIntervals:
        case PrivateTag.SpectroTurboEchoSpacing:
        case PrivateTag.SpectroVerticalShift:
        case PrivateTag.SpectroVerticalWindow:
        case PrivateTag.SpectroOffset:
        case PrivateTag.SpectrumPitch:
            return VR.FL;
        case PrivateTag.ScanoGramSurveyNumberOfImages:
        case PrivateTag.GeolinkID:
        case PrivateTag.StationNumber:
            return VR.IS;
        case PrivateTag.SpectrumPeakLabel:
        case PrivateTag.SpectroProcessingHistory:
        case PrivateTag.SpectroTitleLine:
        case PrivateTag.AnatomicRegionCodeValue:
            return VR.LO;
        case PrivateTag.PatientNameJobInParams:
            return VR.PN;
        case PrivateTag.NumberOfMFImageObjects:
            return VR.SL;
        case PrivateTag.SpectrumPeak:
        case PrivateTag.SeriesSPMix:
        case PrivateTag.ViewingProtocol:
            return VR.SQ;
        case PrivateTag.SpectrumExtraNumber:
        case PrivateTag.SpectrumKxCoordinate:
        case PrivateTag.SpectrumKyCoordinate:
        case PrivateTag.SpectrumLocationNumber:
        case PrivateTag.SpectrumMixNumber:
        case PrivateTag.SpectrumXCoordinate:
        case PrivateTag.SpectrumYCoordinate:
        case PrivateTag.SpectrumNumber:
        case PrivateTag.SpectrumNumberOfAverages:
        case PrivateTag.SpectrumNumberOfSamples:
        case PrivateTag.SpectrumScanSequenceNumber:
        case PrivateTag.SpectrumNumberOfPeaks:
        case PrivateTag.NumberOfSpectraAcquired:
        case PrivateTag.SearchIntervalForPeaks:
        case PrivateTag.SpectroDisplayRanges:
        case PrivateTag.SpectroNumberOfDisplayRanges:
        case PrivateTag.SpectroNumberOfEchoPulses:
        case PrivateTag.SpectroSpectralBW:
        case PrivateTag.NumberMixesSpectro:
        case PrivateTag.SPMixTResolution:
        case PrivateTag.SPMixKXResolution:
        case PrivateTag.SPMixKYResolution:
        case PrivateTag.SPMixFResolution:
        case PrivateTag.SPMixXResolution:
        case PrivateTag.SPMixYResolution:
        case PrivateTag.SPMixNumberOfSpectraIntended:
        case PrivateTag.SPMixNumberOfAverages:
        case PrivateTag.NumberOfSortAttributes:
        case PrivateTag.DisplayLayoutNumberOfColumns:
        case PrivateTag.DisplayLayoutNumberOfRows:
            return VR.SS;
        case PrivateTag.ViewProcedureString:
            return VR.ST;
        case PrivateTag.NumberOfProcedureCodes:
            return VR.UL;
        }
        return VR.UN;
    }

}
