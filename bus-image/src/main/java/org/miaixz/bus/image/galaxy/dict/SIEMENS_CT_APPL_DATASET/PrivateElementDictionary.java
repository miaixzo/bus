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
