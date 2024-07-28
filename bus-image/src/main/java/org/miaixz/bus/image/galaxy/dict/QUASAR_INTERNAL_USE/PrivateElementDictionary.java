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
package org.miaixz.bus.image.galaxy.dict.QUASAR_INTERNAL_USE;

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

        case PrivateTag._0009_xx40_:
            return VR.DA;
        case PrivateTag._0037_xx92_:
            return VR.DS;
        case PrivateTag._0009_xx22_:
            return VR.FL;
        case PrivateTag._0037_xx71_:
        case PrivateTag._0037_xx73_:
        case PrivateTag._0037_xx78_:
            return VR.FD;
        case PrivateTag._0037_xx90_:
            return VR.IS;
        case PrivateTag._0009_xx12_:
        case PrivateTag.ImageTypeString:
        case PrivateTag._0009_xx42_:
        case PrivateTag._0009_xx45_:
        case PrivateTag._0009_xx48_:
        case PrivateTag._0037_xx1B_:
        case PrivateTag._0037_xx30_:
        case PrivateTag._0037_xx40_:
        case PrivateTag._0037_xx50_:
        case PrivateTag._0037_xx60_:
        case PrivateTag._0037_xx70_:
            return VR.LO;
        case PrivateTag._0009_xx44_:
        case PrivateTag._0037_xx72_:
            return VR.SH;
        case PrivateTag._0037_xx10_:
            return VR.SQ;
        case PrivateTag.SequenceType:
        case PrivateTag.SequenceName:
        case PrivateTag._0009_xx1E_:
            return VR.ST;
        case PrivateTag._0009_xx41_:
            return VR.TM;
        case PrivateTag._0009_xx39_:
            return VR.UI;
        case PrivateTag.RateVector:
        case PrivateTag.CountVector:
        case PrivateTag.TimeVector:
        case PrivateTag.AngleVector:
        case PrivateTag.AverageRRTimeVector:
        case PrivateTag.LowLimitVector:
        case PrivateTag.HighLimitVector:
        case PrivateTag.BeginIndexVector:
        case PrivateTag.EndIndexVector:
        case PrivateTag.RawTimeVector:
            return VR.UL;
        case PrivateTag.CameraShape:
        case PrivateTag.WholeBodySpots:
        case PrivateTag.WorklistFlag:
        case PrivateTag._0009_xx1D_:
        case PrivateTag._0009_xx23_:
            return VR.US;
        case PrivateTag._0041_xx01_:
            return VR.UT;
        }
        return VR.UN;
    }
}
