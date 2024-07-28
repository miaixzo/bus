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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Private_ICS_Release_1_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag._0029_xx00_:
            return "_0029_xx00_";
        case PrivateTag._0029_xx05_:
            return "_0029_xx05_";
        case PrivateTag._0029_xx06_:
            return "_0029_xx06_";
        case PrivateTag._0029_xx20_:
            return "_0029_xx20_";
        case PrivateTag._0029_xx21_:
            return "_0029_xx21_";
        case PrivateTag._0029_xxC0_:
            return "_0029_xxC0_";
        case PrivateTag._0029_xxC1_:
            return "_0029_xxC1_";
        case PrivateTag._0029_xxCB_:
            return "_0029_xxCB_";
        case PrivateTag._0029_xxCC_:
            return "_0029_xxCC_";
        case PrivateTag._0029_xxCD_:
            return "_0029_xxCD_";
        case PrivateTag._0029_xxD0_:
            return "_0029_xxD0_";
        case PrivateTag._0029_xxD1_:
            return "_0029_xxD1_";
        case PrivateTag._0029_xxD2_:
            return "_0029_xxD2_";
        case PrivateTag._0029_xxD3_:
            return "_0029_xxD3_";
        case PrivateTag._0029_xxD4_:
            return "_0029_xxD4_";
        case PrivateTag._0029_xxD5_:
            return "_0029_xxD5_";
        case PrivateTag._0029_xxD6_:
            return "_0029_xxD6_";
        }
        return "";
    }

}
