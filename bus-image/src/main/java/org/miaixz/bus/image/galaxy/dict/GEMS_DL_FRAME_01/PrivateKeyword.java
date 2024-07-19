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
