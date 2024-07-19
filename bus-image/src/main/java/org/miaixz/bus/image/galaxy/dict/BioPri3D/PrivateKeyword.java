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
package org.miaixz.bus.image.galaxy.dict.BioPri3D;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag._0011_xx20_:
            return "_0011_xx20_";
        case PrivateTag._0011_xx24_:
            return "_0011_xx24_";
        case PrivateTag._0011_xx30_:
            return "_0011_xx30_";
        case PrivateTag._0011_xx31_:
            return "_0011_xx31_";
        case PrivateTag._0011_xx32_:
            return "_0011_xx32_";
        case PrivateTag._0011_xx39_:
            return "_0011_xx39_";
        case PrivateTag._0011_xx3A_:
            return "_0011_xx3A_";
        case PrivateTag._0011_xxD0_:
            return "_0011_xxD0_";
        case PrivateTag._0011_xxE0_:
            return "_0011_xxE0_";
        case PrivateTag._0011_xxE1_:
            return "_0011_xxE1_";
        case PrivateTag._0011_xxE2_:
            return "_0011_xxE2_";
        case PrivateTag._0011_xxE3_:
            return "_0011_xxE3_";
        case PrivateTag._0011_xxE4_:
            return "_0011_xxE4_";
        case PrivateTag._0011_xxE5_:
            return "_0011_xxE5_";
        case PrivateTag._0063_xx0C_:
            return "_0063_xx0C_";
        case PrivateTag._0063_xx35_:
            return "_0063_xx35_";
        case PrivateTag._0063_xx20_:
            return "_0063_xx20_";
        case PrivateTag._0063_xx21_:
            return "_0063_xx21_";
        }
        return "";
    }

}
