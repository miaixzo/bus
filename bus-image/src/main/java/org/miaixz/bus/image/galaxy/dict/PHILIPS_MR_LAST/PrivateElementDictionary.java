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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_MR_LAST;

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

        case PrivateTag.MainMagneticField:
        case PrivateTag._0019_xxE5_:
        case PrivateTag._0019_xxc0_:
        case PrivateTag._0019_xxd5_:
        case PrivateTag.PrepulseDelay:
        case PrivateTag._0019_xxe3_:
        case PrivateTag.SliceGap:
        case PrivateTag.StackRadialAngle:
        case PrivateTag._0027_xx12_:
        case PrivateTag._0027_xx13_:
        case PrivateTag._0027_xx14_:
        case PrivateTag._0027_xx15_:
        case PrivateTag.FPMin:
        case PrivateTag.FPMax:
        case PrivateTag.ScaledMinimum:
        case PrivateTag.ScaledMaximum:
        case PrivateTag.WindowMinimum:
        case PrivateTag.WindowMaximum:
        case PrivateTag._0029_xx70_:
        case PrivateTag._0029_xx71_:
        case PrivateTag._0041_xx09_:
            return VR.DS;
        case PrivateTag.FlowCompensation:
        case PrivateTag._0019_xxB7_:
        case PrivateTag._0019_xxE4_:
        case PrivateTag.MinimumRRInterval:
        case PrivateTag.MaximumRRInterval:
        case PrivateTag.NumberOfRejections:
        case PrivateTag.NumberOfRRIntervals:
        case PrivateTag.ArrhythmiaRejection:
        case PrivateTag.CycledMultipleSlice:
        case PrivateTag.REST:
        case PrivateTag.FourierInterpolation:
        case PrivateTag._0019_xxd9_:
        case PrivateTag.Prepulse:
        case PrivateTag._0019_xxe2_:
        case PrivateTag._0021_xx00_:
        case PrivateTag._0021_xx10_:
        case PrivateTag._0021_xx20_:
        case PrivateTag._0029_xx61_:
        case PrivateTag._0029_xx62_:
        case PrivateTag._0029_xx72_:
        case PrivateTag.ViewCenter:
        case PrivateTag.ViewSize:
        case PrivateTag.ViewZoom:
        case PrivateTag.ViewTransform:
            return VR.IS;
        case PrivateTag._0027_xx16_:
        case PrivateTag._0041_xx07_:
            return VR.LO;
        case PrivateTag.WSProtocolString1:
        case PrivateTag.WSProtocolString2:
        case PrivateTag.WSProtocolString3:
        case PrivateTag.WSProtocolString4:
        case PrivateTag._6001_xx00_:
            return VR.LT;
        case PrivateTag._0027_xx00_:
        case PrivateTag._0027_xx11_:
            return VR.US;
        }
        return VR.UN;
    }
}
