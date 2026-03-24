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
package org.miaixz.bus.image.galaxy.dict.SIENET;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.SIENETCommandField:
                return "SIENETCommandField";

            case PrivateTag.ReceiverPLA:
                return "ReceiverPLA";

            case PrivateTag.TransferPriority:
                return "TransferPriority";

            case PrivateTag.ActualUser:
                return "ActualUser";

            case PrivateTag._0009_xx70_:
                return "_0009_xx70_";

            case PrivateTag._0009_xx71_:
                return "_0009_xx71_";

            case PrivateTag._0009_xx72_:
                return "_0009_xx72_";

            case PrivateTag._0009_xx73_:
                return "_0009_xx73_";

            case PrivateTag._0009_xx74_:
                return "_0009_xx74_";

            case PrivateTag._0009_xx75_:
                return "_0009_xx75_";

            case PrivateTag.RISPatientName:
                return "RISPatientName";

            case PrivateTag._0093_xx02_:
                return "_0093_xx02_";

            case PrivateTag.ExaminationFolderID:
                return "ExaminationFolderID";

            case PrivateTag.FolderReportedStatus:
                return "FolderReportedStatus";

            case PrivateTag.FolderReportingRadiologist:
                return "FolderReportingRadiologist";

            case PrivateTag.SIENETISAPLA:
                return "SIENETISAPLA";

            case PrivateTag._0095_xx0C_:
                return "_0095_xx0C_";

            case PrivateTag._0097_xx03_:
                return "_0097_xx03_";

            case PrivateTag._0097_xx05_:
                return "_0097_xx05_";

            case PrivateTag.DataObjectAttributes:
                return "DataObjectAttributes";

            case PrivateTag._0099_xx05_:
                return "_0099_xx05_";

            case PrivateTag._00A5_xx05_:
                return "_00A5_xx05_";
        }
        return "";
    }

}
