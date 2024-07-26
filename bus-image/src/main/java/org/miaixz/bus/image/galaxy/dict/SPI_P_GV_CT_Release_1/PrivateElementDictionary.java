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
package org.miaixz.bus.image.galaxy.dict.SPI_P_GV_CT_Release_1;

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

        case PrivateTag._0019_xx60_:
        case PrivateTag._0021_xx30_:
        case PrivateTag._0021_xx60_:
        case PrivateTag._0021_xx70_:
        case PrivateTag._0021_xx80_:
        case PrivateTag._0021_xx90_:
        case PrivateTag._0021_xxA1_:
        case PrivateTag._0021_xxA2_:
            return VR.DS;
        case PrivateTag._0019_xx08_:
        case PrivateTag._0019_xx09_:
        case PrivateTag._0019_xx0A_:
        case PrivateTag._0019_xx65_:
        case PrivateTag._0029_xxD0_:
        case PrivateTag._0029_xxD1_:
            return VR.IS;
        case PrivateTag._0009_xx00_:
        case PrivateTag._0009_xx10_:
        case PrivateTag._0009_xx20_:
        case PrivateTag._0009_xx30_:
        case PrivateTag._0009_xx40_:
        case PrivateTag._0009_xx50_:
        case PrivateTag._0009_xx60_:
        case PrivateTag._0009_xx70_:
        case PrivateTag._0009_xx75_:
        case PrivateTag._0009_xx80_:
        case PrivateTag._0009_xx90_:
        case PrivateTag._0019_xx10_:
        case PrivateTag._0019_xx50_:
        case PrivateTag._0019_xx63_:
        case PrivateTag._0019_xx80_:
        case PrivateTag._0019_xx81_:
        case PrivateTag._0019_xx90_:
        case PrivateTag._0019_xxA0_:
        case PrivateTag._0019_xxB0_:
        case PrivateTag._0019_xxB1_:
        case PrivateTag._0021_xx20_:
        case PrivateTag._0021_xx40_:
        case PrivateTag._0021_xx50_:
        case PrivateTag._0021_xxB0_:
        case PrivateTag._0021_xxC0_:
        case PrivateTag._0029_xx10_:
        case PrivateTag._0029_xx80_:
        case PrivateTag._0029_xx90_:
            return VR.LO;
        case PrivateTag._0019_xx70_:
        case PrivateTag._0021_xxA3_:
        case PrivateTag._0021_xxA4_:
            return VR.LT;
        case PrivateTag._0019_xx20_:
            return VR.TM;
        case PrivateTag._0029_xx30_:
        case PrivateTag._0029_xx31_:
        case PrivateTag._0029_xx32_:
        case PrivateTag._0029_xx33_:
            return VR.UL;
        case PrivateTag._0019_xx61_:
        case PrivateTag._0019_xx64_:
        case PrivateTag._0019_xxA1_:
        case PrivateTag._0019_xxA2_:
        case PrivateTag._0019_xxA3_:
        case PrivateTag._0021_xxA0_:
            return VR.US;
        }
        return VR.UN;
    }
}
