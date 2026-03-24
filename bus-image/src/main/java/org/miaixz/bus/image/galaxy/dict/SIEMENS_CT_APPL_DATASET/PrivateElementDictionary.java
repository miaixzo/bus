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

            case PrivateTag.FlashMode:
                return VR.CS;

            case PrivateTag.FirstAcquisitionDate:
            case PrivateTag.LastAcquisitionDate:
                return VR.DA;

            case PrivateTag.ImageOrientationPatient:
            case PrivateTag.FirstSliceZCoordinate:
            case PrivateTag.LastSliceZCoordinate:
            case PrivateTag.ContentDateTime:
            case PrivateTag.DeltaTime:
            case PrivateTag.FrameCount:
                return VR.DS;

            case PrivateTag.CalibrationFactor:
                return VR.FD;

            case PrivateTag.DualEnergyAlgorithmParameters:
            case PrivateTag.ScanOptions:
            case PrivateTag.FrameOfReferenceUid:
            case PrivateTag.PatientPosition:
            case PrivateTag.ConvolutionKernel:
            case PrivateTag.Kvp:
            case PrivateTag.ReconstructionDiameter:
            case PrivateTag.RescaleIntercept:
            case PrivateTag.RescaleSlope:
            case PrivateTag.SliceThickness:
            case PrivateTag.TableHeight:
            case PrivateTag.GantryDetectorTilt:
            case PrivateTag.PixelSpacing:
            case PrivateTag.VolumePositionOfGaps:
            case PrivateTag.Warnings:
            case PrivateTag.CalculatedGantryDetectorTilt:
                return VR.LT;

            case PrivateTag.AcquisitionDateandTime:
            case PrivateTag.AcquisitionNumber:
            case PrivateTag.DynamicData:
            case PrivateTag.VolumePatientPositionNotEqual:
            case PrivateTag.VolumeLossyImageCompressionNotEqual:
            case PrivateTag.VolumeConvolutionKernelNotEqual:
            case PrivateTag.VolumePixelSpacingNotEqual:
            case PrivateTag.VolumeKvpNotEqual:
            case PrivateTag.VolumeReconstructionDiameterNotEqual:
            case PrivateTag.VolumeTableHeightNotEqual:
            case PrivateTag.VolumeHasGaps:
            case PrivateTag.VolumeNumberOfMissingImages:
            case PrivateTag.VolumeMaxGap:
            case PrivateTag.VolumeHighBitNotEqual:
            case PrivateTag.VolumeImageTypeNotEqual:
            case PrivateTag.ImageType0:
            case PrivateTag.ImageType1:
            case PrivateTag.ImageType2:
            case PrivateTag.ImageType3:
            case PrivateTag.PhotometricInterpretationNotMONOCHROME2:
            case PrivateTag.InternalData:
            case PrivateTag.RangesSOM7:
            case PrivateTag.VolumeSliceDistance:
                return VR.ST;

            case PrivateTag.FirstAcquisitionTime:
            case PrivateTag.LastAcquisitionTime:
                return VR.TM;

            case PrivateTag.ValidCTVolumeMBoxTasks:
                return VR.US;
        }
        return VR.UN;
    }

}
