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
package org.miaixz.bus.image.galaxy.dict.Philips_US_Imaging_DD_043;

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
        
            case PrivateTag._200D_xx1F_:
            case PrivateTag._200D_xx23_:
            case PrivateTag._200D_xx24_:
            case PrivateTag._200D_xx25_:
            case PrivateTag._200D_xx26_:
            case PrivateTag._200D_xx27_:
            case PrivateTag._200D_xx28_:
            case PrivateTag._200D_xx29_:
            case PrivateTag._200D_xx2A_:
            case PrivateTag._200D_xx2B_:
            case PrivateTag._200D_xx2C_:
            case PrivateTag._200D_xx2D_:
            case PrivateTag._200D_xx2E_:
            case PrivateTag._200D_xx2F_:
            case PrivateTag._200D_xx30_:
            case PrivateTag._200D_xx31_:
            case PrivateTag._200D_xx32_:
            case PrivateTag._200D_xx33_:
            case PrivateTag._200D_xx34_:
            case PrivateTag._200D_xx37_:
            case PrivateTag._200D_xx38_:
            case PrivateTag._200D_xx39_:
                return VR.FD;
            case PrivateTag._200D_xx09_:
            case PrivateTag._200D_xx0A_:
            case PrivateTag._200D_xx0B_:
            case PrivateTag._200D_xx0C_:
            case PrivateTag._200D_xx0D_:
            case PrivateTag._200D_xx0E_:
            case PrivateTag._200D_xx0F_:
            case PrivateTag._200D_xx10_:
            case PrivateTag._200D_xx11_:
            case PrivateTag._200D_xx17_:
            case PrivateTag._200D_xx1A_:
            case PrivateTag._200D_xx1B_:
            case PrivateTag._200D_xx1E_:
            case PrivateTag._200D_xx21_:
            case PrivateTag._200D_xx35_:
            case PrivateTag._200D_xx36_:
            case PrivateTag._200D_xx40_:
            case PrivateTag._200D_xx41_:
            case PrivateTag._200D_xx42_:
                return VR.IS;
            case PrivateTag._200D_xx05_:
                return VR.SH;
        }
        return VR.UN;
    }
}
