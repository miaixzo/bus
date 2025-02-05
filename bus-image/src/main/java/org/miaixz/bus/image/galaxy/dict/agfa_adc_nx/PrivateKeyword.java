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
package org.miaixz.bus.image.galaxy.dict.agfa_adc_nx;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag._0019_xx07_:
            return "_0019_xx07_";
        case PrivateTag._0019_xx09_:
            return "_0019_xx09_";
        case PrivateTag._0019_xx21_:
            return "_0019_xx21_";
        case PrivateTag._0019_xx28_:
            return "_0019_xx28_";
        case PrivateTag.UserDefinedField1:
            return "UserDefinedField1";
        case PrivateTag.UserDefinedField2:
            return "UserDefinedField2";
        case PrivateTag.UserDefinedField3:
            return "UserDefinedField3";
        case PrivateTag.UserDefinedField4:
            return "UserDefinedField4";
        case PrivateTag.UserDefinedField5:
            return "UserDefinedField5";
        case PrivateTag.CassetteOrientation:
            return "CassetteOrientation";
        case PrivateTag.PlateSensitivity:
            return "PlateSensitivity";
        case PrivateTag.PlateErasability:
            return "PlateErasability";
        case PrivateTag._0019_xxF8_:
            return "_0019_xxF8_";
        case PrivateTag._0019_xxFA_:
            return "_0019_xxFA_";
        case PrivateTag._0019_xxFC_:
            return "_0019_xxFC_";
        case PrivateTag._0019_xxFD_:
            return "_0019_xxFD_";
        case PrivateTag._0019_xxFE_:
            return "_0019_xxFE_";
        }
        return "";
    }

}
