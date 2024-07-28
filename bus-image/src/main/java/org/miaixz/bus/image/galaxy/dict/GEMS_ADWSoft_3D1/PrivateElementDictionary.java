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
package org.miaixz.bus.image.galaxy.dict.GEMS_ADWSoft_3D1;

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
