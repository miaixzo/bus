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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_RIS;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS RIS";

    /** (0011,xx10) VR=LO VM=1 Patient UID */
    public static final int PatientUID = 0x00110010;

    /** (0011,xx11) VR=LO VM=1 Patient ID */
    public static final int PatientID = 0x00110011;

    /** (0011,xx20) VR=DA VM=1 Patient Registration Date */
    public static final int PatientRegistrationDate = 0x00110020;

    /** (0011,xx21) VR=TM VM=1 Patient Registration Time */
    public static final int PatientRegistrationTime = 0x00110021;

    /** (0011,xx30) VR=PN VM=1 Patientname RIS */
    public static final int PatientnameRIS = 0x00110030;

    /** (0011,xx31) VR=LO VM=1 Patientprename RIS */
    public static final int PatientprenameRIS = 0x00110031;

    /** (0011,xx40) VR=LO VM=1 Patient Hospital Status */
    public static final int PatientHospitalStatus = 0x00110040;

    /** (0011,xx41) VR=LO VM=1 Medical Alerts */
    public static final int MedicalAlerts = 0x00110041;

    /** (0011,xx42) VR=LO VM=1 Contrast Allergies */
    public static final int ContrastAllergies = 0x00110042;

    /** (0031,xx10) VR=LO VM=1 Request UID */
    public static final int RequestUID = 0x00310010;

    /** (0031,xx45) VR=LO VM=1 Requesting Physician */
    public static final int RequestingPhysician = 0x00310045;

    /** (0031,xx50) VR=LO VM=1 Requested Physician */
    public static final int RequestedPhysician = 0x00310050;

    /** (0033,xx10) VR=LO VM=1 Patient Study UID */
    public static final int PatientStudyUID = 0x00330010;

}
