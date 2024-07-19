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
package org.miaixz.bus.image.galaxy.dict.Philips_US_Imaging_DD_039;

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
        
            case PrivateTag._200D_xx50_:
            case PrivateTag._200D_xx51_:
            case PrivateTag._200D_xx52_:
            case PrivateTag._200D_xx53_:
            case PrivateTag._200D_xx54_:
            case PrivateTag._200D_xx55_:
            case PrivateTag._200D_xx56_:
            case PrivateTag._200D_xx57_:
            case PrivateTag._200D_xx58_:
            case PrivateTag._200D_xx59_:
            case PrivateTag._200D_xx60_:
            case PrivateTag._200D_xx61_:
                return VR.IS;
            case PrivateTag._200D_xx01_:
            case PrivateTag._200D_xx02_:
            case PrivateTag._200D_xx03_:
            case PrivateTag._200D_xx04_:
            case PrivateTag._200D_xx05_:
            case PrivateTag._200D_xx06_:
            case PrivateTag._200D_xx07_:
            case PrivateTag._200D_xx08_:
            case PrivateTag._200D_xx09_:
            case PrivateTag._200D_xx0A_:
            case PrivateTag._200D_xx0B_:
            case PrivateTag._200D_xx0C_:
            case PrivateTag._200D_xx0D_:
            case PrivateTag._200D_xx10_:
            case PrivateTag._200D_xx11_:
            case PrivateTag._200D_xx12_:
            case PrivateTag._200D_xx13_:
            case PrivateTag._200D_xx14_:
            case PrivateTag._200D_xx15_:
                return VR.LO;
        }
        return VR.UN;
    }
}
