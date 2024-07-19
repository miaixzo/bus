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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_OBJECT_GRAPHICS;

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
        
            case PrivateTag.LineZBlendFlag:
            case PrivateTag.TextLogFont:
            case PrivateTag.TextZBlendFlag:
            case PrivateTag.VisiblilityFlag:
            case PrivateTag.GraphicCustomAttributes:
            case PrivateTag.GraphicCustomAttributesKey:
            case PrivateTag.GraphicCustomAttributesValue:
            case PrivateTag.GraphicViewName:
            case PrivateTag.GraphicType:
            case PrivateTag.AxisTextFormat:
            case PrivateTag.AxisShowCenterTextFlag:
            case PrivateTag.AxisShowTickTextFlag:
            case PrivateTag.GraphicInterpolation:
            case PrivateTag.CutLineSide:
            case PrivateTag.RangeDirection:
            case PrivateTag.RangeShowScans:
            case PrivateTag.RangeOrthogonalHeight:
            case PrivateTag.GraphicClosedFlag:
            case PrivateTag.AxisFlipTextFlag:
            case PrivateTag.CurveDiagramType:
            case PrivateTag.LiveWireSplineFlag:
            case PrivateTag.EllipseCircleFlag:
            case PrivateTag.GraphicSquareFlag:
            case PrivateTag.GraphicText:
            case PrivateTag.AxisDiagramGridType:
            case PrivateTag.CircleSegmentClockwiseFlag:
            case PrivateTag.AxisDiagramAutoResizeFlag:
            case PrivateTag.GroupRoot:
            case PrivateTag.TextSegmentSize:
                return VR.CS;
            case PrivateTag.LineType:
            case PrivateTag.LineThickness:
            case PrivateTag.LineShadowXOffset:
            case PrivateTag.LineShadowYOffset:
            case PrivateTag.ShadowStyle:
            case PrivateTag.StipplePattern:
            case PrivateTag.LineAntiAliasing:
            case PrivateTag.TextShadowXOffset:
            case PrivateTag.TextShadowYOffset:
            case PrivateTag.GraphicData:
            case PrivateTag.AxisMainTickLength:
            case PrivateTag.AxisDetailTickLength:
            case PrivateTag.AxisMainTickSpacing:
            case PrivateTag.AxisDetailTickSpacing:
            case PrivateTag.AxisMainTickCount:
            case PrivateTag.AxisDetailTickCount:
            case PrivateTag.AxisStep:
            case PrivateTag.BitmapXOrientation:
            case PrivateTag.BitmapYOrientation:
            case PrivateTag.GraphicAngle:
            case PrivateTag.GraphicSize:
            case PrivateTag.GraphicTipLength:
            case PrivateTag.CutLineArrowLength:
            case PrivateTag.LineGapLength:
            case PrivateTag.GraphicCircleRadius:
            case PrivateTag.LineDistanceMove:
            case PrivateTag.LineMarkerLength:
            case PrivateTag.GraphicCenter:
            case PrivateTag.RangeCenterAreaTopLeft:
            case PrivateTag.RangeCenterAreaBottomRight:
            case PrivateTag.RangeTilt:
            case PrivateTag.RangeMinimumTilt:
            case PrivateTag.RangeMaximumTilt:
            case PrivateTag.GraphicWidth:
            case PrivateTag.RangeMinimumWidth:
            case PrivateTag.RangeMaximumWidth:
            case PrivateTag.GraphicHeight:
            case PrivateTag.RangeFeed:
            case PrivateTag.RangeMinimumScanDistance:
            case PrivateTag.GraphicPosition:
            case PrivateTag.GraphicStartAngle:
            case PrivateTag.GraphicEndAngle:
            case PrivateTag.CurveSectionStartIndex:
            case PrivateTag.CurveSectionEndIndex:
            case PrivateTag.MarkerAlpha:
            case PrivateTag.TableRowHeight:
            case PrivateTag.TableColumnWidth:
            case PrivateTag.CircleSegmentOuterRadius:
            case PrivateTag.AxisDiagramStepStart:
            case PrivateTag.TextFontScalingFactor:
                return VR.DS;
            case PrivateTag.FillBackgroundColor:
            case PrivateTag.FillForegroundColor:
            case PrivateTag.LineBackgroundColor:
            case PrivateTag.LineForegroundColor:
            case PrivateTag.ShadowColor:
            case PrivateTag.TextColor:
            case PrivateTag.TextShadowColor:
                return VR.FL;
            case PrivateTag.LiveWireSmoothness:
            case PrivateTag.TableRowCount:
            case PrivateTag.TableColumnCount:
            case PrivateTag.RectangleSelectionSegmentOffset:
                return VR.IS;
            case PrivateTag.FillPattern:
            case PrivateTag.GraphicBitMask:
            case PrivateTag.GraphicBlob:
                return VR.OB;
            case PrivateTag.FillStyleVersion:
            case PrivateTag.FillMode:
            case PrivateTag.LineStyleVersion:
            case PrivateTag.TextStyleVersion:
            case PrivateTag.TextHorizontalAlign:
            case PrivateTag.TextVerticalAlign:
            case PrivateTag.TextShadowStyle:
            case PrivateTag.GraphicSensitivity:
            case PrivateTag.GraphicPickModeType:
            case PrivateTag.GraphicLayer:
            case PrivateTag.GraphicObjectVersion:
            case PrivateTag.GraphicCoordinateSystem:
            case PrivateTag.AxisTickBehavior:
            case PrivateTag.AxisTickAligment:
            case PrivateTag.AxisStepIndex:
            case PrivateTag.RangeLineTipMode:
            case PrivateTag.GraphicListCount:
            case PrivateTag.AxisTickSpacingCoordinateSystem:
            case PrivateTag.PolarPlotCircleCount:
            case PrivateTag.PolarPlotLinesPerCircle:
            case PrivateTag.PolarPlotCompartmentCount:
            case PrivateTag.PolarPlotRadiusWeight:
            case PrivateTag.TextMinimumHeight:
            case PrivateTag.TextMaximumExtensions:
            case PrivateTag.GraphicObjectReferenceLabel:
                return VR.SL;
            case PrivateTag.GraphicObjectSequence:
            case PrivateTag.GraphicAnnotationSequence:
                return VR.SQ;
            case PrivateTag.GroupName:
                return VR.ST;
            case PrivateTag.NumberOfGraphicPoints:
                return VR.US;
        }
        return VR.UN;
    }
}
