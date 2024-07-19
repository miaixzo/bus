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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CT_APPL_DATASET;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.DualEnergyAlgorithmParameters:
            return "DualEnergyAlgorithmParameters";
        case PrivateTag.ValidCTVolumeMBoxTasks:
            return "ValidCTVolumeMBoxTasks";
        case PrivateTag.ScanOptions:
            return "ScanOptions";
        case PrivateTag.AcquisitionDateandTime:
            return "AcquisitionDateandTime";
        case PrivateTag.AcquisitionNumber:
            return "AcquisitionNumber";
        case PrivateTag.DynamicData:
            return "DynamicData";
        case PrivateTag.ImageOrientationPatient:
            return "ImageOrientationPatient";
        case PrivateTag.FrameOfReferenceUid:
            return "FrameOfReferenceUid";
        case PrivateTag.PatientPosition:
            return "PatientPosition";
        case PrivateTag.ConvolutionKernel:
            return "ConvolutionKernel";
        case PrivateTag.Kvp:
            return "Kvp";
        case PrivateTag.ReconstructionDiameter:
            return "ReconstructionDiameter";
        case PrivateTag.RescaleIntercept:
            return "RescaleIntercept";
        case PrivateTag.RescaleSlope:
            return "RescaleSlope";
        case PrivateTag.SliceThickness:
            return "SliceThickness";
        case PrivateTag.TableHeight:
            return "TableHeight";
        case PrivateTag.GantryDetectorTilt:
            return "GantryDetectorTilt";
        case PrivateTag.PixelSpacing:
            return "PixelSpacing";
        case PrivateTag.VolumePatientPositionNotEqual:
            return "VolumePatientPositionNotEqual";
        case PrivateTag.VolumeLossyImageCompressionNotEqual:
            return "VolumeLossyImageCompressionNotEqual";
        case PrivateTag.VolumeConvolutionKernelNotEqual:
            return "VolumeConvolutionKernelNotEqual";
        case PrivateTag.VolumePixelSpacingNotEqual:
            return "VolumePixelSpacingNotEqual";
        case PrivateTag.VolumeKvpNotEqual:
            return "VolumeKvpNotEqual";
        case PrivateTag.VolumeReconstructionDiameterNotEqual:
            return "VolumeReconstructionDiameterNotEqual";
        case PrivateTag.VolumeTableHeightNotEqual:
            return "VolumeTableHeightNotEqual";
        case PrivateTag.VolumeHasGaps:
            return "VolumeHasGaps";
        case PrivateTag.VolumeNumberOfMissingImages:
            return "VolumeNumberOfMissingImages";
        case PrivateTag.VolumeMaxGap:
            return "VolumeMaxGap";
        case PrivateTag.VolumePositionOfGaps:
            return "VolumePositionOfGaps";
        case PrivateTag.CalibrationFactor:
            return "CalibrationFactor";
        case PrivateTag.FlashMode:
            return "FlashMode";
        case PrivateTag.Warnings:
            return "Warnings";
        case PrivateTag.VolumeHighBitNotEqual:
            return "VolumeHighBitNotEqual";
        case PrivateTag.VolumeImageTypeNotEqual:
            return "VolumeImageTypeNotEqual";
        case PrivateTag.ImageType0:
            return "ImageType0";
        case PrivateTag.ImageType1:
            return "ImageType1";
        case PrivateTag.ImageType2:
            return "ImageType2";
        case PrivateTag.ImageType3:
            return "ImageType3";
        case PrivateTag.PhotometricInterpretationNotMONOCHROME2:
            return "PhotometricInterpretationNotMONOCHROME2";
        case PrivateTag.FirstAcquisitionDate:
            return "FirstAcquisitionDate";
        case PrivateTag.LastAcquisitionDate:
            return "LastAcquisitionDate";
        case PrivateTag.FirstAcquisitionTime:
            return "FirstAcquisitionTime";
        case PrivateTag.LastAcquisitionTime:
            return "LastAcquisitionTime";
        case PrivateTag.InternalData:
            return "InternalData";
        case PrivateTag.RangesSOM7:
            return "RangesSOM7";
        case PrivateTag.CalculatedGantryDetectorTilt:
            return "CalculatedGantryDetectorTilt";
        case PrivateTag.VolumeSliceDistance:
            return "VolumeSliceDistance";
        case PrivateTag.FirstSliceZCoordinate:
            return "FirstSliceZCoordinate";
        case PrivateTag.LastSliceZCoordinate:
            return "LastSliceZCoordinate";
        case PrivateTag.ContentDateTime:
            return "ContentDateTime";
        case PrivateTag.DeltaTime:
            return "DeltaTime";
        case PrivateTag.FrameCount:
            return "FrameCount";
        }
        return "";
    }

}
