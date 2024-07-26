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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_DFR_01_MANIPULATED;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag._0017_xx11_:
            return "_0017_xx11_";
        case PrivateTag._0017_xx12_:
            return "_0017_xx12_";
        case PrivateTag._0017_xx14_:
            return "_0017_xx14_";
        case PrivateTag._0017_xx15_:
            return "_0017_xx15_";
        case PrivateTag._0017_xx25_:
            return "_0017_xx25_";
        case PrivateTag._0017_xx27_:
            return "_0017_xx27_";
        case PrivateTag.EdgeEnhancement:
            return "EdgeEnhancement";
        case PrivateTag.Harmonization:
            return "Harmonization";
        case PrivateTag._0017_xx31_:
            return "_0017_xx31_";
        case PrivateTag._0017_xx32_:
            return "_0017_xx32_";
        case PrivateTag._0017_xx33_:
            return "_0017_xx33_";
        case PrivateTag._0017_xx35_:
            return "_0017_xx35_";
        case PrivateTag._0017_xx37_:
            return "_0017_xx37_";
        case PrivateTag._0017_xx38_:
            return "_0017_xx38_";
        case PrivateTag.Landmark:
            return "Landmark";
        case PrivateTag._0017_xx72_:
            return "_0017_xx72_";
        case PrivateTag._0017_xx73_:
            return "_0017_xx73_";
        case PrivateTag._0017_xx74_:
            return "_0017_xx74_";
        case PrivateTag.PixelShiftHorizontal:
            return "PixelShiftHorizontal";
        case PrivateTag.PixelShiftVertical:
            return "PixelShiftVertical";
        case PrivateTag._0017_xx79_:
            return "_0017_xx79_";
        case PrivateTag._0017_xx7A_:
            return "_0017_xx7A_";
        case PrivateTag._0017_xx80_:
            return "_0017_xx80_";
        case PrivateTag.LeftMarker:
            return "LeftMarker";
        case PrivateTag.RightMarker:
            return "RightMarker";
        case PrivateTag._0017_xxA1_:
            return "_0017_xxA1_";
        case PrivateTag.ImageNameExtension1:
            return "ImageNameExtension1";
        case PrivateTag.ImageNameExtension2:
            return "ImageNameExtension2";
        }
        return "";
    }

}
