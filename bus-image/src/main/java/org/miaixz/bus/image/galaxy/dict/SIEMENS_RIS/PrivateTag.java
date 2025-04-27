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
