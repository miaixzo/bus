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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_CT_APPL_DATASET;

/**
 * @author Kimi Liu
 * @since Java 21+
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
