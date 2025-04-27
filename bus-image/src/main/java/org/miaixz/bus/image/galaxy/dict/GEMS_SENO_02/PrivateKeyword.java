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
package org.miaixz.bus.image.galaxy.dict.GEMS_SENO_02;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.DigitalSenographConfiguration:
            return "DigitalSenographConfiguration";
        case PrivateTag.SystemSeriesDescription:
            return "SystemSeriesDescription";
        case PrivateTag.Track:
            return "Track";
        case PrivateTag.AES:
            return "AES";
        case PrivateTag.Angulation:
            return "Angulation";
        case PrivateTag.CompressionThickness:
            return "CompressionThickness";
        case PrivateTag.CompressionForce:
            return "CompressionForce";
        case PrivateTag.RealMagnificationFactor:
            return "RealMagnificationFactor";
        case PrivateTag.DisplayedMagnificationFactor:
            return "DisplayedMagnificationFactor";
        case PrivateTag.SenographType:
            return "SenographType";
        case PrivateTag.IntegrationTime:
            return "IntegrationTime";
        case PrivateTag.ROIOriginXY:
            return "ROIOriginXY";
        case PrivateTag.CorrectionType:
            return "CorrectionType";
        case PrivateTag.AcquisitionType:
            return "AcquisitionType";
        case PrivateTag.CCDTemperature:
            return "CCDTemperature";
        case PrivateTag.ReceptorSizeCmXY:
            return "ReceptorSizeCmXY";
        case PrivateTag.ReceptorSizePixelsXY:
            return "ReceptorSizePixelsXY";
        case PrivateTag.Screen:
            return "Screen";
        case PrivateTag.PixelPitchMicrons:
            return "PixelPitchMicrons";
        case PrivateTag.PixelDepthBits:
            return "PixelDepthBits";
        case PrivateTag.BinningFactorXY:
            return "BinningFactorXY";
        case PrivateTag.QuantumGain:
            return "QuantumGain";
        case PrivateTag.ElectronEDURatio:
            return "ElectronEDURatio";
        case PrivateTag.ElectronicGain:
            return "ElectronicGain";
        case PrivateTag.IDSDataBuffer:
            return "IDSDataBuffer";
        case PrivateTag.ClinicalView:
            return "ClinicalView";
        case PrivateTag.BreastLaterality:
            return "BreastLaterality";
        case PrivateTag.MeanOfRawGrayLevels:
            return "MeanOfRawGrayLevels";
        case PrivateTag.MeanOfOffsetGrayLevels:
            return "MeanOfOffsetGrayLevels";
        case PrivateTag.MeanOfCorrectedGrayLevels:
            return "MeanOfCorrectedGrayLevels";
        case PrivateTag.MeanOfRegionGrayLevels:
            return "MeanOfRegionGrayLevels";
        case PrivateTag.MeanOfLogRegionGrayLevels:
            return "MeanOfLogRegionGrayLevels";
        case PrivateTag.StandardDeviationOfRawGrayLevels:
            return "StandardDeviationOfRawGrayLevels";
        case PrivateTag.StandardDeviationOfCorrectedGrayLevels:
            return "StandardDeviationOfCorrectedGrayLevels";
        case PrivateTag.StandardDeviationOfRegionGrayLevels:
            return "StandardDeviationOfRegionGrayLevels";
        case PrivateTag.StandardDeviationOfLogRegionGrayLevels:
            return "StandardDeviationOfLogRegionGrayLevels";
        case PrivateTag.MAOBuffer:
            return "MAOBuffer";
        case PrivateTag.SetNumber:
            return "SetNumber";
        case PrivateTag.WindowingType:
            return "WindowingType";
        case PrivateTag.WindowingParameters:
            return "WindowingParameters";
        case PrivateTag.CrosshairCursorXCoordinates:
            return "CrosshairCursorXCoordinates";
        case PrivateTag.CrosshairCursorYCoordinates:
            return "CrosshairCursorYCoordinates";
        case PrivateTag.ReferenceLandmarkAX3DCoordinates:
            return "ReferenceLandmarkAX3DCoordinates";
        case PrivateTag.ReferenceLandmarkAY3DCoordinates:
            return "ReferenceLandmarkAY3DCoordinates";
        case PrivateTag.ReferenceLandmarkAZ3DCoordinates:
            return "ReferenceLandmarkAZ3DCoordinates";
        case PrivateTag.ReferenceLandmarkAXImageCoordinates:
            return "ReferenceLandmarkAXImageCoordinates";
        case PrivateTag.ReferenceLandmarkAYImageCoordinates:
            return "ReferenceLandmarkAYImageCoordinates";
        case PrivateTag.ReferenceLandmarkBX3DCoordinates:
            return "ReferenceLandmarkBX3DCoordinates";
        case PrivateTag.ReferenceLandmarkBY3DCoordinates:
            return "ReferenceLandmarkBY3DCoordinates";
        case PrivateTag.ReferenceLandmarkBZ3DCoordinates:
            return "ReferenceLandmarkBZ3DCoordinates";
        case PrivateTag.ReferenceLandmarkBXImageCoordinates:
            return "ReferenceLandmarkBXImageCoordinates";
        case PrivateTag.ReferenceLandmarkBYImageCoordinates:
            return "ReferenceLandmarkBYImageCoordinates";
        case PrivateTag.XRaySourceXLocation:
            return "XRaySourceXLocation";
        case PrivateTag.XRaySourceYLocation:
            return "XRaySourceYLocation";
        case PrivateTag.XRaySourceZLocation:
            return "XRaySourceZLocation";
        case PrivateTag.VignetteRows:
            return "VignetteRows";
        case PrivateTag.VignetteColumns:
            return "VignetteColumns";
        case PrivateTag.VignetteBitsAllocated:
            return "VignetteBitsAllocated";
        case PrivateTag.VignetteBitsStored:
            return "VignetteBitsStored";
        case PrivateTag.VignetteHighBit:
            return "VignetteHighBit";
        case PrivateTag.VignettePixelRepresentation:
            return "VignettePixelRepresentation";
        case PrivateTag.VignettePixelData:
            return "VignettePixelData";
        case PrivateTag.RadiologicalThickness:
            return "RadiologicalThickness";
        case PrivateTag.FallbackInstanceUID:
            return "FallbackInstanceUID";
        case PrivateTag.FallbackSeriesUID:
            return "FallbackSeriesUID";
        case PrivateTag.RawDiagnosticLow:
            return "RawDiagnosticLow";
        case PrivateTag.RawDiagnosticHigh:
            return "RawDiagnosticHigh";
        case PrivateTag.Exponent:
            return "Exponent";
        case PrivateTag.ACoefficients:
            return "ACoefficients";
        case PrivateTag.NoiseReductionSensitivity:
            return "NoiseReductionSensitivity";
        case PrivateTag.NoiseReductionThreshold:
            return "NoiseReductionThreshold";
        case PrivateTag.Mu:
            return "Mu";
        case PrivateTag.Threshold:
            return "Threshold";
        case PrivateTag.BreastROIX:
            return "BreastROIX";
        case PrivateTag.BreastROIY:
            return "BreastROIY";
        case PrivateTag.UserWindowCenter:
            return "UserWindowCenter";
        case PrivateTag.UserWindowWidth:
            return "UserWindowWidth";
        case PrivateTag.SegmentationThreshold:
            return "SegmentationThreshold";
        case PrivateTag.DetectorEntranceDose:
            return "DetectorEntranceDose";
        case PrivateTag.AsymmetricalCollimationInformation:
            return "AsymmetricalCollimationInformation";
        case PrivateTag.STXBuffer:
            return "STXBuffer";
        case PrivateTag.ImageCropPoint:
            return "ImageCropPoint";
        case PrivateTag.PremiumViewBeta:
            return "PremiumViewBeta";
        case PrivateTag.SignalAverageFactor:
            return "SignalAverageFactor";
        case PrivateTag.OrganDoseForSourceImages:
            return "OrganDoseForSourceImages";
        case PrivateTag.EntranceDoseInmGyForSourceImages:
            return "EntranceDoseInmGyForSourceImages";
        case PrivateTag.OrganDoseIndGyForCompleteDBTSequence:
            return "OrganDoseIndGyForCompleteDBTSequence";
        case PrivateTag.SOPInstanceUIDForLossyCompression:
            return "SOPInstanceUIDForLossyCompression";
        case PrivateTag.ReconstructionParameters:
            return "ReconstructionParameters";
        case PrivateTag.EntranceDoseIndGyForCompleteDBTSequence:
            return "EntranceDoseIndGyForCompleteDBTSequence";
        case PrivateTag.ReplacementImage:
            return "ReplacementImage";
        case PrivateTag.ReplacemeImageSequence:
            return "ReplacemeImageSequence";
        case PrivateTag.CumulativeOrganDoseIndGy:
            return "CumulativeOrganDoseIndGy";
        case PrivateTag.CumulativeEntranceDoseInmGy:
            return "CumulativeEntranceDoseInmGy";
        case PrivateTag.PaddleProperties:
            return "PaddleProperties";
        }
        return "";
    }

}
