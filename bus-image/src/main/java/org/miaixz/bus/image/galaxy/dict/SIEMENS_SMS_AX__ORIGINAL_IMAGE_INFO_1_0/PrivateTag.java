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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__ORIGINAL_IMAGE_INFO_1_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SMS-AX ORIGINAL IMAGE INFO 1.0";

    /** (0025,xx00) VR=US VM=1 View Native */
    public static final int ViewNative = 0x00250000;

    /** (0025,xx01) VR=US VM=1 Original Series Number */
    public static final int OriginalSeriesNumber = 0x00250001;

    /** (0025,xx02) VR=US VM=1 Original Image Number */
    public static final int OriginalImageNumber = 0x00250002;

    /** (0025,xx03) VR=US VM=1 Win Center */
    public static final int WinCenter = 0x00250003;

    /** (0025,xx04) VR=US VM=1 Win Width */
    public static final int WinWidth = 0x00250004;

    /** (0025,xx05) VR=US VM=1 Win Brightness */
    public static final int WinBrightness = 0x00250005;

    /** (0025,xx06) VR=US VM=1 Win Contrast */
    public static final int WinContrast = 0x00250006;

    /** (0025,xx07) VR=US VM=1 Original Frame Number */
    public static final int OriginalFrameNumber = 0x00250007;

    /** (0025,xx08) VR=US VM=1 Original Mask Frame Number */
    public static final int OriginalMaskFrameNumber = 0x00250008;

    /** (0025,xx09) VR=US VM=1 Opac */
    public static final int Opac = 0x00250009;

    /** (0025,xx0A) VR=US VM=1 Original Number of Frames */
    public static final int OriginalNumberofFrames = 0x0025000A;

    /** (0025,xx0B) VR=DS VM=1 Original Scene Duration */
    public static final int OriginalSceneDuration = 0x0025000B;

    /** (0025,xx0C) VR=LO VM=1 Identifier LOID */
    public static final int IdentifierLOID = 0x0025000C;

    /** (0025,xx0D) VR=SS VM=1-n Original Scene VFR Info */
    public static final int OriginalSceneVFRInfo = 0x0025000D;

    /** (0025,xx0E) VR=SS VM=1 Original Frame ECG Position */
    public static final int OriginalFrameECGPosition = 0x0025000E;

    /** (0025,xx0F) VR=SS VM=1 Original ECG 1st Frame Offset */
    public static final int OriginalECG1stFrameOffset = 0x0025000F;

    /** (0025,xx10) VR=SS VM=1 Zoom Flag */
    public static final int ZoomFlag = 0x00250010;

    /** (0025,xx11) VR=US VM=1 Flexible Pixel Shift */
    public static final int FlexiblePixelShift = 0x00250011;

    /** (0025,xx12) VR=US VM=1 Number of Mask Frames */
    public static final int NumberOfMaskFrames = 0x00250012;

    /** (0025,xx13) VR=US VM=1 Number of Fill Frames */
    public static final int NumberOfFillFrames = 0x00250013;

    /** (0025,xx14) VR=IS VM=1 Series Number */
    public static final int SeriesNumber = 0x00250014;

    /** (0025,xx15) VR=IS VM=1 Image Number */
    public static final int ImageNumber = 0x00250015;

    /** (0025,xx16) VR=IS VM=1 Ready Processing Status */
    public static final int ReadyProcessingStatus = 0x00250016;

}
