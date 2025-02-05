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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_MR_LAST;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.MainMagneticField:
            return "MainMagneticField";
        case PrivateTag.FlowCompensation:
            return "FlowCompensation";
        case PrivateTag._0019_xxB7_:
            return "_0019_xxB7_";
        case PrivateTag._0019_xxE4_:
            return "_0019_xxE4_";
        case PrivateTag._0019_xxE5_:
            return "_0019_xxE5_";
        case PrivateTag.MinimumRRInterval:
            return "MinimumRRInterval";
        case PrivateTag.MaximumRRInterval:
            return "MaximumRRInterval";
        case PrivateTag.NumberOfRejections:
            return "NumberOfRejections";
        case PrivateTag.NumberOfRRIntervals:
            return "NumberOfRRIntervals";
        case PrivateTag.ArrhythmiaRejection:
            return "ArrhythmiaRejection";
        case PrivateTag._0019_xxc0_:
            return "_0019_xxc0_";
        case PrivateTag.CycledMultipleSlice:
            return "CycledMultipleSlice";
        case PrivateTag.REST:
            return "REST";
        case PrivateTag._0019_xxd5_:
            return "_0019_xxd5_";
        case PrivateTag.FourierInterpolation:
            return "FourierInterpolation";
        case PrivateTag._0019_xxd9_:
            return "_0019_xxd9_";
        case PrivateTag.Prepulse:
            return "Prepulse";
        case PrivateTag.PrepulseDelay:
            return "PrepulseDelay";
        case PrivateTag._0019_xxe2_:
            return "_0019_xxe2_";
        case PrivateTag._0019_xxe3_:
            return "_0019_xxe3_";
        case PrivateTag.WSProtocolString1:
            return "WSProtocolString1";
        case PrivateTag.WSProtocolString2:
            return "WSProtocolString2";
        case PrivateTag.WSProtocolString3:
            return "WSProtocolString3";
        case PrivateTag.WSProtocolString4:
            return "WSProtocolString4";
        case PrivateTag._0021_xx00_:
            return "_0021_xx00_";
        case PrivateTag._0021_xx10_:
            return "_0021_xx10_";
        case PrivateTag._0021_xx20_:
            return "_0021_xx20_";
        case PrivateTag.SliceGap:
            return "SliceGap";
        case PrivateTag.StackRadialAngle:
            return "StackRadialAngle";
        case PrivateTag._0027_xx00_:
            return "_0027_xx00_";
        case PrivateTag._0027_xx11_:
            return "_0027_xx11_";
        case PrivateTag._0027_xx12_:
            return "_0027_xx12_";
        case PrivateTag._0027_xx13_:
            return "_0027_xx13_";
        case PrivateTag._0027_xx14_:
            return "_0027_xx14_";
        case PrivateTag._0027_xx15_:
            return "_0027_xx15_";
        case PrivateTag._0027_xx16_:
            return "_0027_xx16_";
        case PrivateTag.FPMin:
            return "FPMin";
        case PrivateTag.FPMax:
            return "FPMax";
        case PrivateTag.ScaledMinimum:
            return "ScaledMinimum";
        case PrivateTag.ScaledMaximum:
            return "ScaledMaximum";
        case PrivateTag.WindowMinimum:
            return "WindowMinimum";
        case PrivateTag.WindowMaximum:
            return "WindowMaximum";
        case PrivateTag._0029_xx61_:
            return "_0029_xx61_";
        case PrivateTag._0029_xx62_:
            return "_0029_xx62_";
        case PrivateTag._0029_xx70_:
            return "_0029_xx70_";
        case PrivateTag._0029_xx71_:
            return "_0029_xx71_";
        case PrivateTag._0029_xx72_:
            return "_0029_xx72_";
        case PrivateTag.ViewCenter:
            return "ViewCenter";
        case PrivateTag.ViewSize:
            return "ViewSize";
        case PrivateTag.ViewZoom:
            return "ViewZoom";
        case PrivateTag.ViewTransform:
            return "ViewTransform";
        case PrivateTag._0041_xx07_:
            return "_0041_xx07_";
        case PrivateTag._0041_xx09_:
            return "_0041_xx09_";
        case PrivateTag._6001_xx00_:
            return "_6001_xx00_";
        }
        return "";
    }

}
