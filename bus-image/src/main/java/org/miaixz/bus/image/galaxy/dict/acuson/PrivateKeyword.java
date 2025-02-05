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
package org.miaixz.bus.image.galaxy.dict.acuson;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag._0009_xx00_:
            return "_0009_xx00_";
        case PrivateTag._0009_xx01_:
            return "_0009_xx01_";
        case PrivateTag._0009_xx02_:
            return "_0009_xx02_";
        case PrivateTag._0009_xx03_:
            return "_0009_xx03_";
        case PrivateTag._0009_xx04_:
            return "_0009_xx04_";
        case PrivateTag._0009_xx05_:
            return "_0009_xx05_";
        case PrivateTag._0009_xx06_:
            return "_0009_xx06_";
        case PrivateTag._0009_xx07_:
            return "_0009_xx07_";
        case PrivateTag._0009_xx08_:
            return "_0009_xx08_";
        case PrivateTag._0009_xx09_:
            return "_0009_xx09_";
        case PrivateTag._0009_xx0a_:
            return "_0009_xx0a_";
        case PrivateTag._0009_xx0b_:
            return "_0009_xx0b_";
        case PrivateTag._0009_xx0c_:
            return "_0009_xx0c_";
        case PrivateTag._0009_xx0d_:
            return "_0009_xx0d_";
        case PrivateTag._0009_xx0e_:
            return "_0009_xx0e_";
        case PrivateTag._0009_xx0f_:
            return "_0009_xx0f_";
        case PrivateTag._0009_xx10_:
            return "_0009_xx10_";
        case PrivateTag._0009_xx11_:
            return "_0009_xx11_";
        case PrivateTag._0009_xx12_:
            return "_0009_xx12_";
        case PrivateTag._0009_xx13_:
            return "_0009_xx13_";
        case PrivateTag._0009_xx14_:
            return "_0009_xx14_";
        case PrivateTag._0009_xx15_:
            return "_0009_xx15_";
        }
        return "";
    }

}
