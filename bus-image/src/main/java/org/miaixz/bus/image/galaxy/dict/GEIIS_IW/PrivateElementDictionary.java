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
package org.miaixz.bus.image.galaxy.dict.GEIIS_IW;

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
        
            case PrivateTag._0075_xx05_:
            case PrivateTag._0075_xx73_:
            case PrivateTag._0075_xx74_:
            case PrivateTag._0075_xx75_:
            case PrivateTag._0075_xx79_:
            case PrivateTag._0075_xx7C_:
            case PrivateTag._0075_xx8F_:
            case PrivateTag._0075_xx91_:
            case PrivateTag._0075_xx93_:
            case PrivateTag._0075_xx94_:
                return VR.CS;
            case PrivateTag._0075_xx7A_:
            case PrivateTag._0075_xx7B_:
            case PrivateTag._0075_xx7E_:
            case PrivateTag._0075_xx81_:
            case PrivateTag._0075_xx82_:
            case PrivateTag._0075_xx83_:
                return VR.DS;
            case PrivateTag._0075_xx7F_:
                return VR.FD;
            case PrivateTag._0075_xx02_:
            case PrivateTag._0075_xx10_:
            case PrivateTag._0075_xx19_:
            case PrivateTag._0075_xx1A_:
            case PrivateTag._0075_xx61_:
            case PrivateTag._0075_xx62_:
            case PrivateTag._0075_xx63_:
            case PrivateTag._0075_xx71_:
            case PrivateTag._0075_xx72_:
            case PrivateTag._0075_xx76_:
            case PrivateTag._0075_xx77_:
            case PrivateTag._0075_xx7D_:
            case PrivateTag._0075_xx84_:
            case PrivateTag._0075_xx85_:
            case PrivateTag._0075_xx89_:
            case PrivateTag._0075_xx8A_:
            case PrivateTag._0075_xx90_:
                return VR.IS;
            case PrivateTag._0075_xx95_:
                return VR.LO;
            case PrivateTag._0075_xx1B_:
            case PrivateTag._0075_xx70_:
            case PrivateTag._0075_xx86_:
            case PrivateTag._0075_xx87_:
            case PrivateTag._0075_xx88_:
            case PrivateTag._0075_xxB5_:
                return VR.LT;
            case PrivateTag._0075_xx16_:
                return VR.OB;
            case PrivateTag._0075_xx11_:
                return VR.SH;
            case PrivateTag._0075_xx17_:
            case PrivateTag._0075_xx69_:
            case PrivateTag._0075_xxB6_:
                return VR.SQ;
            case PrivateTag._0075_xx60_:
                return VR.ST;
            case PrivateTag._0075_xxC0_:
            case PrivateTag._0075_xxC1_:
                return VR.UI;
        }
        return VR.UN;
    }
}
