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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_IMAGE_SHADOW_ATTRIBUTES;

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
        
            case PrivateTag._0021_xx03_:
            case PrivateTag._0021_xx04_:
            case PrivateTag._0021_xx20_:
            case PrivateTag._0021_xx22_:
            case PrivateTag._0021_xx27_:
            case PrivateTag._0021_xx2A_:
            case PrivateTag._0021_xx2B_:
            case PrivateTag._0021_xx2C_:
            case PrivateTag._0021_xx2D_:
            case PrivateTag._0021_xx37_:
            case PrivateTag._0021_xx3A_:
            case PrivateTag._0021_xx40_:
                return VR.DS;
            case PrivateTag._0021_xx02_:
            case PrivateTag._0021_xx34_:
            case PrivateTag._0021_xx3C_:
            case PrivateTag._0021_xx46_:
            case PrivateTag._0021_xx53_:
            case PrivateTag._0021_xx5A_:
            case PrivateTag._0021_xx5B_:
                return VR.FD;
            case PrivateTag._0021_xx05_:
            case PrivateTag._0021_xx1C_:
            case PrivateTag._0021_xx1F_:
            case PrivateTag._0021_xx24_:
            case PrivateTag._0021_xx26_:
            case PrivateTag._0021_xx33_:
            case PrivateTag._0021_xx35_:
            case PrivateTag._0021_xx3B_:
            case PrivateTag._0021_xx3D_:
            case PrivateTag._0021_xx42_:
            case PrivateTag._0021_xx44_:
            case PrivateTag._0021_xx47_:
            case PrivateTag._0021_xx48_:
            case PrivateTag._0021_xx49_:
            case PrivateTag._0021_xx4E_:
            case PrivateTag._0021_xx59_:
            case PrivateTag._0021_xx5E_:
                return VR.IS;
            case PrivateTag._0021_xx06_:
            case PrivateTag._0021_xx4F_:
            case PrivateTag._0021_xx56_:
                return VR.LO;
            case PrivateTag._0021_xx25_:
            case PrivateTag._0021_xx43_:
            case PrivateTag._0021_xx4B_:
                return VR.LT;
            case PrivateTag._0021_xx1A_:
            case PrivateTag._0021_xx3F_:
            case PrivateTag._0021_xx41_:
            case PrivateTag._0021_xx58_:
                return VR.SH;
            case PrivateTag._0021_xx45_:
                return VR.SL;
            case PrivateTag._0021_xx2E_:
            case PrivateTag._0021_xx51_:
                return VR.UL;
            case PrivateTag._0021_xx01_:
            case PrivateTag._0021_xx52_:
            case PrivateTag._0021_xx54_:
                return VR.US;
        }
        return VR.UN;
    }
}
