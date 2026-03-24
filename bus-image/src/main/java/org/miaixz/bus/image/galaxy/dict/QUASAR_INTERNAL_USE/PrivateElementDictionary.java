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
package org.miaixz.bus.image.galaxy.dict.QUASAR_INTERNAL_USE;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
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

            case PrivateTag._0009_xx40_:
                return VR.DA;

            case PrivateTag._0037_xx92_:
                return VR.DS;

            case PrivateTag._0009_xx22_:
                return VR.FL;

            case PrivateTag._0037_xx71_:
            case PrivateTag._0037_xx73_:
            case PrivateTag._0037_xx78_:
                return VR.FD;

            case PrivateTag._0037_xx90_:
                return VR.IS;

            case PrivateTag._0009_xx12_:
            case PrivateTag.ImageTypeString:
            case PrivateTag._0009_xx42_:
            case PrivateTag._0009_xx45_:
            case PrivateTag._0009_xx48_:
            case PrivateTag._0037_xx1B_:
            case PrivateTag._0037_xx30_:
            case PrivateTag._0037_xx40_:
            case PrivateTag._0037_xx50_:
            case PrivateTag._0037_xx60_:
            case PrivateTag._0037_xx70_:
                return VR.LO;

            case PrivateTag._0009_xx44_:
            case PrivateTag._0037_xx72_:
                return VR.SH;

            case PrivateTag._0037_xx10_:
                return VR.SQ;

            case PrivateTag.SequenceType:
            case PrivateTag.SequenceName:
            case PrivateTag._0009_xx1E_:
                return VR.ST;

            case PrivateTag._0009_xx41_:
                return VR.TM;

            case PrivateTag._0009_xx39_:
                return VR.UI;

            case PrivateTag.RateVector:
            case PrivateTag.CountVector:
            case PrivateTag.TimeVector:
            case PrivateTag.AngleVector:
            case PrivateTag.AverageRRTimeVector:
            case PrivateTag.LowLimitVector:
            case PrivateTag.HighLimitVector:
            case PrivateTag.BeginIndexVector:
            case PrivateTag.EndIndexVector:
            case PrivateTag.RawTimeVector:
                return VR.UL;

            case PrivateTag.CameraShape:
            case PrivateTag.WholeBodySpots:
            case PrivateTag.WorklistFlag:
            case PrivateTag._0009_xx1D_:
            case PrivateTag._0009_xx23_:
                return VR.US;

            case PrivateTag._0041_xx01_:
                return VR.UT;
        }
        return VR.UN;
    }

}
