/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SP_DXMG_WH_AWS_1;

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

            case PrivateTag._0051_xx10_:
            case PrivateTag._0051_xx20_:
            case PrivateTag._0051_xx32_:
            case PrivateTag._0051_xx37_:
            case PrivateTag.PrimaryPositionerScanArc:
            case PrivateTag.SecondaryPositionerScanArc:
            case PrivateTag.PrimaryPositionerScanStartAngle:
            case PrivateTag.SecondaryPositionerScanStartAngle:
            case PrivateTag.PrimaryPositionerIncrement:
            case PrivateTag.SecondaryPositionerIncrement:
                return VR.DS;

            case PrivateTag._0051_xx21_:
            case PrivateTag.ProjectionViewDisplayString:
                return VR.LO;

            case PrivateTag.ReasonForTheRequestedProcedure:
                return VR.SH;

            case PrivateTag.DerivationDescription:
                return VR.ST;

            case PrivateTag._0051_xx50_:
                return VR.UI;

            case PrivateTag.AECCoordinates:
                return VR.UL;

            case PrivateTag.AECCoordinatesSize:
                return VR.US;
        }
        return VR.UN;
    }

}
