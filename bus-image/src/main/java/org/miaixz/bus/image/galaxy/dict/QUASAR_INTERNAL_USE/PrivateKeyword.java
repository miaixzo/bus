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
package org.miaixz.bus.image.galaxy.dict.QUASAR_INTERNAL_USE;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.RateVector:
            return "RateVector";
        case PrivateTag.CountVector:
            return "CountVector";
        case PrivateTag.TimeVector:
            return "TimeVector";
        case PrivateTag.AngleVector:
            return "AngleVector";
        case PrivateTag.CameraShape:
            return "CameraShape";
        case PrivateTag.WholeBodySpots:
            return "WholeBodySpots";
        case PrivateTag.WorklistFlag:
            return "WorklistFlag";
        case PrivateTag._0009_xx12_:
            return "_0009_xx12_";
        case PrivateTag.SequenceType:
            return "SequenceType";
        case PrivateTag.SequenceName:
            return "SequenceName";
        case PrivateTag.AverageRRTimeVector:
            return "AverageRRTimeVector";
        case PrivateTag.LowLimitVector:
            return "LowLimitVector";
        case PrivateTag.HighLimitVector:
            return "HighLimitVector";
        case PrivateTag.BeginIndexVector:
            return "BeginIndexVector";
        case PrivateTag.EndIndexVector:
            return "EndIndexVector";
        case PrivateTag.RawTimeVector:
            return "RawTimeVector";
        case PrivateTag.ImageTypeString:
            return "ImageTypeString";
        case PrivateTag._0009_xx1D_:
            return "_0009_xx1D_";
        case PrivateTag._0009_xx1E_:
            return "_0009_xx1E_";
        case PrivateTag._0009_xx22_:
            return "_0009_xx22_";
        case PrivateTag._0009_xx23_:
            return "_0009_xx23_";
        case PrivateTag._0009_xx39_:
            return "_0009_xx39_";
        case PrivateTag._0009_xx40_:
            return "_0009_xx40_";
        case PrivateTag._0009_xx41_:
            return "_0009_xx41_";
        case PrivateTag._0009_xx42_:
            return "_0009_xx42_";
        case PrivateTag._0009_xx44_:
            return "_0009_xx44_";
        case PrivateTag._0009_xx45_:
            return "_0009_xx45_";
        case PrivateTag._0009_xx48_:
            return "_0009_xx48_";
        case PrivateTag._0037_xx10_:
            return "_0037_xx10_";
        case PrivateTag._0037_xx1B_:
            return "_0037_xx1B_";
        case PrivateTag._0037_xx30_:
            return "_0037_xx30_";
        case PrivateTag._0037_xx40_:
            return "_0037_xx40_";
        case PrivateTag._0037_xx50_:
            return "_0037_xx50_";
        case PrivateTag._0037_xx60_:
            return "_0037_xx60_";
        case PrivateTag._0037_xx70_:
            return "_0037_xx70_";
        case PrivateTag._0037_xx71_:
            return "_0037_xx71_";
        case PrivateTag._0037_xx72_:
            return "_0037_xx72_";
        case PrivateTag._0037_xx73_:
            return "_0037_xx73_";
        case PrivateTag._0037_xx78_:
            return "_0037_xx78_";
        case PrivateTag._0037_xx90_:
            return "_0037_xx90_";
        case PrivateTag._0037_xx92_:
            return "_0037_xx92_";
        case PrivateTag._0041_xx01_:
            return "_0041_xx01_";
        }
        return "";
    }

}
