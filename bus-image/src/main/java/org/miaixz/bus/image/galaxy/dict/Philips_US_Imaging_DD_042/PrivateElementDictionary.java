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
package org.miaixz.bus.image.galaxy.dict.Philips_US_Imaging_DD_042;

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

        case PrivateTag._200D_xx16_:
            return VR.FD;
        case PrivateTag._200D_xx15_:
            return VR.IS;
        case PrivateTag._200D_xx20_:
        case PrivateTag._200D_xx30_:
        case PrivateTag._200D_xx31_:
        case PrivateTag._200D_xx40_:
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
        case PrivateTag._200D_xx5A_:
        case PrivateTag._200D_xx5B_:
        case PrivateTag._200D_xx5C_:
        case PrivateTag._200D_xx5D_:
        case PrivateTag._200D_xx5E_:
        case PrivateTag._200D_xx5F_:
        case PrivateTag._200D_xx60_:
        case PrivateTag._200D_xx70_:
        case PrivateTag._200D_xx71_:
        case PrivateTag._200D_xx72_:
        case PrivateTag._200D_xx73_:
        case PrivateTag._200D_xx74_:
        case PrivateTag._200D_xx75_:
        case PrivateTag._200D_xx76_:
        case PrivateTag._200D_xx77_:
        case PrivateTag._200D_xx78_:
        case PrivateTag._200D_xx8C_:
            return VR.LO;
        }
        return VR.UN;
    }
}
