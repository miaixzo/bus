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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_DFR_01_ORIGINAL;

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

        case PrivateTag._0017_xx45_:
        case PrivateTag._0017_xx46_:
            return VR.DS;
        case PrivateTag._0017_xx25_:
        case PrivateTag._0017_xx27_:
        case PrivateTag._0017_xx30_:
        case PrivateTag._0017_xx41_:
        case PrivateTag._0017_xx48_:
        case PrivateTag._0017_xx4A_:
        case PrivateTag._0017_xx71_:
        case PrivateTag._0017_xx7B_:
        case PrivateTag._0017_xxA0_:
            return VR.IS;
        case PrivateTag._0017_xx49_:
        case PrivateTag._0017_xxC1_:
        case PrivateTag._0017_xxC2_:
            return VR.LO;
        case PrivateTag._0017_xx47_:
            return VR.SH;
        case PrivateTag._0017_xx52_:
            return VR.UL;
        case PrivateTag._0017_xx11_:
        case PrivateTag._0017_xx12_:
        case PrivateTag._0017_xx14_:
        case PrivateTag._0017_xx15_:
        case PrivateTag._0017_xx16_:
        case PrivateTag._0017_xx18_:
        case PrivateTag._0017_xx21_:
        case PrivateTag._0017_xx22_:
        case PrivateTag._0017_xx23_:
        case PrivateTag._0017_xx24_:
        case PrivateTag._0017_xx26_:
        case PrivateTag._0017_xx2A_:
        case PrivateTag._0017_xx31_:
        case PrivateTag._0017_xx32_:
        case PrivateTag._0017_xx33_:
        case PrivateTag._0017_xx37_:
        case PrivateTag._0017_xx38_:
        case PrivateTag._0017_xx43_:
        case PrivateTag._0017_xx44_:
        case PrivateTag._0017_xx51_:
        case PrivateTag._0017_xx61_:
        case PrivateTag._0017_xx62_:
        case PrivateTag._0017_xx72_:
        case PrivateTag._0017_xx73_:
        case PrivateTag._0017_xx74_:
        case PrivateTag._0017_xx79_:
        case PrivateTag._0017_xx7A_:
            return VR.US;
        }
        return VR.UN;
    }

}
