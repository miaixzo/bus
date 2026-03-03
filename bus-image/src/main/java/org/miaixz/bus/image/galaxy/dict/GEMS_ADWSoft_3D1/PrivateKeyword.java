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
