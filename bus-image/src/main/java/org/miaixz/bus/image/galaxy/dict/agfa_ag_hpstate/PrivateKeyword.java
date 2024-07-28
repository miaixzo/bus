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
package org.miaixz.bus.image.galaxy.dict.agfa_ag_hpstate;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag._0011_xx11_:
            return "_0011_xx11_";
        case PrivateTag._0019_xxA0_:
            return "_0019_xxA0_";
        case PrivateTag._0019_xxA1_:
            return "_0019_xxA1_";
        case PrivateTag._0019_xxA2_:
            return "_0019_xxA2_";
        case PrivateTag._0019_xxA3_:
            return "_0019_xxA3_";
        case PrivateTag._0019_xxA4_:
            return "_0019_xxA4_";
        case PrivateTag._0071_xx18_:
            return "_0071_xx18_";
        case PrivateTag._0071_xx19_:
            return "_0071_xx19_";
        case PrivateTag._0071_xx1A_:
            return "_0071_xx1A_";
        case PrivateTag._0071_xx1C_:
            return "_0071_xx1C_";
        case PrivateTag._0071_xx1E_:
            return "_0071_xx1E_";
        case PrivateTag._0071_xx20_:
            return "_0071_xx20_";
        case PrivateTag._0071_xx21_:
            return "_0071_xx21_";
        case PrivateTag._0071_xx22_:
            return "_0071_xx22_";
        case PrivateTag._0071_xx23_:
            return "_0071_xx23_";
        case PrivateTag._0071_xx24_:
            return "_0071_xx24_";
        case PrivateTag._0071_xx2B_:
            return "_0071_xx2B_";
        case PrivateTag._0071_xx2C_:
            return "_0071_xx2C_";
        case PrivateTag._0071_xx2D_:
            return "_0071_xx2D_";
        case PrivateTag._0073_xx23_:
            return "_0073_xx23_";
        case PrivateTag._0073_xx24_:
            return "_0073_xx24_";
        case PrivateTag._0073_xx28_:
            return "_0073_xx28_";
        case PrivateTag._0073_xx80_:
            return "_0073_xx80_";
        case PrivateTag._0075_xx10_:
            return "_0075_xx10_";
        case PrivateTag._0087_xx01_:
            return "_0087_xx01_";
        case PrivateTag._0087_xx02_:
            return "_0087_xx02_";
        case PrivateTag._0087_xx03_:
            return "_0087_xx03_";
        case PrivateTag._0087_xx04_:
            return "_0087_xx04_";
        case PrivateTag._0087_xx05_:
            return "_0087_xx05_";
        case PrivateTag._0087_xx06_:
            return "_0087_xx06_";
        case PrivateTag._0087_xx07_:
            return "_0087_xx07_";
        case PrivateTag._0087_xx08_:
            return "_0087_xx08_";
        }
        return "";
    }

}
