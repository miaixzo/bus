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
package org.miaixz.bus.image.galaxy.dict.Philips_PET_Private_Group;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.PrivateData:
            return "PrivateData";
        case PrivateTag.SUVFactor:
            return "SUVFactor";
        case PrivateTag.OriginalFileName:
            return "OriginalFileName";
        case PrivateTag._7053_xx04_:
            return "_7053_xx04_";
        case PrivateTag.WorklistInfoFileName:
            return "WorklistInfoFileName";
        case PrivateTag._7053_xx06_:
            return "_7053_xx06_";
        case PrivateTag._7053_xx07_:
            return "_7053_xx07_";
        case PrivateTag._7053_xx08_:
            return "_7053_xx08_";
        case PrivateTag.ActivityConcentrationScaleFactor:
            return "ActivityConcentrationScaleFactor";
        case PrivateTag._7053_xx0F_:
            return "_7053_xx0F_";
        case PrivateTag._7053_xx10_:
            return "_7053_xx10_";
        case PrivateTag._7053_xx11_:
            return "_7053_xx11_";
        case PrivateTag._7053_xx12_:
            return "_7053_xx12_";
        case PrivateTag._7053_xx13_:
            return "_7053_xx13_";
        case PrivateTag._7053_xx14_:
            return "_7053_xx14_";
        case PrivateTag._7053_xx15_:
            return "_7053_xx15_";
        case PrivateTag._7053_xx16_:
            return "_7053_xx16_";
        case PrivateTag._7053_xx17_:
            return "_7053_xx17_";
        case PrivateTag._7053_xx18_:
            return "_7053_xx18_";
        case PrivateTag._7053_xxC2_:
            return "_7053_xxC2_";
        }
        return "";
    }

}
