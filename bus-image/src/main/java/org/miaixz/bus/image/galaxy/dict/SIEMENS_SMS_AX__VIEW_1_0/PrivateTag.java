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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__VIEW_1_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SMS-AX VIEW 1.0";

    /** (0019,xx00) VR=US VM=1 Review Mode */
    public static final int ReviewMode = 0x00190000;

    /** (0019,xx01) VR=US VM=1 Anatomical Background Percent */
    public static final int AnatomicalBackgroundPercent = 0x00190001;

    /** (0019,xx02) VR=US VM=1 Number of Phases */
    public static final int NumberOfPhases = 0x00190002;

    /** (0019,xx03) VR=US VM=1 Apply Anatomical Background */
    public static final int ApplyAnatomicalBackground = 0x00190003;

    /** (0019,xx04) VR=SS VM=4-4n Pixel Shift Array */
    public static final int PixelShiftArray = 0x00190004;

    /** (0019,xx05) VR=US VM=1 Brightness */
    public static final int Brightness = 0x00190005;

    /** (0019,xx06) VR=US VM=1 Contrast */
    public static final int Contrast = 0x00190006;

    /** (0019,xx07) VR=US VM=1 Enabled Shutters */
    public static final int EnabledShutters = 0x00190007;

    /**
     * (0019,xx08) VR=US VM=1 Native Edge Enhancement Percent Gain
     */
    public static final int NativeEdgeEnhancementPercentGain = 0x00190008;

    /** (0019,xx09) VR=SS VM=1 Native Edge Enhancement LUT Index */
    public static final int NativeEdgeEnhancementLUTIndex = 0x00190009;

    /** (0019,xx0A) VR=SS VM=1 Native Edge Enhancement Kernel Size */
    public static final int NativeEdgeEnhancementKernelSize = 0x0019000A;

    /**
     * (0019,xx0B) VR=US VM=1 Subtracted Edge Enhancement Percent Gain
     */
    public static final int SubtractedEdgeEnhancementPercentGain = 0x0019000B;

    /**
     * (0019,xx0C) VR=SS VM=1 Subtracted Edge Enhancement LUT Index
     */
    public static final int SubtractedEdgeEnhancementLUTIndex = 0x0019000C;

    /**
     * (0019,xx0D) VR=SS VM=1 Subtracted Edge Enhancement Kernel Size
     */
    public static final int SubtractedEdgeEnhancementKernelSize = 0x0019000D;

    /** (0019,xx0E) VR=US VM=1 Fade Percent */
    public static final int FadePercent = 0x0019000E;

    /** (0019,xx0F) VR=US VM=1 Flipped Before Laterality Applied */
    public static final int FlippedBeforeLateralityApplied = 0x0019000F;

    /** (0019,xx10) VR=US VM=1 Apply Fade */
    public static final int ApplyFade = 0x00190010;

    /** (0019,xx11) VR=US VM=1 Reference Images Taken Flag */
    public static final int ReferenceImagesTakenFlag = 0x00190011;

    /** (0019,xx12) VR=US VM=1 Zoom */
    public static final int Zoom = 0x00190012;

    /** (0019,xx13) VR=SS VM=1 Pan X */
    public static final int PanX = 0x00190013;

    /** (0019,xx14) VR=SS VM=1 Pan Y */
    public static final int PanY = 0x00190014;

    /**
     * (0019,xx15) VR=SS VM=1 Native Edge Enhancement Adv Percent Gain
     */
    public static final int NativeEdgeEnhancementAdvPercentGain = 0x00190015;

    /**
     * (0019,xx16) VR=SS VM=1 Subtracted Edge Enhancement Adv Percent Gain
     */
    public static final int SubtractedEdgeEnhancementAdvPercentGain = 0x00190016;

    /** (0019,xx17) VR=US VM=1 Invert Flag */
    public static final int InvertFlag = 0x00190017;

    /** (0019,xx1A) VR=OB VM=1 Quant 1K Overlay */
    public static final int Quant1KOverlay = 0x0019001A;

    /** (0019,xx1B) VR=US VM=1 Original Resolution */
    public static final int OriginalResolution = 0x0019001B;

    /** (0019,xx1C) VR=DS VM=1 Auto Window Center */
    public static final int AutoWindowCenter = 0x0019001C;

    /** (0019,xx1D) VR=DS VM=1 Auto Window Width */
    public static final int AutoWindowWidth = 0x0019001D;

    /** (0019,xx1E) VR=IS VM=2 Auto Window Correct Value */
    public static final int AutoWindowCorrectValue = 0x0019001E;

    /** (0019,xx1F) VR=DS VM=1 Sigmoid Window Parameter */
    public static final int SigmoidWindowParameter = 0x0019001F;

    /** (0019,xx41) VR=SL VM=2 Dispayed Area Top Left Hand Corner */
    public static final int DispayedAreaTopLeftHandCorner = 0x00190041;

    /**
     * (0019,xx42) VR=SL VM=2 Dispayed Area Bottom Right Hand Corner
     */
    public static final int DispayedAreaBottomRightHandCorner = 0x00190042;

}
