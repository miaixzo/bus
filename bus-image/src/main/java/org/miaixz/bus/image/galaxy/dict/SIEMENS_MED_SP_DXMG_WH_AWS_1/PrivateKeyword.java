/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
