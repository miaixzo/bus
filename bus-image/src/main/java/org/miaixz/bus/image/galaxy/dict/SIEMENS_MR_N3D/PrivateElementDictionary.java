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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_N3D;

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

        case PrivateTag.Visible:
        case PrivateTag.TintingEnabled:
        case PrivateTag.PrimaryShowHide:
        case PrivateTag.AlphaDependentFieldmap:
        case PrivateTag.SecondaryShowHide:
        case PrivateTag.SceneInteractionOn:
        case PrivateTag.PlaneEnableGLClip:
        case PrivateTag.PlaneEnabled:
        case PrivateTag.PlaneShowGraphics:
        case PrivateTag.PlaneSingleSelectionMode:
        case PrivateTag.PlaneAlignment:
        case PrivateTag.PlaneSelected:
        case PrivateTag.PlaneMPRLocked:
        case PrivateTag.PlaneScalingDisabled:
        case PrivateTag.OrthoMPRAtBoundingBox:
        case PrivateTag.ClusteringEnabled:
        case PrivateTag.MaskEnabled:
        case PrivateTag.ShowCursor:
        case PrivateTag.ShowLabel:
        case PrivateTag.MeanPlot:
        case PrivateTag.MotionPlot:
        case PrivateTag.ActivateNormallizedCurve:
        case PrivateTag.AutoScale:
        case PrivateTag.Legend:
        case PrivateTag.ScrollBarX:
        case PrivateTag.ScrollBarY:
        case PrivateTag.LineFilled:
        case PrivateTag.ShowMarker:
        case PrivateTag.CurrentActivePlane:
            return VR.CS;
        case PrivateTag.BackgroundColor:
        case PrivateTag.TintingColor:
        case PrivateTag.RGBALUT:
        case PrivateTag.BlendFactor:
        case PrivateTag.PwlVertexIndex:
        case PrivateTag.BoundingBoxColor:
        case PrivateTag.PlaneCenter:
        case PrivateTag.PlaneNormal:
        case PrivateTag.PlaneScale:
        case PrivateTag.PlaneHandleRatio:
        case PrivateTag.PlaneBoundingPoints:
        case PrivateTag.PlaneMotionMatrix:
        case PrivateTag.PlaneShiftVelocity:
        case PrivateTag.PlaneRotateVelocity:
        case PrivateTag.Offset:
        case PrivateTag.ClusterSize:
        case PrivateTag.MaskingRange:
        case PrivateTag.RegistrationMatrix:
        case PrivateTag.FrameAcquitionNumbers:
        case PrivateTag.CurrentFrame:
        case PrivateTag.PlotArea:
        case PrivateTag.PlotTextPosition:
        case PrivateTag.BaseLinePoints:
        case PrivateTag.ActivePoints:
        case PrivateTag.PlotSize:
        case PrivateTag.FixedScale:
        case PrivateTag.PlotPosition:
        case PrivateTag.CurveValues:
        case PrivateTag.LineColor:
        case PrivateTag.MarkerColor:
        case PrivateTag.MarkerSize:
        case PrivateTag.LineWidth:
            return VR.DS;
        case PrivateTag.VolumeID:
        case PrivateTag.VolumeIDAsBound:
        case PrivateTag.VolumeFilter:
        case PrivateTag.PrimaryShadingIndex:
        case PrivateTag.SecondaryShadingIndex:
        case PrivateTag.ClusterMaskVolID:
        case PrivateTag.Title:
        case PrivateTag.LabelX:
        case PrivateTag.LabelY:
        case PrivateTag.ConnectScrollX:
        case PrivateTag.PlotID:
        case PrivateTag.CurveID:
        case PrivateTag.PlotType:
        case PrivateTag.Label:
        case PrivateTag.MarkerShape:
        case PrivateTag.SmoothingAlgo:
        case PrivateTag.LineStyle:
        case PrivateTag.LinePattern:
        case PrivateTag.FilterType:
            return VR.LO;
        case PrivateTag.ColorLUT:
            return VR.OB;
        case PrivateTag.BackgroundColorDRSequence:
        case PrivateTag.FieldMapDRSequence:
        case PrivateTag.FloatingMPRColorLUTDRSequence:
        case PrivateTag.RGBALUTDataSequence:
        case PrivateTag.OrthoMPRColorLUTDRSequence:
        case PrivateTag.VRTColorLUTDRSequence:
        case PrivateTag.PwlTransferFunctionDataSequence:
        case PrivateTag.PwlTransferFunctionSequence:
        case PrivateTag.PwlVertexSequence:
        case PrivateTag.FloatingMPRRenderDRSequence:
        case PrivateTag.OrthoMPRRenderDRSequence:
        case PrivateTag.VRTRenderDRSequence:
        case PrivateTag.ClipPlaneDRSequence:
        case PrivateTag.SplitPlaneDRSequence:
        case PrivateTag.FloatingMPRDRSequence:
        case PrivateTag.OrthoMPRDRSequence:
        case PrivateTag.ClusteringDRSequence:
        case PrivateTag.HeadMaskingDRSequence:
        case PrivateTag.BrainMaskingDRSequence:
        case PrivateTag.MaskingStatusDRSequence:
        case PrivateTag.VRTMaskingDRSequence:
        case PrivateTag.OrthoMPRMaskingDRSequence:
        case PrivateTag.FloatingMPRMaskingDRSequence:
        case PrivateTag.AlignDRSequence:
        case PrivateTag.FunctionalEvaluationDRSequence:
        case PrivateTag.PlotDRSequence:
        case PrivateTag.CurveDRSequence:
        case PrivateTag.VRTFilterDRSequence:
            return VR.SQ;
        }
        return VR.UN;
    }

}
