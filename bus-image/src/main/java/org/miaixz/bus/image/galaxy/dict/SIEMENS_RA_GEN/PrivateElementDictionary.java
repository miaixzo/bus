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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_RA_GEN;

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

        case PrivateTag._0011_xx40_:
        case PrivateTag._0019_xx15_:
            return VR.CS;
        case PrivateTag._0011_xx30_:
        case PrivateTag._0011_xx35_:
        case PrivateTag._0019_xx24_:
            return VR.LO;
        case PrivateTag._0019_xx1F_:
        case PrivateTag._0019_xxFF_:
            return VR.OB;
        case PrivateTag._0011_xx20_:
        case PrivateTag._0011_xx25_:
        case PrivateTag._0011_xx26_:
        case PrivateTag._0019_xx20_:
        case PrivateTag._0019_xx22_:
        case PrivateTag._0019_xx46_:
        case PrivateTag._0019_xx5E_:
        case PrivateTag._0019_xx60_:
        case PrivateTag._0019_xx62_:
        case PrivateTag._0019_xx66_:
        case PrivateTag._0019_xx68_:
        case PrivateTag._0019_xx6A_:
        case PrivateTag._0019_xx76_:
        case PrivateTag._0019_xx9C_:
        case PrivateTag._0019_xx9E_:
        case PrivateTag._0019_xxA2_:
        case PrivateTag._0019_xxA6_:
        case PrivateTag._0019_xxA7_:
        case PrivateTag._0019_xxA8_:
        case PrivateTag._0019_xxA9_:
        case PrivateTag._0019_xxAA_:
        case PrivateTag._0019_xxAD_:
            return VR.UL;
        case PrivateTag._0019_xx26_:
        case PrivateTag._0019_xx28_:
        case PrivateTag._0019_xx2C_:
        case PrivateTag._0019_xx2A_:
        case PrivateTag._0019_xx2E_:
        case PrivateTag._0019_xx30_:
        case PrivateTag._0019_xx34_:
        case PrivateTag._0019_xx36_:
        case PrivateTag._0019_xx38_:
        case PrivateTag._0019_xx3A_:
        case PrivateTag._0019_xx3C_:
        case PrivateTag._0019_xx3E_:
        case PrivateTag._0019_xx32_:
        case PrivateTag._0019_xx40_:
        case PrivateTag._0019_xx42_:
        case PrivateTag._0019_xx44_:
        case PrivateTag._0019_xx48_:
        case PrivateTag._0019_xx4A_:
        case PrivateTag._0019_xx4C_:
        case PrivateTag._0019_xx50_:
        case PrivateTag._0019_xx52_:
        case PrivateTag._0019_xx54_:
        case PrivateTag._0019_xx56_:
        case PrivateTag._0019_xx58_:
        case PrivateTag._0019_xx5A_:
        case PrivateTag._0019_xx64_:
        case PrivateTag._0019_xx70_:
        case PrivateTag._0019_xx72_:
        case PrivateTag._0019_xx74_:
        case PrivateTag._0019_xx78_:
        case PrivateTag._0019_xx7A_:
        case PrivateTag._0019_xx7C_:
        case PrivateTag._0019_xx7E_:
        case PrivateTag._0019_xx80_:
        case PrivateTag._0019_xx82_:
        case PrivateTag._0019_xx84_:
        case PrivateTag._0019_xx86_:
        case PrivateTag._0019_xx88_:
        case PrivateTag._0019_xx8A_:
        case PrivateTag._0019_xx8C_:
        case PrivateTag._0019_xx8E_:
        case PrivateTag._0019_xx92_:
        case PrivateTag._0019_xx96_:
        case PrivateTag._0019_xx98_:
        case PrivateTag._0019_xx9A_:
        case PrivateTag._0019_xx94_:
        case PrivateTag._0019_xxA4_:
        case PrivateTag._0019_xxA5_:
        case PrivateTag._0019_xxAB_:
        case PrivateTag._0019_xxAC_:
        case PrivateTag._0021_xx15_:
        case PrivateTag._0021_xx20_:
        case PrivateTag._0021_xx25_:
        case PrivateTag._0021_xx27_:
        case PrivateTag._0021_xx28_:
        case PrivateTag._0021_xx30_:
        case PrivateTag._0021_xx40_:
            return VR.US;
        }
        return VR.UN;
    }
}
