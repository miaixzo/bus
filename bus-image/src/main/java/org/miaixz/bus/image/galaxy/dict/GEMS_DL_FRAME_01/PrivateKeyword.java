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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.FrameID:
                return "FrameID";

            case PrivateTag.DistanceSourceToDetector:
                return "DistanceSourceToDetector";

            case PrivateTag.DistanceSourceToPatient:
                return "DistanceSourceToPatient";

            case PrivateTag.DistanceSourceToSkin:
                return "DistanceSourceToSkin";

            case PrivateTag.PositionerPrimaryAngle:
                return "PositionerPrimaryAngle";

            case PrivateTag.PositionerSecondaryAngle:
                return "PositionerSecondaryAngle";

            case PrivateTag.BeamOrientation:
                return "BeamOrientation";

            case PrivateTag.LArmAngle:
                return "LArmAngle";

            case PrivateTag.FrameSequence:
                return "FrameSequence";

            case PrivateTag.PivotAngle:
                return "PivotAngle";

            case PrivateTag.ArcAngle:
                return "ArcAngle";

            case PrivateTag.TableVerticalPosition:
                return "TableVerticalPosition";

            case PrivateTag.TableLongitudinalPosition:
                return "TableLongitudinalPosition";

            case PrivateTag.TableLateralPosition:
                return "TableLateralPosition";

            case PrivateTag.BeamCoverArea:
                return "BeamCoverArea";

            case PrivateTag.kVPActual:
                return "kVPActual";

            case PrivateTag.mASActual:
                return "mASActual";

            case PrivateTag.PWActual:
                return "PWActual";

            case PrivateTag.KvpCommanded:
                return "KvpCommanded";

            case PrivateTag.MasCommanded:
                return "MasCommanded";

            case PrivateTag.PwCommanded:
                return "PwCommanded";

            case PrivateTag.Grid:
                return "Grid";

            case PrivateTag.SensorFeedback:
                return "SensorFeedback";

            case PrivateTag.TargetEntranceDose:
                return "TargetEntranceDose";

            case PrivateTag.CnrCommanded:
                return "CnrCommanded";

            case PrivateTag.ContrastCommanded:
                return "ContrastCommanded";

            case PrivateTag.EPTActual:
                return "EPTActual";

            case PrivateTag.SpectralFilterZnb:
                return "SpectralFilterZnb";

            case PrivateTag.SpectralFilterWeight:
                return "SpectralFilterWeight";

            case PrivateTag.SpectralFilterDensity:
                return "SpectralFilterDensity";

            case PrivateTag.SpectralFilterThickness:
                return "SpectralFilterThickness";

            case PrivateTag.SpectralFilterStatus:
                return "SpectralFilterStatus";

            case PrivateTag.FOVDimension:
                return "FOVDimension";

            case PrivateTag.FOVOrigin:
                return "FOVOrigin";

            case PrivateTag.CollimatorLeftVerticalEdge:
                return "CollimatorLeftVerticalEdge";

            case PrivateTag.CollimatorRightVerticalEdge:
                return "CollimatorRightVerticalEdge";

            case PrivateTag.CollimatorUpHorizontalEdge:
                return "CollimatorUpHorizontalEdge";

            case PrivateTag.CollimatorLowHorizontalEdge:
                return "CollimatorLowHorizontalEdge";

            case PrivateTag.VerticesPolygonalCollimator:
                return "VerticesPolygonalCollimator";

            case PrivateTag.ContourFilterDistance:
                return "ContourFilterDistance";

            case PrivateTag.ContourFilterAngle:
                return "ContourFilterAngle";

            case PrivateTag.TableRotationStatus:
                return "TableRotationStatus";

            case PrivateTag.InternalLabelFrame:
                return "InternalLabelFrame";
        }
        return "";
    }

}
