/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.GEMS_DL_FRAME_01;

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

            case PrivateTag.Grid:
            case PrivateTag.TableRotationStatus:
            case PrivateTag.InternalLabelFrame:
                return VR.CS;

            case PrivateTag.DistanceSourceToDetector:
            case PrivateTag.DistanceSourceToPatient:
            case PrivateTag.DistanceSourceToSkin:
            case PrivateTag.PositionerPrimaryAngle:
            case PrivateTag.PositionerSecondaryAngle:
            case PrivateTag.LArmAngle:
            case PrivateTag.PivotAngle:
            case PrivateTag.ArcAngle:
            case PrivateTag.TableVerticalPosition:
            case PrivateTag.TableLongitudinalPosition:
            case PrivateTag.TableLateralPosition:
            case PrivateTag.kVPActual:
            case PrivateTag.mASActual:
            case PrivateTag.PWActual:
            case PrivateTag.KvpCommanded:
            case PrivateTag.MasCommanded:
            case PrivateTag.PwCommanded:
            case PrivateTag.SensorFeedback:
            case PrivateTag.TargetEntranceDose:
            case PrivateTag.CnrCommanded:
            case PrivateTag.ContrastCommanded:
            case PrivateTag.EPTActual:
            case PrivateTag.SpectralFilterWeight:
            case PrivateTag.SpectralFilterDensity:
                return VR.DS;

            case PrivateTag.FrameID:
            case PrivateTag.BeamOrientation:
            case PrivateTag.BeamCoverArea:
            case PrivateTag.SpectralFilterZnb:
            case PrivateTag.SpectralFilterThickness:
            case PrivateTag.SpectralFilterStatus:
            case PrivateTag.FOVDimension:
            case PrivateTag.FOVOrigin:
            case PrivateTag.CollimatorLeftVerticalEdge:
            case PrivateTag.CollimatorRightVerticalEdge:
            case PrivateTag.CollimatorUpHorizontalEdge:
            case PrivateTag.CollimatorLowHorizontalEdge:
            case PrivateTag.VerticesPolygonalCollimator:
            case PrivateTag.ContourFilterDistance:
                return VR.IS;

            case PrivateTag.FrameSequence:
                return VR.SQ;

            case PrivateTag.ContourFilterAngle:
                return VR.UL;
        }
        return VR.UN;
    }

}
