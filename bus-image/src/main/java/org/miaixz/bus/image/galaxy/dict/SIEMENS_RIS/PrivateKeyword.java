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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_RIS;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.PatientUID:
                return "PatientUID";

            case PrivateTag.PatientID:
                return "PatientID";

            case PrivateTag.PatientRegistrationDate:
                return "PatientRegistrationDate";

            case PrivateTag.PatientRegistrationTime:
                return "PatientRegistrationTime";

            case PrivateTag.PatientnameRIS:
                return "PatientnameRIS";

            case PrivateTag.PatientprenameRIS:
                return "PatientprenameRIS";

            case PrivateTag.PatientHospitalStatus:
                return "PatientHospitalStatus";

            case PrivateTag.MedicalAlerts:
                return "MedicalAlerts";

            case PrivateTag.ContrastAllergies:
                return "ContrastAllergies";

            case PrivateTag.RequestUID:
                return "RequestUID";

            case PrivateTag.RequestingPhysician:
                return "RequestingPhysician";

            case PrivateTag.RequestedPhysician:
                return "RequestedPhysician";

            case PrivateTag.PatientStudyUID:
                return "PatientStudyUID";
        }
        return "";
    }

}
