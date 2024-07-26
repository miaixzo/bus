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
package org.miaixz.bus.image.galaxy.dict.Philips_Imaging_DD_002;

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

        case PrivateTag._2001_xx18_:
        case PrivateTag._2001_xx39_:
        case PrivateTag._2001_xxB8_:
            return VR.CS;
        case PrivateTag._2001_xx72_:
        case PrivateTag._2001_xx73_:
            return VR.FL;
        case PrivateTag._2001_xx02_:
        case PrivateTag._2001_xx14_:
        case PrivateTag._2001_xx15_:
        case PrivateTag._2001_xx16_:
        case PrivateTag._2001_xx17_:
        case PrivateTag._2001_xx19_:
        case PrivateTag._2001_xx1A_:
        case PrivateTag._2001_xx1B_:
        case PrivateTag._2001_xx1C_:
        case PrivateTag._2001_xx1D_:
        case PrivateTag._2001_xx1E_:
        case PrivateTag._2001_xx1F_:
        case PrivateTag._2001_xx20_:
        case PrivateTag._2001_xx21_:
        case PrivateTag._2001_xx22_:
        case PrivateTag._2001_xx23_:
        case PrivateTag._2001_xx24_:
        case PrivateTag._2001_xx25_:
        case PrivateTag._2001_xx26_:
        case PrivateTag._2001_xx27_:
        case PrivateTag._2001_xx2C_:
        case PrivateTag._2001_xx2D_:
        case PrivateTag._2001_xx35_:
        case PrivateTag._2001_xx36_:
        case PrivateTag._2001_xx37_:
            return VR.FD;
        case PrivateTag._2001_xx6B_:
        case PrivateTag._2001_xx6C_:
            return VR.LO;
        case PrivateTag._2001_xx3A_:
        case PrivateTag._2001_xx3B_:
        case PrivateTag._2001_xx3C_:
        case PrivateTag._2001_xx3D_:
            return VR.SQ;
        case PrivateTag._2001_xx13_:
        case PrivateTag._2001_xx2B_:
        case PrivateTag._2001_xx2E_:
        case PrivateTag._2001_xx2F_:
        case PrivateTag._2001_xx30_:
        case PrivateTag._2001_xx31_:
        case PrivateTag._2001_xx32_:
        case PrivateTag._2001_xx33_:
        case PrivateTag._2001_xx34_:
        case PrivateTag._2001_xx3E_:
        case PrivateTag._2001_xx3F_:
        case PrivateTag._2001_xx40_:
            return VR.SS;
        case PrivateTag._2001_xx01_:
        case PrivateTag._2001_xx28_:
        case PrivateTag._2001_xx29_:
        case PrivateTag._2001_xx2A_:
            return VR.US;
        }
        return VR.UN;
    }
}
