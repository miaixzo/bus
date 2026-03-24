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
package org.miaixz.bus.image.galaxy.dict.GEMS_DL_STUDY_01;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.StudyDose:
                return "StudyDose";

            case PrivateTag.StudyTotalDap:
                return "StudyTotalDap";

            case PrivateTag.FluoroDoseAreaProduct:
                return "FluoroDoseAreaProduct";

            case PrivateTag.StudyFluoroTime:
                return "StudyFluoroTime";

            case PrivateTag.CineDoseAreaProduct:
                return "CineDoseAreaProduct";

            case PrivateTag.StudyRecordTime:
                return "StudyRecordTime";

            case PrivateTag.LastXANumber:
                return "LastXANumber";

            case PrivateTag.DefOperatorName:
                return "DefOperatorName";

            case PrivateTag.DefPerformingPhysicianName:
                return "DefPerformingPhysicianName";

            case PrivateTag.DefPatientOrientation:
                return "DefPatientOrientation";

            case PrivateTag.LastScNumber:
                return "LastScNumber";

            case PrivateTag.CommonSeriesInstanceUID:
                return "CommonSeriesInstanceUID";

            case PrivateTag.StudyNumber:
                return "StudyNumber";

            case PrivateTag._0015_xx92_:
                return "_0015_xx92_";

            case PrivateTag._0015_xx93_:
                return "_0015_xx93_";

            case PrivateTag._0015_xx94_:
                return "_0015_xx94_";

            case PrivateTag._0015_xx95_:
                return "_0015_xx95_";

            case PrivateTag._0015_xx96_:
                return "_0015_xx96_";

            case PrivateTag._0015_xx97_:
                return "_0015_xx97_";

            case PrivateTag._0015_xx98_:
                return "_0015_xx98_";

            case PrivateTag._0015_xx99_:
                return "_0015_xx99_";

            case PrivateTag._0015_xx9A_:
                return "_0015_xx9A_";

            case PrivateTag._0015_xx9B_:
                return "_0015_xx9B_";

            case PrivateTag._0015_xx9C_:
                return "_0015_xx9C_";

            case PrivateTag._0015_xx9D_:
                return "_0015_xx9D_";
        }
        return "";
    }

}
