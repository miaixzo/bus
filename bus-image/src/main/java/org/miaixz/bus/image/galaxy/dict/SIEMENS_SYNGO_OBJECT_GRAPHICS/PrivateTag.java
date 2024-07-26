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
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SYNGO OBJECT GRAPHICS";

    /** (0071,xx00) VR=SQ VM=1 Graphic Object Sequence */
    public static final int GraphicObjectSequence = 0x00710000;

    /** (0071,xx01) VR=SL VM=1 Fill Style Version */
    public static final int FillStyleVersion = 0x00710001;

    /** (0071,xx02) VR=FL VM=4 Fill Background Color */
    public static final int FillBackgroundColor = 0x00710002;

    /** (0071,xx03) VR=FL VM=4 Fill Foreground Color */
    public static final int FillForegroundColor = 0x00710003;

    /** (0071,xx04) VR=SL VM=1 Fill Mode */
    public static final int FillMode = 0x00710004;

    /** (0071,xx05) VR=OB VM=1 Fill Pattern */
    public static final int FillPattern = 0x00710005;

    /** (0071,xx06) VR=SL VM=1 Line Style Version */
    public static final int LineStyleVersion = 0x00710006;

    /** (0071,xx07) VR=FL VM=4 Line Background Color */
    public static final int LineBackgroundColor = 0x00710007;

    /** (0071,xx08) VR=FL VM=4 Line Foreground Color */
    public static final int LineForegroundColor = 0x00710008;

    /** (0071,xx09) VR=DS VM=1 Line Type */
    public static final int LineType = 0x00710009;

    /** (0071,xx10) VR=DS VM=1 Line Thickness */
    public static final int LineThickness = 0x00710010;

    /** (0071,xx11) VR=DS VM=1 Line Shadow X Offset */
    public static final int LineShadowXOffset = 0x00710011;

    /** (0071,xx12) VR=DS VM=1 Line Shadow Y Offset */
    public static final int LineShadowYOffset = 0x00710012;

    /** (0071,xx13) VR=DS VM=1 Shadow Style */
    public static final int ShadowStyle = 0x00710013;

    /** (0071,xx14) VR=FL VM=4 Shadow Color */
    public static final int ShadowColor = 0x00710014;

    /** (0071,xx15) VR=DS VM=1 Stipple Pattern */
    public static final int StipplePattern = 0x00710015;

    /** (0071,xx16) VR=DS VM=1 Line Anti Aliasing */
    public static final int LineAntiAliasing = 0x00710016;

    /** (0071,xx17) VR=CS VM=1 Line-Z-Blend Flag */
    public static final int LineZBlendFlag = 0x00710017;

    /** (0071,xx18) VR=SL VM=1 Text Style Version */
    public static final int TextStyleVersion = 0x00710018;

    /** (0071,xx19) VR=FL VM=4 Text Color */
    public static final int TextColor = 0x00710019;

    /** (0071,xx20) VR=SL VM=1 Text Horizontal Align */
    public static final int TextHorizontalAlign = 0x00710020;

    /** (0071,xx21) VR=SL VM=1 Text Vertical Align */
    public static final int TextVerticalAlign = 0x00710021;

    /** (0071,xx22) VR=DS VM=1 Text Shadow X Offset */
    public static final int TextShadowXOffset = 0x00710022;

    /** (0071,xx23) VR=DS VM=1 Text Shadow Y Offset */
    public static final int TextShadowYOffset = 0x00710023;

    /** (0071,xx24) VR=SL VM=1 Text Shadow Style */
    public static final int TextShadowStyle = 0x00710024;

    /** (0071,xx25) VR=FL VM=4 Text Shadow Color */
    public static final int TextShadowColor = 0x00710025;

    /** (0071,xx26) VR=CS VM=1-n Text Log Font */
    public static final int TextLogFont = 0x00710026;

    /** (0071,xx27) VR=CS VM=1 Text-Z-Blend Flag */
    public static final int TextZBlendFlag = 0x00710027;

    /** (0071,xx28) VR=OB VM=1 Graphic Bit Mask */
    public static final int GraphicBitMask = 0x00710028;

    /** (0071,xx29) VR=CS VM=1 Visiblility Flag */
    public static final int VisiblilityFlag = 0x00710029;

    /** (0071,xx30) VR=SL VM=1 Graphic Sensitivity */
    public static final int GraphicSensitivity = 0x00710030;

    /** (0071,xx31) VR=SL VM=1 Graphic Pick Mode Type */
    public static final int GraphicPickModeType = 0x00710031;

    /** (0071,xx32) VR=SL VM=1 Graphic Layer */
    public static final int GraphicLayer = 0x00710032;

    /** (0071,xx33) VR=SL VM=1 Graphic Object Version */
    public static final int GraphicObjectVersion = 0x00710033;

    /** (0071,xx34) VR=SL VM=1 Graphic Coordinate System */
    public static final int GraphicCoordinateSystem = 0x00710034;

    /** (0071,xx35) VR=CS VM=1 Graphic Custom Attributes */
    public static final int GraphicCustomAttributes = 0x00710035;

    /** (0071,xx36) VR=CS VM=1 Graphic Custom Attributes Key */
    public static final int GraphicCustomAttributesKey = 0x00710036;

    /** (0071,xx37) VR=CS VM=1 Graphic Custom Attributes Value */
    public static final int GraphicCustomAttributesValue = 0x00710037;

    /** (0071,xx38) VR=CS VM=1 Graphic View Name */
    public static final int GraphicViewName = 0x00710038;

    /** (0071,xx39) VR=DS VM=3 Graphic Data */
    public static final int GraphicData = 0x00710039;

    /** (0071,xx40) VR=CS VM=1 Graphic Type */
    public static final int GraphicType = 0x00710040;

    /** (0071,xx41) VR=US VM=1 Number of Graphic Points */
    public static final int NumberOfGraphicPoints = 0x00710041;

    /** (0071,xx42) VR=DS VM=1 Axis Main Tick Length */
    public static final int AxisMainTickLength = 0x00710042;

    /** (0071,xx43) VR=DS VM=1 Axis Detail Tick Length */
    public static final int AxisDetailTickLength = 0x00710043;

    /** (0071,xx44) VR=DS VM=1 Axis Main Tick Spacing */
    public static final int AxisMainTickSpacing = 0x00710044;

    /** (0071,xx45) VR=DS VM=1-n Axis Detail Tick Spacing */
    public static final int AxisDetailTickSpacing = 0x00710045;

    /** (0071,xx46) VR=DS VM=1 Axis Main Tick Count */
    public static final int AxisMainTickCount = 0x00710046;

    /** (0071,xx47) VR=DS VM=1 Axis Detail Tick Count */
    public static final int AxisDetailTickCount = 0x00710047;

    /** (0071,xx48) VR=SL VM=1 Axis Tick Behavior */
    public static final int AxisTickBehavior = 0x00710048;

    /** (0071,xx49) VR=SL VM=1 Axis Tick Aligment */
    public static final int AxisTickAligment = 0x00710049;

    /** (0071,xx50) VR=DS VM=1 Axis Step */
    public static final int AxisStep = 0x00710050;

    /** (0071,xx51) VR=SL VM=1 Axis Step Index */
    public static final int AxisStepIndex = 0x00710051;

    /** (0071,xx52) VR=CS VM=1 Axis Text Format */
    public static final int AxisTextFormat = 0x00710052;

    /** (0071,xx53) VR=CS VM=1 Axis Show Center Text Flag */
    public static final int AxisShowCenterTextFlag = 0x00710053;

    /** (0071,xx54) VR=CS VM=1 Axis Show Tick Text Flag */
    public static final int AxisShowTickTextFlag = 0x00710054;

    /** (0071,xx55) VR=DS VM=3 Bitmap X Orientation */
    public static final int BitmapXOrientation = 0x00710055;

    /** (0071,xx56) VR=DS VM=3 Bitmap Y Orientation */
    public static final int BitmapYOrientation = 0x00710056;

    /** (0071,xx57) VR=OB VM=1 Graphic Blob */
    public static final int GraphicBlob = 0x00710057;

    /** (0071,xx58) VR=CS VM=1 Graphic Interpolation */
    public static final int GraphicInterpolation = 0x00710058;

    /** (0071,xx59) VR=DS VM=1 Graphic Angle */
    public static final int GraphicAngle = 0x00710059;

    /** (0071,xx60) VR=DS VM=1 Graphic Size */
    public static final int GraphicSize = 0x00710060;

    /** (0071,xx61) VR=CS VM=1 Cut Line Side */
    public static final int CutLineSide = 0x00710061;

    /** (0071,xx62) VR=DS VM=1 Graphic Tip Length */
    public static final int GraphicTipLength = 0x00710062;

    /** (0071,xx63) VR=DS VM=1 Cut Line Arrow Length */
    public static final int CutLineArrowLength = 0x00710063;

    /** (0071,xx64) VR=DS VM=1 Line Gap Length */
    public static final int LineGapLength = 0x00710064;

    /** (0071,xx65) VR=DS VM=1 Graphic Circle Radius */
    public static final int GraphicCircleRadius = 0x00710065;

    /** (0071,xx66) VR=DS VM=1 Line Distance Move */
    public static final int LineDistanceMove = 0x00710066;

    /** (0071,xx67) VR=DS VM=1 Line Marker Length */
    public static final int LineMarkerLength = 0x00710067;

    /** (0071,xx68) VR=DS VM=3 Graphic Center */
    public static final int GraphicCenter = 0x00710068;

    /** (0071,xx69) VR=DS VM=3 Range Center Area Top Left */
    public static final int RangeCenterAreaTopLeft = 0x00710069;

    /** (0071,xx70) VR=DS VM=3 Range Center Area Bottom Right */
    public static final int RangeCenterAreaBottomRight = 0x00710070;

    /** (0071,xx71) VR=DS VM=1 Range Tilt */
    public static final int RangeTilt = 0x00710071;

    /** (0071,xx72) VR=DS VM=1 Range Minimum Tilt */
    public static final int RangeMinimumTilt = 0x00710072;

    /** (0071,xx73) VR=DS VM=1 Range Maximum Tilt */
    public static final int RangeMaximumTilt = 0x00710073;

    /** (0071,xx74) VR=DS VM=1 Graphic Width */
    public static final int GraphicWidth = 0x00710074;

    /** (0071,xx75) VR=DS VM=1 Range Minimum Width */
    public static final int RangeMinimumWidth = 0x00710075;

    /** (0071,xx76) VR=DS VM=1 Range Maximum Width */
    public static final int RangeMaximumWidth = 0x00710076;

    /** (0071,xx77) VR=DS VM=1 Graphic Height */
    public static final int GraphicHeight = 0x00710077;

    /** (0071,xx78) VR=DS VM=1 Range Feed */
    public static final int RangeFeed = 0x00710078;

    /** (0071,xx79) VR=CS VM=1 Range Direction */
    public static final int RangeDirection = 0x00710079;

    /** (0071,xx80) VR=CS VM=1 Range Show Scans */
    public static final int RangeShowScans = 0x00710080;

    /** (0071,xx81) VR=DS VM=1 Range Minimum Scan Distance */
    public static final int RangeMinimumScanDistance = 0x00710081;

    /** (0071,xx82) VR=CS VM=1 Range Orthogonal Height */
    public static final int RangeOrthogonalHeight = 0x00710082;

    /** (0071,xx83) VR=DS VM=3 Graphic Position */
    public static final int GraphicPosition = 0x00710083;

    /** (0071,xx84) VR=CS VM=1 Graphic Closed Flag */
    public static final int GraphicClosedFlag = 0x00710084;

    /** (0071,xx85) VR=SL VM=1 Range Line Tip Mode */
    public static final int RangeLineTipMode = 0x00710085;

    /** (0071,xx86) VR=SL VM=1 Graphic List Count */
    public static final int GraphicListCount = 0x00710086;

    /** (0071,xx87) VR=CS VM=1 Axis Flip Text Flag */
    public static final int AxisFlipTextFlag = 0x00710087;

    /** (0071,xx88) VR=CS VM=1 Curve Diagram Type */
    public static final int CurveDiagramType = 0x00710088;

    /** (0071,xx89) VR=DS VM=1 Graphic Start Angle */
    public static final int GraphicStartAngle = 0x00710089;

    /** (0071,xx90) VR=DS VM=1 Graphic End Angle */
    public static final int GraphicEndAngle = 0x00710090;

    /** (0071,xx91) VR=IS VM=1 Live Wire Smoothness */
    public static final int LiveWireSmoothness = 0x00710091;

    /** (0071,xx92) VR=CS VM=1 Live Wire Spline Flag */
    public static final int LiveWireSplineFlag = 0x00710092;

    /** (0071,xx93) VR=CS VM=1 Ellipse Circle Flag */
    public static final int EllipseCircleFlag = 0x00710093;

    /** (0071,xx94) VR=CS VM=1 Graphic Square Flag */
    public static final int GraphicSquareFlag = 0x00710094;

    /** (0071,xx95) VR=DS VM=1 Curve Section Start Index */
    public static final int CurveSectionStartIndex = 0x00710095;

    /** (0071,xx96) VR=DS VM=1 Curve Section End Index */
    public static final int CurveSectionEndIndex = 0x00710096;

    /** (0071,xx97) VR=DS VM=1 Marker Alpha */
    public static final int MarkerAlpha = 0x00710097;

    /** (0071,xx98) VR=IS VM=1 Table Row Count */
    public static final int TableRowCount = 0x00710098;

    /** (0071,xx99) VR=IS VM=1 Table Column Count */
    public static final int TableColumnCount = 0x00710099;

    /** (0071,xx9A) VR=DS VM=1 Table Row Height */
    public static final int TableRowHeight = 0x0071009A;

    /** (0071,xx9B) VR=DS VM=1 Table Column Width */
    public static final int TableColumnWidth = 0x0071009B;

    /** (0071,xx9C) VR=IS VM=1 Rectangle Selection Segment Offset */
    public static final int RectangleSelectionSegmentOffset = 0x0071009C;

    /** (0071,xx9D) VR=CS VM=1 Graphic Text */
    public static final int GraphicText = 0x0071009D;

    /** (0071,xxA0) VR=SL VM=1 Axis Tick Spacing Coordinate System */
    public static final int AxisTickSpacingCoordinateSystem = 0x007100A0;

    /** (0071,xxA1) VR=CS VM=1 Axis Diagram Grid Type */
    public static final int AxisDiagramGridType = 0x007100A1;

    /** (0071,xxA2) VR=SL VM=1 Polar Plot Circle Count */
    public static final int PolarPlotCircleCount = 0x007100A2;

    /** (0071,xxA3) VR=SL VM=1 Polar Plot Lines-per-Circle */
    public static final int PolarPlotLinesPerCircle = 0x007100A3;

    /** (0071,xxA4) VR=SL VM=1 Polar Plot Compartment Count */
    public static final int PolarPlotCompartmentCount = 0x007100A4;

    /** (0071,xxA5) VR=SL VM=1 Polar Plot Radius Weight */
    public static final int PolarPlotRadiusWeight = 0x007100A5;

    /** (0071,xxA6) VR=DS VM=1 Circle Segment Outer Radius */
    public static final int CircleSegmentOuterRadius = 0x007100A6;

    /** (0071,xxA7) VR=CS VM=1 Circle Segment Clockwise Flag */
    public static final int CircleSegmentClockwiseFlag = 0x007100A7;

    /** (0071,xxA8) VR=CS VM=1 Axis Diagram Auto Resize Flag */
    public static final int AxisDiagramAutoResizeFlag = 0x007100A8;

    /** (0071,xxA9) VR=DS VM=1 Axis Diagram Step Start */
    public static final int AxisDiagramStepStart = 0x007100A9;

    /** (0071,xxB0) VR=CS VM=1 Group Root */
    public static final int GroupRoot = 0x007100B0;

    /** (0071,xxB1) VR=ST VM=1 Group Name */
    public static final int GroupName = 0x007100B1;

    /** (0071,xxB2) VR=SQ VM=1 Graphic Annotation Sequence */
    public static final int GraphicAnnotationSequence = 0x007100B2;

    /** (0071,xxB3) VR=SL VM=1 Text Minimum Height */
    public static final int TextMinimumHeight = 0x007100B3;

    /** (0071,xxB4) VR=DS VM=1 Text Font Scaling Factor */
    public static final int TextFontScalingFactor = 0x007100B4;

    /** (0071,xxB5) VR=SL VM=2 Text Maximum Extensions */
    public static final int TextMaximumExtensions = 0x007100B5;

    /** (0071,xxB6) VR=CS VM=1 Text Segment Size */
    public static final int TextSegmentSize = 0x007100B6;

    /** (0071,xxB7) VR=SL VM=1 Graphic Object Reference Label */
    public static final int GraphicObjectReferenceLabel = 0x007100B7;

}
