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
package org.miaixz.bus.image.galaxy.dict.SPI_P_PCR_Release_2;

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

        case PrivateTag._0019_xx40_:
        case PrivateTag._0019_xxA3_:
        case PrivateTag._0019_xxA4_:
        case PrivateTag._0019_xxA5_:
        case PrivateTag._0019_xxA6_:
        case PrivateTag._0019_xxA7_:
        case PrivateTag._0019_xxA8_:
        case PrivateTag._0019_xxA9_:
        case PrivateTag._0019_xxAA_:
        case PrivateTag._0019_xxAB_:
        case PrivateTag._0019_xxAC_:
        case PrivateTag._0019_xxAD_:
        case PrivateTag._0019_xxAE_:
        case PrivateTag._0019_xxB5_:
        case PrivateTag._0019_xxB6_:
        case PrivateTag._0019_xxB8_:
            return VR.DS;
        case PrivateTag._0019_xx20_:
        case PrivateTag._0019_xxB2_:
        case PrivateTag._0019_xxB3_:
        case PrivateTag._0019_xxB4_:
            return VR.IS;
        case PrivateTag._0019_xx21_:
        case PrivateTag._0019_xx60_:
        case PrivateTag._0019_xx90_:
            return VR.LO;
        case PrivateTag._0019_xxA1_:
        case PrivateTag._0019_xxAF_:
        case PrivateTag._0019_xxB0_:
        case PrivateTag._0019_xxB1_:
        case PrivateTag._0019_xxB7_:
        case PrivateTag._0019_xxB9_:
        case PrivateTag._0019_xxBA_:
            return VR.ST;
        case PrivateTag._0019_xx10_:
        case PrivateTag._0019_xx30_:
        case PrivateTag._0019_xx80_:
            return VR.US;
        }
        return VR.UN;
    }

}
