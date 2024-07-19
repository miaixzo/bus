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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.GraphicObjectSequence:
            return "GraphicObjectSequence";
        case PrivateTag.FillStyleVersion:
            return "FillStyleVersion";
        case PrivateTag.FillBackgroundColor:
            return "FillBackgroundColor";
        case PrivateTag.FillForegroundColor:
            return "FillForegroundColor";
        case PrivateTag.FillMode:
            return "FillMode";
        case PrivateTag.FillPattern:
            return "FillPattern";
        case PrivateTag.LineStyleVersion:
            return "LineStyleVersion";
        case PrivateTag.LineBackgroundColor:
            return "LineBackgroundColor";
        case PrivateTag.LineForegroundColor:
            return "LineForegroundColor";
        case PrivateTag.LineType:
            return "LineType";
        case PrivateTag.LineThickness:
            return "LineThickness";
        case PrivateTag.LineShadowXOffset:
            return "LineShadowXOffset";
        case PrivateTag.LineShadowYOffset:
            return "LineShadowYOffset";
        case PrivateTag.ShadowStyle:
            return "ShadowStyle";
        case PrivateTag.ShadowColor:
            return "ShadowColor";
        case PrivateTag.StipplePattern:
            return "StipplePattern";
        case PrivateTag.LineAntiAliasing:
            return "LineAntiAliasing";
        case PrivateTag.LineZBlendFlag:
            return "LineZBlendFlag";
        case PrivateTag.TextStyleVersion:
            return "TextStyleVersion";
        case PrivateTag.TextColor:
            return "TextColor";
        case PrivateTag.TextHorizontalAlign:
            return "TextHorizontalAlign";
        case PrivateTag.TextVerticalAlign:
            return "TextVerticalAlign";
        case PrivateTag.TextShadowXOffset:
            return "TextShadowXOffset";
        case PrivateTag.TextShadowYOffset:
            return "TextShadowYOffset";
        case PrivateTag.TextShadowStyle:
            return "TextShadowStyle";
        case PrivateTag.TextShadowColor:
            return "TextShadowColor";
        case PrivateTag.TextLogFont:
            return "TextLogFont";
        case PrivateTag.TextZBlendFlag:
            return "TextZBlendFlag";
        case PrivateTag.GraphicBitMask:
            return "GraphicBitMask";
        case PrivateTag.VisiblilityFlag:
            return "VisiblilityFlag";
        case PrivateTag.GraphicSensitivity:
            return "GraphicSensitivity";
        case PrivateTag.GraphicPickModeType:
            return "GraphicPickModeType";
        case PrivateTag.GraphicLayer:
            return "GraphicLayer";
        case PrivateTag.GraphicObjectVersion:
            return "GraphicObjectVersion";
        case PrivateTag.GraphicCoordinateSystem:
            return "GraphicCoordinateSystem";
        case PrivateTag.GraphicCustomAttributes:
            return "GraphicCustomAttributes";
        case PrivateTag.GraphicCustomAttributesKey:
            return "GraphicCustomAttributesKey";
        case PrivateTag.GraphicCustomAttributesValue:
            return "GraphicCustomAttributesValue";
        case PrivateTag.GraphicViewName:
            return "GraphicViewName";
        case PrivateTag.GraphicData:
            return "GraphicData";
        case PrivateTag.GraphicType:
            return "GraphicType";
        case PrivateTag.NumberOfGraphicPoints:
            return "NumberOfGraphicPoints";
        case PrivateTag.AxisMainTickLength:
            return "AxisMainTickLength";
        case PrivateTag.AxisDetailTickLength:
            return "AxisDetailTickLength";
        case PrivateTag.AxisMainTickSpacing:
            return "AxisMainTickSpacing";
        case PrivateTag.AxisDetailTickSpacing:
            return "AxisDetailTickSpacing";
        case PrivateTag.AxisMainTickCount:
            return "AxisMainTickCount";
        case PrivateTag.AxisDetailTickCount:
            return "AxisDetailTickCount";
        case PrivateTag.AxisTickBehavior:
            return "AxisTickBehavior";
        case PrivateTag.AxisTickAligment:
            return "AxisTickAligment";
        case PrivateTag.AxisStep:
            return "AxisStep";
        case PrivateTag.AxisStepIndex:
            return "AxisStepIndex";
        case PrivateTag.AxisTextFormat:
            return "AxisTextFormat";
        case PrivateTag.AxisShowCenterTextFlag:
            return "AxisShowCenterTextFlag";
        case PrivateTag.AxisShowTickTextFlag:
            return "AxisShowTickTextFlag";
        case PrivateTag.BitmapXOrientation:
            return "BitmapXOrientation";
        case PrivateTag.BitmapYOrientation:
            return "BitmapYOrientation";
        case PrivateTag.GraphicBlob:
            return "GraphicBlob";
        case PrivateTag.GraphicInterpolation:
            return "GraphicInterpolation";
        case PrivateTag.GraphicAngle:
            return "GraphicAngle";
        case PrivateTag.GraphicSize:
            return "GraphicSize";
        case PrivateTag.CutLineSide:
            return "CutLineSide";
        case PrivateTag.GraphicTipLength:
            return "GraphicTipLength";
        case PrivateTag.CutLineArrowLength:
            return "CutLineArrowLength";
        case PrivateTag.LineGapLength:
            return "LineGapLength";
        case PrivateTag.GraphicCircleRadius:
            return "GraphicCircleRadius";
        case PrivateTag.LineDistanceMove:
            return "LineDistanceMove";
        case PrivateTag.LineMarkerLength:
            return "LineMarkerLength";
        case PrivateTag.GraphicCenter:
            return "GraphicCenter";
        case PrivateTag.RangeCenterAreaTopLeft:
            return "RangeCenterAreaTopLeft";
        case PrivateTag.RangeCenterAreaBottomRight:
            return "RangeCenterAreaBottomRight";
        case PrivateTag.RangeTilt:
            return "RangeTilt";
        case PrivateTag.RangeMinimumTilt:
            return "RangeMinimumTilt";
        case PrivateTag.RangeMaximumTilt:
            return "RangeMaximumTilt";
        case PrivateTag.GraphicWidth:
            return "GraphicWidth";
        case PrivateTag.RangeMinimumWidth:
            return "RangeMinimumWidth";
        case PrivateTag.RangeMaximumWidth:
            return "RangeMaximumWidth";
        case PrivateTag.GraphicHeight:
            return "GraphicHeight";
        case PrivateTag.RangeFeed:
            return "RangeFeed";
        case PrivateTag.RangeDirection:
            return "RangeDirection";
        case PrivateTag.RangeShowScans:
            return "RangeShowScans";
        case PrivateTag.RangeMinimumScanDistance:
            return "RangeMinimumScanDistance";
        case PrivateTag.RangeOrthogonalHeight:
            return "RangeOrthogonalHeight";
        case PrivateTag.GraphicPosition:
            return "GraphicPosition";
        case PrivateTag.GraphicClosedFlag:
            return "GraphicClosedFlag";
        case PrivateTag.RangeLineTipMode:
            return "RangeLineTipMode";
        case PrivateTag.GraphicListCount:
            return "GraphicListCount";
        case PrivateTag.AxisFlipTextFlag:
            return "AxisFlipTextFlag";
        case PrivateTag.CurveDiagramType:
            return "CurveDiagramType";
        case PrivateTag.GraphicStartAngle:
            return "GraphicStartAngle";
        case PrivateTag.GraphicEndAngle:
            return "GraphicEndAngle";
        case PrivateTag.LiveWireSmoothness:
            return "LiveWireSmoothness";
        case PrivateTag.LiveWireSplineFlag:
            return "LiveWireSplineFlag";
        case PrivateTag.EllipseCircleFlag:
            return "EllipseCircleFlag";
        case PrivateTag.GraphicSquareFlag:
            return "GraphicSquareFlag";
        case PrivateTag.CurveSectionStartIndex:
            return "CurveSectionStartIndex";
        case PrivateTag.CurveSectionEndIndex:
            return "CurveSectionEndIndex";
        case PrivateTag.MarkerAlpha:
            return "MarkerAlpha";
        case PrivateTag.TableRowCount:
            return "TableRowCount";
        case PrivateTag.TableColumnCount:
            return "TableColumnCount";
        case PrivateTag.TableRowHeight:
            return "TableRowHeight";
        case PrivateTag.TableColumnWidth:
            return "TableColumnWidth";
        case PrivateTag.RectangleSelectionSegmentOffset:
            return "RectangleSelectionSegmentOffset";
        case PrivateTag.GraphicText:
            return "GraphicText";
        case PrivateTag.AxisTickSpacingCoordinateSystem:
            return "AxisTickSpacingCoordinateSystem";
        case PrivateTag.AxisDiagramGridType:
            return "AxisDiagramGridType";
        case PrivateTag.PolarPlotCircleCount:
            return "PolarPlotCircleCount";
        case PrivateTag.PolarPlotLinesPerCircle:
            return "PolarPlotLinesPerCircle";
        case PrivateTag.PolarPlotCompartmentCount:
            return "PolarPlotCompartmentCount";
        case PrivateTag.PolarPlotRadiusWeight:
            return "PolarPlotRadiusWeight";
        case PrivateTag.CircleSegmentOuterRadius:
            return "CircleSegmentOuterRadius";
        case PrivateTag.CircleSegmentClockwiseFlag:
            return "CircleSegmentClockwiseFlag";
        case PrivateTag.AxisDiagramAutoResizeFlag:
            return "AxisDiagramAutoResizeFlag";
        case PrivateTag.AxisDiagramStepStart:
            return "AxisDiagramStepStart";
        case PrivateTag.GroupRoot:
            return "GroupRoot";
        case PrivateTag.GroupName:
            return "GroupName";
        case PrivateTag.GraphicAnnotationSequence:
            return "GraphicAnnotationSequence";
        case PrivateTag.TextMinimumHeight:
            return "TextMinimumHeight";
        case PrivateTag.TextFontScalingFactor:
            return "TextFontScalingFactor";
        case PrivateTag.TextMaximumExtensions:
            return "TextMaximumExtensions";
        case PrivateTag.TextSegmentSize:
            return "TextSegmentSize";
        case PrivateTag.GraphicObjectReferenceLabel:
            return "GraphicObjectReferenceLabel";
        }
        return "";
    }

}
