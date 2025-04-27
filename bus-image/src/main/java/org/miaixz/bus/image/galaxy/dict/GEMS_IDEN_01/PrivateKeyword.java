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
package org.miaixz.bus.image.galaxy.dict.GEMS_IDEN_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.FullFidelity:
            return "FullFidelity";
        case PrivateTag.SuiteId:
            return "SuiteId";
        case PrivateTag.ProductId:
            return "ProductId";
        case PrivateTag._0009_xx17_:
            return "_0009_xx17_";
        case PrivateTag._0009_xx1A_:
            return "_0009_xx1A_";
        case PrivateTag._0009_xx20_:
            return "_0009_xx20_";
        case PrivateTag.ImageActualDate:
            return "ImageActualDate";
        case PrivateTag._0009_xx2F_:
            return "_0009_xx2F_";
        case PrivateTag.ServiceId:
            return "ServiceId";
        case PrivateTag.MobileLocationNumber:
            return "MobileLocationNumber";
        case PrivateTag._0009_xxE2_:
            return "_0009_xxE2_";
        case PrivateTag.EquipmentUID:
            return "EquipmentUID";
        case PrivateTag.GenesisVersionNow:
            return "GenesisVersionNow";
        case PrivateTag.ExamRecordChecksum:
            return "ExamRecordChecksum";
        case PrivateTag.SeriesSuiteID:
            return "SeriesSuiteID";
        case PrivateTag.ActualSeriesDataTimeStamp:
            return "ActualSeriesDataTimeStamp";
        }
        return "";
    }

}
