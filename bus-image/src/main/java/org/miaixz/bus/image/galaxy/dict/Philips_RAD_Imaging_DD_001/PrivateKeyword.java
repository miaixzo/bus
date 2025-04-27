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
package org.miaixz.bus.image.galaxy.dict.Philips_RAD_Imaging_DD_001;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag._200B_xx00_:
            return "_200B_xx00_";
        case PrivateTag._200B_xx01_:
            return "_200B_xx01_";
        case PrivateTag._200B_xx02_:
            return "_200B_xx02_";
        case PrivateTag._200B_xx05_:
            return "_200B_xx05_";
        case PrivateTag._200B_xx11_:
            return "_200B_xx11_";
        case PrivateTag._200B_xx27_:
            return "_200B_xx27_";
        case PrivateTag._200B_xx28_:
            return "_200B_xx28_";
        case PrivateTag._200B_xx29_:
            return "_200B_xx29_";
        case PrivateTag._200B_xx2A_:
            return "_200B_xx2A_";
        case PrivateTag._200B_xx2B_:
            return "_200B_xx2B_";
        case PrivateTag._200B_xx2C_:
            return "_200B_xx2C_";
        case PrivateTag._200B_xx2D_:
            return "_200B_xx2D_";
        case PrivateTag._200B_xx3B_:
            return "_200B_xx3B_";
        case PrivateTag._200B_xx40_:
            return "_200B_xx40_";
        case PrivateTag._200B_xx41_:
            return "_200B_xx41_";
        case PrivateTag._200B_xx42_:
            return "_200B_xx42_";
        case PrivateTag._200B_xx43_:
            return "_200B_xx43_";
        case PrivateTag._200B_xx47_:
            return "_200B_xx47_";
        case PrivateTag._200B_xx48_:
            return "_200B_xx48_";
        case PrivateTag._200B_xx4C_:
            return "_200B_xx4C_";
        case PrivateTag._200B_xx4D_:
            return "_200B_xx4D_";
        case PrivateTag._200B_xx4F_:
            return "_200B_xx4F_";
        case PrivateTag._200B_xx52_:
            return "_200B_xx52_";
        }
        return "";
    }

}
