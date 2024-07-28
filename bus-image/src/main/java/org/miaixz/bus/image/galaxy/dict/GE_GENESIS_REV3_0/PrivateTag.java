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
package org.miaixz.bus.image.galaxy.dict.GE_GENESIS_REV3_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GE_GENESIS_REV3.0";

    /** (0019,xx39) VR=SS VM=1 Axial Type */
    public static final int AxialType = 0x00190039;

    /** (0019,xx8F) VR=SS VM=1 Swap Phase Frequency */
    public static final int SwapPhaseFrequency = 0x0019008F;

    /** (0019,xx9C) VR=SS VM=1 Pulse Sequence Name */
    public static final int PulseSequenceName = 0x0019009C;

    /** (0019,xx9F) VR=SS VM=1 Coil Type */
    public static final int CoilType = 0x0019009F;

    /** (0019,xxA4) VR=SS VM=1 SAT Fat Water Bone */
    public static final int SATFatWaterBone = 0x001900A4;

    /** (0019,xxC0) VR=SS VM=1 Bitmap Of SAT Selections */
    public static final int BitmapOfSATSelections = 0x001900C0;

    /** (0019,xxC1) VR=SS VM=1 Surface Coil Intensity Correction Flag */
    public static final int SurfaceCoilIntensityCorrectionFlag = 0x001900C1;

    /** (0019,xxCB) VR=SS VM=1 Phase Contrast Flow Axis */
    public static final int PhaseContrastFlowAxis = 0x001900CB;

    /** (0019,xxCC) VR=SS VM=1 Phase Contrast Velocity Encoding */
    public static final int PhaseContrastVelocityEncoding = 0x001900CC;

    /** (0019,xxD5) VR=SS VM=1 Fractional Echo */
    public static final int FractionalEcho = 0x001900D5;

    /** (0019,xxD8) VR=SS VM=1 Variable Echo Flag */
    public static final int VariableEchoFlag = 0x001900D8;

    /** (0019,xxD9) VR=DS VM=1 Concatenated Sat */
    public static final int ConcatenatedSat = 0x001900D9;

    /** (0019,xxF2) VR=SS VM=1 Number Of Phases */
    public static final int NumberOfPhases = 0x001900F2;

    /** (0043,xx1E) VR=DS VM=1 Delta Start Time */
    public static final int DeltaStartTime = 0x0043001E;

    /** (0043,xx27) VR=SH VM=1 Scan Pitch Ratio */
    public static final int ScanPitchRatio = 0x00430027;

}
