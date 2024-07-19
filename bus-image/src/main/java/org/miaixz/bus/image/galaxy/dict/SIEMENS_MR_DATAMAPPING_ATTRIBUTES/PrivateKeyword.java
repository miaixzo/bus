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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_DATAMAPPING_ATTRIBUTES;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.ReprocessingInfo:
            return "ReprocessingInfo";
        case PrivateTag.DataRoleType:
            return "DataRoleType";
        case PrivateTag.DataRoleName:
            return "DataRoleName";
        case PrivateTag.RescanName:
            return "RescanName";
        case PrivateTag._0011_xx05_:
            return "_0011_xx05_";
        case PrivateTag.CardiacTypeName:
            return "CardiacTypeName";
        case PrivateTag.CardiacTypeNameL2:
            return "CardiacTypeNameL2";
        case PrivateTag.MiscIndicator:
            return "MiscIndicator";
        case PrivateTag._0011_xx09_:
            return "_0011_xx09_";
        case PrivateTag._0011_xx0A_:
            return "_0011_xx0A_";
        case PrivateTag._0011_xx0B_:
            return "_0011_xx0B_";
        case PrivateTag.SplitBaggingName:
            return "SplitBaggingName";
        case PrivateTag.SplitSubBaggingName:
            return "SplitSubBaggingName";
        case PrivateTag.StageSubBaggingName:
            return "StageSubBaggingName";
        case PrivateTag.IsInternalDataRole:
            return "IsInternalDataRole";
        }
        return "";
    }

}
