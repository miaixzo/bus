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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_DFR_01_MANIPULATED;

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

        case PrivateTag.PixelShiftHorizontal:
        case PrivateTag.PixelShiftVertical:
            return VR.DS;
        case PrivateTag._0017_xx25_:
        case PrivateTag._0017_xx27_:
        case PrivateTag.EdgeEnhancement:
        case PrivateTag.Harmonization:
        case PrivateTag.Landmark:
            return VR.IS;
        case PrivateTag.LeftMarker:
        case PrivateTag.RightMarker:
            return VR.LO;
        case PrivateTag._0017_xxA1_:
        case PrivateTag.ImageNameExtension1:
        case PrivateTag.ImageNameExtension2:
            return VR.SH;
        case PrivateTag._0017_xx11_:
        case PrivateTag._0017_xx12_:
        case PrivateTag._0017_xx14_:
        case PrivateTag._0017_xx15_:
        case PrivateTag._0017_xx31_:
        case PrivateTag._0017_xx32_:
        case PrivateTag._0017_xx33_:
        case PrivateTag._0017_xx35_:
        case PrivateTag._0017_xx37_:
        case PrivateTag._0017_xx38_:
        case PrivateTag._0017_xx72_:
        case PrivateTag._0017_xx73_:
        case PrivateTag._0017_xx74_:
        case PrivateTag._0017_xx79_:
        case PrivateTag._0017_xx7A_:
        case PrivateTag._0017_xx80_:
            return VR.US;
        }
        return VR.UN;
    }
}
