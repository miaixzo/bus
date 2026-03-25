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

            case PrivateTag.XA3DReconstructionAlgorithmVersion:
            case PrivateTag.DLXCalibrationStatus:
                return VR.CS;

            case PrivateTag.DLXCalibrationDate:
                return VR.DA;

            case PrivateTag.VolumeVoxelRatio:
            case PrivateTag.VolumeVoxelSize:
            case PrivateTag.VolumeBaseLine:
            case PrivateTag.VolumeCenterPoint:
            case PrivateTag.VolumeRegistrationTransformRotationMatrix:
            case PrivateTag.VolumeRegistrationTransformTranslationVector:
            case PrivateTag.KVPList:
            case PrivateTag.ContrastAgentVolumeList:
            case PrivateTag.TransformRotationMatrix:
            case PrivateTag.TransformTranslationVector:
            case PrivateTag.WireframePointsCoordinates:
            case PrivateTag.VolumeUpperLeftHighCornerRAS:
            case PrivateTag.VolumeSliceToRASRotationMatrix:
            case PrivateTag.VolumeUpperLeftHighCornerTLOC:
                return VR.DS;

            case PrivateTag.XRayTubeCurrentList:
            case PrivateTag.ExposureList:
            case PrivateTag.UsedFrames:
                return VR.IS;

            case PrivateTag.AcquisitionDLXIdentifier:
            case PrivateTag.XA3DReconstructionAlgorithmName:
            case PrivateTag.TransformLabel:
            case PrivateTag.WireframeName:
            case PrivateTag.WireframeGroupName:
            case PrivateTag.WireframeColor:
                return VR.LO;

            case PrivateTag.VolumeSegmentList:
            case PrivateTag.VolumeGradientList:
            case PrivateTag.VolumeDensityList:
            case PrivateTag.VolumeZPositionList:
            case PrivateTag.VolumeOriginalIndexList:
                return VR.OB;

            case PrivateTag.VolumeThresholdValue:
            case PrivateTag.VolumeSkewBase:
            case PrivateTag.WireframeAttributes:
            case PrivateTag.WireframePointCount:
            case PrivateTag.WireframeTimestamp:
                return VR.SL;

            case PrivateTag.ReconstructionParametersSequence:
            case PrivateTag.AcquisitionDLX2DSeriesSequence:
            case PrivateTag.TransformSequence:
            case PrivateTag.WireframeList:
            case PrivateTag.WireframePointList:
                return VR.SQ;

            case PrivateTag.DLXCalibrationTime:
                return VR.TM;

            case PrivateTag.VolumeVoxelCount:
            case PrivateTag.VolumeSegmentCount:
                return VR.UL;

            case PrivateTag.VolumeSliceSize:
            case PrivateTag.VolumeSliceCount:
            case PrivateTag.VolumeZPositionSize:
            case PrivateTag.NumberOfInjections:
            case PrivateTag.FrameCount:
            case PrivateTag.TransformCount:
            case PrivateTag.WireframeCount:
            case PrivateTag.LocationSystem:
                return VR.US;
        }
        return VR.UN;
    }

}
