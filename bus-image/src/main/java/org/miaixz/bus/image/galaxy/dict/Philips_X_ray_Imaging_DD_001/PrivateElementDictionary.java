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
package org.miaixz.bus.image.galaxy.dict.Philips_X_ray_Imaging_DD_001;

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
        
            case PrivateTag._2003_xx00_:
            case PrivateTag._2003_xx30_:
            case PrivateTag._2003_xx31_:
                return VR.CS;
            case PrivateTag._2003_xx02_:
            case PrivateTag._2003_xx14_:
            case PrivateTag._2003_xx15_:
            case PrivateTag._2003_xx24_:
            case PrivateTag._2003_xx29_:
            case PrivateTag._2003_xx2B_:
                return VR.FD;
            case PrivateTag._2003_xx01_:
            case PrivateTag._2003_xx03_:
            case PrivateTag._2003_xx10_:
            case PrivateTag._2003_xx2A_:
                return VR.LO;
            case PrivateTag._2003_xx27_:
            case PrivateTag._2003_xx28_:
            case PrivateTag._2003_xx2C_:
                return VR.SH;
            case PrivateTag._2003_xx06_:
            case PrivateTag._2003_xx09_:
            case PrivateTag._2003_xx11_:
            case PrivateTag._2003_xx12_:
            case PrivateTag._2003_xx13_:
            case PrivateTag._2003_xx16_:
            case PrivateTag._2003_xx17_:
            case PrivateTag._2003_xx18_:
            case PrivateTag._2003_xx19_:
            case PrivateTag._2003_xx22_:
            case PrivateTag._2003_xx25_:
            case PrivateTag._2003_xx26_:
            case PrivateTag._2003_xx2D_:
                return VR.SL;
            case PrivateTag._2003_xx2E_:
                return VR.SQ;
            case PrivateTag._2003_xx32_:
                return VR.UI;
        }
        return VR.UN;
    }
}
