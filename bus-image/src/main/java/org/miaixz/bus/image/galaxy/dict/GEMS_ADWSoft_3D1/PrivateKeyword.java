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
package org.miaixz.bus.image.galaxy.dict.GEMS_ADWSoft_3D1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.ReconstructionParametersSequence:
            return "ReconstructionParametersSequence";
        case PrivateTag.VolumeVoxelCount:
            return "VolumeVoxelCount";
        case PrivateTag.VolumeSegmentCount:
            return "VolumeSegmentCount";
        case PrivateTag.VolumeSliceSize:
            return "VolumeSliceSize";
        case PrivateTag.VolumeSliceCount:
            return "VolumeSliceCount";
        case PrivateTag.VolumeThresholdValue:
            return "VolumeThresholdValue";
        case PrivateTag.VolumeVoxelRatio:
            return "VolumeVoxelRatio";
        case PrivateTag.VolumeVoxelSize:
            return "VolumeVoxelSize";
        case PrivateTag.VolumeZPositionSize:
            return "VolumeZPositionSize";
        case PrivateTag.VolumeBaseLine:
            return "VolumeBaseLine";
        case PrivateTag.VolumeCenterPoint:
            return "VolumeCenterPoint";
        case PrivateTag.VolumeSkewBase:
            return "VolumeSkewBase";
        case PrivateTag.VolumeRegistrationTransformRotationMatrix:
            return "VolumeRegistrationTransformRotationMatrix";
        case PrivateTag.VolumeRegistrationTransformTranslationVector:
            return "VolumeRegistrationTransformTranslationVector";
        case PrivateTag.KVPList:
            return "KVPList";
        case PrivateTag.XRayTubeCurrentList:
            return "XRayTubeCurrentList";
        case PrivateTag.ExposureList:
            return "ExposureList";
        case PrivateTag.AcquisitionDLXIdentifier:
            return "AcquisitionDLXIdentifier";
        case PrivateTag.AcquisitionDLX2DSeriesSequence:
            return "AcquisitionDLX2DSeriesSequence";
        case PrivateTag.ContrastAgentVolumeList:
            return "ContrastAgentVolumeList";
        case PrivateTag.NumberOfInjections:
            return "NumberOfInjections";
        case PrivateTag.FrameCount:
            return "FrameCount";
        case PrivateTag.XA3DReconstructionAlgorithmName:
            return "XA3DReconstructionAlgorithmName";
        case PrivateTag.XA3DReconstructionAlgorithmVersion:
            return "XA3DReconstructionAlgorithmVersion";
        case PrivateTag.DLXCalibrationDate:
            return "DLXCalibrationDate";
        case PrivateTag.DLXCalibrationTime:
            return "DLXCalibrationTime";
        case PrivateTag.DLXCalibrationStatus:
            return "DLXCalibrationStatus";
        case PrivateTag.UsedFrames:
            return "UsedFrames";
        case PrivateTag.TransformCount:
            return "TransformCount";
        case PrivateTag.TransformSequence:
            return "TransformSequence";
        case PrivateTag.TransformRotationMatrix:
            return "TransformRotationMatrix";
        case PrivateTag.TransformTranslationVector:
            return "TransformTranslationVector";
        case PrivateTag.TransformLabel:
            return "TransformLabel";
        case PrivateTag.WireframeList:
            return "WireframeList";
        case PrivateTag.WireframeCount:
            return "WireframeCount";
        case PrivateTag.LocationSystem:
            return "LocationSystem";
        case PrivateTag.WireframeName:
            return "WireframeName";
        case PrivateTag.WireframeGroupName:
            return "WireframeGroupName";
        case PrivateTag.WireframeColor:
            return "WireframeColor";
        case PrivateTag.WireframeAttributes:
            return "WireframeAttributes";
        case PrivateTag.WireframePointCount:
            return "WireframePointCount";
        case PrivateTag.WireframeTimestamp:
            return "WireframeTimestamp";
        case PrivateTag.WireframePointList:
            return "WireframePointList";
        case PrivateTag.WireframePointsCoordinates:
            return "WireframePointsCoordinates";
        case PrivateTag.VolumeUpperLeftHighCornerRAS:
            return "VolumeUpperLeftHighCornerRAS";
        case PrivateTag.VolumeSliceToRASRotationMatrix:
            return "VolumeSliceToRASRotationMatrix";
        case PrivateTag.VolumeUpperLeftHighCornerTLOC:
            return "VolumeUpperLeftHighCornerTLOC";
        case PrivateTag.VolumeSegmentList:
            return "VolumeSegmentList";
        case PrivateTag.VolumeGradientList:
            return "VolumeGradientList";
        case PrivateTag.VolumeDensityList:
            return "VolumeDensityList";
        case PrivateTag.VolumeZPositionList:
            return "VolumeZPositionList";
        case PrivateTag.VolumeOriginalIndexList:
            return "VolumeOriginalIndexList";
        }
        return "";
    }

}
