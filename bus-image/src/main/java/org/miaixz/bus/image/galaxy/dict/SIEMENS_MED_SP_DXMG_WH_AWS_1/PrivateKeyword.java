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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SP_DXMG_WH_AWS_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.AECCoordinates:
            return "AECCoordinates";
        case PrivateTag.AECCoordinatesSize:
            return "AECCoordinatesSize";
        case PrivateTag.DerivationDescription:
            return "DerivationDescription";
        case PrivateTag.ReasonForTheRequestedProcedure:
            return "ReasonForTheRequestedProcedure";
        case PrivateTag._0051_xx10_:
            return "_0051_xx10_";
        case PrivateTag._0051_xx20_:
            return "_0051_xx20_";
        case PrivateTag._0051_xx21_:
            return "_0051_xx21_";
        case PrivateTag._0051_xx32_:
            return "_0051_xx32_";
        case PrivateTag._0051_xx37_:
            return "_0051_xx37_";
        case PrivateTag._0051_xx50_:
            return "_0051_xx50_";
        case PrivateTag.PrimaryPositionerScanArc:
            return "PrimaryPositionerScanArc";
        case PrivateTag.SecondaryPositionerScanArc:
            return "SecondaryPositionerScanArc";
        case PrivateTag.PrimaryPositionerScanStartAngle:
            return "PrimaryPositionerScanStartAngle";
        case PrivateTag.SecondaryPositionerScanStartAngle:
            return "SecondaryPositionerScanStartAngle";
        case PrivateTag.PrimaryPositionerIncrement:
            return "PrimaryPositionerIncrement";
        case PrivateTag.SecondaryPositionerIncrement:
            return "SecondaryPositionerIncrement";
        case PrivateTag.ProjectionViewDisplayString:
            return "ProjectionViewDisplayString";
        }
        return "";
    }

}
