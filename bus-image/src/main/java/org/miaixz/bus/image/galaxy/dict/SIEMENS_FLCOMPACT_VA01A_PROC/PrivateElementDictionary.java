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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_FLCOMPACT_VA01A_PROC;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

        case PrivateTag._0017_xxB0_:
            return VR.DS;
        case PrivateTag._0017_xx0D_:
        case PrivateTag._0017_xx28_:
        case PrivateTag._0017_xx29_:
        case PrivateTag._0017_xx51_:
        case PrivateTag._0017_xx8C_:
        case PrivateTag._0017_xx8D_:
            return VR.FL;
        case PrivateTag._0017_xx0E_:
        case PrivateTag._0017_xx0F_:
        case PrivateTag._0017_xx4E_:
        case PrivateTag._0017_xx4F_:
        case PrivateTag._0017_xxA2_:
        case PrivateTag._0017_xxC0_:
            return VR.LO;
        case PrivateTag._0017_xx5C_:
            return VR.OW;
        case PrivateTag._0017_xx0A_:
        case PrivateTag._0017_xx0B_:
        case PrivateTag._0017_xx0C_:
        case PrivateTag._0017_xx10_:
        case PrivateTag._0017_xx11_:
        case PrivateTag._0017_xx1F_:
        case PrivateTag._0017_xx20_:
        case PrivateTag._0017_xx48_:
        case PrivateTag._0017_xx49_:
        case PrivateTag._0017_xx4D_:
        case PrivateTag._0017_xx50_:
        case PrivateTag._0017_xx52_:
        case PrivateTag._0017_xx53_:
        case PrivateTag._0017_xx54_:
        case PrivateTag._0017_xx55_:
            return VR.SS;
        case PrivateTag._0017_xx14_:
        case PrivateTag._0017_xx16_:
        case PrivateTag._0017_xx17_:
        case PrivateTag._0017_xx18_:
        case PrivateTag._0017_xx19_:
        case PrivateTag._0017_xx1A_:
        case PrivateTag._0017_xx1B_:
        case PrivateTag._0017_xx1C_:
        case PrivateTag._0017_xx1E_:
        case PrivateTag._0017_xx21_:
        case PrivateTag._0017_xx22_:
        case PrivateTag._0017_xx23_:
        case PrivateTag._0017_xx24_:
        case PrivateTag._0017_xx25_:
        case PrivateTag._0017_xx26_:
        case PrivateTag._0017_xx27_:
        case PrivateTag._0017_xx64_:
        case PrivateTag._0017_xx66_:
        case PrivateTag._0017_xx67_:
        case PrivateTag._0017_xx68_:
        case PrivateTag._0017_xx85_:
        case PrivateTag._0017_xx86_:
        case PrivateTag._0017_xx87_:
        case PrivateTag._0017_xx88_:
        case PrivateTag._0017_xx89_:
        case PrivateTag._0017_xx8A_:
        case PrivateTag._0017_xx8B_:
        case PrivateTag._0017_xx8E_:
        case PrivateTag._0017_xx8F_:
        case PrivateTag._0017_xxA0_:
        case PrivateTag._0017_xxA1_:
        case PrivateTag._0017_xxA3_:
        case PrivateTag._0017_xxA4_:
        case PrivateTag._0017_xxA5_:
        case PrivateTag._0017_xxA6_:
        case PrivateTag._0017_xxAA_:
            return VR.US;
        }
        return VR.UN;
    }
}
