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
